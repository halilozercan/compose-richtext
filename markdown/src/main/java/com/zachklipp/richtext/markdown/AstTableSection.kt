package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Immutable

/**
 * Tables in common-markdown are implemented as child classes to
 * CustomBlock and CustomHeader. However, corresponding ASTCustomBlock
 * and ASTCustomNode classes are final. Instead, we are passing
 * identifiable data through [AstCustomBlock.data] or [AstCustomNode.data]
 */
@Immutable
sealed class AstTableSection {

    /**
     * Found in [AstCustomBlock.data]. Marks the node as the root of a table entry.
     */
    @Immutable
    object Root: AstTableSection()

    /**
     * Found in [AstCustomNode.data]. Marks the node as starting point for table body.
     */
    @Immutable
    object Body: AstTableSection()

    /**
     * Found in [AstCustomNode.data]. Marks the node as starting point for table header.
     */
    @Immutable
    object Header: AstTableSection()

    /**
     * Found in [AstCustomNode.data]. Marks the node as starting point for table row.
     */
    @Immutable
    object Row: AstTableSection()

    /**
     * Found in [AstCustomNode.data]. Marks the node as a table cell.
     * No further table entries are expected below a cell.
     */
    @Immutable
    data class Cell(
        val header: Boolean,
        val alignment: AstTableCellAlignment
    ): AstTableSection()

    enum class AstTableCellAlignment {
        LEFT, CENTER, RIGHT
    }
}