package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.ListType
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Text

@Preview
@Composable
private fun UnorderedListPreview() {
  ListPreview(listType = ListType.Unordered, layoutDirection = LayoutDirection.Ltr)
}

@Preview
@Composable
private fun UnorderedListPreviewRtl() {
  ListPreview(listType = ListType.Unordered, layoutDirection = LayoutDirection.Rtl)
}

@Preview
@Composable
private fun OrderedListPreview() {
  ListPreview(listType = ListType.Ordered, layoutDirection = LayoutDirection.Ltr)
}

@Preview
@Composable
private fun OrderedListPreviewRtl() {
  ListPreview(listType = ListType.Ordered, layoutDirection = LayoutDirection.Rtl)
}

@Composable
private fun ListPreview(
  listType: ListType,
  layoutDirection: LayoutDirection
) {
  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Box(Modifier.background(color = Color.White)) {
      RichTextScope.FormattedList(
        listType = listType,
        items = listOf(
          "Foo",
          "Bar",
          "Baz",
          "Foo",
          "Bar",
          "Baz",
          "Foo",
          "Bar",
          "Foo\nBar\nBaz",
          "Foo"
        ).withIndex()
          .toList()
      ) { (index, text) ->
        Text(text)
        if (index == 0) {
          FormattedList(listType, @Composable {
            Text("indented $text")
            FormattedList(listType, @Composable {
              Text("indented $text")
              FormattedList(listType, @Composable {
                Text("indented $text")
              })
            })
          })
        }
      }
    }
  }
}
