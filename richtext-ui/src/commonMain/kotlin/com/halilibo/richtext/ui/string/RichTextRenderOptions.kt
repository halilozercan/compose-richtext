package com.halilibo.richtext.ui.string

/**
 * Allows configuration of the Markdown renderer
 */
public data class RichTextRenderOptions(
  val animate: Boolean = false,
  val textFadeInMs: Int = 500,
  val debounceMs: Int = 100050,
  val delayMs: Int = 70,
  val delayExponent: Double = 0.7,
  val maxPhraseLength: Int = 30,
  val phraseMarkersOverride: List<Char>? = null,
  val onTextAnimate: () -> Unit = {},
  val onPhraseAnimate: () -> Unit = {},
) {
  public companion object {
    public val Default: RichTextRenderOptions = RichTextRenderOptions()
  }
}
