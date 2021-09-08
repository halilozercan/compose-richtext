package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable
import com.halilibo.richtext.ui.string.RichTextString

/**
 * Refer to https://spec.commonmark.org/0.30/#precedence
 *
 * Commonmark specification defines 3 different types of AST nodes;
 *
 * - Container Block
 * - Leaf Block
 * - Inline Content
 *
 * Container blocks are the most generic nodes. They define a structure for their children but
 * do not impose any major restrictions, meaning that container blocks can contain any
 * type of child node.
 *
 * Leaf blocks are self-explanatory, they should not have any children. All the necessary content
 * to render a leaf block should already exist in its payload
 *
 * Inline Content is analogous to [RichTextString] and its styles. Most of the inline content
 * nodes are about styling(bold, italic, strikethrough, code). The rest contains links, images,
 * html content, and of course raw text.
 */
internal sealed class AstNodeType

//region AstBlockNodeType

internal sealed class AstBlockNodeType: AstNodeType()

//region AstContainerBlockNodeType

internal sealed class AstContainerBlockNodeType: AstBlockNodeType()

@Immutable
internal object AstDocument : AstContainerBlockNodeType()

@Immutable
internal object AstBlockQuote : AstContainerBlockNodeType()

@Immutable
internal object AstListItem : AstContainerBlockNodeType()

@Immutable
internal data class AstBulletList(
  val bulletMarker: Char
) : AstContainerBlockNodeType()

@Immutable
internal data class AstOrderedList(
  val startNumber: Int,
  val delimiter: Char
) : AstContainerBlockNodeType()

//endregion

//region AstLeafBlockNodeType

internal sealed class AstLeafBlockNodeType: AstBlockNodeType()

@Immutable
internal object AstThematicBreak : AstLeafBlockNodeType()

@Immutable
internal data class AstHeading(
  val level: Int
) : AstLeafBlockNodeType()

@Immutable
internal data class AstIndentedCodeBlock(
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
internal data class AstFencedCodeBlock(
  val fenceChar: Char,
  val fenceLength: Int,
  val fenceIndent: Int,
  val info: String,
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
internal data class AstHtmlBlock(
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
internal data class AstLinkReferenceDefinition(
  val label: String,
  val destination: String,
  val title: String
) : AstLeafBlockNodeType()

@Immutable
internal object AstParagraph : AstLeafBlockNodeType()

//endregion

//endregion

//region AstInlineNodeType

internal sealed class AstInlineNodeType: AstNodeType()

@Immutable
internal data class AstCode(
  val literal: String
) : AstInlineNodeType()

@Immutable
internal data class AstEmphasis(
  private val delimiter: String
) : AstInlineNodeType()

@Immutable
internal data class AstStrongEmphasis(
  private val delimiter: String
) : AstInlineNodeType()

@Immutable
internal data class AstLink(
  val destination: String,
  val title: String
) : AstInlineNodeType()

@Immutable
internal data class AstImage(
  val title: String,
  val destination: String
) : AstInlineNodeType()

@Immutable
internal data class AstHtmlInline(
  val literal: String
) : AstInlineNodeType()

@Immutable
internal object AstHardLineBreak : AstInlineNodeType()

@Immutable
internal object AstSoftLineBreak : AstInlineNodeType()

@Immutable
internal data class AstText(
  val literal: String
) : AstInlineNodeType()

//endregion