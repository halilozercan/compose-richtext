package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.ui.RichTextScope

/**
 * An extension on RichTextScope. It helps to mark functions that contributes
 * to or reads from Markdown context. [RichTextScope] is also necessary because
 * [Markdown] composable and its children must use [RichTextScope] functions
 * to draw UI.
 */
@Immutable
internal interface MarkdownTextScope : RichTextScope {

    /**
     * A function to invoke when a link is clicked from rendered content.
     */
    fun onLinkClick(destination: String)
}