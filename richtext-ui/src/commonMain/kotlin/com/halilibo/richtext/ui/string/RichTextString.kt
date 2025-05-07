@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "SuspiciousCollectionReassignment")

package com.halilibo.richtext.ui.string

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkAnnotation.Url
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
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
public class RichTextStringStyle(
  public val boldStyle: SpanStyle? = null,
  public val italicStyle: SpanStyle? = null,
  public val underlineStyle: SpanStyle? = null,
  public val strikethroughStyle: SpanStyle? = null,
  public val subscriptStyle: SpanStyle? = null,
  public val superscriptStyle: SpanStyle? = null,
  public val codeStyle: SpanStyle? = null,
  public val linkStyle: TextLinkStyles? = null
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
      linkStyle = linkStyle?.merge(otherStyle.linkStyle) ?: otherStyle.linkStyle
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RichTextStringStyle) return false

    if (boldStyle != other.boldStyle) return false
    if (italicStyle != other.italicStyle) return false
    if (underlineStyle != other.underlineStyle) return false
    if (strikethroughStyle != other.strikethroughStyle) return false
    if (subscriptStyle != other.subscriptStyle) return false
    if (superscriptStyle != other.superscriptStyle) return false
    if (codeStyle != other.codeStyle) return false
    if (linkStyle != other.linkStyle) return false

    return true
  }

  override fun hashCode(): Int {
    var result = boldStyle?.hashCode() ?: 0
    result = 31 * result + (italicStyle?.hashCode() ?: 0)
    result = 31 * result + (underlineStyle?.hashCode() ?: 0)
    result = 31 * result + (strikethroughStyle?.hashCode() ?: 0)
    result = 31 * result + (subscriptStyle?.hashCode() ?: 0)
    result = 31 * result + (superscriptStyle?.hashCode() ?: 0)
    result = 31 * result + (codeStyle?.hashCode() ?: 0)
    result = 31 * result + (linkStyle?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "RichTextStringStyle(boldStyle=$boldStyle, " +
        "italicStyle=$italicStyle, " +
        "underlineStyle=$underlineStyle, " +
        "strikethroughStyle=$strikethroughStyle, " +
        "subscriptStyle=$subscriptStyle, " +
        "superscriptStyle=$superscriptStyle, " +
        "codeStyle=$codeStyle, " +
        "linkStyle=$linkStyle)"
  }

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
public class RichTextString internal constructor(
  private val taggedString: AnnotatedString,
  internal val formatObjects: Map<String, Any>
) {
  private val length: Int get() = taggedString.length
  public val text: String get() = taggedString.text

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
        format.getAnnotation(style, contentColor)
          ?.let { annotation ->
            if (annotation is SpanStyle) {
              addStyle(annotation, range.start, range.end)
            } else if (annotation is LinkAnnotation.Url) {
              addLink(annotation, range.start, range.end)
            }
          }
      }
    }

  internal fun getInlineContents(): Map<String, InlineContent> =
    formatObjects.asSequence()
      .mapNotNull { (tag, format) ->
        tag.removePrefix("inline:")
          // If no prefix was found then we ignore it.
          .takeUnless { it === tag }
          ?.let {
            Pair(it, format as InlineContent)
          }
      }
      .toMap()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RichTextString) return false

    if (taggedString != other.taggedString) return false
    if (formatObjects != other.formatObjects) return false

    return true
  }

  override fun hashCode(): Int {
    var result = taggedString.hashCode()
    result = 31 * result + formatObjects.hashCode()
    return result
  }

  public open class Format(private val simpleTag: String? = null) {

    /**
     * This function should either return [SpanStyle] or [LinkAnnotation.Url]. In future releases of
     * Compose these classes will have a common supertype called `AnnotatedString.Annotation`. Then
     * we can stop returning [Any].
     */
    public open fun getAnnotation(
      richTextStyle: RichTextStringStyle,
      contentColor: Color
    ): Any? = null

    public object Italic : Format("italic") {
      internal val DefaultStyle = SpanStyle(fontStyle = FontStyle.Italic)
      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.italicStyle
    }

    public object Bold : Format(simpleTag = "foo") {
      internal val DefaultStyle = SpanStyle(fontWeight = FontWeight.Bold)
      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.boldStyle
    }

    public object Underline : Format("underline") {
      internal val DefaultStyle = SpanStyle(textDecoration = TextDecoration.Underline)
      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.underlineStyle
    }

    public object Strikethrough : Format("strikethrough") {
      internal val DefaultStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.strikethroughStyle
    }

    public object Subscript : Format("subscript") {
      internal val DefaultStyle = SpanStyle(
        baselineShift = BaselineShift(-0.2f),
        // TODO this should be relative to current font size
        fontSize = 10.sp
      )

      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.subscriptStyle
    }

    public object Superscript : Format("superscript") {
      internal val DefaultStyle = SpanStyle(
        baselineShift = BaselineShift.Superscript,
        fontSize = 10.sp
      )

      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.superscriptStyle
    }

    public object Code : Format("code") {
      internal val DefaultStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        background = DefaultCodeBlockBackgroundColor
      )

      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): SpanStyle? = richTextStyle.codeStyle
    }

    public class Link(
      public val destination: String,
      public val linkInteractionListener: LinkInteractionListener? = null
    ) : Format() {
      public override fun getAnnotation(
        richTextStyle: RichTextStringStyle,
        contentColor: Color
      ): Url = LinkAnnotation.Url(
        url = destination,
        styles = richTextStyle.linkStyle,
        linkInteractionListener = linkInteractionListener
      )

      override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Link) return false

        if (destination != other.destination) return false
        if (linkInteractionListener != other.linkInteractionListener) return false

        return true
      }

      override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + linkInteractionListener.hashCode()
        return result
      }

      override fun toString(): String {
        return "Link(destination='$destination', linkInteractionListener=$linkInteractionListener)"
      }

      internal companion object {
        val DefaultStyle = TextLinkStyles(
          style = SpanStyle(color = Color.Blue),
          hoveredStyle = SpanStyle(
            textDecoration = TextDecoration.Underline,
            color = Color.Blue
          )
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

private fun TextLinkStyles.merge(other: TextLinkStyles?): TextLinkStyles {
  return if (other == null) {
    TextLinkStyles()
  } else {
    TextLinkStyles(
      style = this.style?.merge(other.style) ?: other.style,
      focusedStyle = this.style?.merge(other.focusedStyle) ?: other.focusedStyle,
      hoveredStyle = this.style?.merge(other.hoveredStyle) ?: other.hoveredStyle,
      pressedStyle = this.style?.merge(other.pressedStyle) ?: other.pressedStyle,
    )
  }
}
