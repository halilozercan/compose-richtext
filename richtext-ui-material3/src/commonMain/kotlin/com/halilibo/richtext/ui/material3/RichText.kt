package com.halilibo.richtext.ui.material3

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.BasicRichText
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.RichTextThemeProvider

/**
 * RichText implementation that integrates with Material 3 design.
 *
 * If the consumer app has small composition trees or only uses RichText in
 * a single place, it would be ideal to call this function instead of wrapping
 * everything under [RichTextMaterialTheme].
 */
@Composable
public fun RichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  RichTextMaterialTheme {
    BasicRichText(
      modifier = modifier,
      style = style,
      children = children
    )
  }
}

/**
 * Wraps the given [child] with Material Theme integration for [BasicRichText].
 *
 * This function also keeps track of the parent context by using CompositionLocals
 * to not apply Material Theming if it already exists in the current composition.
 */
@Composable
internal fun RichTextMaterialTheme(
  child: @Composable () -> Unit
) {
  val isApplied = LocalMaterialThemingApplied.current

  if (!isApplied) {
    RichTextThemeProvider(
      textStyleProvider = { LocalTextStyle.current },
      contentColorProvider = { LocalContentColor.current },
      textStyleBackProvider = { textStyle, content ->
        ProvideTextStyle(textStyle, content)
      },
      contentColorBackProvider = { color, content ->
        CompositionLocalProvider(LocalContentColor provides color) {
          content()
        }
      }
    ) {
      CompositionLocalProvider(LocalMaterialThemingApplied provides true) {
        child()
      }
    }
  } else {
    child()
  }
}

private val LocalMaterialThemingApplied = compositionLocalOf { false }