# Commonmark Markdown

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

Library for parsing and rendering Markdown in Compose using [CommonMark](https://github.com/commonmark/commonmark-java)
library/spec to parse, and `richtext-ui` to render.

## Gradle

```kotlin
dependencies {
  implementation("com.halilibo.compose-richtext:richtext-commonmark:${richtext_version}")
}
```

## Parsing

`richtext-markdown` module renders a given Markdown Abstract Syntax Tree. It accepts a root 
`AstNode`. This library gives you a parser called `CommonmarkAstNodeParser` to easily convert any 
String to an `AstNode` that represents the Markdown tree.

```kotlin
    val parser = CommonmarkAstNodeParser()
    val astNode = parser.parse(
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
    // ...
  
    RichTextScope.BasicMarkdown(astNode)
```

## Rendering

The simplest way to render markdown is just pass a string to the [`Markdown`](../api/richtext-commonmark/com.halilibo.richtext.markdown/-markdown.html)
composable under RichText scope:

~~~kotlin
RichText(
  modifier = Modifier.padding(16.dp)
) {
  Markdown(
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
    ---

    ```javascript
    var s = "code blocks use monospace font";
    alert(s);
    ```

    Markdown | Table | Extension
    --- | --- | ---
    *renders* | `beautiful images` | ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1")
    1 | 2 | 3

    > Blockquotes are very handy in email to emulate reply text.
    > This line is part of the same quote.
    """.trimIndent()
  )
}
~~~

Which produces something like this:

![markdown demo](img/markdown-demo.png)

## [`MarkdownParseOptions`](../api/richtext-commonmark/com.halilibo.richtext.commonmark/-markdown-parse-options.html)

Passing `MarkdownParseOptions` into either `Markdown` composable or `CommonmarkAstNodeParser.parse` method provides the ability to control some aspects of the markdown parser:

```kotlin
val markdownParseOptions = MarkdownParseOptions(
  autolink = false
)

Markdown(
  markdownParseOptions = markdownParseOptions
)
```
