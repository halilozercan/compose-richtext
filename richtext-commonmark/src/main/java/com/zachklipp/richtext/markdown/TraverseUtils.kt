package com.zachklipp.richtext.markdown

internal fun AstNode.childrenSequence(
  reverse: Boolean = false
): Sequence<AstNode> {
  return if (!reverse) {
    generateSequence(this.firstChild) { it.next }
  } else {
    generateSequence(this.lastChild) { it.previous }
  }
}

/**
 * Markdown rendering is susceptible to have assumptions. Hence, some rendering rules
 * may force restrictions on children. So, valid children nodes should be selected
 * before traversing. This function returns a LinkedList of children which conforms to
 * [filter] function.
 *
 * @param filter A lambda to select valid children.
 */
internal fun AstNode.filterChildren(
  reverse: Boolean = false,
  filter: (AstNode) -> Boolean
): Sequence<AstNode> {
  return childrenSequence(reverse).filter(filter)
}

internal inline fun <reified T : AstNode> AstNode.filterChildrenIsInstance(): Sequence<T> {
  @Suppress("UNCHECKED_CAST")
  return filterChildren { it is T } as Sequence<T>
}

/**
 * These ASTNode types should never have any children. If any exists, ignore them.
 */
internal fun AstNode.isRichTextTerminal(): Boolean {
  return this is AstText
      || this is AstCode
      || this is AstImage
      || this is AstSoftLineBreak
      || this is AstHardLineBreak
}