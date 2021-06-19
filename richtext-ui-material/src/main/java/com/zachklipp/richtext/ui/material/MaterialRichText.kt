package com.zachklipp.richtext.ui.material

import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import com.zachklipp.richtext.ui.ProvideRichTextLocals

@Composable
public fun ProvideMaterialThemingToRichText(
  content: @Composable () -> Unit
) {
  ProvideRichTextLocals(localTextStyle = LocalTextStyle, localContentColor = LocalContentColor) {
    content()
  }
}