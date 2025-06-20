package com.halilibo.richtext.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

private val DEFAULT_IMAGE_SIZE = 64.dp
private val MIN_IMAGE_SIZE = 16.dp

/**
 * Implementation of RemoteImage by using Coil library for Android.
 */
@Composable
internal actual fun RemoteImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale
) {
  val painter = rememberAsyncImagePainter(
    ImageRequest.Builder(LocalContext.current)
      .data(data = url)
      .size(Size.ORIGINAL)
      .crossfade(true)
      .build()
  )

  val density = LocalDensity.current
  var isMinimumSize = false
  BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
    val sizeModifier by remember(density, painter) {
      derivedStateOf {
        val painterIntrinsicSize = painter.state.painter?.intrinsicSize
        if (painterIntrinsicSize != null &&
          painterIntrinsicSize.isSpecified &&
          painterIntrinsicSize.width != Float.POSITIVE_INFINITY &&
          painterIntrinsicSize.height != Float.POSITIVE_INFINITY
        ) {
          val width = painterIntrinsicSize.width
          val height = painterIntrinsicSize.height
          val scale = if (width > this.constraints.maxWidth) {
            this.constraints.maxWidth.toFloat() / width
          } else {
            1f
          }

          with(density) {
            val calculatedWidth = (width * scale).toDp()
            val calculatedHeight = (height * scale).toDp()
            isMinimumSize = calculatedWidth < MIN_IMAGE_SIZE || calculatedHeight < MIN_IMAGE_SIZE
            Modifier.size(
              maxOf(calculatedWidth, MIN_IMAGE_SIZE),
              maxOf(calculatedHeight, MIN_IMAGE_SIZE)
            )
          }
        } else {
          // if size is not defined at all, Coil fails to render the image
          // here, we give a default size for images until they are loaded.
          // Ensure the width is at least MIN_IMAGE_WIDTH
          Modifier.size(maxOf(DEFAULT_IMAGE_SIZE, MIN_IMAGE_SIZE))
        }
      }
    }

    Image(
      painter = painter,
      contentDescription = contentDescription,
      modifier = sizeModifier,
      contentScale = if (isMinimumSize) ContentScale.FillWidth else contentScale
    )
  }
}
