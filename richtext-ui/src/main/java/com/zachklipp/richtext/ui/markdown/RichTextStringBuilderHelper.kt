package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.Composable
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.Text

/**
 * TODO: Add documentation if not changed
 */
internal class RichTextStringBuilderHelper {

    private var runningRichTextStringBuilder: RichTextString.Builder? = null

    @Composable
    fun update(block: @Composable RichTextString.Builder.() -> Unit) {
        val builder = runningRichTextStringBuilder ?: RichTextString.Builder()
        runningRichTextStringBuilder = builder
        builder.block()
    }

    fun terminate(): RichTextString? {
        val result = runningRichTextStringBuilder?.toRichTextString()
        runningRichTextStringBuilder = null
        return result
    }

}

/**
 * Terminate the ongoing builder for RichText. If there is content
 * in the builder, render it right now as [Text].
 */
@Composable
private fun MarkdownTextScope.terminateRichText() {
    val terminatedString = richTextStringBuilderHelper.terminate()
    if(terminatedString != null) {
        Text(terminatedString)
    }
}

/**
 * Scope the following lambda for RichText interpretation. In other words,
 * when @param [children] is finished, any RichText should not continue
 * during the traversal. For example, a [BlockQuote] can be scoped so that
 * the built text inside will not continue in the next one.
 */
@Composable
internal fun MarkdownTextScope.richTextBlock(
    children: @Composable () -> Unit
) {
    children()
    terminateRichText()
}

/**
 * Used to add formatting to ongoing builder. For example, [Format.StrongEmphasis]
 * can be used in [withFormat].
 */
@Composable
internal fun MarkdownTextScope.updateRichText(
    block: @Composable RichTextString.Builder.() -> Unit
) {
    richTextStringBuilderHelper.update(block)
}