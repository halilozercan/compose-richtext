package com.zachklipp.richtext.ui.markdown

import android.util.Log
import org.commonmark.node.Node

/*internal inline fun <reified T: Node> Node.filterChildren(): List<T> {
    val list = mutableListOf<T>()
    var iterator = this.firstChild
    while (iterator != null) {
        if(iterator is T) {
            val immutableIterator = iterator
            list.add(immutableIterator)
        }
        iterator = iterator.next
    }
    return list
}*/
internal inline fun <reified T: ASTNode> ASTNode.filterChildren(): List<T> {
    val list = mutableListOf<T>()
    var iterator = this.firstChild
    while (iterator != null) {
        if(iterator is T) {
            val immutableIterator = iterator
            list.add(immutableIterator)
        }
        iterator = iterator.next
    }
    return list
}

internal fun traversePreOrder(
    node: Node,
    depth: Int = 0
) {
    Log.d("Visit", "--".repeat(depth) + " $node")
    var iterator: Node? = node.firstChild
    while (iterator != null) {
        traversePreOrder(iterator, depth+1)
        iterator = iterator.next
    }
}