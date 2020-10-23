package com.zachklipp.richtext.ui.markdown

import androidx.compose.foundation.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.withFormat
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.*
import com.zachklipp.richtext.ui.string.Text as InlineRichText

/**
 * Only render the text content that exists below [astNode]. All the content blocks
 * like [ASTBlockQuote] or [ASTFencedCodeBlock] are ignored. This composable is
 * suited for [ASTHeading] and [ASTParagraph] since they are strictly text blocks.
 *
 * @param astNode: Root node to accept as Text Content container.
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
 * [ASTIndentedCodeBlock.literal] or [ASTFencedCodeBlock.literal].
 * - Blocks like [BlockQuote], [FormattedList], [ListItem] must have a [Paragraph]
 * as a child to include any further RichText.
 * - CustomNode and CustomBlock can have their own scope, no idea about that.
 */
@Composable
internal fun MarkdownTextScope.MarkdownRichText(astNode: ASTNode) {
    // Refer to notes at the top this file.
    // Assume that only RichText nodes reside below this level.
    val richText = remember(astNode) {
        val richTextStringBuilder = RichTextString.Builder()

        // Modified pre-order traversal with pushFormat, popFormat support.
        val iteratorStack = LinkedList<Pair<ASTNode, Pair<Boolean, Int?>>>().apply {
            addFirst(astNode to (false to null))
        }

        while(iteratorStack.isNotEmpty()) {
            val (currentNode, isVisitedFormatIndex) = iteratorStack.removeFirst()
            val (isVisited, formatIndex) = isVisitedFormatIndex

            if(!isVisited) {
                val newFormatIndex = when(currentNode) {
                    is ASTCode -> {
                        richTextStringBuilder.withFormat(RichTextString.Format.Code) {
                            append(currentNode.literal)
                        }
                        null
                    }
                    is ASTEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
                    is ASTImage -> {
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
                    is ASTLink -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(onClick = {
                        onLinkClick.invoke(currentNode.destination)
                    }))
                    is ASTSoftLineBreak -> {
                        richTextStringBuilder.append(" ")
                        null
                    }
                    is ASTHardLineBreak -> {
                        richTextStringBuilder.append("\n")
                        null
                    }
                    is ASTStrongEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Bold)
                    is ASTText -> {
                        richTextStringBuilder.append(currentNode.literal)
                        null
                    }
                    is ASTLinkReferenceDefinition -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(onClick = {
                        onLinkClick.invoke(currentNode.destination)
                    }))
                    else -> null
                }

                iteratorStack.addFirst(currentNode to (true to newFormatIndex))

                // Do not visit children of terminals such as Text, Image, etc.
                if(!currentNode.isRichTextTerminal()) {
                    var iterator = currentNode.lastChild
                    while (iterator != null) {
                        iteratorStack.addFirst(iterator to (false to null))
                        iterator = iterator.previous
                    }
                }
            }

            if(formatIndex != null) {
                richTextStringBuilder.pop(formatIndex)
            }
        }

        richTextStringBuilder.toRichTextString()
    }

    InlineRichText(text = richText)
}

