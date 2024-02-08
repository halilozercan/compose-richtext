@file:OptIn(ExperimentalComposeUiApi::class)

package com.halilibo.richtext.ui.util

import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

private val NoPressGesture: suspend PressGestureScope.(Offset) -> Unit = { }

/**
 * If predicate returns true: detects tap, double-tap, and long press gestures and calls [onTap],
 * [onDoubleTap], and [onLongPress], respectively, when detected. [onPress] is called when the press
 * is detected and the [PressGestureScope.tryAwaitRelease] and [PressGestureScope.awaitRelease]
 * can be used to detect when pointers have released or the gesture was canceled.
 * The first pointer down and final pointer up are consumed, and in the
 * case of long press, all changes after the long press is detected are consumed.
 *
 * Each function parameter receives an [Offset] representing the position relative to the containing
 * element. The [Offset] can be outside the actual bounds of the element itself meaning the numbers
 * can be negative or larger than the element bounds if the touch target is smaller than the
 * [ViewConfiguration.minimumTouchTargetSize].
 *
 * When [onDoubleTap] is provided, the tap gesture is detected only after
 * the [ViewConfiguration.doubleTapMinTimeMillis] has passed and [onDoubleTap] is called if the
 * second tap is started before [ViewConfiguration.doubleTapTimeoutMillis]. If [onDoubleTap] is not
 * provided, then [onTap] is called when the pointer up has been received.
 *
 * After the initial [onPress], if the pointer moves out of the input area, the position change
 * is consumed, or another gesture consumes the down or up events, the gestures are considered
 * canceled. That means [onDoubleTap], [onLongPress], and [onTap] will not be called after a
 * gesture has been canceled.
 *
 * If the first down event is consumed somewhere else, the entire gesture will be skipped,
 * including [onPress].
 */
public suspend fun PointerInputScope.detectTapGesturesIf(
  predicate: (Offset) -> Boolean = { true },
  onDoubleTap: ((Offset) -> Unit)? = null,
  onLongPress: ((Offset) -> Unit)? = null,
  onPress: suspend PressGestureScope.(Offset) -> Unit = NoPressGesture,
  onTap: ((Offset) -> Unit)? = null
): Unit = coroutineScope {
  // special signal to indicate to the sending side that it shouldn't intercept and consume
  // cancel/up events as we're only require down events
  val pressScope =
    PressGestureScopeImpl(this@detectTapGesturesIf)

  awaitEachGesture {
    val down = awaitFirstDown()
    if (!predicate(down.position)) {
      pressScope.reset()
      return@awaitEachGesture
    }
    down.consume()
    pressScope.reset()
    if (onPress !== NoPressGesture) launch {
      pressScope.onPress(down.position)
    }
    val longPressTimeout = onLongPress?.let {
      viewConfiguration.longPressTimeoutMillis
    } ?: (Long.MAX_VALUE / 2)
    var upOrCancel: PointerInputChange? = null
    try {
      // wait for first tap up or long press
      upOrCancel = withTimeout(longPressTimeout) {
        waitForUpOrCancellation()
      }
      if (upOrCancel == null) {
        pressScope.cancel() // tap-up was canceled
      } else {
        upOrCancel.consume()
        pressScope.release()
      }
    } catch (_: PointerEventTimeoutCancellationException) {
      onLongPress?.invoke(down.position)
      consumeUntilUp()
      pressScope.release()
    }

    if (upOrCancel != null) {
      // tap was successful.
      if (onDoubleTap == null) {
        onTap?.invoke(upOrCancel.position) // no need to check for double-tap.
      } else {
        // check for second tap
        val secondDown = awaitSecondDown(upOrCancel)

        if (secondDown == null) {
          onTap?.invoke(upOrCancel.position) // no valid second tap started
        } else {
          // Second tap down detected
          pressScope.reset()
          if (onPress !== NoPressGesture) {
            launch { pressScope.onPress(secondDown.position) }
          }

          try {
            // Might have a long second press as the second tap
            withTimeout(longPressTimeout) {
              val secondUp = waitForUpOrCancellation()
              if (secondUp != null) {
                secondUp.consume()
                pressScope.release()
                onDoubleTap(secondUp.position)
              } else {
                pressScope.cancel()
                onTap?.invoke(upOrCancel.position)
              }
            }
          } catch (e: PointerEventTimeoutCancellationException) {
            // The first tap was valid, but the second tap is a long press.
            // notify for the first tap
            onTap?.invoke(upOrCancel.position)

            // notify for the long press
            onLongPress?.invoke(secondDown.position)
            consumeUntilUp()
            pressScope.release()
          }
        }
      }
    }
  }
}

/**
 * Consumes all pointer events until nothing is pressed and then returns. This method assumes
 * that something is currently pressed.
 */
private suspend fun AwaitPointerEventScope.consumeUntilUp() {
  do {
    val event = awaitPointerEvent()
    event.changes.fastForEach { it.consume() }
  } while (event.changes.fastAny { it.pressed })
}

/**
 * Waits for [ViewConfiguration.doubleTapTimeoutMillis] for a second press event. If a
 * second press event is received before the time out, it is returned or `null` is returned
 * if no second press is received.
 */
private suspend fun AwaitPointerEventScope.awaitSecondDown(
  firstUp: PointerInputChange
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
  val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
  var change: PointerInputChange
  // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
  do {
    change = awaitFirstDown()
  } while (change.uptimeMillis < minUptime)
  change
}

/**
 * Reads events until all pointers are up or the gesture was canceled. The gesture
 * is considered canceled when a pointer leaves the event region, a position change
 * has been consumed or a pointer down change event was consumed in the [PointerEventPass.Main]
 * pass. If the gesture was not canceled, the final up change is returned or `null` if the
 * event was canceled.
 */
private suspend fun AwaitPointerEventScope.waitForUpOrCancellation(): PointerInputChange? {
  while (true) {
    val event = awaitPointerEvent(PointerEventPass.Main)
    if (event.changes.fastAll { it.changedToUp() }) {
      // All pointers are up
      return event.changes[0]
    }

    if (event.changes.fastAny {
        it.isConsumed || it.isOutOfBounds(size, extendedTouchPadding)
      }
    ) {
      return null // Canceled
    }

    // Check for cancel by position consumption. We can look on the Final pass of the
    // existing pointer event because it comes after the Main pass we checked above.
    val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
    if (consumeCheck.changes.fastAny { it.isConsumed }) {
      return null
    }
  }
}

/**
 * [detectTapGesturesIf]'s implementation of [PressGestureScope].
 */
private class PressGestureScopeImpl(
  density: Density
) : PressGestureScope, Density by density {
  private var isReleased = false
  private var isCanceled = false
  private val mutex = Mutex(locked = false)

  /**
   * Called when a gesture has been canceled.
   */
  fun cancel() {
    isCanceled = true
    mutex.unlock()
  }

  /**
   * Called when all pointers are up.
   */
  fun release() {
    isReleased = true
    mutex.unlock()
  }

  /**
   * Called when a new gesture has started.
   */
  fun reset() {
    mutex.tryLock() // If tryAwaitRelease wasn't called, this will be unlocked.
    isReleased = false
    isCanceled = false
  }

  override suspend fun awaitRelease() {
    if (!tryAwaitRelease()) {
      throw GestureCancellationException("The press gesture was canceled.")
    }
  }

  override suspend fun tryAwaitRelease(): Boolean {
    if (!isReleased && !isCanceled) {
      mutex.lock()
    }
    return isReleased
  }
}
