package com.zachklipp.richtext.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ripple.ExperimentalRippleApi

class MainActivity : AppCompatActivity() {

  @OptIn(ExperimentalRippleApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleLauncher()
    }
  }
}
