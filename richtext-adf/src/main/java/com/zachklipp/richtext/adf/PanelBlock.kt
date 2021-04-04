package com.zachklipp.richtext.adf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zachklipp.richtext.adf.model.PanelType
import com.zachklipp.richtext.adf.model.PanelType.info
import com.zachklipp.richtext.adf.model.PanelType.note
import com.zachklipp.richtext.adf.model.PanelType.success
import com.zachklipp.richtext.adf.model.PanelType.warning
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.RichTextScope

@Composable internal fun RichTextScope.PanelBlock(
  panelType: PanelType,
  children: @Composable RichTextScope.() -> Unit
) {
  val (backgroundColor, iconColor) = when (panelType) {
    info -> Color(0xFFDEEBFF) to Color(0xFF0052CC)
    warning -> Color(0xFFFFFAE6) to Color(0xFFFF991F)
    success -> Color(0xFFE3FCEF) to Color(0xFF00875A)
    PanelType.error -> Color(0xFFFFEBE6) to Color(0xFFDE350B)
    note -> Color(0xFFEAE6FF) to Color(0xFF5243AA)
  }

  val (icon, description) = when (panelType) {
    info -> R.drawable.ic_info to "info"
    warning -> R.drawable.ic_warning to "warning"
    success -> R.drawable.ic_success to "success"
    PanelType.error -> R.drawable.ic_error to "error"
    note -> R.drawable.ic_note to "note"
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(backgroundColor, shape = RoundedCornerShape(2.dp))
      .padding(4.dp)
  ) {
    Icon(
      painter = painterResource(id = icon),
      contentDescription = description,
      tint = iconColor,
      modifier = Modifier.padding(4.dp)
    )
    RichText(
      children = children,
      modifier = Modifier.padding(8.dp)
    )
  }
}