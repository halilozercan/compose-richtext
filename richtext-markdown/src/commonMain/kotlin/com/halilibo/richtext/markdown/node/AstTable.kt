package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable

@Immutable
public object AstTableRoot: AstContainerBlockNodeType()

@Immutable
public object AstTableBody: AstContainerBlockNodeType()

@Immutable
public object AstTableHeader: AstContainerBlockNodeType()

@Immutable
public object AstTableRow: AstContainerBlockNodeType()

@Immutable
public data class AstTableCell(
  val header: Boolean,
  val alignment: AstTableCellAlignment
) : AstContainerBlockNodeType()

public enum class AstTableCellAlignment {
  LEFT,
  CENTER,
  RIGHT
}
