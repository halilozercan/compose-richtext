@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "SuspiciousCollectionReassignment")

package com.halilibo.richtext.ui.string

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.DefaultCodeBlockBackgroundColor
import com.halilibo.richtext.ui.string.RichTextString.Builder
import com.halilibo.richtext.ui.string.RichTextString.Format
import com.halilibo.richtext.ui.string.RichTextString.Format.Bold
import com.halilibo.richtext.ui.string.RichTextString.Format.Code
import com.halilibo.richtext.ui.string.RichTextString.Format.Companion.FormatAnnotationScope
import com.halilibo.richtext.ui.string.RichTextString.Format.Italic
import com.halilibo.richtext.ui.string.RichTextString.Format.Link
import com.halilibo.richtext.ui.string.RichTextString.Format.Strikethrough
import com.halilibo.richtext.ui.string.RichTextString.Format.Subscript
import com.halilibo.richtext.ui.string.RichTextString.Format.Superscript
import com.halilibo.richtext.ui.string.RichTextString.Format.Underline
import com.halilibo.richtext.ui.util.randomUUID
import kotlin.LazyThreadSafetyMode.NONE

/** Copied from inline content. */
@PublishedApi
internal const val REPLACEMENT_CHAR: String = "\uFFFD"

/**
 * Defines the [SpanStyle]s that are used for various [RichTextString] formatting directives.
 */
@Immutable
public data class RichTextStringStyle(
  val boldStyle: SpanStyle? = null,
  val italicStyle: SpanStyle? = null,
  val underlineStyle: SpanStyle? = null,
  val strikethroughStyle: SpanStyle? = null,
  val subscriptStyle: SpanStyle? = null,
  val superscriptStyle: SpanStyle? = null,
  val codeStyle: SpanStyle? = null,
  val linkStyle: SpanStyle? = null
) {
  internal fun merge(otherStyle: RichTextStringStyle?): RichTextStringStyle {
    if (otherStyle == null) return this
    return RichTextStringStyle(
      boldStyle = boldStyle.merge(otherStyle.boldStyle),
      italicStyle = italicStyle.merge(otherStyle.italicStyle),
      underlineStyle = underlineStyle.merge(otherStyle.underlineStyle),
      strikethroughStyle = strikethroughStyle.merge(otherStyle.strikethroughStyle),
      subscriptStyle = subscriptStyle.merge(otherStyle.subscriptStyle),
      superscriptStyle = superscriptStyle.merge(otherStyle.superscriptStyle),
      codeStyle = codeStyle.merge(otherStyle.codeStyle),
      linkStyle = linkStyle.merge(otherStyle.linkStyle)
    )
  }

  internal fun resolveDefaults(): RichTextStringStyle =
    RichTextStringStyle(
      boldStyle = boldStyle ?: Bold.DefaultStyle,
      italicStyle = italicStyle ?: Italic.DefaultStyle,
      underlineStyle = underlineStyle ?: Underline.DefaultStyle,
      strikethroughStyle = strikethroughStyle ?: Strikethrough.DefaultStyle,
      subscriptStyle = subscriptStyle ?: Subscript.DefaultStyle,
      superscriptStyle = superscriptStyle ?: Superscript.DefaultStyle,
      codeStyle = codeStyle ?: Code.DefaultStyle,
      linkStyle = linkStyle ?: Link.DefaultStyle
    )

  public companion object {
    public val Default: RichTextStringStyle = RichTextStringStyle()

    private fun SpanStyle?.merge(otherStyle: SpanStyle?): SpanStyle? =
      this?.merge(otherStyle) ?: otherStyle
  }
}

/**
 * Convenience function for creating a [RichTextString] using a [Builder].
 */
public inline fun richTextString(builder: Builder.() -> Unit): RichTextString =
  Builder().apply(builder)
    .toRichTextString()

/**
 * A special type of [AnnotatedString] that is formatted using higher-level directives that are
 * configured using a [RichTextStringStyle].
 */
