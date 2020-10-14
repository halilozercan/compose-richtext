package com.zachklipp.richtext.ui.printing

import android.content.Context
import android.content.Context.PRINT_SERVICE
import android.content.ContextWrapper
import android.print.PrintManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.LayoutModifier
import androidx.compose.ui.Measurable
import androidx.compose.ui.MeasureScope
import androidx.compose.ui.MeasureScope.MeasureResult
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.Constraints
import com.zachklipp.richtext.ui.printing.PrintableController.PrintableComposable

/**
 * True if the composable is currently being rendered to a PDF canvas for printing.
 */
private val PrintingAmbient = staticAmbientOf { false }

/**
 * Returns true if a [Printable] is being used to print the composition somewhere up in the tree.
 */
@Composable val isBeingPrinted: Boolean get() = PrintingAmbient.current

/**
 * Returns a [Modifier] that will hide the composable it's applied to when printing.
 */
fun Modifier.hideWhenPrinting(): Modifier = composed {
  val printing = PrintingAmbient.current
  object : LayoutModifier {
    override fun MeasureScope.measure(
      measurable: Measurable,
      constraints: Constraints
    ): MeasureResult {
      val placeable = if (!printing) measurable.measure(constraints) else null
      return layout(placeable?.width ?: 0, placeable?.height ?: 0) {
        placeable?.placeRelative(Offset.Zero)
      }
    }
  }
}

/**
 * Provides the [print] method to trigger printing [Printable] composables.
 */
abstract class PrintableController {

  internal class PrintableComposable(
    val modifier: Modifier,
    val pageDpi: Int,
    val printBreakpoints: Boolean,
    val content: @Composable () -> Unit
  )

  // TODO support multiple composables, ComposePrintAdapter needs to take a list and concatenate
  //  multiple Pageds
  private var composable: PrintableComposable? = null

  /**
   * Asks the [PrintManager] to start printing the [Printable] composable.
   */
  fun print(
    documentName: String,
    jobName: String = documentName
  ) {
    composable?.let {
      doPrint(documentName, jobName, it.modifier, it.pageDpi, it.printBreakpoints, it.content)
    }
  }

  protected abstract fun doPrint(
    documentName: String,
    jobName: String,
    modifier: Modifier,
    pageDpi: Int,
    printBreakpoints: Boolean,
    content: @Composable () -> Unit
  )

  internal fun registerComposable(composable: PrintableComposable) {
    check(this.composable == null) { "Composable already registered" }
    this.composable = composable
  }

  internal fun unregisterComposable(composable: PrintableComposable) {
    // Don't stomp out-of-order registrations.
    if (this.composable === composable) {
      this.composable = null
    }
  }
}

/**
 * Creates and remembers a [PrintableController] that can be passed to [Printable] to start
 * print jobs.
 */
@Composable fun rememberPrintableController(): PrintableController {
  val context = ContextAmbient.current
  val coroutineScope = rememberCoroutineScope()
  return remember {
    val activity = context.findComponentActivity()
    val printManager = activity?.getSystemService(PRINT_SERVICE) as PrintManager?

    object : PrintableController() {
      override fun doPrint(
        documentName: String,
        jobName: String,
        modifier: Modifier,
        pageDpi: Int,
        printBreakpoints: Boolean,
        content: @Composable () -> Unit
      ) {
        if (activity == null || printManager == null) return
        val adapter = ComposePrintAdapter(
          activity, documentName, modifier, pageDpi, printBreakpoints,
          mainContext = coroutineScope.coroutineContext
        ) {
          Providers(PrintingAmbient provides true, children = content)
        }
        printManager.print(jobName, adapter, null)
      }
    }
  }
}

/**
 * Renders [content] as-is, and will allow the content to be printed with [PrintableController]. To
 * start a print job, call [PrintableController.print].
 *
 * Inside [content], the [hideWhenPrinting] modifier and [isBeingPrinted] property can be used to
 * conditionally change your UI based on whether it's being printed or not.
 *
 * @param controller A [PrintableController] created by [rememberPrintableController].
 * @param modifier A [Modifier] that will be applied to the content normally when composing to the
 * screen, and to every individual page when printing.
 * @param pageDpi The resolution to use for the printed page. Lower values mean smaller text. Has
 * no effect when composing to the screen.
 * @param printBreakpoints If true, horizontal lines are drawn at each breakpoint for debugging.
 * False by default.
 */
@Composable fun Printable(
  controller: PrintableController,
  modifier: Modifier = Modifier,
  pageDpi: Int = DefaultPageDpi,
  printBreakpoints: Boolean = false,
  content: @Composable () -> Unit
) {
  // Don't pass any keys because the PrintableComposable depends on all the parameters, so it always
  // needs to be recreated when this function is recomposed.
  onCommit {
    val printable = PrintableComposable(modifier, pageDpi, printBreakpoints, content)
    controller.registerComposable(printable)
    onDispose { controller.unregisterComposable(printable) }
  }

  WithConstraints {
    Box(modifier) { content() }
  }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? {
  return when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
  }
}
