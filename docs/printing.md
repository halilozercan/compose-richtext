# Printing

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)

A library for using Compose to generated printed documents, using Android's printing services.

## Gradle

```groovy
dependencies {
  implementation "com.halilibo.compose-richtext:printing:${richtext_version}"
}
```

## Usage

There are multiple entry points into this library. See their kdoc for usage and parameter
documentation, and take a look at the samples for example code.

### [`Printable`](../api/printing/com.zachklipp.richtext.ui.printing/-printable.html)

This is the simplest entry point. It's a composable function that displays its children on screen,
but can also print itself. Printing is triggered by the [`PrintableController`](../api/printing/com.zachklipp.richtext.ui.printing/-printable-controller/index.html)
passed to `Printable`. `PrintableController` is a hoisted state type, just like `ScrollState`,
created by calling `rememberPrintableController`.

```kotlin
val printController = rememberPrintableController()
Printable(printController) {
  ScrollableColumn {
    Card { … }
    Card { … }
    Button(onClick = { printController.print("sales report") }) { … }
  }
}
```

### [`ComposePrintAdapter`](../api/printing/com.zachklipp.richtext.ui.printing/-compose-print-adapter/-compose-print-adapter.html)

This is a [`PrintDocumentAdapter`](https://developer.android.com/reference/android/print/PrintDocumentAdapter)
that can be used directly with Android's printing APIs to print any composable function. It takes,
at minimum, the `ComponentActivity` that owns the print adapter (as required by Android's printing
framework), a string name for the document, and the composable function that defines the content to
print. See the linked API documentation for more details.

### [`Paged`](../api/printing/com.zachklipp.richtext.ui.printing/-paged.html)

This is another composable, but doesn't actually have anything to do with printing.
Conceptually it's similar to a `ScrollableColumn` – it lays its contents out at full height, then
can display them at various vertical offsets. However, it also tries to ensure that no composables
are clipped at the bottom, by measuring where all the leaf composables (those without any
children) are located clipping the content before them. It is used by the printing APIs to try to
ensure that composable content looks decent when split into printer pages.

See the [`PagedSample`](https://github.com/halilozercan/compose-richtext/blob/main/sample/src/main/java/com/zachklipp/richtext/sample/PagedSample.kt)
for more information.

## Demo

The [`DocumentSample`](https://github.com/halilozercan/compose-richtext/blob/main/sample/src/main/java/com/zachklipp/richtext/sample/DocumentSample.kt)
tries to match the style of one of the Google Docs templates. It looks great
on small phone screens, but also prints:

![printing demo](img/printing-demo.gif)
