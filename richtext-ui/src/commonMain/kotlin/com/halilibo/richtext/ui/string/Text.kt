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
import kotlin.math.sqrt
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
    mutableIntStateOf(DefaultMarkdownAnimationState),
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
    contentColor = contentColor,
    renderOptions = renderOptions,
    isLeafText = isLeafText,
    sharedAnimationState = sharedAnimationState,
    hasInlineTextContent = inlineContents.isNotEmpty(),
  )

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
        annotated.getConsumableAnnotations(text.formatObjects, offset.coerceAtMost(annotated.length - 1)).any()
      },
      onClick = { offset ->
        annotated.getConsumableAnnotations(text.formatObjects, offset.coerceAtMost(annotated.length - 1))
          .firstOrNull()
          ?.let { link -> link.onClick() }
      }
    )
  }
}

public typealias MarkdownAnimationState = Int
// Add a default value
public val DefaultMarkdownAnimationState: MarkdownAnimationState = 0
private fun MarkdownAnimationState.addAnimation(): MarkdownAnimationState = this + 1
private fun MarkdownAnimationState.removeAnimation(): MarkdownAnimationState = this - 1
private fun MarkdownAnimationState.toDelayMs(defaultDelayMs: Int): Int =
  (sqrt(this.toDouble()) * defaultDelayMs).toInt()

@Composable
@OptIn(FlowPreview::class)
private fun rememberAnimatedText(
  annotated: AnnotatedString,
  renderOptions: RichTextRenderOptions,
  contentColor: Color,
  sharedAnimationState: MutableState<MarkdownAnimationState>,
  isLeafText: Boolean,
  hasInlineTextContent: Boolean,
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

    LaunchedEffect(annotated) {
      debouncedTextFlow.value = annotated
      // If we detect a new phrase, kick off the animation now.
      val phrases = annotated.segmentIntoPhrases(isComplete = !isLeafText)
      if (phrases.hasNewPhrasesFrom(readyToAnimateText.value)) {
        readyToAnimateText.value = phrases
      }
    }
    LaunchedEffect(isLeafText, annotated) {
      if (!isLeafText) {
        readyToAnimateText.value = annotated.segmentIntoPhrases(isComplete = true)
      }
    }
    LaunchedEffect(debouncedText) {
      if (debouncedText.text.isNotEmpty()) {
        readyToAnimateText.value = debouncedText.segmentIntoPhrases(isComplete = true)
      }
    }

    LaunchedEffect(readyToAnimateText.value) {
      val phrases = readyToAnimateText.value
      phrases.phraseSegments
        .filter { it > lastAnimationIndex.value }
        .forEach { phraseIndex ->
          animations[phraseIndex] = TextAnimation(phraseIndex, 0f)
          lastAnimationIndex.value = phraseIndex
          coroutineScope.launch {
            textToRender.value = readyToAnimateText.value.makeCompletePhraseString(!isLeafText)
            sharedAnimationState.value = sharedAnimationState.value.addAnimation()
            Animatable(0f).animateTo(
              targetValue = 1f,
              animationSpec = tween(
                durationMillis = renderOptions.textFadeInMs,
                delayMillis = sharedAnimationState.value.toDelayMs(renderOptions.delayMs),
              )
            ) {
              animations[phraseIndex] = TextAnimation(phraseIndex, value)
              renderOptions.onTextAnimate()
            }
            sharedAnimationState.value = sharedAnimationState.value.removeAnimation()
            animations.remove(phraseIndex)
          }
        }
      if (phrases.isComplete) {
        textToRender.value = phrases.annotatedString
      }
    }
  } else {
    // If we're not animating, just render the text as is.
    textToRender.value = annotated
  }


  // Ignore animated text if we have inline content, since it causes crashes.
  return if (!hasInlineTextContent) {
    textToRender.value.animateAlphas(animations.values, contentColor)
  } else {
    annotated
  }
}

private data class TextAnimation(val startIndex: Int, val alpha: Float) 

private fun AnnotatedString.animateAlphas(
  animations: Collection<TextAnimation>, contentColor: Color): AnnotatedString {
  if (this.text.isEmpty() || animations.isEmpty()) {
    return this
  }
  var remainingText = this
  val modifiedTextSnippets = mutableStateListOf<AnnotatedString>()
  animations.sortedByDescending { it.startIndex }.forEach { animation ->
    if (animation.startIndex >= remainingText.length) return@forEach
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
  val newWordsStyles = spanStyles.map { spanstyle ->
        spanstyle.copy(item = spanstyle.item.copy(color = spanstyle.item.color.copy(alpha = alpha)))
      } + listOf(AnnotatedString.Range(SpanStyle(contentColor.copy(alpha = alpha)), 0, length))
  return AnnotatedString(text, newWordsStyles)
}


private fun AnnotatedString.getConsumableAnnotations(textFormatObjects: Map<String, Any>, offset: Int): Sequence<Format.Link> =
  getStringAnnotations(Format.FormatAnnotationScope, offset, offset)
    .asSequence()
    .mapNotNull {
      Format.findTag(
        it.item,
        textFormatObjects
      ) as? Format.Link
    }
