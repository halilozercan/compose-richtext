package com.zachklipp.richtext.ui.printing

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.pdf.PdfDocument.Page
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.view.ViewTreeObserver.OnDrawListener
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.coroutines.resume

internal interface PdfComposeScope {
  /**
   * Wait for the next frame to commit, then render the composable content to [page]'s canvas.

   * @param page A [Page] returned from
   * [PdfDocument.startPage][android.graphics.pdf.PdfDocument.startPage].
   */
  suspend fun composePage(page: Page)
}

/**
 * Renders a composable function ([content]) to a [PrintedPdfDocument].
 *
 * Compose requires a [View] that is actually attached to a window to work, so this function creates
 * a temporary window to compose into, then draws the window's contents into the page's canvas.
 * Since this looks weird, the window is covered by another, identically-sized window that displays
 * [progressIndicator] to the user while the [Page] is being rendered.
 *
 * This is a suspend function, and must be called on the main dispatcher.
 *
 * @param activity The [ComponentActivity] that is used for [Context] and to own the windows.
 * @param pageDpi The density of the page in DPI. The default value makes the default Material font
 * size roughly 11pt.
 * @param minIndicatorMillis The minimum duration to show the progress indicator. Rendering can
 * actually be so fast that the indicator just flickers, which makes for a bad UX. This value
 * ensures that the indicator is displayed for a longer period of time so it looks like it's
 * actually doing something.
 * @param progressIndicator A composable that is used to cover the window used for rendering pages
 * with a more user-friendly UI. Note that this should normally be hidden completely behind the
 * print preview activity and only seen if the preview is cancelled and the app doesn't handle the
 * cancellation right away.
 * @param content The composable to render.
 * @param writer A function which calls [PdfComposeScope.composePage] for each page of composable
 * content that needs to be written. The composition is retained until this block returns, so the
 * block can do things like mutate `MutableState` objects to update the composable between pages.
 */
internal suspend fun composeToPdf(
  activity: ComponentActivity,
  pdfDocument: PrintedPdfDocument,
  pageDpi: Int,
  minIndicatorMillis: Long = 1000,
  progressIndicator: @Composable () -> Unit = { DefaultPdfRenderingProgress() },
  content: @Composable PrinterMetrics.() -> Unit,
  writer: suspend PdfComposeScope.() -> Unit
) {
  // Can't create a new window until the activity's first window has been assigned a token.
  activity.awaitWindowToken()
  val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

  val printerMetrics = PrinterMetrics(
      screenDensity = Density(activity),
      pageDpi = pageDpi,
      pageWidth = pdfDocument.pageWidth.pts,
      pageHeight = pdfDocument.pageHeight.pts
  )

  val windowParams = with(printerMetrics) {
    LayoutParams(
        pageWidth.value,
        pageHeight.value,
        LayoutParams.TYPE_APPLICATION_SUB_PANEL,
        LayoutParams.FLAG_NOT_FOCUSABLE or
            LayoutParams.FLAG_NOT_TOUCHABLE or
            LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.RGBA_8888
    )
  }

  val renderingView = createWindowComposeView(activity) {
    Providers(
        // Render the page with the page density, not the screen density.
        DensityAmbient provides printerMetrics.renderDensity,
        children = { content(printerMetrics) }
    )
  }
  // Hide the rendering window.
  val progressView = createWindowComposeView(activity, progressIndicator)

  // Adding views to the window manager creates windows, and attaches the views to them.
  wm.addView(renderingView, windowParams)
  wm.addView(
      progressView,
      LayoutParams().apply {
        copyFrom(windowParams)
        format = PixelFormat.OPAQUE
        flags = flags or LayoutParams.FLAG_DIM_BEHIND
        dimAmount = .5f
      })


  coroutineScope {
    val minIndicatorTimer = launch { delay(minIndicatorMillis) }
    try {
      writer(object : PdfComposeScope {
        override suspend fun composePage(page: Page) {
          with(renderingView) {
            invalidate()
            awaitDraw()
            draw(page.canvas)
          }
        }
      })

      minIndicatorTimer.join()
    } finally {
      // Close both the windows we created.
      wm.removeView(renderingView)
      wm.removeView(progressView)
    }
  }
}

@Composable private fun DefaultPdfRenderingProgress() {
  val isDarkTheme = isSystemInDarkTheme()
  val backgroundColor = if (isDarkTheme) Black else White
  val textColor = if (isDarkTheme) White else Black
  Box(
      Modifier.background(backgroundColor)
          .fillMaxSize()
          .wrapContentSize()
  ) {
    Text("Rendering PDF…", color = textColor)
  }
}

private suspend fun Activity.awaitWindowToken() {
  withTimeoutOrNull(1000) {
    // This loop should probably only run for one iteration max.
    while (window.decorView.windowToken == null) {
      // Let the main thread do some more work.
      yield()
    }
  } ?: error("Timed out waiting for activity window token.")
}

private fun createWindowComposeView(
  activity: ComponentActivity,
  content: @Composable () -> Unit
): View = ComposeView(activity).apply {
  ViewTreeLifecycleOwner.set(this, activity)
  ViewTreeViewModelStoreOwner.set(this, activity)
  ViewTreeSavedStateRegistryOwner.set(this, activity)
  setContent(content)
}

private suspend fun View.awaitDraw() {
  suspendCancellableCoroutine<Unit> { continuation ->
    var handled = false
    val onDrawListener = object : OnDrawListener {
      override fun onDraw() {
        if (handled) return
        handled = true

        // Wait for the next frame.
        post {
          viewTreeObserver.removeOnDrawListener(this)
          continuation.resume(Unit)
        }
      }
    }
    viewTreeObserver.addOnDrawListener(onDrawListener)
    continuation.invokeOnCancellation { viewTreeObserver.removeOnDrawListener(onDrawListener) }
  }
}
