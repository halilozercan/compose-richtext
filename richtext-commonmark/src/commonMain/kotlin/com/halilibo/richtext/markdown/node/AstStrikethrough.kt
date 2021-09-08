package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable

@Immutable
internal data class AstStrikethrough(
  val delimiter: String
) : AstInlineNodeType()
