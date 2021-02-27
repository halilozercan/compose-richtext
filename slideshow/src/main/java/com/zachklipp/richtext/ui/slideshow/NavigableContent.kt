package com.zachklipp.richtext.ui.slideshow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onActive
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember

/**
 * Receiver type for [NavigableContentContainer] children.
 */
public interface NavigableContentScope : SlideScope {
  /**
   * Defines a composable which will be initially passed `visible=false`, and then invoked with
   * `visible=true` as the slideshow is advanced.
   *
   * A [State] object is passed, instead of passing the boolean directly, to prevent unnecessary
   * recomposition if the value is not used in the immediate scope.
   */
  @Composable public fun NavigableContent(children: @Composable (visible: State<Boolean>) -> Unit)
}

/**
 * Wrapper for [Slideshow] slides that want to initially display a subset of their content, and
 * eventually reveal more content as the slideshow is advanced. The children of this content
 * receive a [NavigableContentScope], which can be used to define pieces of content which should
 * be gradually revealed. Every occurrence of [NavigableContentScope.NavigableContent] will be
 * initially invoked with `visible=false`, and then gradually invoked with `visible=true` as the
 * slideshow is advanced.
 *
 * This composable works really well with [androidx.compose.animation.AnimatedVisibility], which
 * takes a boolean visible flag and optional enter/exit animations.
 */
@Composable
public fun SlideScope.NavigableContentContainer(
  children: @Composable NavigableContentScope.() -> Unit
) {
  val state = remember { NavigableContentState(this) }

  // Unless all content is shown or hidden, we need to handle navigation events ourself.
  interceptNavigation(state::doInterceptNavigate)

  state.startRecordingModifiers()
  children(state)
  state.stopRecordingModifiers()
  onCommit {
    state.onChildrenCommitted()
  }
}

private class NavigableContentState(private val slideScope: SlideScope) : NavigableContentScope,
  SlideScope by slideScope {

  private var recording = false

  /**
   * Used to track the order of calls, so that content is revealed in the correct order.
   */
  private var currentIndex = 0

  private val childVisibilities = mutableListOf<MutableState<Boolean>>()

  /**
   * Controls how many [NavigableContent] children are currently "visible". When navigating forward,
   * no children are initially visible, but when navigating back, all children are. Note that this
   * max value will be clamped by [onChildrenCommitted].
   */
  private var numberChildrenVisible = if (navigatedForward) 0 else Int.MAX_VALUE

  /**
   * Handles navigation (via [SlideScope.interceptNavigation]) to either show or hide our child
   * content.
   */
  fun doInterceptNavigate(forward: Boolean): Boolean {
    if (forward) numberChildrenVisible++ else numberChildrenVisible--
    updateVisibilities()

    // If we're navigating back and all content is hidden, or forward and all content is shown, then
    // let the slideshow change slides.
    return numberChildrenVisible in 0..childVisibilities.size
  }

  fun startRecordingModifiers() {
    recording = true
    currentIndex = 0
  }

  @Composable override fun NavigableContent(children: @Composable (State<Boolean>) -> Unit) {
    // TODO I think this is not going to work for more complex compositions, since a child may
    //  be added to the composition without recomposing the NavigableContentContainer. Not sure how
    //  to do index tracking in that case.
    check(recording) { "Can't use NavigableContentScope outside of NavigableContentContainer." }

    val visibleState = remember {
      val initiallyVisible = currentIndex++ < numberChildrenVisible
      mutableStateOf(initiallyVisible)
    }

    // When the content is initially committed, start tracking the visibility, and then stop
    // tracking it if it is removed from the composition.
    onActive {
      childVisibilities += visibleState
      onDispose {
        childVisibilities -= visibleState
      }
    }

    children(visibleState)
  }

  fun stopRecordingModifiers() {
    recording = false
  }

  /**
   * Ensures that the visibility states of all children are correct given the current [numberChildrenVisible]
   * after children are added or removed.
   *
   * TODO This might not get called in more complex compositions where the [NavigableContent] is
   *  composed without the container being composed. Figure out how to handle that case.
   */
  fun onChildrenCommitted() {
    numberChildrenVisible = numberChildrenVisible.coerceAtMost(childVisibilities.size)
    updateVisibilities()
  }

  private fun updateVisibilities() {
    childVisibilities.forEachIndexed { i, visible ->
      visible.value = i < numberChildrenVisible
    }
  }
}
