package com.zachklipp.richtext.sample

import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLifecycleObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.TransformOrigin
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.drawLayer
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.ui.tooling.preview.Preview

private val Samples = listOf<Pair<String, @Composable () -> Unit>>(
    "RichText Demo" to @Composable { RichTextSample() },
    "Markdown Demo" to @Composable { MarkdownSample() },
    "Pagination" to @Composable { PagedSample() },
    "Printable Document" to @Composable { DocumentSample() },
    "Slideshow" to @Composable { SlideshowSample() },
)

@Preview(showBackground = true)
@Composable private fun SampleLauncherPreview() {
  SamplesListScreen(onSampleClicked = {})
}

@Composable fun SampleLauncher() {
  var currentSampleIndex: Int? by remember { mutableStateOf(null) }

  Crossfade(currentSampleIndex) { index ->
    index?.let {
      BackPressedHandler(onBackPressed = { currentSampleIndex = null })
      Samples[it].second()
    }
        ?: SamplesListScreen(onSampleClicked = { currentSampleIndex = it })
  }
}

@OptIn(ExperimentalLayout::class)
@Composable private fun SamplesListScreen(onSampleClicked: (Int) -> Unit) {
  MaterialTheme(colors = darkColors()) {
    Scaffold(
        topBar = {
          TopAppBar(title = { Text("Samples") })
        }
    ) {
      LazyColumnForIndexed(Samples) { index, (title, sampleContent) ->
        ListItem(
            Modifier.clickable(onClick = { onSampleClicked(index) }),
            icon = { SamplePreview(sampleContent) }
        ) {
          Text(title)
        }
      }
    }
  }
}

@Composable private fun SamplePreview(content: @Composable () -> Unit) {
  ScreenPreview(
      Modifier.preferredHeight(50.dp)
          .aspectRatio(1f)
          .clipToBounds()
          // "Zoom in" to the top-start corner to make the preview more legible.
          .drawLayer(
              scaleX = 1.5f, scaleY = 1.5f,
              transformOrigin = TransformOrigin(0f, 0f)
          ),
  ) {
    MaterialTheme(colors = lightColors()) {
      Surface(content = content)
    }
  }
}

@Composable private fun BackPressedHandler(onBackPressed: () -> Unit) {
  val context = ContextAmbient.current
  val backPressedDispatcher = remember {
    generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<OnBackPressedDispatcherOwner>()
        .firstOrNull()
        ?.onBackPressedDispatcher
  } ?: return
  val compositionLifecycleOwner: LifecycleOwner = remember(onBackPressed) {
    object : LifecycleOwner, CompositionLifecycleObserver {
      val registry = LifecycleRegistry(this)
      override fun getLifecycle(): Lifecycle = registry

      override fun onEnter() {
        super.onEnter()
        registry.currentState = STARTED
      }

      override fun onLeave() {
        registry.currentState = DESTROYED
        super.onLeave()
      }
    }
  }

  onCommit(backPressedDispatcher, compositionLifecycleOwner, onBackPressed) {
    val callback = object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        onBackPressed()
      }
    }
    backPressedDispatcher.addCallback(compositionLifecycleOwner, callback)
  }
}
