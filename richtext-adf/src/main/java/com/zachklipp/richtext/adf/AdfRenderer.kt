package com.zachklipp.richtext.adf

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle.Italic
import androidx.compose.ui.unit.sp
import com.zachklipp.richtext.adf.model.AdfBlockQuote
import com.zachklipp.richtext.adf.model.AdfBulletList
import com.zachklipp.richtext.adf.model.AdfCodeBlock
import com.zachklipp.richtext.adf.model.AdfHeading
import com.zachklipp.richtext.adf.model.AdfNode
import com.zachklipp.richtext.adf.model.AdfOrderedList
import com.zachklipp.richtext.adf.model.AdfPanelBlock
import com.zachklipp.richtext.adf.model.AdfParagraph
import com.zachklipp.richtext.adf.model.AdfParser
import com.zachklipp.richtext.adf.model.AdfRule
import com.zachklipp.richtext.adf.model.AdfTable
import com.zachklipp.richtext.adf.model.AdfText
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.CodeBlockStyle
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.Heading
import com.zachklipp.richtext.ui.HorizontalRule
import com.zachklipp.richtext.ui.ListStyle
import com.zachklipp.richtext.ui.ListType
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.TableStyle
import com.zachklipp.richtext.ui.string.RichTextStringStyle
import com.zachklipp.richtext.ui.string.Text
import com.zachklipp.richtext.ui.string.richTextString

/**
 * A composable that renders ADF documents using [RichText].
 *
 * @param content Json representation of ADF document.
 */
@Composable
public fun AdfRenderer(
  content: String,
  modifier: Modifier = Modifier
) {
  SelectionContainer {
    ProvideTextStyle(
      value = TextStyle(
        color = Color(0xFF172B4D),
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 24.sp
      )
    ) {
      RichText(
        modifier = modifier,
        style = RichTextStyle(
          tableStyle = TableStyle(
            borderColor = Color(0xFFC1C7D0),
            headerBackgroundColor = Color(0xFFEBECF0)
          ),
          stringStyle = RichTextStringStyle(
            linkStyle = SpanStyle(

              color = Color(0xFF0052BC)
            )
          ),
          listStyle = ListStyle(),
          codeBlockStyle = CodeBlockStyle(
            background = Color(0xFFF4F5F7),
            textStyle = TextStyle(
              fontFamily = FontFamily.Monospace
            )
          ),
          headingStyle = { level, textStyle ->
            when (level) {
              0 -> TextStyle(
                fontSize = 36.sp,
              )
              1 -> TextStyle(
                fontSize = 26.sp,
              )
              2 -> TextStyle(
                fontSize = 22.sp,
                color = textStyle.color.copy(alpha = .7F)
              )
              3 -> TextStyle(
                fontSize = 20.sp,
                fontStyle = Italic
              )
              4 -> TextStyle(
                fontSize = 18.sp,
                color = textStyle.color.copy(alpha = .7F)
              )
              5 -> TextStyle(
                color = textStyle.color.copy(alpha = .5f)
              )
              else -> textStyle
            }
          }
        )
      ) {
        val adfTree = parsedAdfTree(text = content)
        RecursiveRenderAdf(adfNode = adfTree)
      }
    }
  }
}

@Composable
internal fun RichTextScope.RecursiveRenderAdf(adfNode: AdfNode?) {
  adfNode ?: return

  when (adfNode) {
    is AdfBlockQuote -> {
      BlockQuote {
        RenderChildren(adfNode)
      }
    }
    is AdfPanelBlock -> {
      PanelBlock(panelType = adfNode.panelType) {
        RenderChildren(adfNode)
      }
    }
    is AdfBulletList -> {
      FormattedList(
        listType = ListType.Unordered,
        items = adfNode.childrenSequence().toList()
      ) { astListItem ->
        RenderChildren(astListItem)
      }
    }
    is AdfCodeBlock -> {
      LineNumberedCodeBlock(adfNode)
    }
    is AdfHeading -> {
      Heading(level = adfNode.level) {
        AdfRichText(adfNode)
      }
    }
    is AdfRule -> {
      HorizontalRule()
    }
    is AdfOrderedList -> {
      FormattedList(
        listType = ListType.Ordered,
        items = adfNode.childrenSequence().toList()
      ) { astListItem ->
        RenderChildren(astListItem)
      }
    }
    is AdfParagraph -> {
      AdfRichText(adfNode)
    }
    is AdfText -> {
      Text(richTextString { append(adfNode.text) })
    }
    is AdfTable -> {
      AdfTableBlock(adfNode)
    }
    else -> RenderChildren(adfNode)
  }
}

@Composable
internal fun parsedAdfTree(text: String): AdfNode? {
  val rootASTNode by produceState<AdfNode?>(null, text) {
    value = AdfParser.parse(text)
  }

  return rootASTNode
}

@Composable
internal fun RichTextScope.RenderChildren(node: AdfNode) {
  node.childrenSequence().forEach {
    RecursiveRenderAdf(adfNode = it)
  }
}
