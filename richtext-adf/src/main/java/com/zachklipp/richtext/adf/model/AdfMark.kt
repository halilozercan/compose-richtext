package com.zachklipp.richtext.adf.model

internal sealed class AdfMark

internal object AdfCodeMark : AdfMark()

internal object AdfEmMark: AdfMark()

internal object AdfStrikeMark: AdfMark()

internal object AdfStrongMark: AdfMark()

internal object AdfUnderlineMark: AdfMark()

internal data class AdfLinkMark(
  val collection: String? = null,
  val href: String,
  val id: String? = null,
  val occurrenceKey: String? = null,
  val title: String? = null
) : AdfMark()

internal data class AdfSubSupMark(
  val type: AdfSubSupMarkType
) : AdfMark()

internal enum class AdfSubSupMarkType {
  sub, sup
}

internal data class AdfTextColorMark(
  val color: String
) : AdfMark()