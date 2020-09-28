package com.zachklipp.richtext.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.ExperimentalSubcomposeLayoutApi
import androidx.compose.ui.platform.setContent

class MainActivity : AppCompatActivity() {

  @OptIn(ExperimentalSubcomposeLayoutApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleLauncher()
    }
  }
}
