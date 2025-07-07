# Compose Markdown and Rich Text

[![Maven Central](https://img.shields.io/maven-central/v/com.halilibo.compose-richtext/richtext-ui.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.halilibo.compose-richtext%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

> **Warning**
> compose-richtext library and all its modules are very experimental. The roadmap is unclear at the moment. Thanks for your patience. Fork option is available as always.

A collection of Compose libraries for working with Markdown rendering and rich text formatting.

All modules are Compose Multiplatform compatible but lacks iOS support.

----

**Documentation is available at [halilibo.com/compose-richtext](https://halilibo.com/compose-richtext).**

----

```kotlin
@Composable fun App() {
  RichText(Modifier.background(color = Color.LightGray)) {
    Heading(0, "Title")
    Text("Summary paragraph.")

    HorizontalRule()

    BlockQuote {
      Text("A wise person once said…")
    }
    
    Markdown("**Hello** `World`")
  }
}
```

## License
```
Copyright 2025 Halil Ozercan

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
