package com.zachklipp.richtext.adf.model

import com.google.gson.Gson
import com.zachklipp.richtext.adf.model.AdfTableUnit.AdfTableCell
import com.zachklipp.richtext.adf.model.AdfTableUnit.AdfTableHeader

internal object AdfParser {

  fun parse(content: String) : AdfDocument {
    val document = Gson().fromJson(content, Map::class.java) as Map<String, Any?>
    // top level must be "doc"
    require(document["type"] == "doc") {
      "Document does not start with doc node"
    }

    require("content" in document && "version" in document) {
      "Document node must have content and version"
    }

    return AdfDocument(
      content = (document["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
      version = (document["version"] as Double).toInt(),
    )
  }

  private fun parseRecursively(node: Map<String, Any>) : AdfNode {
    require("type" in node) { "Every AdfNode must have type" }

    return when(node["type"] as String) {
      "text" -> {
        AdfText(
          text = node["text"] as String,
          marks = parseMarks(node["marks"] as List<Any>?) ?: emptyList()
        )
      }
      "mention" -> {
        val attrs = (node["attrs"] as Map<String, Any?>)
        AdfMention(
          text = attrs["text"] as String?,
          id = attrs["id"] as String,
          accessLevel = (attrs["accessLevel"] as String?)?.takeIf { it.isNotBlank() }?.let { MentionAccessLevel.valueOf(it) },
          userType = (attrs["userType"] as String?)?.takeIf { it.isNotBlank() }?.let { MentionUserType.valueOf(it) }
        )
      }
      "hardBreak" -> {
        AdfHardBreak()
      }
      "paragraph" -> {
        AdfParagraph(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) }
        )
      }
      "heading" -> {
        AdfHeading(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
          level = ((node["attrs"] as Map<String, Any>)["level"] as Double).toInt()
        )
      }
      "blockquote" -> {
        AdfBlockQuote(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
        )
      }
      "codeBlock" -> {
        AdfCodeBlock(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) as AdfText },
          language = ((node["attrs"] as Map<String, Any>)["language"] as String?)
        )
      }
      "panel" -> {
        AdfPanelBlock(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
          panelType = PanelType.valueOf((node["attrs"] as Map<String, Any>)["panelType"] as String)
        )
      }
      "bulletList" -> {
        AdfBulletList(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) as AdfListItem },
        )
      }
      "orderedList" -> {
        AdfOrderedList(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) as AdfListItem },
        )
      }
      "listItem" -> {
        AdfListItem(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
        )
      }
      "table" -> {
        AdfTable(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) as AdfTableRow },
          isNumberColumnEnabled = (node["attrs"] as Map<String, Any>)["isNumberColumnEnabled"] as Boolean,
          layout = (node["attrs"] as Map<String, Any>)["layout"] as String
        )
      }
      "tableRow" -> {
        AdfTableRow(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) as AdfTableUnit },
        )
      }
      "tableHeader" -> {
        AdfTableHeader(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
        )
      }
      "tableCell" -> {
        AdfTableCell(
          content = (node["content"] as List<Any>).map { parseRecursively(it as Map<String, Any>) },
        )
      }
      "rule" -> AdfRule
      else -> error("Unknown type")
    }
  }

  private fun parseMarks(marks: List<Any>?) : List<AdfMark>? = marks?.map {
    val mark = it as Map<String, Any>
    require("type" in mark) { "Every AdfMark must have type" }

    when(it["type"]!!) {
      "code" -> AdfCodeMark
      "em" -> AdfEmMark
      "strike" -> AdfStrikeMark
      "strong" -> AdfStrongMark
      "subsup" -> {
        val type = (it["attrs"] as Map<String, Any>)["type"] as String
        AdfSubSupMark(type = AdfSubSupMarkType.valueOf(type))
      }
      "underline" -> AdfUnderlineMark
      "textcolor" -> {
        val color = (it["attrs"] as Map<String, Any>)["color"] as String
        AdfTextColorMark(color)
      }
      "link" -> {
        AdfLinkMark(
          href = (it["attrs"] as Map<String, Any>)["href"] as String
        )
      }
      else -> AdfStrongMark
    }
  }
}
