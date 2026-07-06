package com.halilibo.richtext.ios

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val Samples = listOf<Pair<String, @Composable () -> Unit>>(
  "RichText Demo" to @Composable { RichTextSample() },
  "Markdown Demo" to @Composable { MarkdownSample() },
  "Lazy Markdown Demo" to @Composable { LazyMarkdownSample() },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleLauncher() {
  var currentSampleIndex: Int? by remember { mutableStateOf(null) }

  Box(modifier = Modifier.fillMaxSize()) {
    Crossfade(currentSampleIndex) { index ->
      if (index != null) {
        Scaffold(
          topBar = {
            TopAppBar(
              title = { Text(Samples[index].first) },
              navigationIcon = {
                TextButton(onClick = { currentSampleIndex = null }) {
                  Text("< Back")
                }
              }
            )
          }
        ) { contentPadding ->
          Surface(modifier = Modifier.padding(contentPadding)) {
            Samples[index].second()
          }
        }
      } else {
        SamplesListScreen(onSampleClicked = { currentSampleIndex = it })
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SamplesListScreen(onSampleClicked: (Int) -> Unit) {
  Scaffold(
    topBar = {
      LargeTopAppBar(title = { Text("Samples") })
    }
  ) { contentPadding ->
    LazyColumn(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxWidth()
    ) {
      itemsIndexed(Samples) { index, (title, _) ->
        ListItem(
          headlineContent = { Text(title) },
          modifier = Modifier.clickable(onClick = { onSampleClicked(index) })
        )
      }
    }
  }
}
