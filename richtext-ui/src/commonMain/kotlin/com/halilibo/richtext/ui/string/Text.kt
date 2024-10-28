package com.halilibo.richtext.ui.string

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
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
  sharedAnimationState: MutableState<MarkdownAnimationState> =
    mutableStateOf(DefaultMarkdownAnimationState),
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

  val animatedText = rememberAnimatedText(
    annotated = annotated,
    renderOptions = renderOptions,
    contentColor = contentColor,
    sharedAnimationState = sharedAnimationState,
    isLeafText = isLeafText,
  )

  BoxWithConstraints(modifier = modifier) {
    val inlineTextContents = manageInlineTextContents(
      annotatedString = animatedText,
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

public data class MarkdownAnimationState(
  val lastAnimationStartMs: Long = 0,
) {
  public fun addAnimation(renderOptions: RichTextRenderOptions): MarkdownAnimationState = copy(
    lastAnimationStartMs = calculatedDelay(renderOptions) + System.currentTimeMillis()
  )

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

// Add a default value
public val DefaultMarkdownAnimationState: MarkdownAnimationState = MarkdownAnimationState()

@Composable
@OptIn(FlowPreview::class)
private fun rememberAnimatedText(
  annotated: AnnotatedString,
  renderOptions: RichTextRenderOptions,
  contentColor: Color,
  sharedAnimationState: MutableState<MarkdownAnimationState>,
  isLeafText: Boolean,
): AnnotatedString {
  val coroutineScope = rememberCoroutineScope()
  val animations = remember { mutableStateMapOf<Int, TextAnimation>() }
  val textToRender = remember { mutableStateOf(AnnotatedString("")) }
  if (renderOptions.animate) {
    val lastAnimationIndex = remember { mutableIntStateOf(-1) }
    val readyToAnimateText = remember { mutableStateOf(PhraseAnnotatedString()) }
    // In case no changes happen for a while, we'll render after some timeout
    val debouncedTextFlow = remember { MutableStateFlow(AnnotatedString("")) }
    val debouncedText by remember {
      debouncedTextFlow.debounce(renderOptions.debounceMs.milliseconds)
    }.collectAsState(AnnotatedString(""), coroutineScope.coroutineContext)

    val animationUpdate: () -> Unit = {
      val phrases = readyToAnimateText.value
      phrases.phraseSegments
        .filter { it > lastAnimationIndex.value }
        .forEach { phraseIndex ->
          animations[phraseIndex] = TextAnimation(phraseIndex, 0f)
          lastAnimationIndex.value = phraseIndex
          coroutineScope.launch {
            textToRender.value = readyToAnimateText.value.makeCompletePhraseString(!isLeafText)
            sharedAnimationState.value = sharedAnimationState.value.addAnimation(renderOptions)
            var hasAnimationFired = false
            Animatable(0f).animateTo(
              targetValue = 1f,
              animationSpec = tween(
                durationMillis = renderOptions.textFadeInMs,
                delayMillis = sharedAnimationState.value.toDelayMs(),
              )
            ) {
              if (!hasAnimationFired) {
                renderOptions.onPhraseAnimate()
                hasAnimationFired = true
              } else {
                renderOptions.onTextAnimate()
              }
              animations[phraseIndex] = TextAnimation(phraseIndex, value)
            }
            animations.remove(phraseIndex)
          }
        }
      if (phrases.isComplete) {
        textToRender.value = phrases.annotatedString
      }
    }
    LaunchedEffect(annotated) {
      debouncedTextFlow.value = annotated
      // If we detect a new phrase, kick off the animation now.
      val phrases = annotated.segmentIntoPhrases(renderOptions, isComplete = !isLeafText)
      if (phrases.hasNewPhrasesFrom(readyToAnimateText.value).not()) return@LaunchedEffect
      readyToAnimateText.value = phrases
      animationUpdate()
    }
    LaunchedEffect(isLeafText, annotated) {
      if (isLeafText) return@LaunchedEffect
      val phrases = annotated.segmentIntoPhrases(renderOptions, isComplete = true)
      if (phrases != readyToAnimateText.value) {
        readyToAnimateText.value = phrases
        animationUpdate()
      }
    }
    LaunchedEffect(debouncedText) {
      if (debouncedText.text.isEmpty()) return@LaunchedEffect
      val phrases = debouncedText.segmentIntoPhrases(renderOptions, isComplete = true)
      if (phrases != readyToAnimateText.value) {
        readyToAnimateText.value = phrases
        animationUpdate()
      }
    }

  } else {
    // If we're not animating, just render the text as is.
    textToRender.value = annotated
  }

  return textToRender.value.animateAlphas(animations.values, contentColor)
}

private data class TextAnimation(val startIndex: Int, val alpha: Float)

private fun AnnotatedString.animateAlphas(
  animations: Collection<TextAnimation>, contentColor: Color
): AnnotatedString {
  if (this.text.isEmpty() || animations.isEmpty()) {
    return this
  }
  var remainingText = this
  val modifiedTextSnippets = mutableStateListOf<AnnotatedString>()
  animations.sortedByDescending { it.startIndex }.forEach { animation ->
    if (animation.startIndex >= remainingText.length) {
      return@forEach
    }
    modifiedTextSnippets.add(
      remainingText.subSequence(animation.startIndex, remainingText.length)
        .changeAlpha(animation.alpha, contentColor)
    )
    remainingText = remainingText.subSequence(0, animation.startIndex)
  }
  return AnnotatedString.Builder(remainingText).apply {
    modifiedTextSnippets.reversed().forEach { append(it) }
  }.toAnnotatedString()
}

private fun AnnotatedString.changeAlpha(alpha: Float, contentColor: Color): AnnotatedString {
  val newWordsStyles = spanStyles.map { spanStyle ->
    spanStyle.copy(item = spanStyle.item.copy(color = spanStyle.item.color.copy(alpha = alpha)))
  } + listOf(AnnotatedString.Range(SpanStyle(contentColor.copy(alpha = alpha)), 0, length))
  val stringAnnotations = getStringAnnotations(0, length)
  if (paragraphStyles.isEmpty() && stringAnnotations.isEmpty()) {
    return AnnotatedString(text, newWordsStyles)
  }
  return buildAnnotatedString {
    append(text)
    newWordsStyles.forEach { addStyle(it.item, it.start, it.end) }
    stringAnnotations.forEach { addStringAnnotation(it.tag, it.item, it.start, it.end) }
    paragraphStyles.forEach { addStyle(it.item, it.start, it.end) }
  }
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
