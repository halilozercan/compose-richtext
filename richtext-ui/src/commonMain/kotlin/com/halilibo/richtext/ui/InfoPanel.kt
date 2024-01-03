package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.InfoPanelType.Danger
import com.halilibo.richtext.ui.InfoPanelType.Primary
import com.halilibo.richtext.ui.InfoPanelType.Secondary
import com.halilibo.richtext.ui.InfoPanelType.Success
import com.halilibo.richtext.ui.InfoPanelType.Warning

@Stable
public data class InfoPanelStyle(
  val contentPadding: PaddingValues? = null,
  val background: @Composable ((InfoPanelType) -> Modifier)? = null,
  val textStyle: @Composable ((InfoPanelType) -> TextStyle)? = null
) {
  public companion object {
    public val Default: InfoPanelStyle = InfoPanelStyle()
  }
}

public enum class InfoPanelType {
  Primary,
  Secondary,
  Success,
  Danger,
  Warning
}

private val DefaultContentPadding = PaddingValues(8.dp)
private val DefaultInfoPanelBackground = @Composable { infoPanelType: InfoPanelType ->
  remember {
    val (borderColor, backgroundColor) = when (infoPanelType) {
      Primary -> Color(0xffb8daff) to Color(0xffcce5ff)
      Secondary -> Color(0xffd6d8db) to Color(0xffe2e3e5)
      Success -> Color(0xffc3e6cb) to Color(0xffd4edda)
      Danger -> Color(0xfff5c6cb) to Color(0xfff8d7da)
      Warning -> Color(0xffffeeba) to Color(0xfffff3cd)
    }

    Modifier
      .border(1.dp, borderColor, RoundedCornerShape(4.dp))
      .background(backgroundColor, RoundedCornerShape(4.dp))
  }
}

private val DefaultInfoPanelTextStyle = @Composable { infoPanelType: InfoPanelType ->
  remember {
    val color = when(infoPanelType) {
      Primary -> Color(0xff004085)
      Secondary -> Color(0xff383d41)
      Success -> Color(0xff155724)
      Danger -> Color(0xff721c24)
      Warning -> Color(0xff856404)
    }
    TextStyle(color = color)
  }
}

internal fun InfoPanelStyle.resolveDefaults() = InfoPanelStyle(
  contentPadding = contentPadding ?: DefaultContentPadding,
  background = background ?: DefaultInfoPanelBackground,
  textStyle = textStyle ?: DefaultInfoPanelTextStyle
)

/**
 * A panel to show content similar to Bootstrap alerts, categorized as [InfoPanelType].
 * This composable is a shortcut to show only [text] in an info panel.
 */
@Composable
public fun RichTextScope.InfoPanel(
  infoPanelType: InfoPanelType,
  text: String
) {
  InfoPanel(infoPanelType) {
    Text(text)
  }
}

/**
 * A panel to show content similar to Bootstrap alerts, categorized as [InfoPanelType].
 */
@Composable
public fun RichTextScope.InfoPanel(
  infoPanelType: InfoPanelType,
  content: @Composable () -> Unit
) {
  val infoPanelStyle = currentRichTextStyle.resolveDefaults().infoPanelStyle!!
  val backgroundModifier = infoPanelStyle.background!!.invoke(infoPanelType)
  val infoPanelTextStyle = infoPanelStyle.textStyle!!.invoke(infoPanelType)

  val resolvedTextStyle = currentTextStyle.merge(infoPanelTextStyle)

  textStyleBackProvider(resolvedTextStyle) {
    Box(modifier = backgroundModifier.padding(infoPanelStyle.contentPadding!!)) {
      content()
    }
  }
}
