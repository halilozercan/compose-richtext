package com.halilibo.richtext.ui.util

import androidx.compose.ui.text.AnnotatedString

public data class PhraseAnnotatedString(
  val annotatedString: AnnotatedString = AnnotatedString(""),
  val phraseSegments: List<Int> = emptyList(),
  val isComplete: Boolean = false
) {
  public fun makeCompletePhraseString(isComplete: Boolean): AnnotatedString {
    return when {
      isComplete -> annotatedString
      else -> annotatedString.subSequence(0,
        annotatedString.length.coerceAtMost(
          phraseSegments.lastOrNull() ?: annotatedString.length))
    }
  }

  public fun hasNewPhrasesFrom(other: PhraseAnnotatedString): Boolean {
    return phraseSegments.lastOrNull() != other.phraseSegments.lastOrNull()
  }
}

public fun AnnotatedString.segmentIntoPhrases(isComplete: Boolean = false): PhraseAnnotatedString {
  val stylePhrases = stylePhrases()
  val phrases = stylePhrases
    .map { it.split(delimiters = phraseMarkers.toCharArray(), ignoreCase = false) }
    .flatten()
  val phraseSegments = mutableListOf(0)
  for (phrase in phrases) {
    if (phrase != phrases.last()) {
      if (phrase.length > MAX_PHRASE_LENGTH * 1.2) {
        phraseSegments.add(phraseSegments.last() + MAX_PHRASE_LENGTH)
      }
      phraseSegments.add(text.length.coerceAtMost(phraseSegments.last() + phrase.length + 1))
    }
  }
  if (isComplete || phrases.lastOrNull()?.lastOrNull() in phraseMarkers) {
    phraseSegments.add(text.length)
  }
  return PhraseAnnotatedString(
    annotatedString = this,
    phraseSegments = phraseSegments.distinct().filter { it <= text.length },
    isComplete = isComplete,
  )
}

private fun AnnotatedString.stylePhrases(): List<String> {
  val spans = (listOf(0, text.length) + spanStyles.map { listOf(it.start, it.end) }.flatten())
    .sortedBy { it }
    .filter { it <= text.length }
    .distinct()
  return spans.zipWithNext{ firstIndex, secondIndex ->  text.substring(firstIndex, secondIndex) }
}

private val phraseMarkers = listOf(
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

private const val MAX_PHRASE_LENGTH = 50