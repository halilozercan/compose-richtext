package com.zachklipp.richtext.adf.model

internal sealed class AdfNode

internal sealed class AdfBlockNode : AdfNode() {
  abstract val content: List<AdfNode>
}

internal sealed class AdfInlineNode : AdfNode()

internal data class AdfTable(
  override val content: List<AdfTableRow>,
  val isNumberColumnEnabled: Boolean,
  val layout: String
) : AdfBlockNode()

internal data class AdfTableRow(
  override val content: List<AdfTableUnit>
) : AdfBlockNode()

internal sealed class AdfTableUnit : AdfBlockNode() {

  internal data class AdfTableCell(
    override val content: List<AdfNode>,
    val background: String? = null,
    val colSpan: Int = 1,
    val colWidth: List<Int> = emptyList(),
    val rowSpan: Int = 1
  ) : AdfTableUnit()

  internal data class AdfTableHeader(
    override val content: List<AdfNode>,
    val background: String? = null,
    val colSpan: Int = 1,
    val colWidth: List<Int> = emptyList(),
    val rowSpan: Int = 1
  ) : AdfTableUnit()
}

internal data class AdfDocument(
  override val content: List<AdfNode>,
  val version: Int
) : AdfBlockNode()

internal object AdfRule : AdfNode()

internal data class AdfText(
  val text: String,
  val marks: List<AdfMark>
) : AdfInlineNode()

internal data class AdfMention(
  val accessLevel: MentionAccessLevel?,
  val id: String,
  val text: String?,
  val userType: MentionUserType?
) : AdfInlineNode()

internal data class AdfHardBreak(
  val text: String = "\n"
) : AdfInlineNode()

internal data class AdfParagraph(
  override val content: List<AdfNode>
) : AdfBlockNode()

internal data class AdfHeading(
  val level: Int,
  override val content: List<AdfNode>
) : AdfBlockNode()

internal data class AdfBlockQuote(
  override val content: List<AdfNode>
) : AdfBlockNode()

internal data class AdfCodeBlock(
  override val content: List<AdfText>,
  val language: String? = null
) : AdfBlockNode()

internal data class AdfPanelBlock(
  override val content: List<AdfNode>,
  val panelType: PanelType
) : AdfBlockNode()

internal enum class PanelType {
  info, warning, success, error, note
}

internal data class AdfBulletList(
  override val content: List<AdfListItem>
) : AdfBlockNode()

internal data class AdfOrderedList(
  override val content: List<AdfListItem>
) : AdfBlockNode()

internal data class AdfListItem(
  override val content: List<AdfNode>
) : AdfBlockNode()

internal enum class MentionAccessLevel {
  NONE,
  SITE,
  APPLICATION,
  CONTAINER
}

internal enum class MentionUserType {
  DEFAULT,
  SPECIAL,
  APP
}