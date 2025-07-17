package com.halilibo.richtext.markdown.local

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public interface ImageLinkHandler {
  public fun openImage(uri: String)
}

public val LocalImageLinkHandler: ProvidableCompositionLocal<ImageLinkHandler> =
  compositionLocalOf {
    object : ImageLinkHandler {
      override fun openImage(uri: String) {
        // DO NOTHING
      }
    }
  }