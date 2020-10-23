package com.zachklipp.richtext.sample

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.selection.SelectionContainer
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.markdown.Markdown
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.resolveDefaults

@Preview
@Composable private fun MarkdownSamplePreview() {
    MarkdownSample()
}

@Composable fun MarkdownSample() {
    var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }

    val colors = if (isDarkModeEnabled) darkColors() else lightColors()
    val context = ContextAmbient.current

    MaterialTheme(colors = colors) {
        Surface {
            Column {
                // Config
                Card(elevation = 4.dp) {
                    Column {
                        Row(
                            Modifier
                                .clickable(onClick = { isDarkModeEnabled = !isDarkModeEnabled })
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = isDarkModeEnabled,
                                onCheckedChange = { isDarkModeEnabled = it },

                                )
                            Text("Dark Mode")
                        }
                        RichTextStyleConfig(
                            richTextStyle = richTextStyle,
                            onChanged = { richTextStyle = it }
                        )
                    }
                }

                var selection: Selection? by remember { mutableStateOf(null) }
                SelectionContainer(selection = selection, onSelectionChange = { selection = it }) {
                    ScrollableColumn {
                        Markdown(
                            content = sampleMarkdown,
                            style = richTextStyle,
                            modifier = Modifier.padding(8.dp),
                            onLinkClicked = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

private val sampleMarkdown = """
  # Demo
  Based on [this cheatsheet][cheatsheet]

  ---

  ## Headers
  ---
  # Header 1
  ## Header 2
  ### Header 3
  #### Header 4
  ##### Header 5
  ###### Header 6
  ---

  ## Emphasis

  Emphasis, aka italics, with *asterisks* or _underscores_.

  Strong emphasis, aka bold, with **asterisks** or __underscores__.

  Combined emphasis with **asterisks and _underscores_**.

  ---

  ## Lists
  1. First ordered list item
  2. Another item
    * Unordered sub-list.
  1. Actual numbers don't matter, just that it's a number
    1. Ordered sub-list
  4. And another item.

     You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).

     To have a line break without a paragraph, you will need to use two trailing spaces.
     Note that this line is separate, but within the same paragraph.
     (This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)

  * Unordered list can use asterisks
  - Or minuses
  + Or pluses

  ---

  ## Links

  [I'm an inline-style link](https://www.google.com)

  [I'm a reference-style link][Arbitrary case-insensitive reference text]

  [I'm a relative reference to a repository file](../blob/master/LICENSE)

  [You can use numbers for reference-style link definitions][1]

  Or leave it empty and use the [link text itself].

  ---

  ## Code

  Inline `code` has `back-ticks around` it.

  ```javascript
  var s = "JavaScript syntax highlighting";
  alert(s);
  ```

  ```python
  s = "Python syntax highlighting"
  print s
  ```

  ```java
  /**
   * Helper method to obtain a Parser with registered strike-through &amp; table extensions
   * &amp; task lists (added in 1.0.1)
   *
   * @return a Parser instance that is supported by this library
   * @since 1.0.0
   */
  @NonNull
  public static Parser createParser() {
    return new Parser.Builder()
        .extensions(Arrays.asList(
            StrikethroughExtension.create(),
            TablesExtension.create(),
            TaskListExtension.create()
        ))
        .build();
  }
  ```

  ```xml
  <ScrollView
    android:id="@+id/scroll_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginTop="?android:attr/actionBarSize">

    <TextView
      android:id="@+id/text"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_margin="16dip"
      android:lineSpacingExtra="2dip"
      android:textSize="16sp"
      tools:text="yo\nman" />

  </ScrollView>
  ```

  ```
  No language indicated, so no syntax highlighting.
  But let's throw in a <b>tag</b>.
  ```
  
  ---

  ## Images
  
  Inline-style: 
  
  ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1")
  
  Reference-style:
   
  ![random image][logo]
  
  [logo]: https://picsum.photos/seed/picsum2/400/400 "Text 2"

  ---

  ## Tables

  Colons can be used to align columns.

  | Tables        | Are           | Cool  |
  | ------------- |:-------------:| -----:|
  | col 3 is      | right-aligned | ${'$'}1600 |
  | col 2 is      | centered      |   ${'$'}12 |
  | zebra stripes | are neat      |    ${'$'}1 |

  There must be at least 3 dashes separating each header cell.
  The outer pipes (|) are optional, and you don't need to make the
  raw Markdown line up prettily. You can also use inline Markdown.

  Markdown | Less | Pretty
  --- | --- | ---
  *Still* | `renders` | ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1")
  1 | 2 | 3

  ---

  ## Blockquotes

  > Blockquotes are very handy in email to emulate reply text.
  > This line is part of the same quote.

  Quote break.

  > This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.

  Nested quotes
  > Hello!
  >> And to you!

  ---

  ## Inline HTML

  ```html
  <u><i>H<sup>T<sub>M</sub></sup><b><s>L</s></b></i></u>
  ```

  <body><u><i>H<sup>T<sub>M</sub></sup><b><s>L</s></b></i></u></body>

  ---

  ## Horizontal Rule

  Three or more...

  ---

  Hyphens (`-`)

  ***

  Asterisks (`*`)

  ___

  Underscores (`_`)


  ## License

  ```
    Copyright 2019 Dimitry Ivanov (legal@noties.io)

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

  [cheatsheet]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet

  [arbitrary case-insensitive reference text]: https://www.mozilla.org
  [1]: http://slashdot.org
  [link text itself]: http://www.reddit.com
""".trimIndent()