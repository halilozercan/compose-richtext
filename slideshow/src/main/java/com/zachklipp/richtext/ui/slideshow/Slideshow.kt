package com.zachklipp.richtext.ui.slideshow

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ContentColorAmbient
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Slider
import androidx.compose.runtime.CommitScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation.Vertical
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.ViewAmbient
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.selection.SelectionContainer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SlideshowController {
  var currentSlide by mutableStateOf(0)
  var showingScrubber by mutableStateOf(false)
}

@Composable fun rememberSlideshowController() = remember { SlideshowController() }

/**
 * A slideshow consisting of a sequence of slides which can be navigated through by tapping on them.
 *
 * Each slide is defined as a composable function with a [SlideScope] receiver. The [SlideScope]
 * can be used to get information about the current slide and control navigation.
 *
 * A number of scaffold slide composables are provided to create common slide layouts, which can be
 * customized through the [SlideshowTheme]. These include:
 *  - [TitleSlide]
 *  - [BodySlide]
 *
 * There are other helpers as well:
 *  - [SlideDivider]
 *  - [NavigableContentContainer]
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable fun Slideshow(
  vararg slides: @Composable SlideScope.() -> Unit,
  controller: SlideshowController = rememberSlideshowController(),
  theme: SlideshowTheme = SlideshowTheme()
) {
  if (slides.isEmpty()) return
  val state = remember { SlidesContainerState(slides, controller) }

  val rootView = ViewAmbient.current
  onCommit {
    configureFullScreenWindow(rootView)
  }

  Box(
    Modifier
      // Wire up navigation controls.
      .splitClickable { left -> state.navigate(!left) }
      // Show the slide scrubber when dragging up.
      .draggable(
        orientation = Vertical,
        onDragStopped = { velocity ->
          // Show or hide the scrubber depending on the direction of the drag.
          controller.showingScrubber = velocity < 0
        },
        onDrag = { /* Noop */ }
      )
      // Fill the entire window.
      .fillMaxSize()
      // Always draw a black background for letterboxing.
      .background(Color.Black)
  ) {
    Providers(
      ContentColorAmbient provides theme.contentColor,
      SlideshowThemeAmbient provides theme,
    ) {
      ProvideTextStyle(theme.baseTextStyle) {
        // This crossfade provides transitions between slides.
        Crossfade(
          state.currentSlide,
          Modifier
            // Aspect-ratioed content should be centered inside the window.
            .align(Alignment.Center)
            // Draw the slide background outside of the crossfade so it doesn't fade to black in
            // between slides.
            .background(theme.backgroundColor)
        ) { slide ->
          if (slide < slides.size) {
            // Make slide text selectable.
            var selection by remember { mutableStateOf<Selection?>(null) }
            SelectionContainer(
              Modifier
                // Be as big as possible with the correct aspect ratio.
                .aspectRatio(theme.aspectRatio)
                // Center slide content that doesn't expand. This is a more visually pleasing
                // default than putting it at the top-left.
                .wrapContentSize(),
              selection = selection,
              onSelectionChange = { selection = it }
            ) {
              val slideScope = remember { state.createSlideScopeForSlide(slide) }
              slides[slide].invoke(slideScope)
            }
          } else {
            EndMarker()
          }
        }

        // Scrubber control.
        // TODO This probably shouldn't be built into the main slideshow composable.
        AnimatedVisibility(
          controller.showingScrubber,
          modifier = Modifier.fillMaxSize().wrapContentSize(align = Alignment.BottomCenter),
          enter = slideInVertically({ it }),
          exit = slideOutVertically({ it })
        ) {
          SlideshowScrubber(controller, slides)
        }
      }
    }
  }
}

