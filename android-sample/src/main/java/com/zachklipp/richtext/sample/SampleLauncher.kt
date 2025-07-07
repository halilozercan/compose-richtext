package com.zachklipp.richtext.sample

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val Samples = listOf<Pair<String, @Composable () -> Unit>>(
  "RichText Demo" to @Composable { RichTextSample() },
  "Markdown Demo" to @Composable { MarkdownSample() },
  "Lazy Markdown Demo" to @Composable { LazyMarkdownSample() },
)

@Preview(showBackground = true)
@Composable private fun SampleLauncherPreview() {
  SamplesListScreen(isDarkTheme = true, onSampleClicked = {}, onThemeToggleClicked = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SampleLauncher() {
  val systemDarkTheme = isSystemInDarkTheme()
  var isDarkTheme by remember(systemDarkTheme) { mutableStateOf(systemDarkTheme) }
  var currentSampleIndex: Int? by remember { mutableStateOf(null) }

  SampleTheme(isDarkTheme) {
    Crossfade(currentSampleIndex) { index ->
      if (index != null) {
        BackHandler(onBack = { currentSampleIndex = null })
        Scaffold(
          topBar = {
            TopAppBar(title = { Text(Samples[index].first) }, actions = {
              val icon = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
              IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                Icon(icon, contentDescription = "Change color scheme")
              }
            })
          }
        ) {
          Surface(Modifier.padding(it)) {
            Samples[index].second()
          }
        }
      } else {
        SamplesListScreen(
          isDarkTheme,
          onSampleClicked = { currentSampleIndex = it },
          onThemeToggleClicked = { isDarkTheme = !isDarkTheme }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SamplesListScreen(
  isDarkTheme: Boolean,
  onSampleClicked: (Int) -> Unit,
  onThemeToggleClicked: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(title = { Text("Samples") }, actions = {
        val icon = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
        IconButton(onClick = onThemeToggleClicked) {
          Icon(icon, contentDescription = "Change color scheme")
        }
      })
    }
  ) { contentPadding ->
    LazyColumn(modifier = Modifier.padding(contentPadding)) {
      itemsIndexed(Samples) { index, (title, sampleContent) ->
        ListItem(
          headlineContent = { Text(title) },
          modifier = Modifier.clickable(onClick = { onSampleClicked(index) }),
          leadingContent = { SamplePreview(sampleContent) }
        )
      }
    }
  }
}

@Composable private fun SamplePreview(content: @Composable () -> Unit) {
  ScreenPreview(
    Modifier
      .size(50.dp)
      .aspectRatio(1f)
      .clipToBounds()
      // "Zoom in" to the top-start corner to make the preview more legible.
      .graphicsLayer(
        scaleX = 1.5f, scaleY = 1.5f,
        transformOrigin = TransformOrigin(0f, 0f)
      ),
  ) {
    SampleTheme {
      Surface(content = content)
    }
  }
}
