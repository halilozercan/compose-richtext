# Markdown

Library for rendering Markdown in Compose using Atlassian's [CommonMark](https://github.com/atlassian/commonmark-java)
library to parse, and `richtext-ui` to render.

## Gradle

```groovy
dependencies {
  implementation "com.zachklipp.compose-richtext:richtext-commonmark:${richtext_version}"
}
```

## Usage

The simplest way to render markdown is just pass a string to the [`Markdown`](/api/com.zachklipp.richtext.markdown/-markdown/)
composable:

```kotlin
Markdown(
  """
    # Markdown

    Library for rendering Markdown in Compose using Atlassian's [CommonMark](https://github.com/atlassian/commonmark-java)
    library to parse, and `richtext-ui` to render.

    ## Gradle

    ```groovy
    dependencies {
      implementation "com.zachklipp.compose-richtext:richtext-commonmark:{richtext_version}"
    }
    ```

    ## Usage

    ```kotlin
    // etc.
    ```
  """.trimIndent(),
  Modifier.padding(16.dp)
)
```

Which produces something like this:

![markdown demo](img/markdown-demo.png)

The `Markdown` composable also takes an optional `RichTextStyle` which can be used to customize how
it's rendered.
