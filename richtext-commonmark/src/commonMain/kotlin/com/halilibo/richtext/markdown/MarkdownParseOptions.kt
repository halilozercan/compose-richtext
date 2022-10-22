package com.halilibo.richtext.markdown

/**
 * Allows configuration of the Markdown parser
 *
 * @param autolink Detect plain text links and turn them into Markdown links.
 */
public data class MarkdownParseOptions(
  val autolink: Boolean
) {
  public companion object {
    public val Default: MarkdownParseOptions = MarkdownParseOptions(
      autolink = true
    )
  }
}
