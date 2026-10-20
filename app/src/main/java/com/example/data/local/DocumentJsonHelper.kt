package com.example.data.local

import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.CalloutType
import com.example.data.model.CommentReply
import com.example.data.model.DocumentBlock
import com.example.data.model.TextAlignment
import org.json.JSONArray
import org.json.JSONObject

object DocumentJsonHelper {

    fun blocksToJson(blocks: List<DocumentBlock>): String {
        val array = JSONArray()
        for (block in blocks) {
            val obj = JSONObject()
            obj.put("id", block.id)
            obj.put("type", block.type.name)
            obj.put("text", block.text)
            obj.put("calloutType", block.calloutType.name)

            val fmt = JSONObject()
            fmt.put("bold", block.formatting.bold)
            fmt.put("italic", block.formatting.italic)
            fmt.put("underline", block.formatting.underline)
            fmt.put("strikethrough", block.formatting.strikethrough)
            fmt.put("fontSizeSp", block.formatting.fontSizeSp)
            fmt.put("fontFamily", block.formatting.fontFamily)
            fmt.put("textColorHex", block.formatting.textColorHex)
            fmt.put("highlightColorHex", block.formatting.highlightColorHex)
            fmt.put("alignment", block.formatting.alignment.name)
            fmt.put("lineSpacing", block.formatting.lineSpacing.toDouble())
            fmt.put("indentLevel", block.formatting.indentLevel)
            fmt.put("isChecked", block.formatting.isChecked)
            obj.put("formatting", fmt)

            val tableArr = JSONArray()
            for (row in block.tableRows) {
                val rowArr = JSONArray()
                for (cell in row) {
                    rowArr.put(cell)
                }
                tableArr.put(rowArr)
            }
            obj.put("tableRows", tableArr)

            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToBlocks(jsonStr: String): List<DocumentBlock> {
        if (jsonStr.isBlank()) return emptyList()
        val blocks = mutableListOf<DocumentBlock>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val typeStr = obj.optString("type", BlockType.PARAGRAPH.name)
                val type = try { BlockType.valueOf(typeStr) } catch (_: Exception) { BlockType.PARAGRAPH }
                val text = obj.optString("text", "")
                val calloutStr = obj.optString("calloutType", CalloutType.INFO.name)
                val calloutType = try { CalloutType.valueOf(calloutStr) } catch (_: Exception) { CalloutType.INFO }

                val fmtObj = obj.optJSONObject("formatting")
                val formatting = if (fmtObj != null) {
                    val alignStr = fmtObj.optString("alignment", TextAlignment.LEFT.name)
                    val alignment = try { TextAlignment.valueOf(alignStr) } catch (_: Exception) { TextAlignment.LEFT }
                    BlockFormatting(
                        bold = fmtObj.optBoolean("bold", false),
                        italic = fmtObj.optBoolean("italic", false),
                        underline = fmtObj.optBoolean("underline", false),
                        strikethrough = fmtObj.optBoolean("strikethrough", false),
                        fontSizeSp = fmtObj.optInt("fontSizeSp", 16),
                        fontFamily = fmtObj.optString("fontFamily", "Calibri"),
                        textColorHex = fmtObj.optString("textColorHex", "#1F242C"),
                        highlightColorHex = fmtObj.optString("highlightColorHex", "#00000000"),
                        alignment = alignment,
                        lineSpacing = fmtObj.optDouble("lineSpacing", 1.15).toFloat(),
                        indentLevel = fmtObj.optInt("indentLevel", 0),
                        isChecked = fmtObj.optBoolean("isChecked", false)
                    )
                } else {
                    BlockFormatting()
                }

                val tableRows = mutableListOf<List<String>>()
                val tableArr = obj.optJSONArray("tableRows")
                if (tableArr != null) {
                    for (r in 0 until tableArr.length()) {
                        val rowArr = tableArr.getJSONArray(r)
                        val row = mutableListOf<String>()
                        for (c in 0 until rowArr.length()) {
                            row.add(rowArr.getString(c))
                        }
                        tableRows.add(row)
                    }
                }

                blocks.add(
                    DocumentBlock(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        type = type,
                        text = text,
                        formatting = formatting,
                        tableRows = tableRows,
                        calloutType = calloutType
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return blocks
    }

    fun repliesToJson(replies: List<CommentReply>): String {
        val array = JSONArray()
        for (reply in replies) {
            val obj = JSONObject()
            obj.put("id", reply.id)
            obj.put("authorName", reply.authorName)
            obj.put("authorColorHex", reply.authorColorHex)
            obj.put("content", reply.content)
            obj.put("timestamp", reply.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToReplies(jsonStr: String): List<CommentReply> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<CommentReply>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CommentReply(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        authorName = obj.optString("authorName", "Collaborator"),
                        authorColorHex = obj.optString("authorColorHex", "#185ABD"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
