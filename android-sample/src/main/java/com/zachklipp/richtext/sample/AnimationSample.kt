package com.zachklipp.richtext.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.RichText
import com.halilibo.richtext.ui.string.RichTextRenderOptions
import kotlinx.coroutines.delay
import kotlin.random.Random

@Preview
@Composable private fun AnimatedRichTextSamplePreview() {
  AnimatedRichTextSample()
}

@Composable fun AnimatedRichTextSample() {
  var isChunked by remember { mutableStateOf(false) }

  MaterialTheme(colors = colors) {
    Surface {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            "Chunking",
            modifier = Modifier
              .weight(1f)
              .padding(16.dp),
          )
          Checkbox(isChunked, onCheckedChange = { isChunked = it })
        }
        Box(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
          if (!isChunked) {
            CompleteTextSample()
          } else {
            ChunkingTextSample()
          }
        }
      }
    }
  }
}

@Composable
private fun CompleteTextSample() {
  val markdownOptions = remember {
    RichTextRenderOptions(
      animate = true,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      SampleText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

@Composable
private fun ChunkingTextSample() {
  var currentText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    var remaining = SampleText
    while (remaining.isNotEmpty()) {
      delay(200L + Random.nextInt(500))
      val chunkLength = 10 + Random.nextInt(100)
      currentText += remaining.take(chunkLength)
      remaining = remaining.drop(chunkLength)
    }
    isComplete = true
  }

  val markdownOptions = remember(isComplete) {
    RichTextRenderOptions(
      animate = !isComplete,
      textFadeInMs = 500,
      delayMs = 70,
      debounceMs = 200,
    )
  }

  RichText {
    Markdown(
      currentText,
      richtextRenderOptions = markdownOptions,
    )
  }
}

private const val SampleText = """
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.
1-The quick brown fox jumps over the lazy dog.

* Formatted list 1
* Formatted list 2
  * Sub bullet point

# Header 1
2-The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.

| Column A | Column B |
|----------|----------|
| The quick brown fox jumps over the lazy dog. | The quick brown fox jumps over the lazy dog. |

##### Header 5
4-The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.
The quick brown fox jumps over the lazy dog.
The quick brown fox **jumps over the lazy dog.**
"""
