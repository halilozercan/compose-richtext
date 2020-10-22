package com.zachklipp.richtext.sample

import android.content.Context
import android.content.Context.DISPLAY_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLifecycleObserver
import androidx.compose.runtime.Providers
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.ConsumedData
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventPass.Initial
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputFilter
import androidx.compose.ui.input.pointer.PointerInputModifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.ViewAmbient
import androidx.compose.ui.selection.DisableSelection
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
  WithConstraints(
      modifier.aspectRatio(aspectRatio)
          // Disable touch input.
          .then(PassthroughTouchToParentModifier)
          .semantics(mergeAllDescendants = true) {
            // TODO Block semantics. Is this enough?
            disabled()
          }
  ) {
    val actualDensity = DensityAmbient.current.density
    // Can use width or height to do the calculation, since the aspect ratio is enforced.
    val previewDensityScale = constraints.maxWidth / screenSize.width.toFloat()
    val previewDensity = actualDensity * previewDensityScale

    // Provide a fake host view, since the preview doesn't really belong to this host view.
    val context = ContextAmbient.current
    val previewView = remember {
      val previewContext = context.applicationContext
      View(previewContext)
    }

    DisableSelection {
      Providers(
          DensityAmbient provides Density(previewDensity),
          ViewAmbient provides previewView,
          children = content
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
  val context = ContextAmbient.current
  val state = remember { DisplaySizeCalculator(context) }
  return state.displaySize.value
}

private class DisplaySizeCalculator(context: Context) : CompositionLifecycleObserver,
    DisplayListener {
  private val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
  private val displayManager = context.getSystemService(DISPLAY_SERVICE) as DisplayManager
  private val display = windowManager.defaultDisplay

  val displaySize = mutableStateOf(getDisplaySize())

  override fun onEnter() {
    // Update the preview on device rotation, for example.
    displayManager.registerDisplayListener(this, Handler())
  }

  override fun onLeave() {
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
  ): List<PointerInputChange> {
    return if (pass == Initial) {
      // On the initial pass (ancestors -> descendants), mark all pointer events as completely
      // consumed. This prevents children from handling any pointer events.
      // These events are all marked as unconsumed by default.
      pointerEvent.changes.map {
        it.copy(
            consumed = ConsumedData(
                downChange = true, positionChange = it.current.position ?: Offset.Zero
            )
        )
      }
    } else {
      // On the main pass (descendants -> ancestors), pointer events are all marked as completely
      // consumed by default, which prevents the parent from handling them. We explicitly want
      // the parent to handle them, so we mark them as _unconsumed_.
      // The final pass (ancestors -> descendants) also marks all events as consumed by default.
      // I'm not really sure what the effect of modifying that pass is, but this seems to work.
      pointerEvent.changes.map {
        it.copy(consumed = ConsumedData(downChange = false, positionChange = Offset.Zero))
      }
    }
  }

  override fun onCancel() {
    // Noop.
  }
}
