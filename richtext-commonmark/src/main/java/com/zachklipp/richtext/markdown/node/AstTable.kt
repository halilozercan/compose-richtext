package com.zachklipp.richtext.markdown.node

import androidx.compose.runtime.Immutable

@Immutable
internal object AstTableRoot: AstContainerBlockNodeType()

@Immutable
internal object AstTableBody: AstContainerBlockNodeType()

@Immutable
internal object AstTableHeader: AstContainerBlockNodeType()

@Immutable
internal object AstTableRow: AstContainerBlockNodeType()

@Immutable
internal data class AstTableCell(
  val header: Boolean,
  val alignment: AstTableCellAlignment
) : AstContainerBlockNodeType()

internal enum class AstTableCellAlignment {
  LEFT,
  CENTER,
  RIGHT
}
