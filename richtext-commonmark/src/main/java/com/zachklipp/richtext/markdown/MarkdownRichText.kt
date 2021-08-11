package com.zachklipp.richtext.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.zachklipp.richtext.markdown.node.AstCode
import com.zachklipp.richtext.markdown.node.AstEmphasis
import com.zachklipp.richtext.markdown.node.AstHardLineBreak
import com.zachklipp.richtext.markdown.node.AstImage
import com.zachklipp.richtext.markdown.node.AstLink
import com.zachklipp.richtext.markdown.node.AstLinkReferenceDefinition
import com.zachklipp.richtext.markdown.node.AstNode
import com.zachklipp.richtext.markdown.node.AstBlockQuote
import com.zachklipp.richtext.markdown.node.AstFencedCodeBlock
import com.zachklipp.richtext.markdown.node.AstHeading
import com.zachklipp.richtext.markdown.node.AstParagraph
import com.zachklipp.richtext.markdown.node.AstIndentedCodeBlock
import com.zachklipp.richtext.markdown.node.AstSoftLineBreak
import com.zachklipp.richtext.markdown.node.AstStrikethrough
import com.zachklipp.richtext.markdown.node.AstStrongEmphasis
import com.zachklipp.richtext.markdown.node.AstText
import com.zachklipp.richtext.markdown.node.AstListItem
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.withFormat
import java.util.*
import com.zachklipp.richtext.ui.string.Text as InlineRichText

/**
 * Only render the text content that exists below [astNode]. All the content blocks
 * like [AstBlockQuote] or [AstFencedCodeBlock] are ignored. This composable is
 * suited for [AstHeading] and [AstParagraph] since they are strictly text blocks.
 *
 * Some notes about commonmark and in general Markdown parsing.
 *
 * - Paragraph and Heading are the only RichTextString containers in base implementation.
 *   - RichTextString is build by traversing the children of Heading or Paragraph.
 *   - RichTextString can include;
 *     - Emphasis
 *     - StrongEmphasis
 *     - Image
 *     - Link
 *     - Code
 * - Code blocks should not have any children. Their whole content must reside in
 * [AstIndentedCodeBlock.literal] or [AstFencedCodeBlock.literal].
 * - Blocks like [BlockQuote], [FormattedList], [AstListItem] must have an [AstParagraph]
 * as a child to include any further RichText.
 * - CustomNode and CustomBlock can have their own scope, no idea about that.
 *
 * @param astNode Root node to accept as Text Content container.
 */
@Composable
internal fun RichTextScope.MarkdownRichText(astNode: AstNode) {
  val onLinkClicked = LocalOnLinkClicked.current
  // Assume that only RichText nodes reside below this level.
  val richText = remember(astNode, onLinkClicked) {
    computeRichTextString(astNode, onLinkClicked)
  }

  InlineRichText(text = richText)
}

@OptIn(ExperimentalCoilApi::class)
private fun computeRichTextString(
  astNode: AstNode,
  onLinkClicked: (String) -> Unit
): RichTextString {
  val richTextStringBuilder = RichTextString.Builder()

  // Modified pre-order traversal with pushFormat, popFormat support.
  val iteratorStack = LinkedList<AstNodeTraversalEntry>().apply {
    addFirst(
      AstNodeTraversalEntry(
        astNode = astNode,
        isVisited = false,
        formatIndex = null
      )
    )
  }

  while (iteratorStack.isNotEmpty()) {
    val (currentNode, isVisited, formatIndex) = iteratorStack.removeFirst()

    if (!isVisited) {
      val newFormatIndex = when (val currentNodeType = currentNode.type) {
        is AstCode -> {
          richTextStringBuilder.withFormat(RichTextString.Format.Code) {
            append(currentNodeType.literal)
          }
          null
        }
        is AstEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
        is AstStrikethrough -> richTextStringBuilder.pushFormat(
          RichTextString.Format.Strikethrough
        )
        is AstImage -> {
          richTextStringBuilder.appendInlineContent(
            content = inlineImageContent(
              currentNodeType.title, currentNodeType.destination
            )
          )
          null
        }
        is AstLink -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(
          onClick = { onLinkClicked(currentNodeType.destination) }
        ))
        is AstSoftLineBreak -> {
          richTextStringBuilder.append(" ")
          null
        }
        is AstHardLineBreak -> {
          richTextStringBuilder.append("\n")
          null
        }
        is AstStrongEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Bold)
        is AstText -> {
          richTextStringBuilder.append(currentNodeType.literal)
          null
        }
        is AstLinkReferenceDefinition -> richTextStringBuilder.pushFormat(
          RichTextString.Format.Link(
            onClick = { onLinkClicked(currentNodeType.destination) }
          ))
        else -> null
      }

      iteratorStack.addFirst(
        AstNodeTraversalEntry(
          astNode = currentNode,
          isVisited = true,
          formatIndex = newFormatIndex
        )
      )

      // Do not visit children of terminals such as Text, Image, etc.
      if (!currentNode.isRichTextTerminal()) {
        currentNode.childrenSequence(reverse = true).forEach {
          iteratorStack.addFirst(
            AstNodeTraversalEntry(
              astNode = it,
              isVisited = false,
              formatIndex = null
            )
          )
        }
      }
    }

    if (formatIndex != null) {
      richTextStringBuilder.pop(formatIndex)
    }
  }

  return richTextStringBuilder.toRichTextString()
}

//TODO(halilozercan): This should be provided from consumer side.
@OptIn(ExperimentalCoilApi::class)
private fun inlineImageContent(
  title: String,
  destination: String
): InlineContent {
  return InlineContent(
    initialSize = {
      IntSize(128.dp.roundToPx(), 128.dp.roundToPx())
    }
  ) {
    val painter = rememberImagePainter(
      data = destination
    )

    BoxWithConstraints {
      val sizeModifier by remember {
        derivedStateOf {
          val painterIntrinsicSize = painter.state.painter?.intrinsicSize
          if (painterIntrinsicSize != null &&
            painterIntrinsicSize.isSpecified &&
            painterIntrinsicSize.width != Float.POSITIVE_INFINITY &&
            painterIntrinsicSize.height != Float.POSITIVE_INFINITY
          ) {
            val width = painterIntrinsicSize.width
            val height = painterIntrinsicSize.height
            val scale = if (width > constraints.maxWidth) {
              constraints.maxWidth.toFloat() / width
            } else {
              1f
            }

            Modifier.size(
              (width * scale).toDp(),
              (height * scale).toDp()
            )
          } else {
            Modifier.size(128.dp)
          }
        }
      }

      Image(
        painter = painter,
        contentDescription = title,
        modifier = sizeModifier
      )
    }
  }
}

private data class AstNodeTraversalEntry(
  val astNode: AstNode,
  val isVisited: Boolean,
  val formatIndex: Int?
)
