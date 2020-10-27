package com.zachklipp.richtext.markdown.extensions

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.markdown.AstNode
import com.zachklipp.richtext.markdown.AstNodeLinks

@Immutable
data class AstTableRoot(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstTableBody(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstTableHeader(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstTableRow(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstTableCell(
    val header: Boolean,
    val alignment: AstTableCellAlignment,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

enum class AstTableCellAlignment {
    LEFT, CENTER, RIGHT
}