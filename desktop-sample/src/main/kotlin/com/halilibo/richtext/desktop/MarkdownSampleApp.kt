package com.halilibo.richtext.desktop

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.currentRichTextStyle
import com.halilibo.richtext.ui.material.RichText
import com.halilibo.richtext.ui.resolveDefaults

fun main(): Unit = singleWindowApplication(
  title = "RichText KMP"
) {
  var richTextStyle by remember {
    mutableStateOf(
      RichTextStyle(
        codeBlockStyle = CodeBlockStyle(wordWrap = true)
      ).resolveDefaults()
    )
  }

  Surface {
    CompositionLocalProvider(
      LocalScrollbarStyle provides defaultScrollbarStyle().copy(
        hoverColor = Color.DarkGray,
        unhoverColor = Color.Gray
      )
    ) {
      SelectionContainer {
        val state = rememberTextFieldState(sampleMarkdown)
        Row(
          modifier = Modifier
            .padding(32.dp)
            .fillMaxSize(),
          horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            DisableSelection {
              RichTextStyleConfig(richTextStyle = richTextStyle, onChanged = { richTextStyle = it })
            }
            BasicTextField(
              state = state,
              modifier = Modifier
                .fillMaxHeight()
                .background(Color.LightGray)
                .padding(8.dp)
            )
          }
          var selectedTab by remember { mutableStateOf(0) }
          Column(Modifier.weight(1f)) {
            DisableSelection {
              TabRow(selectedTab) {
                LeadingIconTab(
                  selected = selectedTab == 0,
                  onClick = { selectedTab = 0 },
                  text = { Text("Normal") },
                  icon = { Icon(Icons.Default.Info, "") })
                LeadingIconTab(
                  selected = selectedTab == 1,
                  onClick = { selectedTab = 1 },
                  text = { Text("Lazy") },
                  icon = { Icon(Icons.Default.Favorite, "") })
              }
            }
            ProvidePrintUriHandler {
              if (selectedTab == 0) {
                RichText(
                  modifier = Modifier.verticalScroll(rememberScrollState()),
                  style = richTextStyle,
                ) {
                  Markdown(content = state.text.toString())
                }
              } else {
                val parser = remember { CommonmarkAstNodeParser() }

                val astNode = remember(parser) {
                  parser.parse(sampleMarkdown)
                }

                RichText(
                  style = richTextStyle,
                ) {
                  LazyMarkdown(astNode)
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * A function that renders Markdown content lazily at the top level. All markdown trees start with
 * an AstDocument. If a document is long enough, usually there are more than hundred child nodes
 * under the root. Then in turn rendering the whole content into the internal column of
 * `BasicRichText` becomes extremely inefficient. Instead, this renderer at least relieves the top
 * level rendering by turning the internal column into a LazyColumn. All other nodes below the
 * first level are rendered as usual.
 *
 * @param astNode Root node of Markdown tree. This can be obtained via a parser.
 */
@Composable
fun RichTextScope.LazyMarkdown(astNode: AstNode) {
  require(astNode.type == AstDocument) {
    "Lazy Markdown rendering requires root level node to have a type of AstDocument."
  }
  // keep the same blockSpacing
  val currentStyle = currentRichTextStyle
  val resolvedStyle = remember(currentStyle) { currentStyle.resolveDefaults() }
  val blockSpacing = with(LocalDensity.current) {
    resolvedStyle.paragraphSpacing!!.toDp()
  }
  LazyColumn(verticalArrangement = Arrangement.spacedBy(blockSpacing)) {
    var iter = astNode.links.firstChild
    while (iter != null) {
      // We need to store iter in a final variable because composition of `item` happens after
      // iteration
      val node = iter
      item {
        BasicMarkdown(node)
      }
      iter = iter.links.next
    }
  }
}

@Composable
fun RichTextStyleConfig(
  richTextStyle: RichTextStyle,
  onChanged: (RichTextStyle) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row {
      Column(Modifier.weight(1f)) {
        Text("Paragraph spacing:\n${richTextStyle.paragraphSpacing}")
        Slider(
          value = richTextStyle.paragraphSpacing!!.value,
          valueRange = 0f..20f,
          onValueChange = {
            onChanged(richTextStyle.copy(paragraphSpacing = it.sp))
          }
        )
      }
      Column(Modifier.weight(1f)) {
        Text("List item spacing:\n${richTextStyle.listStyle!!.itemSpacing}")
        Slider(
          value = richTextStyle.listStyle!!.itemSpacing!!.value,
          valueRange = 0f..20f,
          onValueChange = {
            onChanged(
              richTextStyle.copy(
                listStyle = richTextStyle.listStyle!!.copy(
                  itemSpacing = it.sp
                )
              )
            )
          }
        )
      }
    }
    Row {
      Column(Modifier.weight(1f)) {
        Text("Table cell padding:\n${richTextStyle.tableStyle!!.cellPadding}")
        Slider(
          value = richTextStyle.tableStyle!!.cellPadding!!.value,
          valueRange = 0f..20f,
          onValueChange = {
            onChanged(
              richTextStyle.copy(
                tableStyle = richTextStyle.tableStyle!!.copy(
                  cellPadding = it.sp
                )
              )
            )
          }
        )
      }
      Column(Modifier.weight(1f)) {
        Text("Table border width padding:\n${richTextStyle.tableStyle!!.borderStrokeWidth!!}")
        Slider(
          value = richTextStyle.tableStyle!!.borderStrokeWidth!!,
          valueRange = 0f..20f,
          onValueChange = {
            onChanged(
              richTextStyle.copy(
                tableStyle = richTextStyle.tableStyle!!.copy(
                  borderStrokeWidth = it
                )
              )
            )
          }
        )
      }
    }
  }
}

@Composable
fun ProvidePrintUriHandler(content: @Composable () -> Unit) {
  val uriHandler = remember {
    object : UriHandler {
      override fun openUri(uri: String) {
        println("Link clicked destination=$uri")
      }
    }
  }

  CompositionLocalProvider(LocalUriHandler provides uriHandler, content)
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
  
  ## Full-bleed Image
  ![](https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/1920px-Image_created_with_a_mobile_phone.png)

  ## Images smaller than the width should center
  ![](https://cdn.nostr.build/p/4a84.png)
  
  On LineHeight bug, the image below goes over this text. 
  ![](https://cdn.nostr.build/p/PxZ0.jpg)

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
<!-- -->
  2. Ordered list starting with `2.`
  3. Another item
<!-- -->
  0. Ordered list starting with `0.`
<!-- -->
  003. Ordered list starting with `003.`
<!-- -->
  -1. Starting with `-1.` should not be list


  ---

  ## Links

  [I'm an inline-style link](https://www.google.com)

  [I'm a reference-style link][Arbitrary case-insensitive reference text]

  [I'm a relative reference to a repository file](../blob/master/LICENSE)

  [You can use numbers for reference-style link definitions][1]

  Or leave it empty and use the [link text itself].
  
  Autolink option will detect text links like https://www.google.com and turn them into Markdown links automatically.

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
   
  ![random image](https://picsum.photos/seed/picsum/400/400)
  
  ![random image](https://picsum.photos/seed/picsum/400/400 "Text 1")
  
  Reference-style:
   
  ![random image][logo]
  
  [logo]: https://picsum.photos/seed/picsum2/400/400 "Text 2"
  
  Base64 Inline
  
  ![][image1]

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
  
  [image1]: <data:image/png;base64,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>
""".trimIndent()