package com.halilibo.richtext.ui.util

import androidx.compose.ui.text.AnnotatedString
import com.halilibo.richtext.ui.string.RichTextRenderOptions



public data class PhraseAnnotatedString(
  val annotatedString: AnnotatedString = AnnotatedString(""),
  val phraseSegments: List<Int> = emptyList(),
  val isComplete: Boolean = false
) {
  public fun makeCompletePhraseString(isComplete: Boolean): AnnotatedString {
    val shouldShowFullText = isComplete || phraseSegments.size <= 1
    return when {
      shouldShowFullText -> annotatedString
      else -> annotatedString.subSequence(
        0,
        annotatedString.length.coerceAtMost(phraseSegments.lastOrNull() ?: annotatedString.length)
      )
    }
  }

  public fun hasNewPhrasesFrom(other: PhraseAnnotatedString): Boolean {
    return phraseSegments.lastOrNull() != other.phraseSegments.lastOrNull()
  }
}

public fun AnnotatedString.segmentIntoPhrases(
  renderOptions: RichTextRenderOptions,
  isComplete: Boolean = false,
): PhraseAnnotatedString {
  val markers = (renderOptions.phraseMarkersOverride ?: DefaultPhraseMarkers).toSet()
  val segments = mutableListOf(0)
  val source = text
  val state = BoundaryState()

  source.forEachCodePoint { codePoint, index, charCount ->
    state.record(codePoint)
    if (state.shouldCreateBoundary(source[index], markers, renderOptions.maxPhraseLength)) {
      segments.addBoundary(index + charCount, source.length, state)
    }
  }

  if (isComplete && segments.last() != source.length) {
    segments.addBoundary(source.length, source.length, state)
  }

  return PhraseAnnotatedString(
    annotatedString = this,
    phraseSegments = segments.distinct().filter { it <= source.length },
    isComplete = isComplete,
  )
}

private class BoundaryState {
  var codePointsSinceBoundary: Int = 0
    private set
  var nonAsciiRun: Int = 0
    private set

  private var currentCategory: CodePointCategory = CodePointCategory.ASCII

  fun record(codePoint: Int) {
    val category = categorizeCodePoint(codePoint)
    if (category == CodePointCategory.ASCII) {
      nonAsciiRun = 0
    } else {
      if (category != currentCategory) {
        nonAsciiRun = 0
      }
      nonAsciiRun += 1
    }
    currentCategory = category
    codePointsSinceBoundary += 1
  }

  fun shouldCreateBoundary(currentChar: Char, markers: Set<Char>, maxPhraseLength: Int): Boolean {
    val nonAsciiLimit = when (currentCategory) {
      CodePointCategory.CJK -> CjkNonAsciiRunThreshold
      CodePointCategory.OTHER_NON_ASCII -> OtherNonAsciiRunThreshold
      CodePointCategory.ASCII -> Int.MAX_VALUE
    }
    val nonAsciiExceeded = currentCategory != CodePointCategory.ASCII && nonAsciiRun > nonAsciiLimit
    return currentChar in markers || nonAsciiExceeded || codePointsSinceBoundary >= maxPhraseLength
  }

  fun reset() {
    codePointsSinceBoundary = 0
    nonAsciiRun = 0
    currentCategory = CodePointCategory.ASCII
  }
}

private fun MutableList<Int>.addBoundary(index: Int, sourceLength: Int, state: BoundaryState) {
  if (index > last() && index <= sourceLength) {
    add(index)
  }
  state.reset()
}

private inline fun String.forEachCodePoint(action: (codePoint: Int, index: Int, charCount: Int) -> Unit) {
  var i = 0
  while (i < length) {
    val codePoint = Character.codePointAt(this, i)
    val count = Character.charCount(codePoint)
    action(codePoint, i, count)
    i += count
  }
}

private fun categorizeCodePoint(codePoint: Int): CodePointCategory {
  if (codePoint <= AsciiMaxCodePoint) return CodePointCategory.ASCII
  val block = Character.UnicodeBlock.of(codePoint)
  return if (block in CJK_UNICODE_BLOCKS) CodePointCategory.CJK else CodePointCategory.OTHER_NON_ASCII
}

private val DefaultPhraseMarkers = listOf(
  ' ',    // Space
  '.',    // Period
  '!',    // Exclamation mark
  '?',    // Question mark
  ',',    // Comma
  ';',    // Semicolon
  ':',    // Colon
  '\n',   // Newline
  '\r',   // Carriage return
  '\t',   // Tab
  '…',    // Ellipsis
  '—',    // Em dash
  '·',    // Interpunct
  '¡',    // Inverted exclamation mark
  '¿',    // Inverted question mark
  '。',   // Chinese/Japanese period
  '、',   // Chinese/Japanese comma
  '，',   // Chinese/Japanese full-width comma
  '？',   // Full-width question mark
  '！',   // Full-width exclamation mark
  '：',   // Full-width colon
  '；',   // Full-width semicolon
  '…',    // Ellipsis
  '¡',    // Inverted exclamation mark
  '¿',    // Inverted question mark
  '።',    // Ethiopic full stop
  '፣',    // Ethiopic comma
  '፤',    // Ethiopic semicolon
  '፥',    // Ethiopic colon
  '፦',    // Ethiopic preface colon
  '፧',    // Ethiopic question mark
  '፨'     // Ethiopic paragraph separator
)

private const val AsciiMaxCodePoint = 0x7F
private const val CjkNonAsciiRunThreshold = 5
private const val OtherNonAsciiRunThreshold = 15

private val CJK_UNICODE_BLOCKS = setOf(
  Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
  Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
  Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
  Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
  Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
  Character.UnicodeBlock.CJK_COMPATIBILITY,
  Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS,
  Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
  Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
  Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT,
  Character.UnicodeBlock.KANGXI_RADICALS,
  Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
  Character.UnicodeBlock.HIRAGANA,
  Character.UnicodeBlock.KATAKANA,
  Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS,
  Character.UnicodeBlock.BOPOMOFO,
  Character.UnicodeBlock.BOPOMOFO_EXTENDED,
  Character.UnicodeBlock.HANGUL_SYLLABLES,
  Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
  Character.UnicodeBlock.HANGUL_JAMO,
  Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A,
  Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B
)

private enum class CodePointCategory { ASCII, CJK, OTHER_NON_ASCII }
