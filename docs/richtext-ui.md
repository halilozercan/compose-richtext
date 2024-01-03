# Richtext UI

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

A library of composables for formatting text using higher-level concepts than are supported by
compose foundation, such as "bullet lists" and "headings".

RichText UI is a base library that is non-opinionated about higher level design requirements.
If you are already using `MaterialTheme` in your compose app, you can jump to [RichText UI Material](../richtext-ui-material/index.html)
for quick start. There is also Material3 flavor at [RichText UI Material3](../richtext-ui-material3/index.html)

## Gradle

```kotlin
dependencies {
  implementation("com.halilibo.compose-richtext:richtext-ui:${richtext_version}")
}
```

## [`BasicRichText`](../api/richtext-ui/com.halilibo.richtext.ui/-basic-rich-text.html)

Richtext UI does not depend on Material artifact of Compose. Design agnostic API allows anyone
to adopt RichText UI and its extensions like Markdown to their own design and typography systems.
Hence, just like `foundation` and `material` modules of Compose, this library also names the 
building block with `Basic` prefix.

If you are planning to adopt RichText within your design system, please go ahead and check out [`RichText Material`](../richtext-ui-material/index.html)
for inspiration.

## [`RichTextScope`](../api/richtext-ui/com.halilibo.richtext.ui/-rich-text-scope/index.html)

`RichTextScope` is a context wrapper around composables that integrate and play well within RichText
content. 

## [`RichTextThemeProvider`](../api/richtext-ui/com.halilibo.richtext.ui/-rich-text-theme-provider.html)

Entry point for integrating app's own typography and theme system with BasicRichText.

API for this integration is highly influenced by how compose-material theming
is designed. RichText library assumes that almost all Theme/Design systems would
have composition locals that provide a TextStyle downstream.

Moreover, text style should not include text color by best practice. Content color
exists to figure out text color in the current context. Light/Dark theming leverages content
color to influence not just text but other parts of theming as well.

## Example

Open the `Demo.kt` file in the `sample` module to play with this. Although the mentioned demo
uses Material integrated version of `RichText`, they share exactly the same API.

```kotlin
BasicRichText(
  modifier = Modifier.background(color = Color.White)
) {
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

  Heading(0, "Info Panel")
  InfoPanel(InfoPanelType.Primary, "Only text primary info panel")
  InfoPanel(InfoPanelType.Success) {
    Column {
      Text("Successfully sent some data")
      HorizontalRule()
      BlockQuote {
        Text("This is a quote")
      }
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

![demo rendering](img/richtext-demo.png)
