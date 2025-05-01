package com.halilibo.richtext.ui.string

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.halilibo.richtext.ui.ClickableText
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.currentContentColor
import com.halilibo.richtext.ui.currentRichTextStyle
import com.halilibo.richtext.ui.string.RichTextString.Format
import com.halilibo.richtext.ui.util.PhraseAnnotatedString
import com.halilibo.richtext.ui.util.segmentIntoPhrases
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Renders a [RichTextString] as created with [richTextString].
 *
 * @sample com.halilibo.richtext.ui.previews.TextPreview
 */
@Composable
public fun RichTextScope.Text(
  text: RichTextString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  softWrap: Boolean = true,
  isLeafText: Boolean = true,
  renderOptions: RichTextRenderOptions = RichTextRenderOptions(),
  sharedAnimationState: MarkdownAnimationState = remember { MarkdownAnimationState() },
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) {
  val style = currentRichTextStyle.stringStyle
  val contentColor = currentContentColor
  val annotated = remember(text, style, contentColor) {
    val resolvedStyle = (style ?: RichTextStringStyle.Default).resolveDefaults()
    text.toAnnotatedString(resolvedStyle, contentColor)
  }
  val inlineContents = remember(text) { text.getInlineContents() }

  val animatedText = if (renderOptions.animate && inlineContents.isEmpty()) {
    rememberAnimatedText(
      annotated = annotated,
      contentColor = contentColor,
      renderOptions = renderOptions,
      isLeafText = isLeafText,
      sharedAnimationState = sharedAnimationState,
    )
  } else {
    annotated
  }

  BoxWithConstraints(modifier = modifier) {
    val inlineTextContents = manageInlineTextContents(
      inlineContents = inlineContents,
      textConstraints = constraints,
    )
    ClickableText(
      text = animatedText,
      onTextLayout = onTextLayout,
      inlineContent = inlineTextContents,
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      isOffsetClickable = { offset ->
        // When you click past the end of the string, the offset is where the caret should be
        // placed. However, when it is at the end, offset == text.length but parent links will at
        // most end at length - 1. So we need to coerce the offset to be at most length - 1.
        // This fixes an image where only the left side of an image wrapped with a link was only
        // clickable on the left side.
        // However, if a paragraph ends with a link, the link will be clickable past the
        // end of the last line.
        annotated.getConsumableAnnotations(
          text.formatObjects,
          offset.coerceAtMost(annotated.length - 1)
        ).any()
      },
      onClick = { offset ->
        annotated.getConsumableAnnotations(
          text.formatObjects,
          offset.coerceAtMost(annotated.length - 1)
        )
          .firstOrNull()
          ?.let { link -> link.onClick() }
      }
    )
  }
}

@Stable
public class MarkdownAnimationState {

  private var lastAnimationStartMs by mutableLongStateOf(0L)

  public fun addAnimation(renderOptions: RichTextRenderOptions) {
    lastAnimationStartMs = calculatedDelay(renderOptions) + System.currentTimeMillis()
  }

  private fun calculatedDelay(renderOptions: RichTextRenderOptions): Long {
    val now = System.currentTimeMillis()
    val diffMs = lastAnimationStartMs - now

    return when {
      lastAnimationStartMs <= 0L -> 0
      diffMs < -renderOptions.delayMs -> 0 // We are past the last animation, so launch it now.
      diffMs <= 0 -> renderOptions.delayMs - diffMs
      else -> diffMs + (renderOptions.delayMs * (renderOptions.delayMs / diffMs.toDouble()).pow(
        renderOptions.delayExponent
      )).toLong()
    }.also {
      println("Calculated delay: $it now: $now last: $lastAnimationStartMs diff: $diffMs")
    }
  }

  public fun toDelayMs(): Int =
    (lastAnimationStartMs - System.currentTimeMillis()).coerceAtLeast(0).toInt()
}

