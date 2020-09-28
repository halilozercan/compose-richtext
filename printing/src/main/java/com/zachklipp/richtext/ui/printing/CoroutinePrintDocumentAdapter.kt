package com.zachklipp.richtext.ui.printing

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A [PrintDocumentAdapter] that exposes [onLayout] and [onWrite] functions as suspend functions
 * that will automatically invoke the correct callback methods.
 */
abstract class CoroutinePrintDocumentAdapter(context: CoroutineContext) : PrintDocumentAdapter() {

  private val printAdapterScope = CoroutineScope(context + Job(parent = context[Job]))

  override fun onFinish() {
    printAdapterScope.cancel("Print adapter finished")
    super.onFinish()
  }

  abstract suspend fun onLayout(
    oldAttributes: PrintAttributes?,
    newAttributes: PrintAttributes,
    extras: Bundle?
  ): PrintDocumentInfo

  abstract suspend fun onWrite(
    pages: Array<out PageRange>,
    destination: ParcelFileDescriptor
  ): Array<out PageRange>

  final override fun onLayout(
    oldAttributes: PrintAttributes?,
    newAttributes: PrintAttributes,
    cancellationSignal: CancellationSignal,
    callback: LayoutResultCallback,
    extras: Bundle?
  ) {
    val job = printAdapterScope.launch {
      try {
        val result = onLayout(oldAttributes, newAttributes, extras)
        callback.onLayoutFinished(result, true)
      } catch (e: CancellationException) {
        callback.onLayoutCancelled()
      } catch (e: Throwable) {
        callback.onLayoutFailed(e.message)
      }
    }
    cancellationSignal.setOnCancelListener { job.cancel() }
  }

  final override fun onWrite(
    pages: Array<out PageRange>,
    destination: ParcelFileDescriptor,
    cancellationSignal: CancellationSignal,
    callback: WriteResultCallback
  ) {
    val job = printAdapterScope.launch {
      try {
        val writtenPages = onWrite(pages, destination)
        callback.onWriteFinished(writtenPages)
      } catch (e: CancellationException) {
        callback.onWriteCancelled()
      } catch (e: Throwable) {
        callback.onWriteFailed(e.message)
      }
    }
    cancellationSignal.setOnCancelListener { job.cancel() }
  }
}