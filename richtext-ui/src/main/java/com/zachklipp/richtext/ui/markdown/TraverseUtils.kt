package com.zachklipp.richtext.ui.markdown

import android.util.Log
import org.commonmark.node.Node
import java.util.*

internal inline fun <reified T: ASTNode> ASTNode.filterChildrenIsInstance(): LinkedList<T> {
    return filterChildren { it is T } as LinkedList<T>
}

/**
 * Markdown rendering is susceptible to have assumptions. Hence, some rendering rules
 * may force restrictions on children. So, valid children nodes should be selected
 * before traversing. This function returns a LinkedList of children which conforms to
 * [filter] function.
 *
 * @param filter: A lambda to select valid children.
 */
internal inline fun ASTNode.filterChildren(
    filter: (ASTNode) -> Boolean
): LinkedList<ASTNode> {
    val list = LinkedList<ASTNode>()
    var iterator = this.firstChild
    while (iterator != null) {
        if(filter(iterator)) {
            val immutableIterator = iterator
            list.add(immutableIterator)
        }
        iterator = iterator.next
    }
    return list
}

internal fun traversePreOrder(
    node: ASTNode?,
    depth: Int = 0
) {
    if(node == null) return

    Log.d("Visit", "--".repeat(depth) + " ${node::class.simpleName}")
    var iterator: ASTNode? = node.firstChild
    while (iterator != null) {
        traversePreOrder(iterator, depth+1)
        iterator = iterator.next
    }
}

/**
 * These ASTNode types should never have any children. If any exists, ignore them.
 */
internal fun ASTNode.isRichTextTerminal(): Boolean {
    return this is ASTText
            || this is ASTCode
            || this is ASTImage
            || this is ASTSoftLineBreak
            || this is ASTHardLineBreak
}