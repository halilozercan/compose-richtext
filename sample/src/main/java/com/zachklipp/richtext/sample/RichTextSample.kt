package com.zachklipp.richtext.sample

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.selection.SelectionContainer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.RichTextDemo
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.resolveDefaults

@Preview
@Composable private fun RichTextSamplePreview() {
  RichTextSample()
}

@Composable fun RichTextSample() {
  var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
  var isDarkModeEnabled by remember { mutableStateOf(false) }

  val colors = if (isDarkModeEnabled) darkColors() else lightColors()

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

        var selection: Selection? by remember { mutableStateOf(null) }
        SelectionContainer(selection = selection, onSelectionChange = { selection = it }) {
          ScrollableColumn {
            RichTextDemo(style = richTextStyle)
          }
        }
      }
    }
  }
}

@Composable
private fun RichTextStyleConfig(
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

  Text("Table border width padding: ${richTextStyle.tableStyle!!.borderStrokeWidth!!}")
  Slider(
    value = richTextStyle.tableStyle!!.borderStrokeWidth!!,
    valueRange = 0f..20f,
    onValueChange = {
      onChanged(
        richTextStyle.copy(
          tableStyle = richTextStyle.tableStyle!!.copy(
            borderStrokeWidth = it
          )
        )
      )
    }
  )
}
