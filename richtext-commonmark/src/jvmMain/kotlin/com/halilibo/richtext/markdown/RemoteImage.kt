package com.halilibo.richtext.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image.Companion.makeFromEncoded
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

@Composable
internal actual fun RemoteImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
  onClickImg: ((url: String) -> Unit)?
) {
  val image by produceState<ImageBitmap?>(null, url) {
    loadFullImage(url)?.let {
      value = makeFromEncoded(toByteArray(it)).toComposeImageBitmap()
    }
  }

  if (image != null) {
    val realModifier by remember(onClickImg, url) {
      derivedStateOf {
        if (onClickImg == null) modifier else modifier.clickable { onClickImg(url) }
      }
    }

    Image(
      bitmap = image!!,
      contentDescription = contentDescription,
      modifier = realModifier,
      contentScale = contentScale
    )
  }
}

private fun toByteArray(bitmap: BufferedImage): ByteArray {
  val baos = ByteArrayOutputStream()
  ImageIO.write(bitmap, "png", baos)
  return baos.toByteArray()
}

private suspend fun loadFullImage(source: String): BufferedImage? = withContext(Dispatchers.IO) {
  runCatching {
    val url = URL(source)
    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 5000
    connection.connect()

    val input: InputStream = connection.inputStream
    val bitmap: BufferedImage? = ImageIO.read(input)
    bitmap
  }.getOrNull()
}
