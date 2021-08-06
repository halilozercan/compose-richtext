package com.zachklipp.richtext.ui.material

import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.zachklipp.richtext.ui.BasicRichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle

/**
 * RichText implementation that integrates with Material design.
 */
@Composable
public fun RichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  remember {
    RichTextScope(
      textStyle = { LocalTextStyle.current },
      contentColor = { LocalContentColor.current },
      ProvideTextStyle = { textStyle, content ->
        ProvideTextStyle(textStyle, content)
      },
      ProvideContentColor = { color, content ->
        CompositionLocalProvider(LocalContentColor provides color) {
          content()
        }
      }
    )
  }.BasicRichText(
    modifier = modifier,
    style = style,
    children = children
  )
}
