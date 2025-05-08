# Richtext UI Material 3

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

Library that makes RichText compatible with Material design in Compose.

## Gradle

```kotlin
dependencies {
  implementation("com.halilibo.compose-richtext:richtext-ui-material3:${richtext_version}")
}
```

## Usage

Material3 RichText library provides a single composable called `RichText` which automatically passes
down Material3 theming attributes to `BasicRichText`.

### [`RichText`](../api/richtext-ui-material/com.halilibo.richtext.ui.material3/-rich-text.html)

`RichText` composable wraps around regular `BasicRichText` while introducing the necessary integration
dependencies. `RichText` shares the exact arguments with regular `BasicRichText`.

```kotlin
RichText(modifier = Modifier.background(color = Color.White)) {
  Heading(0, "Paragraphs")
  Text("Simple paragraph.")
  ...
}
```
