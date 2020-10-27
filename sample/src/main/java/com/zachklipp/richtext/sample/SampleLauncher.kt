package com.zachklipp.richtext.sample

import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.dispatch.withFrameMillis
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.TransformOrigin
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.drawLayer
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.ui.tooling.preview.Preview
import kotlin.math.sign

@Composable private fun MarqueeSample() {
  Marquee { Text("Hi!") }
}

/**
 * @param dpPerSec The speed of the scroll. Positive goes start-to-end, negative goes end-to-start.
 */
@Composable private fun Marquee(
  modifier: Modifier = Modifier,
  dpPerSec: Dp = 10.dp,
  content: @Composable () -> Unit
) {
  // TODO use rememberUpdatedState when released.
  var updatedDpPerSec by remember { mutableStateOf(dpPerSec) }
  updatedDpPerSec = dpPerSec

  val offsetDp = produceState(initialValue = 0.dp) {
    var lastMillis = 0L
    while (true) {
      withFrameMillis { millis ->
        if (lastMillis == 0L) lastMillis = millis
        val dpPerMs = updatedDpPerSec / 1000f
        // Overflow should be fine here, we're taking the mod anyway.
        value += (dpPerMs.value * (millis - lastMillis)).dp
        lastMillis = millis
      }
    }
  }

  Layout(
      modifier = modifier.clipToBounds(),
      children = {
        // Wrap in boxes so we only have to deal with 2 layout nodes.
        Box { content() }
        Box { content() }
      }) { measurables, constraints ->
    val (main, overflow) = measurables.map { it.measure(constraints) }
    layout(main.width, main.height) {
      val offset = offsetDp.value.toIntPx() % main.width
      main.placeRelative(offset, 0)
      // Overflow should be on the start if speed is positive, or the end if negative.
      val overflowSign = -updatedDpPerSec.value.sign.toInt()
      overflow.placeRelative(overflowSign * main.width + offset, 0)
    }
  }
}

private val Samples = listOf<Pair<String, @Composable () -> Unit>>(
    "RichText Demo" to @Composable { RichTextSample() },
    "Pagination" to @Composable { PagedSample() },
    "Printable Document" to @Composable { DocumentSample() },
    "Slideshow" to @Composable { SlideshowSample() },
    "Marquee" to { MarqueeSample() },
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
