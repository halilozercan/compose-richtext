package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextStyle.Companion

/**
 * Provides necessary [CompositionLocal] objects for RichText.
 *
 * This function should be called at the start of Compose hierarchy near to Theming
 * declaration. [RichText] exclusively uses these [CompositionLocal] objects. Please
 * refer to [RichTextLocals] for more info.
 */
@Composable
public fun ProvideRichTextLocals(
  localTextStyle: ProvidableCompositionLocal<TextStyle>,
  localContentColor: ProvidableCompositionLocal<Color>,
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(
    LocalRichTextLocals provides RichTextLocals(localTextStyle, localContentColor),
    content = content
  )
}

/**
 * A stable class that holds [CompositionLocal]s that provide [TextStyle] and content [Color].
 *
 * This class is intended to be used in conjunction with [ProvideRichTextLocals]. Once these
 * locals are provided, [RichText] can understand the current text style as well as
 * modify it in accordance with [RichTextStyle] that can be defined at each [RichText] call.
 */
public data class RichTextLocals(
  val localTextStyle: ProvidableCompositionLocal<TextStyle>,
  val localContentColor: ProvidableCompositionLocal<Color>
)

internal val LocalRichTextLocals = staticCompositionLocalOf {
  RichTextLocals(
    localTextStyle = compositionLocalOf { TextStyle.Default },
    localContentColor = compositionLocalOf { Color.White },
  )
}

/**
 * The current [TextStyle].
 */
public val RichTextScope.currentTextStyle: TextStyle
  @Composable get() = LocalRichTextLocals.current.localTextStyle.current

/**
 * The current content [Color].
 */
public val RichTextScope.currentContentColor: Color
  @Composable get() = LocalRichTextLocals.current.localContentColor.current

/**
 * Intended only for internal use to quickly provide these locals when necessary.
 */
@Composable
internal fun BasicLocalsProvider(
  textStyle: TextStyle = TextStyle.Default,
  contentColor: Color = Color.White,
  content: @Composable () -> Unit
) {
  ProvideRichTextLocals(
    localTextStyle = compositionLocalOf { textStyle },
    localContentColor = compositionLocalOf { contentColor },
    content = content
  )
}