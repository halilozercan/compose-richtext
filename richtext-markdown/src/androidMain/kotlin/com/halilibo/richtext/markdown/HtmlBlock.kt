package com.halilibo.richtext.markdown

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import com.aghajari.compose.text.BasicAnnotatedText
import com.aghajari.compose.text.fromHtml
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString

@Composable
internal actual fun RichTextScope.HtmlBlock(content: String) {
  val richTextString = remember(content) {
    richTextString {
      withAnnotatedString {
        append(content.fromHtml().annotatedString)
      }
    }
  }
  Text(richTextString)
}