@Immutable
public data class RichTextString internal constructor(
  private val taggedString: AnnotatedString,
  internal val formatObjects: Map<String, Any>
) {

  private val length: Int get() = taggedString.length
  val text: String get() = taggedString.text

  public operator fun plus(other: RichTextString): RichTextString =
    Builder(length + other.length).run {
      append(this@RichTextString)
      append(other)
      toRichTextString()
    }

  internal fun toAnnotatedString(
    style: RichTextStringStyle,
    contentColor: Color
  ): AnnotatedString =
    buildAnnotatedString {
      append(taggedString)

      // Get all of our format annotations.
      val tags = taggedString.getStringAnnotations(FormatAnnotationScope, 0, taggedString.length)
      // And apply their actual SpanStyles to the string.
      tags.forEach { range ->
        val format = Format.findTag(range.item, formatObjects) ?: return@forEach
        format.getStyle(style, contentColor)
          ?.let { spanStyle -> addStyle(spanStyle, range.start, range.end) }
      }
    }

  internal fun getInlineContents(): Map<String, InlineContent> =
    formatObjects.asSequence()
      .mapNotNull { (tag, format) ->
        tag.removePrefix("inline:")
          // If no prefix was found then we ignore it.
          .takeUnless { it === tag }
          ?.let {
            @Suppress("UNCHECKED_CAST")
            Pair(it, format as InlineContent)
          }
      }
      .toMap()

  public sealed class Format(private val simpleTag: String? = null) {

    internal open fun getStyle(
      richTextStyle: RichTextStringStyle,
      contentColor: Color
    ): SpanStyle? = null

    public object Italic : Format("italic") {
      internal val DefaultStyle = SpanStyle(fontStyle = FontStyle.Italic)
      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.italicStyle
    }

    public object Bold : Format(simpleTag = "foo") {
      internal val DefaultStyle = SpanStyle(fontWeight = FontWeight.Bold)
      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.boldStyle
    }

    public object Underline : Format("underline") {
      internal val DefaultStyle = SpanStyle(textDecoration = TextDecoration.Underline)
      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.underlineStyle
    }

    public object Strikethrough : Format("strikethrough") {
      internal val DefaultStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.strikethroughStyle
    }

    public object Subscript : Format("subscript") {
      internal val DefaultStyle = SpanStyle(
        baselineShift = BaselineShift(-0.2f),
        // TODO this should be relative to current font size
        fontSize = 10.sp
      )

      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.subscriptStyle
    }

    public object Superscript : Format("superscript") {
      internal val DefaultStyle = SpanStyle(
        baselineShift = BaselineShift.Superscript,
        fontSize = 10.sp
      )

      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.superscriptStyle
    }

    public object Code : Format("code") {
      internal val DefaultStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        background = DefaultCodeBlockBackgroundColor
      )

      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.codeStyle
    }

    public data class Link(val destination: String) : Format() {
      override fun getStyle(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ) = richTextStyle.linkStyle

      internal companion object {
        val DefaultStyle = SpanStyle(
          textDecoration = TextDecoration.Underline,
          color = Color.Blue
        )
      }
    }

    internal fun registerTag(tags: MutableMap<String, Any>): String {
      simpleTag?.let { return it }
      val uuid = randomUUID()
      tags[uuid] = this
      return "format:$uuid"
    }

    internal companion object {
      val FormatAnnotationScope = Format::class.qualifiedName!!

      // For some reason, if this isn't lazy, Bold will always be null. Is Compose messing up static
      // initialization order?
      private val simpleTags by lazy(NONE) {
        listOf(Bold, Italic, Underline, Strikethrough, Subscript, Superscript, Code)
      }

      fun findTag(
        tag: String,
        tags: Map<String, Any>
      ): Format? {
        val stripped = tag.removePrefix("format:")
        return if (stripped === tag) {
          // If the original string was returned, it means the string did not have the prefix.
          simpleTags.firstOrNull { it.simpleTag == tag }
        } else {
          tags[stripped] as? Format
        }
      }
    }
  }

  public class Builder(capacity: Int = 16) {
    private val builder = AnnotatedString.Builder(capacity)
    private val formatObjects = mutableMapOf<String, Any>()

    public fun addFormat(
      format: Format,
      start: Int,
      end: Int
    ) {
      val tag = format.registerTag(formatObjects)
      builder.addStringAnnotation(FormatAnnotationScope, tag, start, end)
    }

    public fun pushFormat(format: Format): Int {
      val tag = format.registerTag(formatObjects)
      return builder.pushStringAnnotation(FormatAnnotationScope, tag)
    }

    public fun pop(): Unit = builder.pop()

    public fun pop(index: Int): Unit = builder.pop(index)

    public fun append(text: String): Unit = builder.append(text)

    public fun append(text: RichTextString) {
      builder.append(text.taggedString)
      formatObjects.putAll(text.formatObjects)
    }

    public fun appendInlineContent(
      alternateText: String = REPLACEMENT_CHAR,
      content: InlineContent
    ) {
      val tag = randomUUID()
      formatObjects["inline:$tag"] = content
      builder.appendInlineContent(tag, alternateText)
    }

    /**
     * Provides access to the underlying builder, which can be used to add arbitrary formatting,
     * including mixed with formatting from this Builder.
     */
    public fun <T> withAnnotatedString(block: AnnotatedString.Builder.() -> T): T = builder.block()

    public fun toRichTextString(): RichTextString =
      RichTextString(
        builder.toAnnotatedString(),
        formatObjects.toMap()
      )
  }
}

public inline fun Builder.withFormat(
  format: Format,
  block: Builder.() -> Unit
) {
  val index = pushFormat(format)
  block()
  pop(index)
}
