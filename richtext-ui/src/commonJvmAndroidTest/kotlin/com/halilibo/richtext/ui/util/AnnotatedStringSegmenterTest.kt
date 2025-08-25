package com.halilibo.richtext.ui.util

import androidx.compose.ui.text.AnnotatedString
import com.halilibo.richtext.ui.string.RichTextRenderOptions
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotatedStringSegmenterTest {

  @Test
  fun cjkSegmentsAfterFiveCharacters() {
    val text = "天地玄黄宇宙洪荒日月盈昃" // 12 CJK characters
    val result = AnnotatedString(text).segmentIntoPhrases(
      renderOptions = RichTextRenderOptions(maxPhraseLength = 100),
      isComplete = true
    )
    assertEquals(listOf(0, 6, text.length), result.phraseSegments)
  }

  @Test
  fun thaiSegmentsAfterFifteenCharacters() {
    val text = "ภาษาไทยไม่มีเว้นวรรคและทดสอบยาว" // > 15 Thai characters
    val result = AnnotatedString(text).segmentIntoPhrases(
      renderOptions = RichTextRenderOptions(maxPhraseLength = 100),
      isComplete = true
    )
    assertEquals(listOf(0, 16, text.length), result.phraseSegments)
  }

  @Test
  fun hindiSegmentsAfterFifteenCharacters() {
    val text = "यहपाठदेवनागरीलिपिमेंहैकॉफायलंबाऔरबिनाविरामचिह्न" // > 15 Devanagari characters
    val result = AnnotatedString(text).segmentIntoPhrases(
      renderOptions = RichTextRenderOptions(maxPhraseLength = 100),
      isComplete = true
    )
    assertEquals(listOf(0, 16, text.length), result.phraseSegments)
  }
}
