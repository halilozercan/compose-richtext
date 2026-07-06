package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import kotlin.io.encoding.Base64

//TODO(halilozercan): This should be provided from consumer side.
/**
 * Image rendering is highly platform dependent. Coil is the desired
 * way to show images but it doesn't exist in desktop.
 */
@Composable
internal fun MarkdownImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale
) {
  val libraryImageLoader = rememberMarkdownImageLoader()
  val isBase64 = url.startsWith("data:image") || url.contains("base64,")

  val imageModel: Any = if (isBase64) {
    val base64String = url.substringAfter("base64,")
    Base64.decode(base64String)
  } else {
    url
  }

  AsyncImage(
    imageLoader = libraryImageLoader,
    model = imageModel,
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale
  )
}