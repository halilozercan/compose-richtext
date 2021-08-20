# Richtext UI Material

Library that makes RichText compatible with Material design in Compose.

## Gradle

```groovy
dependencies {
  implementation "com.halilibo.compose-richtext:richtext-ui-material:${richtext_version}"
}
```

## Usage

Material RichText library offers 2 different ways of integrating Material design with RichText in your app.

### MaterialRichText

`MaterialRichText` composable wraps around regular `RichText` while introducing the necessary integration
dependencies. `MaterialRichText` shares the exact arguments with regular `RichText`.

```kotlin
MaterialRichText(modifier = Modifier.background(color = Color.White)) {
  Heading(0, "Paragraphs")
  Text("Simple paragraph.")
  ...
}
```

### SetupMaterialRichText

If the whole application is written in Compose or contains large Compose trees, it would be ideal to call this function right after applying the Material Theme.
Then, calling `MaterialRichText` or `RichText` would have no difference.

```kotlin
SetupMaterialRichText {
  RichText(modifier = Modifier.background(color = Color.White)) {
    Heading(0, "Paragraphs")
    Text("Simple paragraph.")
    ...
  }
}
```
