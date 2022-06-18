# Richtext UI Material3

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

Library that makes RichText compatible with Material3 design in Compose.

## Gradle

```groovy
dependencies {
  implementation "com.halilibo.compose-richtext:richtext-ui-material3:${richtext_version}"
}
```

## Usage

Material3 RichText library offers 2 different ways of integrating Material3 design with RichText in your app.

### [`Material3RichText`](../api/richtext-ui-material3/com.halilibo.richtext.ui.material3/-material3-rich-text.html)

`Material3RichText` composable wraps around regular `RichText` while introducing the necessary integration
dependencies. `Material3RichText` shares the exact arguments with regular `RichText`.

```kotlin
Material3RichText(modifier = Modifier.background(color = Color.White)) {
  Heading(0, "Paragraphs")
  Text("Simple paragraph.")
  ...
}
```

### [`SetupMaterial3RichText`](../api/richtext-ui-material3/com.halilibo.richtext.ui.material3/-setup-material3-rich-text.html)

If the whole application is written in Compose or contains large Compose trees, it would be ideal to call this function right after applying the Material3 Theme.
Then, calling `Material3RichText` or `RichText` would have no difference.

```kotlin
MaterialTheme(...) {
  SetupMaterial3RichText {
    RichText(modifier = Modifier.background(color = Color.White)) {
      Heading(0, "Paragraphs")
      Text("Simple paragraph.")
      ...
    }
  }
}
```
