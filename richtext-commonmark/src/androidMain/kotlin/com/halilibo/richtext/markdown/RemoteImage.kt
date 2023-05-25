package com.halilibo.richtext.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

private val DEFAULT_IMAGE_SIZE = 64.dp

/**
 * Implementation of RemoteImage by using Coil library for Android.
 */
@Composable
internal actual fun RemoteImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
  isFillMaxWidth: Boolean,
  onClickImg: ((url: String) -> Unit)?
) {
  val painter = rememberAsyncImagePainter(
    ImageRequest.Builder(LocalContext.current)
      .data(data = url)
      .crossfade(true)
      .build()
  )

  val density = LocalDensity.current

  BoxWithConstraints {
    val sizeModifier by remember(density, painter, isFillMaxWidth) {
      derivedStateOf {
        val painterIntrinsicSize = painter.state.painter?.intrinsicSize
        if (painterIntrinsicSize != null &&
          painterIntrinsicSize.isSpecified &&
          painterIntrinsicSize.width != Float.POSITIVE_INFINITY &&
          painterIntrinsicSize.height != Float.POSITIVE_INFINITY
        ) {
          val width = painterIntrinsicSize.width
          val height = painterIntrinsicSize.height

          if (isFillMaxWidth) {
            with(density) {
              Modifier.size(
                constraints.maxWidth.toDp(),
                (constraints.maxWidth * height / width).toDp()
              )
            }
          }
          else {
            val scale = if (width > constraints.maxWidth) {
              constraints.maxWidth.toFloat() / width
            } else {
              1f
            }

            with(density) {
              Modifier.size(
                (width * scale).toDp(),
                (height * scale).toDp()
              )
            }
          }
        } else {
          // if size is not defined at all, Coil fails to render the image
          // here, we give a default size for images until they are loaded.
          Modifier.size(DEFAULT_IMAGE_SIZE)
        }
      }
    }

    val realModifier by remember(onClickImg, url) {
      derivedStateOf {
        if (onClickImg == null) sizeModifier else sizeModifier.clickable { onClickImg(url) }
      }
    }

    Image(
      painter = painter,
      contentDescription = contentDescription,
      modifier = realModifier,
      contentScale = contentScale
    )
  }
}
