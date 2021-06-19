package com.zachklipp.richtext.ui.slideshow

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Defines the visual styling for a [Slideshow].
 *
 * @param contentColor Default color used for text and [SlideDivider].
 * @param backgroundColor Color used as the background for slides.
 * @param baseTextStyle Default [TextStyle] used for all slide content. Some scaffolds use other
 * styles from the theme for certain slots.
 * @param titleStyle Default [TextStyle] used for [TitleSlide] titles.
 * @param subtitleStyle Default [TextStyle] used for [TitleSlide] subtitles.
 * @param headerStyle Default [TextStyle] used for [BodySlide] headers.
 * @param footerStyle Default [TextStyle] used for [BodySlide] footers.
 * @param gap Default margins used for [BodySlide]s and spacing between header, body, and footer.
 * @param aspectRatio The aspect ratio for the entire slideshow.
 */
@Immutable
public data class SlideshowTheme(
  val contentColor: Color = Color.White,
  val backgroundColor: Color = Color.DarkGray,
  val baseTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
  val titleStyle: TextStyle = TextStyle(
    fontSize = 48.sp,
    textAlign = Center,
    fontWeight = FontWeight.Bold
  ),
  val subtitleStyle: TextStyle = TextStyle(
    fontSize = 36.sp,
    textAlign = Center
  ),
  val headerStyle: TextStyle = TextStyle(fontSize = 28.sp),
  val footerStyle: TextStyle = TextStyle(fontSize = 12.sp),
  val gap: Dp = 16.dp,
  val aspectRatio: Float = 16 / 9f
)

public val SlideshowThemeAmbient: ProvidableCompositionLocal<SlideshowTheme> =
  staticCompositionLocalOf { SlideshowTheme() }
