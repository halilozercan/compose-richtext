package com.halilibo.richtext.markdown

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
public actual fun HtmlBlock(content: String) {
  AndroidView(
    factory = { context ->
      // TODO: pass current styling to legacy TextView
      TextView(context)
    },
    update = {
      it.text = if (VERSION.SDK_INT >= VERSION_CODES.N) {
        Html.fromHtml(content, 0)
      } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(content)
      }
    }
  )
}
