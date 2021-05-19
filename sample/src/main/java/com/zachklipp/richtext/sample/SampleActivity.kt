package com.zachklipp.richtext.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ripple.ExperimentalRippleApi
import androidx.compose.material.ripple.LocalRippleNativeRendering
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : AppCompatActivity() {

  @OptIn(ExperimentalRippleApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      // There's a bug with the new RippleDrawable implementation introduced in beta07 that causes
      // a crash in some of the samples. See https://issuetracker.google.com/issues/188569367.
      CompositionLocalProvider(LocalRippleNativeRendering provides false) {
        SampleLauncher()
      }
    }
  }
}
