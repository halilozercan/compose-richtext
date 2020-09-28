package com.zachklipp.richtext.ui.printing

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentInfo
import android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT
import android.print.pdf.PrintedPdfDocument
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * A [PrintDocumentAdapter][android.print.PrintDocumentAdapter] that prints a composable function.
 * The composable content is paginated as best-effort to avoid cutting off composables in the
 * middle (see [Paged]).
 *
 * Long blocks of text and subcompositions are not fully supported: the paginator will try to not
 * split them, but if they don't fit on a page, they will be split arbitrarily.
 *
 * See [composeToPdf] for details on how the composable is actually written to the PDF.
 *
 * @param activity The [ComponentActivity] used to create various printing resources.
 * @param documentName The file name that will be reported to the printing system, e.g. to use as
 * the default for the Save to PDF virtual printer.
 * @param pageModifier A [Modifier] that will be applied to each individual page of the printed
 * content. [responsivePadding] is a good choice.
 * @param pageDpi The resolution to use for the `Density` of the composable.
 * @param printBreakpoints If true, horizontal lines are drawn at each breakpoint for debugging.
 * False by default.
 * @param mainContext The [CoroutineContext] to interact with the composable and view system on.
 */
class ComposePrintAdapter(
  private val activity: ComponentActivity,
  private val documentName: String,
  private val pageModifier: Modifier = Modifier,
  private val pageDpi: Int = DefaultPageDpi,
  private val printBreakpoints: Boolean = false,
  mainContext: CoroutineContext = Dispatchers.Main,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val content: @Composable () -> Unit
) : CoroutinePrintDocumentAdapter(mainContext) {

  private lateinit var pdfDocument: PrintedPdfDocument

  override suspend fun onLayout(
    oldAttributes: PrintAttributes?,
    newAttributes: PrintAttributes,
    extras: Bundle?
  ): PrintDocumentInfo {
    pdfDocument = PrintedPdfDocument(activity, newAttributes)

    // Don't bother trying to calculate the page count. The print manager will automatically get
    // the count from the initial preview.
    return PrintDocumentInfo.Builder(documentName)
        .setContentType(CONTENT_TYPE_DOCUMENT)
        .build()
  }

  override suspend fun onWrite(
    pages: Array<out PageRange>,
    destination: ParcelFileDescriptor
  ): Array<out PageRange> {
    var currentPageIndex by mutableStateOf(0)
    val pageCountDeferred = CompletableDeferred<Int>()

    // Initialize the composition, and reuse the same composition to render all the pages.
    composeToPdf(activity, pdfDocument, pageDpi, content = {
      // This is the actual composable that will be rendered to the PDF.
      // Paged tries to ensure that we don't cut off leaf composables in the middle.
      Paged(
        pageIndex = currentPageIndex,
        pageModifier = pageModifier,
        drawBreakpoints = printBreakpoints,
        onPageLayout = { pageCountDeferred.complete(it) },
        content = content
      )
    }) {
      // We need to know the total page count before we can start iterating. This waits for the
      // first composition and frame to commit.
      val pageCount = pageCountDeferred.await()

      (0 until pageCount).asSequence()
        .filter { page -> pages.any { page in it.start..it.end } }
        .forEach { pageNumber ->
          val page = pdfDocument.startPage(pageNumber)

          // Update the Paged to "flip" to the page.
          currentPageIndex = pageNumber

          // Render the page to the PDF. This function will automatically wait for the next frame to
          // finish drawing. It also does not do any IO, so we don't need to switch dispatchers.
          composePage(page)
          pdfDocument.finishPage(page)

          // We're on the main thread, so be a good citizen.
          // Also ensures we handle cancellation in a timely fashion.
          yield()
        }
    }

    // The PDF currently only exists in memory, so to dump it to the printing system we use a
    // background thread.
    withContext(ioDispatcher) {
      @Suppress("BlockingMethodInNonBlockingContext")
      pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
    }
    pdfDocument.close()
    return pages
  }
}