@Composable
private fun rememberAnimatedText(
  annotated: AnnotatedString,
  renderOptions: RichTextRenderOptions,
  contentColor: Color,
  sharedAnimationState: MarkdownAnimationState,
  isLeafText: Boolean,
): AnnotatedString {
  val coroutineScope = rememberCoroutineScope()
  val animations = remember { mutableStateMapOf<Int, TextAnimation>() }
  val textToRender = remember { mutableStateOf(AnnotatedString("")) }

  val lastAnimationIndex = remember { mutableIntStateOf(-1) }
  val lastPhrases = remember { mutableStateOf(PhraseAnnotatedString()) }
  val updatePhrases = { phrases: PhraseAnnotatedString ->
    lastPhrases.value = phrases
    textToRender.value = phrases.makeCompletePhraseString(!isLeafText)
    phrases.phraseSegments
      .filter { it > lastAnimationIndex.value }
      .forEach { phraseIndex ->
        val animation = TextAnimation(phraseIndex)
        animations[phraseIndex] = animation
        lastAnimationIndex.value = phraseIndex
        coroutineScope.launch {
          sharedAnimationState.addAnimation(renderOptions)
          var hasAnimationFired = false
          animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = renderOptions.textFadeInMs,
              delayMillis = sharedAnimationState.toDelayMs(),
            )
          ) { value, _ ->
            animation.alpha = value
            if (!hasAnimationFired) {
              renderOptions.onPhraseAnimate()
              hasAnimationFired = true
            } else {
              renderOptions.onTextAnimate()
            }
          }
        }
      }
    // Since animations are already being updated, remove any animations that have finished.
    animations.forEach { (key, animation) ->
      if (animation.alpha == 1f) {
        animations.remove(key)
      }
    }
  }
  LaunchedEffect(isLeafText, annotated) {
    val isComplete = !isLeafText
    // If we detect a new phrase, kick off the animation now.
    val phrases = annotated.segmentIntoPhrases(renderOptions, isComplete = isComplete)
    if (isComplete && phrases == lastPhrases.value) return@LaunchedEffect
    if (!isComplete && !phrases.hasNewPhrasesFrom(lastPhrases.value)) return@LaunchedEffect
    updatePhrases(phrases)

    if (!isComplete) {
      // In case no changes happen for a while, we'll render after some timeout
      delay(renderOptions.debounceMs.milliseconds)
      if (annotated.text.isNotEmpty()) {
        val debouncedPhrases = annotated.segmentIntoPhrases(renderOptions, isComplete = true)
        if (debouncedPhrases != lastPhrases.value) {
          updatePhrases(debouncedPhrases)
        }
      }
    }
  }

  return textToRender.value.animateAlphas(animations.values, contentColor)
}

private class TextAnimation(val startIndex: Int) {

  var alpha by mutableFloatStateOf(0f)
}

private fun AnnotatedString.animateAlphas(
  animations: Collection<TextAnimation>, contentColor: Color
): AnnotatedString {
  if (text.isEmpty() || animations.isEmpty()) {
    return this
  }
  var remainingLength = length
  val modifiedTextSnippets = mutableListOf<AnnotatedString>()
  for (animation in animations.sortedByDescending { it.startIndex }) {
    if (animation.startIndex >= remainingLength) continue
    modifiedTextSnippets += subSequence(animation.startIndex, remainingLength)
      .changeColor(contentColor, alpha = { animation.alpha })
    remainingLength = animation.startIndex
  }
  return buildAnnotatedString {
    append(this@animateAlphas, start = 0, end = remainingLength)
    modifiedTextSnippets.reversed().forEach { append(it) }
  }
}

private fun AnnotatedString.changeColor(color: Color, alpha: () -> Float): AnnotatedString {
  val subStyles = spanStyles.map {
    it.copy(item = it.item.copy(brush = DynamicSolidColor(it.item.color, alpha)))
  }
  val fullStyle =
    AnnotatedString.Range(SpanStyle(brush = DynamicSolidColor(color, alpha)), 0, length)
  return AnnotatedString(text, subStyles + fullStyle)
}

private fun AnnotatedString.getConsumableAnnotations(
  textFormatObjects: Map<String, Any>,
  offset: Int,
): Sequence<Format.Link> =
  getStringAnnotations(Format.FormatAnnotationScope, offset, offset)
    .asSequence()
    .mapNotNull {
      Format.findTag(
        it.item,
        textFormatObjects
      ) as? Format.Link
    }

/**
 * Custom brush allows animating the alpha of this Brush via recompositions only in the draw phase.
 */
private data class DynamicSolidColor(private val color: Color, private val alpha: () -> Float) :
  ShaderBrush() {

  override fun createShader(size: Size): Shader {
    val color = color.copy(alpha = color.alpha * alpha())
    return LinearGradientShader(Offset.Zero, Offset(size.width, size.height), listOf(color, color))
  }
}
