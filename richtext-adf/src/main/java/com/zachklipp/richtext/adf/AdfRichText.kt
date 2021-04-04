package com.zachklipp.richtext.adf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.PlaceholderVerticalAlign.Bottom
import androidx.compose.ui.unit.dp
import com.zachklipp.richtext.adf.model.AdfBlockNode
import com.zachklipp.richtext.adf.model.AdfCodeMark
import com.zachklipp.richtext.adf.model.AdfEmMark
import com.zachklipp.richtext.adf.model.AdfHardBreak
import com.zachklipp.richtext.adf.model.AdfInlineNode
import com.zachklipp.richtext.adf.model.AdfLinkMark
import com.zachklipp.richtext.adf.model.AdfMention
import com.zachklipp.richtext.adf.model.AdfStrikeMark
import com.zachklipp.richtext.adf.model.AdfStrongMark
import com.zachklipp.richtext.adf.model.AdfSubSupMark
import com.zachklipp.richtext.adf.model.AdfSubSupMarkType
import com.zachklipp.richtext.adf.model.AdfText
import com.zachklipp.richtext.adf.model.AdfTextColorMark
import com.zachklipp.richtext.adf.model.AdfUnderlineMark
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.Text as InlineRichText

@Composable
internal fun RichTextScope.AdfRichText(adfNode: AdfBlockNode) {
  val localUriHandler = LocalUriHandler.current
  val richText = remember(adfNode, localUriHandler) {
    computeRichTextString(adfNode, onLinkClicked = { title, destination ->
      localUriHandler.openUri(destination)
    })
  }

  InlineRichText(text = richText)
}

private fun computeRichTextString(
  adfNode: AdfBlockNode,
  onLinkClicked: (title: String, destination: String) -> Unit
): RichTextString {
  val richTextStringBuilder = RichTextString.Builder()

  adfNode.filterChildrenIsInstance<AdfInlineNode>().forEach { adfInlineNode ->
    when (adfInlineNode) {
      is AdfText -> {
        val pushedFormats = adfInlineNode.marks.mapNotNull { adfMark ->
          when (adfMark) {
            AdfCodeMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Code)
            }
            AdfEmMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
            }
            is AdfLinkMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Link {
                onLinkClicked(adfMark.title.orEmpty(), adfMark.href)
              })
            }
            AdfStrikeMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Strikethrough)
            }
            AdfStrongMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Bold)
            }
            is AdfSubSupMark -> {
              richTextStringBuilder.pushFormat(
                when (adfMark.type) {
                  AdfSubSupMarkType.sub -> RichTextString.Format.Subscript
                  AdfSubSupMarkType.sup -> RichTextString.Format.Superscript
                }
              )
            }
            is AdfTextColorMark -> {
              null
            }
            AdfUnderlineMark -> {
              richTextStringBuilder.pushFormat(RichTextString.Format.Underline)
            }
          }
        }

        richTextStringBuilder.append(adfInlineNode.text)

        pushedFormats.reversed().forEach { richTextStringBuilder.pop(it) }
      }
      is AdfHardBreak -> {
        richTextStringBuilder.append(adfInlineNode.text)
      }
      is AdfMention -> {
        richTextStringBuilder.appendInlineContent(
          content = InlineContent(
            placeholderVerticalAlign = Bottom
          ) {

            Text(
              text = adfInlineNode.text ?: "",
              modifier = Modifier
                .background(Color(0x14091E42), RoundedCornerShape(50))
                .padding(horizontal = 6.dp)
            )
          }
        )
      }
    }
  }

  return richTextStringBuilder.toRichTextString()
}
