package com.zachklipp.richtext.markdown

import androidx.compose.foundation.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.withFormat
import dev.chrisbanes.accompanist.coil.CoilImage
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
 * - Blocks like [BlockQuote], [FormattedList], [ListItem] must have a [Paragraph]
 * as a child to include any further RichText.
 * - CustomNode and CustomBlock can have their own scope, no idea about that.
 *
 * @param astNode Root node to accept as Text Content container.
 */
@Composable
internal fun RichTextScope.MarkdownRichText(astNode: AstNode) {
    val onLinkClicked = AmbientOnLinkClicked.current
    // Refer to notes at the top this file.
    // Assume that only RichText nodes reside below this level.
    val richText = remember(astNode, onLinkClicked) {
        computeRichTextString(astNode, onLinkClicked)
    }

    InlineRichText(text = richText)
}

private fun computeRichTextString(
    astNode: AstNode,
    onLinkClicked: (String) -> Unit
): RichTextString {
    val richTextStringBuilder = RichTextString.Builder()

    // Modified pre-order traversal with pushFormat, popFormat support.
    val iteratorStack = LinkedList<AstNodeTraversalEntry>().apply {
        addFirst(AstNodeTraversalEntry(
            astNode = astNode,
            isVisited = false,
            formatIndex = null
        ))
    }

    while(iteratorStack.isNotEmpty()) {
        val (currentNode, isVisited, formatIndex) = iteratorStack.removeFirst()

        if(!isVisited) {
            val newFormatIndex = when(currentNode) {
                is AstCode -> {
                    richTextStringBuilder.withFormat(RichTextString.Format.Code) {
                        append(currentNode.literal)
                    }
                    null
                }
                is AstEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
                is AstImage -> {
                    richTextStringBuilder.appendInlineContent(content = InlineContent {
                        CoilImage(
                            data = currentNode.destination,
                            loading = {
                                Text("Loading Image...")
                            },
                            error = {
                                Text("Image failed to load")
                            }
                        )
                    })
                    null
                }
                is AstLink -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(
                    onClick = { onLinkClicked(currentNode.destination) }
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
                    richTextStringBuilder.append(currentNode.literal)
                    null
                }
                is AstLinkReferenceDefinition -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(
                    onClick = { onLinkClicked(currentNode.destination) }
                ))
                else -> null
            }

            iteratorStack.addFirst(AstNodeTraversalEntry(
                astNode = currentNode,
                isVisited = true,
                formatIndex = newFormatIndex
            ))

            // Do not visit children of terminals such as Text, Image, etc.
            if(!currentNode.isRichTextTerminal()) {
                currentNode.childrenSequence(reverse = true).forEach {
                    iteratorStack.addFirst(AstNodeTraversalEntry(
                        astNode = it,
                        isVisited = false,
                        formatIndex = null
                    ))
                }
            }
        }

        if(formatIndex != null) {
            richTextStringBuilder.pop(formatIndex)
        }
    }

    return richTextStringBuilder.toRichTextString()
}

private data class AstNodeTraversalEntry(
    val astNode: AstNode,
    val isVisited: Boolean,
    val formatIndex: Int?
)

