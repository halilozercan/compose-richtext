package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.Immutable

/**
 * Tables are implemented as ASTCustomBlock and ASTCustomNode.
 * However, it's not wise to extend data classes. Instead, we are passing
 * necessary data through [ASTCustomBlock.data] or [ASTCustomNode.data]
 */

@Immutable
sealed class ASTTable {

    @Immutable
    object  ASTTableBlock: ASTTable()

    @Immutable
    object  ASTTableBody: ASTTable()

    @Immutable
    object  ASTTableHeader: ASTTable()

    @Immutable
    object  ASTTableRow: ASTTable()

    @Immutable
    data class ASTTableCell(
        val header: Boolean,
        val alignment: ASTTableCellAlignment
    ): ASTTable()

    enum class ASTTableCellAlignment {
        LEFT, CENTER, RIGHT
    }
}