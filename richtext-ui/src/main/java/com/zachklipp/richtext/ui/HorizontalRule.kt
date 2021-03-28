package com.zachklipp.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines how [HorizontalRule]s are rendered.
 *
 * @param color The [Color] of a horizontal rule.
 */
@Immutable
public data class HorizontalRuleStyle(
  val color: Color? = null
) {
  public companion object {
    public val Default: HorizontalRuleStyle = HorizontalRuleStyle()
  }
}

internal val DefaultHorizontalRuleColor: Color = Color.LightGray.copy(alpha = .5f)

internal fun HorizontalRuleStyle.resolveDefaults() = HorizontalRuleStyle(
  color = color ?: DefaultHorizontalRuleColor
)


/**
 * A simple horizontal line drawn with the current content color.
 */
@Composable public fun RichTextScope.HorizontalRule() {
  var color: Color
  var spacing: Dp

  with(currentRichTextStyle.resolveDefaults()) {
    color = horizontalRuleStyle!!.resolveDefaults().color!!
    spacing = with(LocalDensity.current) {
      paragraphSpacing!!.toDp()
    }
  }

  Box(
    Modifier
      .padding(top = spacing, bottom = spacing)
      .fillMaxWidth()
      .height(1.dp)
      .background(color)
  )
}
