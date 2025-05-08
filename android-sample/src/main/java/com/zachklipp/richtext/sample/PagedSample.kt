package com.zachklipp.richtext.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zachklipp.richtext.ui.printing.Paged
import com.zachklipp.richtext.ui.printing.Printable
import com.zachklipp.richtext.ui.printing.isBeingPrinted
import com.zachklipp.richtext.ui.printing.rememberPrintableController

/**
 * Demonstrates the [Paged] composable. Pages can be browsed in the sample, or printed, using the
 * [Printable] composable.
 */
@Composable fun PagedSample() {
  val controller = rememberPrintableController()
  val state = remember { PagedScreenState() }
  Printable(controller, printBreakpoints = state.drawBreakpoints) {
    PagedScreen(state, doPrint = {
      controller.print("Document Sample")
    })
  }
}

@Stable
private class PagedScreenState {
  var pageIndex by mutableStateOf(0)
  var pageCount by mutableStateOf(0)
  var clipPage by mutableStateOf(true)
  var drawBreakpoints by mutableStateOf(true)
}

@Composable private fun PagedScreen(
  state: PagedScreenState,
  doPrint: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    if (isBeingPrinted) {
      // Nested Paged composables aren't supported.
      PagedContent()
    } else {
      Row {
        Text("Page index: ${state.pageIndex + 1} / ${state.pageCount}")
        TextButton(onClick = { state.pageIndex-- }) {
          Text("-")
        }
        TextButton(onClick = { state.pageIndex++ }) {
          Text("+")
        }
        Button(onClick = { doPrint() }) {
          Text("Print")
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
          checked = state.clipPage,
          onCheckedChange = { state.clipPage = !state.clipPage })
        Text("Clip page bottom")
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
          checked = state.drawBreakpoints,
          onCheckedChange = { state.drawBreakpoints = !state.drawBreakpoints })
        Text("Show Breakpoints")
      }

      Paged(
        pageIndex = state.pageIndex,
        onPageLayout = { state.pageCount = it },
        clipLastBreakpoint = state.clipPage,
        drawBreakpoints = state.drawBreakpoints,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(4.dp)
          .shadow(1.dp)
      ) {
        PagedContent()
      }
    }
  }
}

@Composable private fun PagedContent() {
  Column {
    for (i in 0 until 20) {
      // Staggered, to demonstrate breakpoints on overlapping nodes.
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
          Text("${i * 3}", fontSize = 32.sp)
          Text("${i * 3 + 2}", fontSize = 32.sp)
        }
        Text("${i * 3 + 1}", fontSize = 32.sp)
      }
    }
  }
}
