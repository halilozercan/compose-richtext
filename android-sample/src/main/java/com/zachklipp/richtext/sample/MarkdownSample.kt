package com.zachklipp.richtext.sample

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstBlockNodeType
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.resolveDefaults

@Preview
@Composable private fun MarkdownSamplePreview() {
  MarkdownSample()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun MarkdownSample() {
  var richTextStyle by remember { mutableStateOf(RichTextStyle().resolveDefaults()) }
  var isDarkModeEnabled by remember { mutableStateOf(false) }
  var isWordWrapEnabled by remember { mutableStateOf(true) }
  var markdownParseOptions by remember { mutableStateOf(CommonMarkdownParseOptions.Default) }
  var isAutolinkEnabled by remember { mutableStateOf(true) }
  var isRtl by remember { mutableStateOf(false) }

  LaunchedEffect(isWordWrapEnabled) {
    richTextStyle = richTextStyle.copy(
      codeBlockStyle = richTextStyle.codeBlockStyle!!.copy(
        wordWrap = isWordWrapEnabled
      )
    )
  }
  LaunchedEffect(isAutolinkEnabled) {
    markdownParseOptions = markdownParseOptions.copy(
      autolink = isAutolinkEnabled
    )
  }

  val colors = if (isDarkModeEnabled) darkColorScheme() else lightColorScheme()
  val context = LocalContext.current

  CompositionLocalProvider(
    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
  ) {
    SampleTheme(colorScheme = colors) {
      Surface {
        Column {
          // Config
          Card(elevation = CardDefaults.elevatedCardElevation()) {
            Column {
              FlowRow {
                CheckboxPreference(
                  onClick = {
                    isDarkModeEnabled = !isDarkModeEnabled
                  },
                  checked = isDarkModeEnabled,
                  label = "Dark Mode"
                )
                CheckboxPreference(
                  onClick = {
                    isWordWrapEnabled = !isWordWrapEnabled
                  },
                  checked = isWordWrapEnabled,
                  label = "Word Wrap"
                )
                CheckboxPreference(
                  onClick = {
                    isAutolinkEnabled = !isAutolinkEnabled
                  },
                  checked = isAutolinkEnabled,
                  label = "Autolink"
                )
                CheckboxPreference(
                  onClick = {
                    isRtl = !isRtl
                  },
                  checked = isRtl,
                  label = "RTL Layout"
                )
              }

              RichTextStyleConfig(
                richTextStyle = richTextStyle,
                onChanged = { richTextStyle = it }
              )
            }
          }

          SelectionContainer {
            Column(Modifier.verticalScroll(rememberScrollState())) {
              val parser = remember(markdownParseOptions) {
                CommonmarkAstNodeParser(markdownParseOptions)
              }

              val astNode = remember(parser) {
                parser.parse(sampleMarkdown)
              }

              RichText(
                style = richTextStyle,
                linkClickHandler = {
                  Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(8.dp),
              ) {
                BasicMarkdown(astNode, HeadingAstBlockNodeComposer)
              }
            }
          }
        }
      }
    }
  }
}

val HeadingAstBlockNodeComposer = object : AstBlockNodeComposer {
  override fun predicate(astBlockNodeType: AstBlockNodeType): Boolean {
    return astBlockNodeType is AstHeading
  }

  @Composable override fun RichTextScope.Compose(
    astNode: AstNode,
    visitChildren: @Composable (AstNode) -> Unit
  ) {
    val headingNode = astNode.type as? AstHeading ?: return
    Column {
      Heading(level = headingNode.level) {
        visitChildren(astNode)
      }
      Text("Custom rendering is used for this heading!", fontSize = 8.sp)
    }
  }
}

@Composable
private fun CheckboxPreference(
  onClick: () -> Unit,
  checked: Boolean,
  label: String
) {
  Row(
    Modifier.clickable(onClick = onClick),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = { onClick() },
    )
    Text(label)
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