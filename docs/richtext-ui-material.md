# Richtext UI Material

[![Android Library](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge)](https://developer.android.com/studio/build/dependencies)
[![JVM Library](https://img.shields.io/badge/Platform-JVM-red.svg?style=for-the-badge)](https://kotlinlang.org/docs/mpp-intro.html)

Library that makes RichText compatible with Material design in Compose.

## Gradle

```groovy
dependencies {
  implementation "com.halilibo.compose-richtext:richtext-ui-material:${richtext_version}"
}
```

## Usage

Material RichText library offers 2 different ways of integrating Material design with RichText in your app.

### [`MaterialRichText`](../api/richtext-ui-material/com.halilibo.richtext.ui.material/-material-rich-text.html)

`MaterialRichText` composable wraps around regular `RichText` while introducing the necessary integration
dependencies. `MaterialRichText` shares the exact arguments with regular `RichText`.

```kotlin
MaterialRichText(modifier = Modifier.background(color = Color.White)) {
  Heading(0, "Paragraphs")
  Text("Simple paragraph.")
  ...
}
```

### [`SetupMaterialRichText`](../api/richtext-ui-material/com.halilibo.richtext.ui.material/-setup-material-rich-text.html)

If the whole application is written in Compose or contains large Compose trees, it would be ideal to call this function right after applying the Material Theme.
Then, calling `MaterialRichText` or `RichText` would have no difference.

```kotlin
MaterialTheme(...) {
  SetupMaterialRichText {
    RichText(modifier = Modifier.background(color = Color.White)) {
      Heading(0, "Paragraphs")
      Text("Simple paragraph.")
      ...
    }
  }
}
```
