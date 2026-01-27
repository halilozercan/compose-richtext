package com.halilibo.richtext.ui.string

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Text
import com.halilibo.richtext.ui.currentContentColor
import com.halilibo.richtext.ui.currentRichTextStyle
import com.halilibo.richtext.ui.string.RichTextString.Format
import com.halilibo.richtext.ui.util.PhraseAnnotatedString
import com.halilibo.richtext.ui.util.segmentIntoPhrases
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
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
  decorations: RichTextDecorations = RichTextDecorations(),
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE,
) {
  val style = currentRichTextStyle.stringStyle
  val contentColor = currentContentColor
  val resolvedStyle = remember(style) {
    (style ?: RichTextStringStyle.Default).resolveDefaults()
  }
  val annotated = remember(text, resolvedStyle, contentColor, decorations) {
    text.toAnnotatedString(resolvedStyle, contentColor, decorations)
  }
  val baseInlineContents = remember(text) { text.getInlineContents() }
  val resolvedLinkDecorations = remember(text, decorations) {
    text.resolveLinkDecorations(decorations)
  }
  val hasInlineIcons = remember(resolvedLinkDecorations) {
    resolvedLinkDecorations.any { it.hasInlineContent() }
  }
  val decoratedTextResult = remember(
    annotated,
    baseInlineContents,
    resolvedLinkDecorations,
    hasInlineIcons,
  ) {
    if (hasInlineIcons) {
      decorateAnnotatedStringWithLinkIcons(
        annotated = annotated,
        baseInlineContents = baseInlineContents,
        linkDecorations = resolvedLinkDecorations,
      )
    } else {
      DecoratedTextResult(
        annotatedString = annotated,
        inlineContents = baseInlineContents,
        decoratedLinkRanges = resolvedLinkDecorations
          .filter { it.underlineStyle !is UnderlineStyle.Solid }
          .map { range ->
            DecoratedLinkRange(
              start = range.start,
              end = range.end,
              destination = range.destination,
              underlineStyle = range.underlineStyle,
              underlineColor = range.underlineColor,
              linkStyleOverride = range.linkStyleOverride,
            )
          },
      )
    }
  }
  val inlineContents = decoratedTextResult.inlineContents
  val decoratedLinkRanges = decoratedTextResult.decoratedLinkRanges
  var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
  val animatedText = if (renderOptions.animate && inlineContents.isEmpty()) {
    rememberAnimatedText(
      annotated = decoratedTextResult.annotatedString,
      contentColor = contentColor,
      renderOptions = renderOptions,
      isLeafText = isLeafText,
      sharedAnimationState = sharedAnimationState,
    )
  } else {
    decoratedTextResult.annotatedString
  }
  val isPartialText = animatedText.text.length < decoratedTextResult.annotatedString.text.length
  val underlineSpecs = remember(
    decoratedLinkRanges,
    resolvedStyle,
    contentColor,
    animatedText,
    isPartialText,
  ) {
    decoratedLinkRanges.mapNotNull { range ->
      if (isPartialText && range.end > animatedText.text.length) return@mapNotNull null
      val linkStyle = range.linkStyleOverride
        ?.invoke(resolvedStyle.linkStyle)
        ?: resolvedStyle.linkStyle
      val underlineColor = range.underlineColor
        ?: linkStyle?.style?.color
          ?.takeIf { it.isSpecified }
        ?: contentColor
      val textLength = animatedText.text.length
      val clampedStart = range.start.coerceIn(0, textLength)
      val clampedEnd = range.end.coerceIn(0, textLength)
      if (clampedStart >= clampedEnd) return@mapNotNull null
      val hasLinkAnnotation = animatedText
        .getLinkAnnotations(clampedStart, clampedEnd)
        .isNotEmpty()
      if (!hasLinkAnnotation) return@mapNotNull null
      UnderlineSpec(
        range = range,
        color = underlineColor,
      )
    }
  }
  val underlineModifier = if (underlineSpecs.isNotEmpty()) {
    Modifier.drawWithContent {
      drawContent()
      val layoutResult = textLayoutResult ?: return@drawWithContent
      underlineSpecs.fastForEach { spec ->
        drawUnderline(
          layoutResult = layoutResult,
          start = spec.range.start,
          end = spec.range.end,
          underlineStyle = spec.range.underlineStyle,
          color = spec.color,
        )
      }
    }
  } else {
    Modifier
  }

  if (inlineContents.isEmpty()) {
    Text(
      text = animatedText,
      onTextLayout = { layoutResult ->
        textLayoutResult = layoutResult
        onTextLayout(layoutResult)
      },
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      modifier = modifier.then(underlineModifier),
    )
  } else {
    val inlineTextConstraints = remember { mutableStateOf(Constraints()) }
    val inlineTextContents = manageInlineTextContents(
      inlineContents = inlineContents,
      textConstraints = inlineTextConstraints,
    )

    Text(
      text = animatedText,
      onTextLayout = { layoutResult ->
        textLayoutResult = layoutResult
        onTextLayout(layoutResult)
      },
      inlineContent = inlineTextContents,
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      modifier = modifier.then(underlineModifier).layout { measurable, constraints ->
        // Prepares the custom constraints InlineTextContents before they get measured.
        inlineTextConstraints.value = constraints.copy(minWidth = 0, minHeight = 0)
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
          placeable.place(0, 0)
        }
      },
    )
  }
}

