package com.halilibo.richtext.ui.string

/**
 * Allows configuration of the Markdown renderer
 */
public data class RichTextRenderOptions(
  val animate: Boolean = false,
  val textFadeInMs: Int = 600,
  val debounceMs: Int = 100050,
  val delayMs: Int = 10,
  val onTextAnimate: () -> Unit = {},
) {
  public companion object {
    public val Default: RichTextRenderOptions = RichTextRenderOptions()
  }
}
