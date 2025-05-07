package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable
import com.halilibo.richtext.ui.string.RichTextString
import org.commonmark.node.Node

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
public sealed class AstNodeType

//region AstBlockNodeType

public sealed class AstBlockNodeType: AstNodeType()

//region AstContainerBlockNodeType

/**
 * Defines a subtype of Block Node that can contain other nodes.
 */
public sealed class AstContainerBlockNodeType: AstBlockNodeType()

/**
 * Usually defines the root of a markdown document.
 */
@Immutable
public object AstDocument : AstContainerBlockNodeType()

/**
 * A block quote container that will indent its contents relative to its own indentation.
 */
@Immutable
public object AstBlockQuote : AstContainerBlockNodeType()

/**
 * Ordered or Unordered list item.
 */
@Immutable
public object AstListItem : AstContainerBlockNodeType()

/**
 * A list type that marks its items with bullets to signify a lack of order.
 */
@Immutable
public data class AstUnorderedList(
  val bulletMarker: Char
) : AstContainerBlockNodeType()

/**
 * A list type that uses numbers to mark its items.
 */
@Immutable
public data class AstOrderedList(
  val startNumber: Int,
  val delimiter: Char
) : AstContainerBlockNodeType()

//endregion

//region AstLeafBlockNodeType

/**
 * Defines a subtype of Block Node that can only contain plain text and full-length annotations.
 */
public sealed class AstLeafBlockNodeType: AstBlockNodeType()

@Immutable
public object AstThematicBreak : AstLeafBlockNodeType()

@Immutable
public data class AstHeading(
  val level: Int
) : AstLeafBlockNodeType()

@Immutable
public data class AstIndentedCodeBlock(
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
public data class AstFencedCodeBlock(
  val fenceChar: Char,
  val fenceLength: Int,
  val fenceIndent: Int,
  val info: String,
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
public data class AstHtmlBlock(
  val literal: String
) : AstLeafBlockNodeType()

@Immutable
public data class AstLinkReferenceDefinition(
  val label: String,
  val destination: String,
  val title: String
) : AstLeafBlockNodeType()

@Immutable
public object AstParagraph : AstLeafBlockNodeType()

//endregion

//endregion

//region AstInlineNodeType

/**
 * Defines a node type that can only apply to inline content.
 */
public sealed class AstInlineNodeType: AstNodeType()

@Immutable
public data class AstCode(
  val literal: String
) : AstInlineNodeType()

@Immutable
public data class AstEmphasis(
  private val delimiter: String
) : AstInlineNodeType()

@Immutable
public data class AstStrongEmphasis(
  private val delimiter: String
) : AstInlineNodeType()

@Immutable
public data class AstStrikethrough(
  val delimiter: String
) : AstInlineNodeType()

@Immutable
public data class AstLink(
  val destination: String,
  val title: String
) : AstInlineNodeType()

@Immutable
public data class AstImage(
  val title: String,
  val destination: String
) : AstInlineNodeType()

@Immutable
public data class AstHtmlInline(
  val literal: String
) : AstInlineNodeType()

@Immutable
public object AstHardLineBreak : AstInlineNodeType()

@Immutable
public object AstSoftLineBreak : AstInlineNodeType()

@Immutable
public data class AstText(
  val literal: String
) : AstInlineNodeType()

@Immutable
public data class AstCustomNode(
  val node: Node
) : AstInlineNodeType()

@Immutable
public data class AstCustomBlock(
  val node: Node
) : AstInlineNodeType()

//endregion
