package com.zachklipp.richtext.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.material.Card
import androidx.ui.material.Checkbox
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Slider
import androidx.ui.material.Surface
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.zachklipp.richtext.ui.RichTextDemo
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.resolveDefaults

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      App()
    }
  }
}

@Composable private fun App() {
  var richTextStyle by state { RichTextStyle().resolveDefaults() }
  var isDarkModeEnabled by state { false }

  val colors = if (isDarkModeEnabled) darkColorPalette() else lightColorPalette()

  MaterialTheme(colors = colors) {
    Surface {
      Column {
        // Config
        Card(elevation = 4.dp) {
          Column {
            Row {
              Checkbox(
                  checked = isDarkModeEnabled,
                  onCheckedChange = { isDarkModeEnabled = it }
              )
              Text("Dark Mode")
            }
            RichTextStyleConfig(
                richTextStyle = richTextStyle,
                onChanged = { richTextStyle = it }
            )
          }
        }

        VerticalScroller {
          RichTextDemo(style = richTextStyle)
        }
      }
    }
  }
}

@Preview @Composable private fun AppPreview() {
  App()
}

@Composable private fun RichTextStyleConfig(
  richTextStyle: RichTextStyle,
  onChanged: (RichTextStyle) -> Unit
) {
  Text("Paragraph spacing: ${richTextStyle.paragraphSpacing}")
  Slider(
      value = richTextStyle.paragraphSpacing!!.value,
      valueRange = 0f..20f,
      onValueChange = {
        onChanged(richTextStyle.copy(paragraphSpacing = it.sp))
      }
  )

  Text("Table cell padding: ${richTextStyle.tableStyle!!.cellPadding}")
  Slider(
      value = richTextStyle.tableStyle!!.cellPadding!!.value,
      valueRange = 0f..20f,
      onValueChange = {
        onChanged(
            richTextStyle.copy(
                tableStyle = richTextStyle.tableStyle!!.copy(
                    cellPadding = it.sp
                )
            )
        )
      }
  )
}
