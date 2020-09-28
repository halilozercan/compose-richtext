# compose-richtext

A collection of Compose libraries for working with rich text formatting and documents. This repo
is currently very experimental and really just proofs-of-concept: there are no tests, some things
might be broken or very non-performant, and it's not published on Maven. Once Compose stabilizes, if
it's still useful, I might eventually polish it up and actually release something.

```kotlin
@Composable fun App() {
  val printController = rememberPrintableController()

  Printable(printController) {
    RichText(Modifier.background(color = Color.White)) {
      Heading(0, "Title")
      Text("Summary paragraph.")

      HorizontalRule()

      BlockQuote {
        Text("A wise person once said…")
      }
    }
  }

  Button(onClick = { printController.print("README") }) {
    Text("PRINT ME")
  }
}
```

## Table of Contents

* [`richtext-ui`](#richtext-ui)
* [`printing`](#printing)

## `richtext-ui`

A library of composables for formatting text using higher-level concepts than are supported by
compose foundation, such as "bullet lists" and "headings".

Eventually planning to add support for rendering Markdown.

### Example

Open the `Demo.kt` file in the `richtext-ui` module to play with this.

```kotlin
RichText(modifier = Modifier.background(color = Color.White)) {
  Heading(0, "Paragraphs")
  Text("Simple paragraph.")
  Text("Paragraph with\nmultiple lines.")
  Text("Paragraph with really long line that should be getting wrapped.")

  Heading(0, "Lists")
  Heading(1, "Unordered")
  ListDemo(listType = Unordered)
  Heading(1, "Ordered")
  ListDemo(listType = Ordered)

  Heading(0, "Horizontal Line")
  Text("Above line")
  HorizontalRule()
  Text("Below line")

  Heading(0, "Code Block")
  CodeBlock(
    """
      {
        "Hello": "world!"
      }
    """.trimIndent()
  )

  Heading(0, "Block Quote")
  BlockQuote {
    Text("These paragraphs are quoted.")
    Text("More text.")
    BlockQuote {
      Text("Nested block quote.")
    }
  }

  Heading(0, "Table")
  Table(headerRow = {
    cell { Text("Column 1") }
    cell { Text("Column 2") }
  }) {
    row {
      cell { Text("Hello") }
      cell {
        CodeBlock("Foo bar")
      }
    }
    row {
      cell {
        BlockQuote {
          Text("Stuff")
        }
      }
      cell { Text("Hello world this is a really long line that is going to wrap hopefully") }
    }
  }
}
```

Looks like this:

![demo rendering](.images/richtext-demo.png)

## `printing`

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

![printing demo](.images/printing-demo.gif)
