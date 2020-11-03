package com.zachklipp.richtext.markdown.extensions

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.markdown.AstNode
import com.zachklipp.richtext.markdown.AstNodeLinks

@Immutable
internal data class AstStrikethrough(
    val delimiter: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks