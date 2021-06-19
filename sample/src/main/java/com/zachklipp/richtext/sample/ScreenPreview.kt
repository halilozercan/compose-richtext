package com.zachklipp.richtext.sample

import android.content.Context
import android.content.Context.DISPLAY_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventPass.Initial
import androidx.compose.ui.input.pointer.PointerInputFilter
import androidx.compose.ui.input.pointer.PointerInputModifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize

/**
 * Displays [content] according to the current layout constraints, but with the density adjusted so
 * that the content things it's rendering inside a full-size screen, where "full-size" is defined
 * by [screenSize]. The default [screenSize] is read from the current window's default display.
 */
// TODO Disable focus
// TODO Disable key events (maybe covered by focus?)
// TODO use this for Slideshow as well.
@Composable fun ScreenPreview(
  modifier: Modifier = Modifier,
  screenSize: IntSize = rememberDefaultDisplaySize(),
  content: @Composable () -> Unit
) {
  val aspectRatio = screenSize.width.toFloat() / screenSize.height.toFloat()
  BoxWithConstraints(
    modifier
      .aspectRatio(aspectRatio)
      // Disable touch input.
      .then(PassthroughTouchToParentModifier)
      .semantics(mergeDescendants = true) {
        // TODO Block semantics. Is this enough?
        disabled()
      }
  ) {
    val actualDensity = LocalDensity.current.density
    // Can use width or height to do the calculation, since the aspect ratio is enforced.
    val previewDensityScale = constraints.maxWidth / screenSize.width.toFloat()
    val previewDensity = actualDensity * previewDensityScale

    DisableSelection {
      CompositionLocalProvider(
        LocalDensity provides Density(previewDensity),
        content = content
      )
    }
  }
}

/**
 * Returns the size of the default display for the window manager of the window this composable is
 * currently attached to. Will also recompose if the display size changes, e.g. when the device is
 * rotated.
 *
 * If the display reports an empty size (0x0), e.g. when running in a preview, then a reasonable
 * fake size of a phone display in portrait orientation is returned instead.
 */
@Composable private fun rememberDefaultDisplaySize(): IntSize {
  val context = LocalContext.current
  val state = remember { DisplaySizeCalculator(context) }
  return state.displaySize.value
}

private class DisplaySizeCalculator(context: Context) : RememberObserver,
  DisplayListener {
  private val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
  private val displayManager = context.getSystemService(DISPLAY_SERVICE) as DisplayManager
  private val display = windowManager.defaultDisplay

  val displaySize = mutableStateOf(getDisplaySize())

  override fun onAbandoned() {
    // Noop
  }

  override fun onRemembered() {
    // Update the preview on device rotation, for example.
    displayManager.registerDisplayListener(this, Handler())
  }

  override fun onForgotten() {
    displayManager.unregisterDisplayListener(this)
  }

  override fun onDisplayChanged(displayId: Int) {
    if (displayId != display.displayId) return
    displaySize.value = getDisplaySize()
  }

  override fun onDisplayAdded(displayId: Int) = Unit
  override fun onDisplayRemoved(displayId: Int) = Unit

  private fun getDisplaySize(): IntSize {
    val metrics = DisplayMetrics().also(display::getMetrics)
    return if (metrics.widthPixels != 0 && metrics.heightPixels != 0) {
      IntSize(metrics.widthPixels, metrics.heightPixels)
    } else {
      // Zero-sized display? Probably in a preview. Return some fake reasonable default.
      IntSize(1080, 1920)
    }
  }
}

/**
 * A [PointerInputModifier] that blocks all touch events to children of the composable to which it's
 * applied, and instead allows all those events to flow to any filters defined on the parent
 * composable.
 */
private object PassthroughTouchToParentModifier : PointerInputModifier, PointerInputFilter() {
  override val pointerInputFilter: PointerInputFilter get() = this

  override fun onPointerEvent(
    pointerEvent: PointerEvent,
    pass: PointerEventPass,
    bounds: IntSize
  ) {
    if (pass == Initial) {
      // On the initial pass (ancestors -> descendants), mark all pointer events as completely
      // consumed. This prevents children from handling any pointer events.
      // These events are all marked as unconsumed by default.
      pointerEvent.changes.forEach {
        it.consumeAllChanges()
      }
    }
  }

  override fun onCancel() {
    // Noop.
  }
}