private data class UnderlineSpec(
  val range: DecoratedLinkRange,
  val color: Color,
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUnderline(
  layoutResult: TextLayoutResult,
  start: Int,
  end: Int,
  underlineStyle: UnderlineStyle,
  color: Color,
) {
  val textLength = layoutResult.layoutInput.text.text.length
  val clampedStart = start.coerceIn(0, textLength)
  val clampedEnd = end.coerceIn(0, textLength)
  if (clampedStart >= clampedEnd) return

  val strokeWidthPx: Float
  val offsetPx: Float
  val pathEffect: PathEffect?
  val cap: StrokeCap

  with(this) {
    when (underlineStyle) {
      is UnderlineStyle.Solid -> {
        strokeWidthPx = 1.dp.toPx()
        offsetPx = 0.dp.toPx()
        pathEffect = null
        cap = StrokeCap.Butt
      }
      is UnderlineStyle.Dotted -> {
        strokeWidthPx = underlineStyle.strokeWidth.toPx()
        offsetPx = underlineStyle.offset.toPx()
        val gapPx = underlineStyle.gap.toPx()
        val dotPx = strokeWidthPx.coerceAtLeast(1f)
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dotPx, gapPx), 0f)
        cap = StrokeCap.Round
      }
      is UnderlineStyle.Dashed -> {
        strokeWidthPx = underlineStyle.strokeWidth.toPx()
        offsetPx = underlineStyle.offset.toPx()
        val dashPx = underlineStyle.dash.toPx()
        val gapPx = underlineStyle.gap.toPx()
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
        cap = StrokeCap.Butt
      }
    }
  }

  val startLine = layoutResult.getLineForOffset(clampedStart)
  val endLine = layoutResult.getLineForOffset(clampedEnd - 1)
  for (line in startLine..endLine) {
    val lineStart = layoutResult.getLineStart(line)
    val lineEnd = layoutResult.getLineEnd(line, visibleEnd = true)
    val segmentStart = maxOf(clampedStart, lineStart)
    val segmentEnd = minOf(clampedEnd, lineEnd)
    if (segmentEnd <= segmentStart) continue

    val startBox = layoutResult.getBoundingBox(segmentStart)
    val endBox = layoutResult.getBoundingBox(segmentEnd - 1)
    val y = (maxOf(startBox.bottom, endBox.bottom) + offsetPx).roundToInt().toFloat()
    val xStart = startBox.left.roundToInt().toFloat()
    val xEnd = endBox.right.roundToInt().toFloat()

    drawLine(
      color = color,
      start = Offset(xStart, y),
      end = Offset(xEnd, y),
      strokeWidth = strokeWidthPx,
      cap = cap,
      pathEffect = pathEffect,
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
      .filter { it > lastAnimationIndex.intValue }
      .forEach { phraseIndex ->
        val animation = TextAnimation(phraseIndex)
        animations[phraseIndex] = animation
        lastAnimationIndex.intValue = phraseIndex
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
          // Remove animation right away, in case it had split at an inappropriate unicode point.
          animations.remove(phraseIndex)
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

  // contentColor rarely changes, and it's not already a State. When contentColor changes, a new
  // AnnotatedString must be created using the updated contentColor value.
  return remember(contentColor) {
    // textToRender and the set of animations are tracked as States, and trigger the derivedStateOf
    // to return a new value in order to create a new AnnotatedString with the latest text and
    // animated Brushes.
    // This will only return a new value if the actual text changes, or the *set* of animated spans
    // has changed.
    // When a span gets a new animated value, the AnnotatedString will not be updated. Instead,
    // the text will just be re-drawn, since the animated alpha state was read only inside
    // DynamicSolidColor during the draw phase.
    derivedStateOf {
      textToRender.value.withDynamicColorPhrases(
        contentColor = contentColor,
        animations = animations.values,
        onlyVisible = renderOptions.onlyRenderVisibleText,
      )
    }
  }.value
}

private class TextAnimation(val startIndex: Int) {

  var alpha by mutableFloatStateOf(0f)
  val isVisible by derivedStateOf { alpha > 0f }
}

private fun AnnotatedString.withDynamicColorPhrases(
  contentColor: Color,
  animations: Collection<TextAnimation>,
  onlyVisible: Boolean,
): AnnotatedString {
  if (text.isEmpty() || animations.isEmpty()) {
    return this
  }
  var remainingLength = length
  val modifiedTextSnippets = mutableListOf<AnnotatedString>()
  var dropInvisible = onlyVisible
  for (animation in animations.sortedByDescending { it.startIndex }) {
    if (animation.startIndex >= remainingLength) continue
    if (!dropInvisible || animation.isVisible) {
      dropInvisible = false
      modifiedTextSnippets += subSequence(animation.startIndex, remainingLength)
        .withDynamicColor(contentColor, alpha = { animation.alpha })
    }
    remainingLength = animation.startIndex
  }
  return buildAnnotatedString {
    append(this@withDynamicColorPhrases, start = 0, end = remainingLength)
    modifiedTextSnippets.reversed().forEach { append(it) }
  }
}

private fun AnnotatedString.withDynamicColor(color: Color, alpha: () -> Float): AnnotatedString {
  val useDynamicColor = !maybeContainsEmojis()

  val subStyles = spanStyles.map {
    val style = it.item
    if (useDynamicColor) {
      it.copy(item = style.copy(brush = DynamicSolidColor(style.color) { style.alpha * alpha() }))
    } else if (style.color.isSpecified) {
      it.copy(item = style.copy(color = style.color.copy(alpha = style.color.alpha * alpha())))
    } else {
      it.copy(item = style.copy(brush = style.brush, alpha = alpha()))
    }
  }
  val fullStyle = AnnotatedString.Range(
    item = if (useDynamicColor) {
      SpanStyle(brush = DynamicSolidColor(color, alpha))
    } else {
      SpanStyle(brush = DynamicSolidColor(color) { 1f }, alpha = alpha())
    },
    start = 0,
    end = length
  )
  val builder = AnnotatedString.Builder(text)
  subStyles.fastForEach { builder.addStyle(it.item, it.start, it.end) }
  builder.addStyle(fullStyle.item, fullStyle.start, fullStyle.end)
  paragraphStyles.fastForEach { builder.addStyle(it.item, it.start, it.end) }
  getLinkAnnotations(0, length).fastForEach { annotation ->
    when (val link = annotation.item) {
      is LinkAnnotation.Url -> builder.addLink(link, annotation.start, annotation.end)
      is LinkAnnotation.Clickable -> builder.addLink(link, annotation.start, annotation.end)
    }
  }
  return builder.toAnnotatedString()
}

private fun CharSequence.maybeContainsEmojis(): Boolean {
  var i = 0
  val n = length
  while (i < n) {
    val cp = Character.codePointAt(this, i)

    // --- Quick accepts: common emoji blocks ---
    val isEmoji = when (cp) {
      // Misc Symbols + Dingbats + arrows subset that often render as emoji
      in 0x2600..0x27BF -> true
      // Enclosed CJK (e.g., 🈶, 🈚)
      in 0x1F200..0x1F2FF -> true
      // Misc Symbols & Pictographs
      in 0x1F300..0x1F5FF -> true
      // Emoticons
      in 0x1F600..0x1F64F -> true
      // Transport & Map
      in 0x1F680..0x1F6FF -> true
      // Supplemental Symbols & Pictographs
      in 0x1F900..0x1F9FF -> true
      // Symbols & Pictographs Extended-A (newer emoji live here)
      in 0x1FA70..0x1FAFF -> true
      // Regional indicators (flags as pairs, but single is enough for "contains")
      in 0x1F1E6..0x1F1FF -> true
      // Keycap base digits/#/* (paired with VS16 + COMBINING ENCLOSING KEYCAP, but base char is fine)
      in 0x0030..0x0039, 0x0023, 0x002A -> true
      // Variation Selector-16 forces emoji presentation for some BMP symbols
      0xFE0F -> true
      else -> false
    }

    if (isEmoji) return true

    i += Character.charCount(cp)
  }
  return false
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
