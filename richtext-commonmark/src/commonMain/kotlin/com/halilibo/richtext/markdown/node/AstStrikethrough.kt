package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable

@Immutable
public data class AstStrikethrough(
  val delimiter: String
) : AstInlineNodeType()
