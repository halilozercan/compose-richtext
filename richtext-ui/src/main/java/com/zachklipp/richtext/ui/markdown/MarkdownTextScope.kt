package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.ui.RichTextScope

typealias OnLinkClick = (String) -> Unit

@Immutable
internal interface MarkdownTextScope : RichTextScope {
    val onLinkClick: OnLinkClick
}