@Composable private fun SlideshowScrubber(
  controller: SlideshowController,
  slides: Array<out @Composable SlideScope.() -> Unit>
) {
  var scrubberSlide by remember(controller.currentSlide) {
    mutableStateOf(controller.currentSlide.toFloat())
  }
  val theme = SlideshowThemeAmbient.current

  WithConstraints(Modifier.fillMaxWidth()) {
    val outerconstraints = constraints
    Column {
      Box(
        Modifier
          .fillMaxHeight(.4f)
          .aspectRatio(theme.aspectRatio)
          // TODO Doesn't work due to a bug?
          //  See https://kotlinlang.slack.com/archives/CJLTWPH7S/p1601703876455100
          // .align(FractionAlignment(scrubberSlide / slides.size.toFloat()))
          .align(Alignment.CenterHorizontally)
          .border(.5.dp, Color.LightGray)
          .drawShadow(16.dp)
          .background(theme.backgroundColor)
          .wrapContentSize()
      ) {
        WithConstraints {
          // Calculate the amount to change density to make it look like the preview is a shrunk-
          // down version of the full screen.
          val innerConstraints = constraints
          val outerDensity = DensityAmbient.current.density
          val scaleFactor = innerConstraints.maxWidth / outerconstraints.maxWidth.toFloat()
          val innerDensity = outerDensity * scaleFactor
          Providers(DensityAmbient provides Density(innerDensity)) {
            val previewSlide = scrubberSlide.roundToInt().coerceAtMost(slides.size - 1)
            slides[previewSlide].invoke(object : SlideScope {
              override val slideNumber: Int get() = previewSlide

              // Tell slides we navigated back, so they render all their content immediately
              // (e.g. NavigableContent).
              override val navigatedForward: Boolean get() = false

              @Composable override fun interceptNavigation(handler: NavigationInterceptor) {
                // Noop.
              }
            })
          }
        }
      }
      Spacer(Modifier.height(24.dp))

      Box(
        Modifier
          .fillMaxWidth()
          .background(Color.Black)
          .padding(16.dp)
      ) {
        Slider(
          value = scrubberSlide,
          onValueChange = { scrubberSlide = it },
          valueRange = 0f..(slides.size - 1).toFloat(),
          steps = slides.size - 2,
          onValueChangeEnd = {
            controller.currentSlide = scrubberSlide.roundToInt()
            controller.showingScrubber = false
          }
        )
      }
    }
  }
}

/**
 * Kind of like `Modifier.clickable`, but vertically splits the clickable area into two areas,
 * defined by [splitFraction], and invokes [onClick] with true if the click was on the left, or
 * false if it was on the right.
 */
private fun Modifier.splitClickable(
  splitFraction: Float = .3f,
  onClick: (left: Boolean) -> Unit
): Modifier = composed {
  val sizeRef = remember { Ref<IntSize>() }
  val splitPoint by derivedStateOf {
    (sizeRef.value?.width ?: 0) * splitFraction
  }

  onSizeChanged { sizeRef.value = it }
    .tapGestureFilter { offset ->
      onClick(offset.x < splitPoint)
    }
}

@Composable private fun EndMarker() {
  Text(
    "End",
    Modifier
      .aspectRatio(16 / 9f)
      // We don't want to draw the theme background color for this marker, but we still need
      // to have it inside the Crossfade so that
      .background(Color.Black)
      .wrapContentSize(),
    fontSize = 11.sp
  )
}

@Suppress("DEPRECATION")
private fun CommitScope.configureFullScreenWindow(view: View) {
  tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

  val originalSystemUiVisibility = view.systemUiVisibility
  val activity = view.context.findActivity()
  val originalOrientation = activity?.requestedOrientation

  view.systemUiVisibility =
    SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
  onDispose {
    view.systemUiVisibility = originalSystemUiVisibility
    originalOrientation?.let { activity.requestedOrientation = it }
  }
}

private class SlidesContainerState(
  private val slides: Array<out @Composable SlideScope.() -> Unit>,
  val controller: SlideshowController
) {
  private var requestedSlide by controller::currentSlide
  val currentSlide by derivedStateOf { requestedSlide.coerceIn(0, slides.size) }
  private var navigatedForward = true

  /**
   * Navigation interceptors are associated with their slide, so that they are only invoked when
   * the slide that registered them is actually the requested slide. This prevents interceptors on
   * neighboring slides from stomping on each other during slide transitions.
   */
  private val navigationInterceptorsBySlide =
    mutableMapOf<Int, MutableList<NavigationInterceptor>>()

  fun navigate(forward: Boolean) {
    val interceptors = navigationInterceptorsBySlide.getOrElse(requestedSlide, ::mutableListOf)

    // Iterate reversed so that later interceptors can intercept from earlier ones.
    for (i in interceptors.indices.reversed()) {
      val interceptor = interceptors[i]
      if (interceptor.invoke(forward)) return
    }

    requestedSlide = if (forward) {
      navigatedForward = true
      min(requestedSlide + 1, slides.size)
    } else {
      navigatedForward = false
      max(requestedSlide - 1, 0)
    }
  }

  fun createSlideScopeForSlide(slide: Int) = object : SlideScope {
    override val slideNumber: Int = slide

    // Intentionally capture the value when this slide is created, so the slide will never see it
    // change.
    override val navigatedForward: Boolean = this@SlidesContainerState.navigatedForward

    @Composable override fun interceptNavigation(handler: NavigationInterceptor) {
      onCommit(handler) {
        navigationInterceptorsBySlide.getOrPut(slide, ::mutableListOf) += handler
        onDispose {
          navigationInterceptorsBySlide.getValue(slide) -= handler
        }
      }
    }
  }
}
