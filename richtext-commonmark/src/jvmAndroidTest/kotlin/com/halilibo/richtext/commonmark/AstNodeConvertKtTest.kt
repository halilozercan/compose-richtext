package com.halilibo.richtext.markdown

import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.commonmark.convert
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstNodeLinks
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.markdown.node.AstText
import org.commonmark.node.Document
import org.commonmark.node.Image
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

internal class AstNodeConvertKtTest {

  private val parser = CommonmarkAstNodeParser(CommonMarkdownParseOptions.Default)

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

  @Test
  fun `tree links are correctly wired for a document with siblings`() {
    val root = parser.parse("# Heading\n\nParagraph text")

    // Root should be a Document
    assertEquals(AstDocument, root.type)
    assertNull(root.links.parent)

    // First child should be the heading
    val heading = root.links.firstChild
    assertNotNull(heading)
    assertEquals(AstHeading(level = 1), heading.type)
    assertSame(root, heading.links.parent)
    assertNull(heading.links.previous)

    // Second child should be the paragraph
    val paragraph = heading.links.next
    assertNotNull(paragraph)
    assertEquals(AstParagraph, paragraph.type)
    assertSame(root, paragraph.links.parent)
    assertSame(heading, paragraph.links.previous)
    assertNull(paragraph.links.next)

    // lastChild should point to the paragraph
    assertSame(paragraph, root.links.lastChild)
  }

  @Test
  fun `tree links are correctly wired for nested structures`() {
    val root = parser.parse("> quoted text")

    val blockquote = root.links.firstChild
    assertNotNull(blockquote)
    assertEquals(AstBlockQuote, blockquote.type)
    assertSame(root, blockquote.links.parent)

    val paragraph = blockquote.links.firstChild
    assertNotNull(paragraph)
    assertEquals(AstParagraph, paragraph.type)
    assertSame(blockquote, paragraph.links.parent)

    val text = paragraph.links.firstChild
    assertNotNull(text)
    assertEquals(AstText(literal = "quoted text"), text.type)
    assertSame(paragraph, text.links.parent)
  }

  @Test
  fun `document with many sibling paragraphs does not overflow`() {
    // 2000 paragraphs would cause ~2000 frames of sibling recursion
    val markdown = (1..2000).joinToString("\n\n") { "Paragraph $it" }
    val root = parser.parse(markdown)

    assertEquals(AstDocument, root.type)

    // Walk the sibling chain and count paragraphs
    var count = 0
    var node = root.links.firstChild
    while (node != null) {
      assertEquals(AstParagraph, node.type)
      count++
      node = node.links.next
    }
    assertEquals(2000, count)

    // lastChild should be the final paragraph
    assertNotNull(root.links.lastChild)
    assertNull(root.links.lastChild!!.links.next)
  }

  @Test
  fun `deeply nested blockquotes do not overflow`() {
    // 500 levels of nested blockquotes would cause ~500 frames of child recursion
    val markdown = ">".repeat(500) + " deep text"
    val root = parser.parse(markdown)

    assertEquals(AstDocument, root.type)

    // Walk down the child chain counting blockquotes
    var depth = 0
    var node = root.links.firstChild
    while (node != null && node.type is AstBlockQuote) {
      depth++
      node = node.links.firstChild
    }
    assertEquals(500, depth)

    // The innermost blockquote should contain a paragraph with text
    assertNotNull(node)
    assertEquals(AstParagraph, node.type)
  }

  /**
   * Proves that the sibling chain depth we test would overflow a recursive implementation
   * on a constrained thread stack (similar to Android's ~1MB default), while our iterative
   * convert() handles it without issue.
   */
  @Test
  fun `convert handles long sibling chains that would overflow a recursive implementation`() {
    val siblingCount = 5000
    // Build a CommonMark tree directly: Document -> Paragraph("1") -> Paragraph("2") -> ...
    val doc = Document()
    for (i in 1..siblingCount) {
      val para = Paragraph()
      para.appendChild(Text("$i"))
      doc.appendChild(para)
    }

    val stackSize = 256L * 1024 // 256KB — smaller than Android's default ~1MB

    // First, prove this stack size is too small for equivalent-depth recursion.
    // A simple recursive chain of siblingCount depth will overflow.
    val recursionOverflowed = AtomicReference<Boolean>(false)
    val recursionThread = Thread(null, {
      try {
        countRecursively(siblingCount)
      } catch (_: StackOverflowError) {
        recursionOverflowed.set(true)
      }
    }, "recursion-test", stackSize)
    recursionThread.start()
    recursionThread.join()
    assertTrue(
      recursionOverflowed.get(),
      "Expected StackOverflowError for $siblingCount recursive calls on ${stackSize / 1024}KB stack"
    )

    // Now prove our iterative convert() handles the same depth on the same stack size.
    val convertError = AtomicReference<Throwable?>(null)
    val convertResult = AtomicReference<AstNode?>(null)
    val convertThread = Thread(null, {
      try {
        convertResult.set(convert(doc))
      } catch (e: Throwable) {
        convertError.set(e)
      }
    }, "convert-test", stackSize)
    convertThread.start()
    convertThread.join()

    val error = convertError.get()
    if (error != null) {
      fail("convert() should not throw on a long sibling chain, but threw: $error")
    }

    val root = convertResult.get()
    assertNotNull(root, "convert() should return a non-null root")
    assertEquals(AstDocument, root.type)

    // Verify the full sibling chain was converted
    var count = 0
    var node = root.links.firstChild
    while (node != null) {
      count++
      node = node.links.next
    }
    assertEquals(siblingCount, count)
  }
}

/** Simple recursive function that recurses [n] times to demonstrate stack overflow. */
private fun countRecursively(n: Int): Int {
  if (n <= 0) return 0
  return 1 + countRecursively(n - 1)
}
