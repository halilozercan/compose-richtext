# Markdown

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

Library for rendering Markdown tree that is defined as an `AstNode`. This module would be useless
for someone who is looking to just render a Markdown string. Please take a look to 
`richtext-commonmark` for such features. `richtext-markdown` behaves as sort of a building block.
You can create your own parser or use 3rd party ones that converts any Markdown string to an 
`AstNode` tree.

## Gradle

```kotlin
dependencies {
  implementation("com.halilibo.compose-richtext:richtext-markdown:${richtext_version}")
}
```

## Rendering

The simplest way to render markdown is just pass an `AstNode` to the [`Markdown`](../api/richtext-commonmark/com.halilibo.richtext.markdown/-markdown.html)
composable under RichText scope:

~~~kotlin
RichText(
  modifier = Modifier.padding(16.dp)
) {
  // requires richtext-commonmark module.
  val parser = remember(options) { CommonmarkAstNodeParser(options) }
  val astNode = remember(parser) {
    parser.parse(
      """
        # Demo
        
        Emphasis, aka italics, with *asterisks* or _underscores_. Strong emphasis, aka bold, with **asterisks** or __underscores__. Combined emphasis with **asterisks and _underscores_**. [Links with two blocks, text in square-brackets, destination is in parentheses.](https://www.example.com). Inline `code` has `back-ticks around` it.
        
        1. First ordered list item
        2. Another item
            * Unordered sub-list.
        3. And another item.
            You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).
        
        * Unordered list can use asterisks
        - Or minuses
        + Or pluses
        """.trimIndent()
    )
  }
  BasicMarkdown(astNode)
}
~~~
