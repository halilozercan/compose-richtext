package com.zachklipp.richtext.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.resolveDefaults

@Preview
@Composable private fun RichTextSamplePreview() {
  RichTextSample()
}

@Composable fun RichTextSample() {
  var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
  var isDarkModeEnabled by remember { mutableStateOf(false) }

  val colors = if (isDarkModeEnabled) darkColorScheme() else lightColorScheme()

  SampleTheme(colorScheme = colors) {
    Surface {
      Column {
        // Config
        Card(elevation = CardDefaults.elevatedCardElevation()) {
          Column {
            Row(
              Modifier
                .clickable(onClick = { isDarkModeEnabled = !isDarkModeEnabled })
                .padding(8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = isDarkModeEnabled,
                onCheckedChange = { isDarkModeEnabled = it },

                )
              Text("Dark Mode")
            }
            RichTextStyleConfig(
              richTextStyle = richTextStyle,
              onChanged = { richTextStyle = it }
            )
          }
        }

        SelectionContainer {
          Column(Modifier.verticalScroll(rememberScrollState())) {
            RichTextDemo(style = richTextStyle)
          }
        }
      }
    }
  }
}

@Composable
fun RichTextStyleConfig(
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
