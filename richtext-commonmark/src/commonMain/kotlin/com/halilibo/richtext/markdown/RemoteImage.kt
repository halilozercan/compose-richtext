package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

//TODO(halilozercan): This should be provided from consumer side.
/**
 * Image rendering is highly platform dependent. Coil is the desired
 * way to show images but it doesn't exist in desktop.
 */
@Composable
internal expect fun RemoteImage(
  url: String,
  contentDescription: String?,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale
)
