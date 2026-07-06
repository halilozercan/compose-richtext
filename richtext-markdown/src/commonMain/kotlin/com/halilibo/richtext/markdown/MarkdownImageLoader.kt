package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent

@Composable
internal fun rememberMarkdownImageLoader(): ImageLoader {
  val context = LocalPlatformContext.current

  return remember(context) {
    ImageLoader.Builder(context)
      .components {
        val httpClient = HttpClient {
          install(UserAgent) {
            agent = "richtext-markdown/1.0"
          }
        }
        add(KtorNetworkFetcherFactory(httpClient))
      }
      .crossfade(true)
      .build()
  }
}