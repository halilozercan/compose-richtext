package com.halilibo.richtext.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * Handler that will be triggered when a [Format.Link] is clicked.
 */
public fun interface LinkClickHandler {

  public fun onClick(url: String)
}

/**
 * An internal composition local to pass through LinkClickHandler from root [BasicRichText]
 * composable to children that render links.
 */
internal val LocalLinkClickHandler = compositionLocalOf<LinkClickHandler?> { null }
