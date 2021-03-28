package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

internal val LocalMarkdownConfiguration = compositionLocalOf { MarkdownConfiguration.Default }

@Immutable
public data class MarkdownConfiguration(
  val inlineImage: InlineImage? = null,
  val htmlBlock: HtmlBlock? = null
) {
  public companion object {
    public val Default: MarkdownConfiguration = MarkdownConfiguration()
  }
}

public fun MarkdownConfiguration.merge(other: MarkdownConfiguration?): MarkdownConfiguration = MarkdownConfiguration(
  inlineImage = other?.inlineImage ?: inlineImage,
  htmlBlock = other?.htmlBlock ?: htmlBlock
)

public fun MarkdownConfiguration.resolveDefaults(): MarkdownConfiguration = MarkdownConfiguration(
  inlineImage = inlineImage ?: CoilInlineImage(),
  htmlBlock = htmlBlock ?: AndroidHtmlBlock()
)