package com.zachklipp.richtext.ui.material

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import com.zachklipp.richtext.ui.BasicRichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle

@Composable
public fun RichText(
  modifier: Modifier = Modifier,
  richTextStyle: RichTextStyle? = null,
  textStyle: TextStyle? = null,
  color: Color = Color.Unspecified,
  alignment: Alignment.Horizontal = Alignment.Start,
  children: @Composable RichTextScope.() -> Unit
) {
  val fallbackContentColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
  val resolvedContentColor = color.takeOrElse {
    textStyle?.color?.takeOrElse { fallbackContentColor } ?: fallbackContentColor
  }

  val resolvedTextStyle = textStyle ?: LocalTextStyle.current

  BasicRichText(
    modifier = modifier,
    richTextStyle = richTextStyle,
    textStyle = resolvedTextStyle,
    contentColor = resolvedContentColor,
    alignment = alignment,
    children = children
  )
}
