package com.zachklipp.richtext.markdown

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

public interface HtmlBlock {
  @Composable public fun onDraw(html: String)
}

@Immutable
public data class AndroidHtmlBlock(
  val modifier: Modifier = Modifier
) : HtmlBlock {
  @Composable override fun onDraw(html: String) {
    AndroidView(
      factory = { context ->
        // TODO: pass current styling to legacy TextView
        TextView(context).apply {
          text = if (VERSION.SDK_INT >= VERSION_CODES.N) {
            Html.fromHtml(html, 0)
          } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
          }
        }
      },
      modifier = modifier
    )
  }
}