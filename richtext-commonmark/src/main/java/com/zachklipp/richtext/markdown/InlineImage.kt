package com.zachklipp.richtext.markdown

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.google.accompanist.coil.CoilImage

public interface InlineImage {
  @Composable public fun onDraw(text: String, destination: String)
}

@Immutable
public data class CoilInlineImage(
  val modifier: Modifier = Modifier,
  val loading: @Composable () -> Unit = { BasicText("Loading Image...") },
  val error: @Composable () -> Unit = { BasicText("Image failed to load") }
) : InlineImage {
  @Composable override fun onDraw(text: String, destination: String) {
    CoilImage(
      data = destination,
      contentDescription = text,
      modifier = modifier,
      loading = { loading() },
      error = { error() }
    )
  }
}