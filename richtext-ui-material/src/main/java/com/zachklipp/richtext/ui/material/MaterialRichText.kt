package com.zachklipp.richtext.ui.material

import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.RichTextThemeIntegration

/**
 * RichText implementation that integrates with Material design.
 *
 * If the consumer app has small composition trees or only uses RichText in
 * a single place, it would be ideal to call this function instead of wrapping
 * everything under [SetupMaterialRichText].
 */
@Composable
public fun MaterialRichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  SetupMaterialRichText {
    RichText(
      modifier = modifier,
      style = style,
      children = children
    )
  }
}

/**
 * Wraps the given [child] with Material Theme integration for [RichText].
 *
 * This function also keeps track of the parent context by using CompositionLocals
 * to not apply Material Theming if it already exists in the current composition.
 *
 * If the whole application is written in Compose or contains large Compose trees,
 * it would be ideal to call this function right after applying the Material Theme.
 * Then, calling [MaterialRichText] or [RichText] would have no difference.
 */
@Composable
public fun SetupMaterialRichText(
  child: @Composable () -> Unit
) {
  val isApplied = LocalMaterialThemingApplied.current

  if (!isApplied) {
    RichTextThemeIntegration(
      textStyle = { LocalTextStyle.current },
      contentColor = { LocalContentColor.current },
      ProvideTextStyle = { textStyle, content ->
        ProvideTextStyle(textStyle, content)
      },
      ProvideContentColor = { color, content ->
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
