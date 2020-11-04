# Printing

A library for using Compose to generated printed documents, using Android's printing services.

There are multiple entry points into this library. See their kdoc for usage and parameter
documentation, and take a look at the samples for example code.

* **`Printable`** – This is the simplest entry point. It's a composable function that displays its
  content normally on screen, but can also print itself.
* **`ComposePrintAdapter`** – This is a [`PrintDocumentAdapter`](https://developer.android.com/reference/android/print/PrintDocumentAdapter)
  that can be used directly with Android's printing APIs to print any composable function.
* **`Paged`** – This is another composable, but doesn't actually have anything to do with printing.
  Conceptually it's similar to a `ScrollableColumn` – it lays its contents out at full height, then
  can display them at various vertical offsets. However, it also tries to ensure that no composables
  are clipped at the bottom, by measuring where all the leaf composables (those without any
  children) are located clipping the content before them. It is used by the printing APIs to try to
  ensure that composable content looks decent when split into printer pages.

Here's a sample that tries to match the style of one of the Google Docs templates. It looks great
on small phone screens, but also prints:

![printing demo](img/printing-demo.gif)
