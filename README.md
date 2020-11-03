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

**Documentation is available at [zachklipp.com/compose-richtext](http://zachklipp.com/compose-richtext).**

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
