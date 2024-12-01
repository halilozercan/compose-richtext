package com.halilibo.richtext.commonmark

import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension

/**
 * Allows configuration of the Markdown parser
 *
 * @param autolink Detect plain text links and turn them into Markdown links.
 */
public data class MarkdownParseOptions(val extensions: List<Extension>) {
  public companion object {
    public val MarkdownWithLinks: MarkdownParseOptions = MarkdownParseOptions(
      listOfNotNull(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        AutolinkExtension.create()
      )
    )

    public val MarkdownOnly: MarkdownParseOptions = MarkdownParseOptions(
      listOfNotNull(
        TablesExtension.create(),
        StrikethroughExtension.create()
      )
    )

    public val Default: MarkdownParseOptions = MarkdownWithLinks
  }
}