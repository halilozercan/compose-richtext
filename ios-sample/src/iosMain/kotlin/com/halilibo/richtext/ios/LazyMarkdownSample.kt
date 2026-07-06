package com.halilibo.richtext.ios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.currentRichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.resolveDefaults

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LazyMarkdownSample() {
  var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
  var isWordWrapEnabled by remember { mutableStateOf(true) }
  var markdownParseOptions by remember { mutableStateOf(CommonMarkdownParseOptions.Default) }
  var isAutolinkEnabled by remember { mutableStateOf(true) }

  LaunchedEffect(isWordWrapEnabled) {
    richTextStyle = richTextStyle.copy(
      codeBlockStyle = richTextStyle.codeBlockStyle!!.copy(
        wordWrap = isWordWrapEnabled
      )
    )
  }
  LaunchedEffect(isAutolinkEnabled) {
    markdownParseOptions = markdownParseOptions.copy(
      autolink = isAutolinkEnabled
    )
  }

  Column {
    Column(modifier = Modifier.padding(8.dp)) {
      FlowRow {
        CheckboxPreference(
          onClick = { isWordWrapEnabled = !isWordWrapEnabled },
          checked = isWordWrapEnabled,
          label = "Word Wrap"
        )
        CheckboxPreference(
          onClick = { isAutolinkEnabled = !isAutolinkEnabled },
          checked = isAutolinkEnabled,
          label = "Autolink"
        )
      }

      RichTextStyleConfig(
        richTextStyle = richTextStyle,
        onChanged = { richTextStyle = it }
      )
    }

    SelectionContainer {
      val parser = remember(markdownParseOptions) {
        CommonmarkAstNodeParser(markdownParseOptions)
      }

      val astNode = remember(parser) {
        parser.parse(sampleMarkdown)
      }

      RichText(
        style = richTextStyle,
        modifier = Modifier.padding(8.dp),
      ) {
        LazyMarkdown(astNode)
      }
    }
  }
}

/**
 * A function that renders Markdown content lazily at the top level.
 */
@Composable
fun RichTextScope.LazyMarkdown(astNode: AstNode) {
  require(astNode.type == AstDocument) {
    "Lazy Markdown rendering requires root level node to have a type of AstDocument."
  }
  val currentStyle = currentRichTextStyle
  val resolvedStyle = remember(currentStyle) { currentStyle.resolveDefaults() }
  val blockSpacing = with(LocalDensity.current) {
    resolvedStyle.paragraphSpacing!!.toDp()
  }
  LazyColumn(verticalArrangement = Arrangement.spacedBy(blockSpacing)) {
    var iter = astNode.links.firstChild
    while (iter != null) {
      val node = iter
      item {
        BasicMarkdown(node)
      }
      iter = iter.links.next
    }
  }
}

@Composable
private fun CheckboxPreference(
  onClick: () -> Unit,
  checked: Boolean,
  label: String
) {
  Row(
    Modifier.clickable(onClick = onClick),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = { onClick() },
    )
    Text(label)
  }
}
