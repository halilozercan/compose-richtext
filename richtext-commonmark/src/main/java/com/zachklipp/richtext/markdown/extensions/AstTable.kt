package com.zachklipp.richtext.markdown.extensions

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.markdown.AstNode
import com.zachklipp.richtext.markdown.AstNodeLinks

@Immutable
internal data class AstTableRoot(
  private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
internal data class AstTableBody(
  private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
internal data class AstTableHeader(
  private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
internal data class AstTableRow(
  private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
internal data class AstTableCell(
  val header: Boolean,
  val alignment: AstTableCellAlignment,
  private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

internal enum class AstTableCellAlignment {
  LEFT,
  CENTER,
  RIGHT
}