package com.halilibo.richtext.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.halilibo.richtext.ui.string.MarkdownAnimationState
import com.halilibo.richtext.ui.string.RichTextRenderOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun rememberMarkdownFade(
  richTextRenderOptions: RichTextRenderOptions,
  markdownAnimationState: MarkdownAnimationState,
): State<Float> {
  val coroutineScope = rememberCoroutineScope()
  val targetAlpha = remember {
    Animatable(if (richTextRenderOptions.animate) 0f else 1f)
  }
  LaunchedEffect(Unit) {
    if (richTextRenderOptions.animate) {
      coroutineScope.launch {
        markdownAnimationState.addAnimation(richTextRenderOptions)
        delay(markdownAnimationState.toDelayMs().milliseconds)
        targetAlpha.animateTo(
          1f,
          tween(
            durationMillis = richTextRenderOptions.textFadeInMs,
          )
        )
      }
    }
  }
  return targetAlpha.asState()
}
