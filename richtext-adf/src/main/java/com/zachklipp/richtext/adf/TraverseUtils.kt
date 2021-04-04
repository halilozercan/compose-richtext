package com.zachklipp.richtext.adf

import com.zachklipp.richtext.adf.model.AdfBlockNode
import com.zachklipp.richtext.adf.model.AdfNode

internal fun AdfNode.childrenSequence(
  reverse: Boolean = false
): Sequence<AdfNode> {
  return if (this is AdfBlockNode) {
    return if (!reverse) {
      content.asSequence()
    } else {
      content.asReversed().asSequence()
    }
  } else {
    emptySequence()
  }
}

internal fun AdfNode.filterChildren(
  reverse: Boolean = false,
  filter: (AdfNode) -> Boolean
): Sequence<AdfNode> {
  return childrenSequence(reverse).filter(filter)
}

internal inline fun <reified T : AdfNode> AdfNode.filterChildrenIsInstance(): Sequence<T> {
  @Suppress("UNCHECKED_CAST")
  return filterChildren { it is T } as Sequence<T>
}
