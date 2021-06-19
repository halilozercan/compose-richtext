package com.zachklipp.richtext.sample

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
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

private val Samples = listOf<Pair<String, @Composable (isThumbnail: Boolean) -> Unit>>(
  "RichText Demo" to @Composable { RichTextSample() },
  "Markdown Demo" to @Composable { MarkdownSample() },
  "Pagination" to @Composable { PagedSample() },
  "Printable Document" to @Composable { DocumentSample() },
  "Slideshow" to @Composable { isThumbnail ->
    SlideshowSample(configureFullScreenWindow = isThumbnail.not())
  },
)

@Preview(showBackground = true)
@Composable private fun SampleLauncherPreview() {
  SamplesListScreen(onSampleClicked = {})
}

@Composable fun SampleLauncher() {
  var currentSampleIndex: Int? by remember { mutableStateOf(null) }

  Crossfade(currentSampleIndex) { index ->
    index?.let {
      BackHandler(onBack = { currentSampleIndex = null })
      Samples[it].second(isThumbnail = false)
    }
      ?: SamplesListScreen(onSampleClicked = { currentSampleIndex = it })
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable private fun SamplesListScreen(onSampleClicked: (Int) -> Unit) {
  MaterialTheme(colors = darkColors()) {
    Scaffold(
      topBar = {
        TopAppBar(title = { Text("Samples") })
      }
    ) {
      LazyColumn {
        itemsIndexed(Samples) { index, (title, sampleContent) ->
          ListItem(
            Modifier.clickable(onClick = { onSampleClicked(index) }),
            icon = { SamplePreview { sampleContent(isThumbnail = true) } }
          ) {
            Text(title)
          }
        }
      }
    }
  }
}

@Composable private fun SamplePreview(content: @Composable () -> Unit) {
  ScreenPreview(
    Modifier
      .height(50.dp)
      .aspectRatio(1f)
      .clipToBounds()
      // "Zoom in" to the top-start corner to make the preview more legible.
      .graphicsLayer(
        scaleX = 1.5f, scaleY = 1.5f,
        transformOrigin = TransformOrigin(0f, 0f)
      ),
  ) {
    MaterialTheme(colors = lightColors()) {
      Surface(content = content)
    }
  }
}
