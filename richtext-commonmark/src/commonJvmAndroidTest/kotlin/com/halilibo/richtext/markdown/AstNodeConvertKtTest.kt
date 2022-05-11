package com.halilibo.richtext.markdown

import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstNodeLinks
import org.commonmark.node.Image
import org.junit.Test
import kotlin.test.assertEquals

internal class AstNodeConvertKtTest {

  @Test
  fun `when image without title is converted, then the content description is empty`() {
    val destination = "/url"
    val image = Image(destination, null)

    val result = convert(image)

    assertEquals(
      expected = AstNode(
        type = AstImage(title = "", destination = destination),
        links = AstNodeLinks()
      ),
      actual = result
    )
  }
}
