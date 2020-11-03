# compose-richtext

A collection of Compose libraries for working with rich text formatting and documents. This repo
is currently very experimental and really just proofs-of-concept: there are no tests and some things
might be broken or very non-performant.

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
* [`richtext-commonmark`](#richtext-commonmark)
* [`printing`](#printing)
* [`slideshow`](#slideshow)

## `richtext-ui`

A library of composables for formatting text using higher-level concepts than are supported by
compose foundation, such as "bullet lists" and "headings".

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

## `richtext-commonmark`

Library for rendering Markdown in Compose using Atlassian's [CommonMark](https://github.com/atlassian/commonmark-java)
library to parse, and `richtext-ui` to render.

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

## `slideshow`

A library for presenting simple Powerpoint-like slideshows from a phone (e.g. you can share your
phone screen to a Google Hangout and present that way). Slides can contain any composable content,
although a few pre-fab scaffolds are provided for common slide layouts.

![slideshow demo](.images/slideshow-demo.gif)

### Setting up a slideshow

There is a single, simple entry point to this library, that takes a vararg of composable functions
that define your slides:

```kotlin
Slideshow(
  { /* First slide. */ },
  { /* Second slide. */ },
  { /* etc… */ },
)
```

The `Slideshow` composable will automatically lock your phone to portrait and enter immersive
fullscreen while it's composed. You can tap anywhere on the left or right of the screen to navigate.
Currently the only supported slide transition is crossfade, but it shouldn't be hard to make the
library more pluggable and support more advanced transition libraries (like
[this one](https://github.com/zach-klippenstein/compose-backstack)).

### Creating slides

Individual slides are centered by default, but you can put whatever you want in them. The library
has a few scaffolds for common slide layouts that you might find useful.

#### `TitleSlide`

Very simple: a title and a subtitle, centered.

```kotlin
Slideshow(
  {
    TitleSlide(
      title = { Text("Title") },
      subtitle = { Text("Subtitle") },
    )
  },
)
```

#### `BodySlide`

The `BodySlide` composable gives you a top header, bottom footer, and middle body slot to put
stuff into.

```kotlin
Slideshow(
  { … },
  {
    BodySlide(
      header = { Text("Header") },
      footer = { Text("Footer") },
      body = {
        WebComponent(…)
        // or something
      },
    )
  },
)
```

Slide scaffolds like `BodySlide` and `TitleSlide`, as well as some other aspects of slideshow
formatting like background color, are controlled by passing a `SlideshowTheme` to the `Slideshow`
composable.

#### Animating content on a single slide

A corporate presentation wouldn't be a presentation without obtuse visual effects. The
`NavigableContentContainer` composable is a flexible primitive for building such effects. It takes
a slot inside of which `NavigableContent` composables define blocks of content that will be
shown or hidden by slide navigation. Each `NavigableContent` block gets a `State<Boolean>`
indicating whether content should be shown or not, and is free to show or hide content however it
likes. For example, Compose comes with the `AnimatedVisibility` composable out of the box, which
plays very nicely with this API. See the `SlideshowSample` to see it in action.

```kotlin
NavigableContentContainer {
  Column {
    // Show this right away.
    Text("First paragraph")

    // Only show this after tapping to advance the show, then fade it in.
    NavigableContent { visible ->
      val opacity = animate(if (visible) 1f else 0f)
      Text("Second paragraph", Modifier.drawOpacity(opacity))
    }
  }
}
```

### Running the show

If you're in the middle of a presentation and lose your place, just drag up anywhere on the screen.
A slider and preview will pop up to let you scrub through the deck.

![slideshow scrubbing demo](.images/slideshow-scrubbing-demo.gif)

## License
```
Copyright 2020 Zach Klippenstein

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
