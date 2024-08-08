package com.halilibo.richtext.markdown

/**
 * Allows configuration of the Markdown renderer
 */
public data class MarkdownRenderOptions(
  val animate: Boolean = false,
  val textFadeInMs: Int = 600,
  val debounceMs: Int = 150,
) {
  public companion object {
    public val Default: MarkdownRenderOptions = MarkdownRenderOptions()
  }
}
