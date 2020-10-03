package com.zachklipp.richtext.ui.slideshow

import androidx.compose.runtime.Composable

typealias NavigationInterceptor = (forward: Boolean) -> Boolean

/**
 * Receiver for slide composables passed to [Slideshow] that provides some context about the current
 * slide, as well as functions for controlling navigation.
 */
interface SlideScope {
  /** The index of the current slide in the slideshow. */
  val slideNumber: Int

  /** If true, this slide was shown via forward navigation, coming from the previous slide. */
  val navigatedForward: Boolean

  /**
   * Register a callback to be invoked when navigation is requested from this slide. If the callback
   * returns true, the slideshow will not navigate. If it returns false, the next interceptor will
   * have a chance to handle it, or if there are no other interceptors then the slideshow will
   * navigate between slides.
   */
  @Composable fun interceptNavigation(handler: NavigationInterceptor)
}

internal object PreviewSlideScope : SlideScope {
  override val slideNumber: Int get() = 42
  override val navigatedForward: Boolean get() = false

  @Composable override fun interceptNavigation(handler: NavigationInterceptor) = Unit
}
