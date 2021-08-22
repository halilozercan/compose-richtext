package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.RichTextString
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString
import com.halilibo.richtext.ui.string.withFormat

@Preview
@Composable
private fun TextPreview() {
  RichTextScope.Text(richTextString {
    append("I'm ")
    withFormat(RichTextString.Format.Bold) {
      append("bold!")
    }
  })
}
