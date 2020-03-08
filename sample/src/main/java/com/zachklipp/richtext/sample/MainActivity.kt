package com.zachklipp.richtext.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import androidx.ui.foundation.VerticalScroller
import androidx.ui.material.MaterialTheme
import com.zachklipp.richtext.ui.RichTextDemo

/**
 * TODO write documentation
 */
class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        VerticalScroller {
          RichTextDemo()
        }
      }
    }
  }
}
