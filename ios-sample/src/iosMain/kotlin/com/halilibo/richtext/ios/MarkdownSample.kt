package com.halilibo.richtext.ios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstBlockNodeType
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.resolveDefaults

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarkdownSample() {
  var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
  var isWordWrapEnabled by remember { mutableStateOf(true) }
  var markdownParseOptions by remember { mutableStateOf(CommonMarkdownParseOptions.Default) }
  var isAutolinkEnabled by remember { mutableStateOf(true) }
  var isRtl by remember { mutableStateOf(false) }

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

  CompositionLocalProvider(
    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
  ) {
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
          CheckboxPreference(
            onClick = { isRtl = !isRtl },
            checked = isRtl,
            label = "RTL Layout"
          )
        }

        RichTextStyleConfig(
          richTextStyle = richTextStyle,
          onChanged = { richTextStyle = it }
        )
      }

      SelectionContainer {
        Column(Modifier.verticalScroll(rememberScrollState())) {
          val parser = remember(markdownParseOptions) {
            CommonmarkAstNodeParser(markdownParseOptions)
          }

          val astNode = remember(parser) {
            parser.parse(sampleMarkdown)
          }

          ProvidePrintUriHandler {
            RichText(
              style = richTextStyle,
              modifier = Modifier.padding(8.dp),
            ) {
              BasicMarkdown(astNode, HeadingAstBlockNodeComposer)
            }
          }
        }
      }
    }
  }
}

val HeadingAstBlockNodeComposer = object : AstBlockNodeComposer {
  override fun predicate(astBlockNodeType: AstBlockNodeType): Boolean {
    return astBlockNodeType is AstHeading
  }

  @Composable
  override fun RichTextScope.Compose(
    astNode: AstNode,
    visitChildren: @Composable (AstNode) -> Unit
  ) {
    val headingNode = astNode.type as? AstHeading ?: return
    Column {
      Heading(level = headingNode.level) {
        visitChildren(astNode)
      }
      Text("Custom rendering is used for this heading!", fontSize = 8.sp)
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

@Composable
private fun ProvidePrintUriHandler(content: @Composable () -> Unit) {
  content()
}
