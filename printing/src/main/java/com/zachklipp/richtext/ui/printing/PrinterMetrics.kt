@file:Suppress("NOTHING_TO_INLINE")

package com.zachklipp.richtext.ui.printing

import androidx.compose.ui.unit.Density

/** PDF dimensions are always given in points (1/72s of an inch). */
private const val POSTSCRIPT_DPI = 72

/** How many P's in an I. */
private const val DP_DPI = 160

const val DefaultPageDpi = 100

/** Represents a PostScript point (1/72 of an inch). */
internal inline class Pts(val value: Int) {
  override fun toString(): String = "$value.pts"
}

internal inline val Int.pts get() = Pts(this)

/**
 * Helps with converting between PostScript points and regular android units.
 */
internal data class PrinterMetrics(
  val screenDensity: Density,
  val pageDpi: Int,
  val pageWidth: Pts,
  val pageHeight: Pts,
) : Density by screenDensity {

  /** The [Density] that should be used for composing pages. */
  val renderDensity: Density get() = Density(pageDpi / DP_DPI.toFloat())
}
