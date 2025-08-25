package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.string.MarkdownAnimationState
import com.halilibo.richtext.ui.string.RichTextRenderOptions

@Immutable
public data class HorizontalRuleStyle(
  val color: Color? = null,
  val spacing: Dp? = null,
) {
  public companion object {
    public val Default: HorizontalRuleStyle = HorizontalRuleStyle()
  }
}

internal fun HorizontalRuleStyle.resolveDefaults() = HorizontalRuleStyle(
  color = color,
  spacing = spacing,
)

/**
 * A simple horizontal line drawn with the current content color.
 */
@Composable public fun RichTextScope.HorizontalRule(
  markdownAnimationState: MarkdownAnimationState = remember { MarkdownAnimationState() },
  richTextRenderOptions: RichTextRenderOptions = RichTextRenderOptions(),
) {
  val resolvedStyle = currentRichTextStyle.resolveDefaults()
  val horizontalRuleStyle = resolvedStyle.horizontalRuleStyle
  val color = horizontalRuleStyle?.color ?: currentContentColor.copy(alpha = .2f)
  val spacing = horizontalRuleStyle?.spacing ?: with(LocalDensity.current) {
    resolvedStyle.paragraphSpacing!!.toDp()
  }
  val alpha = rememberMarkdownFade(richTextRenderOptions, markdownAnimationState)
  Box(
    Modifier
      .graphicsLayer{ this.alpha = alpha.value }
      .padding(top = spacing, bottom = spacing)
      .fillMaxWidth()
      .height(1.dp)
      .background(color)
  )
}
