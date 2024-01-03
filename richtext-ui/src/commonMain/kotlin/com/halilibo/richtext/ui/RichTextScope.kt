@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.halilibo.richtext.ui

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State

/**
 * Scope object for composables that can draw rich text.
 *
 * RichTextScope facilitates a context for RichText elements. It does not
 * behave like a [State] or a [CompositionLocal]. Starting from [BasicRichText],
 * this scope carries information that should not be passed down as a state.
 */
@Immutable
public object RichTextScope
