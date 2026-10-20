package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TextAlignment
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark

enum class RichSpanType {
    BOLD,
    ITALIC,
    UNDERLINE,
    FONT_FAMILY
}

data class RichSpan(
    val start: Int,
    val end: Int,
    val type: RichSpanType,
    val value: String = ""
)

data class RichTextSnapshot(
    val textFieldValue: TextFieldValue,
    val spans: List<RichSpan>,
    val alignment: TextAlignment
)

class RichTextState(
    initialText: String = "",
    initialSpans: List<RichSpan> = emptyList(),
    initialAlignment: TextAlignment = TextAlignment.LEFT
) {
    private val undoStack = mutableListOf<RichTextSnapshot>()
    private val redoStack = mutableListOf<RichTextSnapshot>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    var textFieldValue by mutableStateOf(
        TextFieldValue(
            annotatedString = buildAnnotatedString(initialText, initialSpans),
            selection = TextRange(initialText.length)
        )
    )
        private set

    var spans by mutableStateOf(initialSpans)
        private set

    var alignment by mutableStateOf(initialAlignment)
        private set

    private var lastSnapshotText = initialText
    private var lastEditTime = 0L

    val isBoldActive: Boolean
        get() = isStyleActiveForSelection(RichSpanType.BOLD)

    val isItalicActive: Boolean
        get() = isStyleActiveForSelection(RichSpanType.ITALIC)

    val isUnderlineActive: Boolean
        get() = isStyleActiveForSelection(RichSpanType.UNDERLINE)

    val currentFontFamily: String
        get() {
            val selection = textFieldValue.selection
            val cursor = if (selection.collapsed) selection.start else selection.min
            val span = spans.findLast {
                it.type == RichSpanType.FONT_FAMILY && it.start <= cursor && it.end >= cursor
            }
            return span?.value ?: "Calibri"
        }

    private fun pushUndoSnapshot(snapshot: RichTextSnapshot) {
        undoStack.add(snapshot)
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        canUndo = true
        canRedo = false
        lastSnapshotText = snapshot.textFieldValue.text
    }

    /**
     * Reverts recent text, formatting, or alignment changes from the undo history stack.
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(RichTextSnapshot(textFieldValue, spans, alignment))
        val previous = undoStack.removeAt(undoStack.lastIndex)
        textFieldValue = previous.textFieldValue
        spans = previous.spans
        alignment = previous.alignment
        lastSnapshotText = previous.textFieldValue.text
        canUndo = undoStack.isNotEmpty()
        canRedo = true
    }

    /**
     * Re-applies recent reverted changes from the redo history stack.
     */
    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(RichTextSnapshot(textFieldValue, spans, alignment))
        val next = redoStack.removeAt(redoStack.lastIndex)
        textFieldValue = next.textFieldValue
        spans = next.spans
        alignment = next.alignment
        lastSnapshotText = next.textFieldValue.text
        canUndo = true
        canRedo = redoStack.isNotEmpty()
    }

    fun updateAlignment(align: TextAlignment) {
        if (alignment == align) return
        pushUndoSnapshot(RichTextSnapshot(textFieldValue, spans, alignment))
        alignment = align
    }

    /**
     * Applies a chosen font family to the selected text range, or the current word at cursor.
     */
    fun applyFontFamily(fontFamily: String) {
        val selection = textFieldValue.selection
        val currentText = textFieldValue.text
        if (currentText.isEmpty()) return

        val targetRange = if (selection.collapsed) {
            getWordRangeAtCursor(currentText, selection.start)
        } else {
            TextRange(selection.min, selection.max)
        }

        if (targetRange.start == targetRange.end) return

        pushUndoSnapshot(RichTextSnapshot(textFieldValue, spans, alignment))

        // Remove any existing FONT_FAMILY spans overlapping target range
        val filteredSpans = spans.filterNot {
            it.type == RichSpanType.FONT_FAMILY && it.start < targetRange.max && it.end > targetRange.min
        }

        spans = filteredSpans + RichSpan(
            start = targetRange.start,
            end = targetRange.end,
            type = RichSpanType.FONT_FAMILY,
            value = fontFamily
        )

        val updatedAnnotatedString = buildAnnotatedString(currentText, spans)
        textFieldValue = textFieldValue.copy(annotatedString = updatedAnnotatedString)
    }

    fun onValueChange(newValue: TextFieldValue) {
        val oldText = textFieldValue.text
        val newText = newValue.text

        if (oldText != newText) {
            val now = System.currentTimeMillis()
            val isWordBoundary = newText.length > oldText.length &&
                (newText.lastOrNull()?.isWhitespace() == true || newText.endsWith(".") || newText.endsWith(","))
            val isDeletion = newText.length < oldText.length
            val isCutOrPaste = kotlin.math.abs(newText.length - oldText.length) > 1
            val isTimeGap = (now - lastEditTime) > 1000L

            if (undoStack.isEmpty() || isWordBoundary || isDeletion || isCutOrPaste || isTimeGap) {
                pushUndoSnapshot(RichTextSnapshot(textFieldValue, spans, alignment))
            }
            lastEditTime = now

            // Text changed: adjust spans based on edit delta
            val diff = newText.length - oldText.length
            val editStart = minOf(textFieldValue.selection.min, newValue.selection.min)

            spans = spans.mapNotNull { span ->
                when {
                    // Span before edit: unaffected
                    span.end <= editStart -> span
                    // Span after edit: shifted by diff
                    span.start >= editStart -> {
                        val newStart = (span.start + diff).coerceAtLeast(0)
                        val newEnd = (span.end + diff).coerceAtLeast(newStart)
                        if (newStart < newEnd && newEnd <= newText.length) {
                            span.copy(start = newStart, end = newEnd)
                        } else null
                    }
                    // Edit happened inside span: expand or shrink
                    else -> {
                        val newEnd = (span.end + diff).coerceAtLeast(span.start)
                        if (span.start < newEnd && newEnd <= newText.length) {
                            span.copy(end = newEnd)
                        } else null
                    }
                }
            }
        }

        val updatedAnnotatedString = buildAnnotatedString(newText, spans)
        textFieldValue = newValue.copy(annotatedString = updatedAnnotatedString)
    }

    fun toggleBold() {
        toggleStyle(RichSpanType.BOLD)
    }

    fun toggleItalic() {
        toggleStyle(RichSpanType.ITALIC)
    }

    fun toggleUnderline() {
        toggleStyle(RichSpanType.UNDERLINE)
    }

    private fun toggleStyle(type: RichSpanType) {
        val selection = textFieldValue.selection
        val currentText = textFieldValue.text
        if (currentText.isEmpty()) return

        // If no text is explicitly selected, apply to the word surrounding the cursor
        val targetRange = if (selection.collapsed) {
            getWordRangeAtCursor(currentText, selection.start)
        } else {
            TextRange(selection.min, selection.max)
        }

        if (targetRange.start == targetRange.end) return

        pushUndoSnapshot(RichTextSnapshot(textFieldValue, spans, alignment))

        val overlappingSpan = spans.find {
            it.type == type && it.start <= targetRange.start && it.end >= targetRange.end
        }

        spans = if (overlappingSpan != null) {
            // Remove or split existing span
            spans.filterNot { it == overlappingSpan }
        } else {
            // Add new span
            val mergedSpans = spans.filterNot {
                it.type == type && it.start >= targetRange.start && it.end <= targetRange.end
            }
            mergedSpans + RichSpan(targetRange.start, targetRange.end, type)
        }

        val updatedAnnotatedString = buildAnnotatedString(currentText, spans)
        textFieldValue = textFieldValue.copy(annotatedString = updatedAnnotatedString)
    }

    fun clearFormatting() {
        if (spans.isEmpty()) return
        pushUndoSnapshot(RichTextSnapshot(textFieldValue, spans, alignment))
        val selection = textFieldValue.selection
        spans = if (selection.collapsed) {
            emptyList()
        } else {
            spans.filterNot { it.start < selection.max && it.end > selection.min }
        }
        val updatedAnnotatedString = buildAnnotatedString(textFieldValue.text, spans)
        textFieldValue = textFieldValue.copy(annotatedString = updatedAnnotatedString)
    }

    private fun isStyleActiveForSelection(type: RichSpanType): Boolean {
        val selection = textFieldValue.selection
        if (selection.collapsed) {
            val cursor = selection.start
            return spans.any { it.type == type && it.start <= cursor && it.end >= cursor }
        }
        return spans.any { it.type == type && it.start <= selection.min && it.end >= selection.max }
    }

    private fun getWordRangeAtCursor(text: String, cursor: Int): TextRange {
        if (text.isEmpty()) return TextRange(0, 0)
        var start = cursor.coerceIn(0, text.length)
        var end = cursor.coerceIn(0, text.length)

        while (start > 0 && !text[start - 1].isWhitespace()) {
            start--
        }
        while (end < text.length && !text[end].isWhitespace()) {
            end++
        }
        return TextRange(start, end)
    }

    companion object {
        fun buildAnnotatedString(text: String, spans: List<RichSpan>): AnnotatedString {
            val builder = AnnotatedString.Builder(text)
            spans.forEach { span ->
                val safeStart = span.start.coerceIn(0, text.length)
                val safeEnd = span.end.coerceIn(0, text.length)
                if (safeStart < safeEnd) {
                    val spanStyle = when (span.type) {
                        RichSpanType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                        RichSpanType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                        RichSpanType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                        RichSpanType.FONT_FAMILY -> {
                            val family = when (span.value.lowercase()) {
                                "georgia", "times new roman", "serif" -> FontFamily.Serif
                                "courier new", "monospace", "consolas" -> FontFamily.Monospace
                                else -> FontFamily.Default
                            }
                            SpanStyle(fontFamily = family)
                        }
                    }
                    builder.addStyle(spanStyle, safeStart, safeEnd)
                }
            }
            return builder.toAnnotatedString()
        }
    }
}

class RichTextVisualTransformation(
    private val state: RichTextState,
    private val searchQuery: String = ""
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = RichTextState.buildAnnotatedString(text.text, state.spans)
        if (searchQuery.isBlank()) {
            return TransformedText(styled, OffsetMapping.Identity)
        }
        val originalText = text.text
        val builder = AnnotatedString.Builder(styled)
        var startIndex = 0
        while (startIndex < originalText.length) {
            val index = originalText.indexOf(searchQuery, startIndex, ignoreCase = true)
            if (index == -1) break
            val endIndex = (index + searchQuery.length).coerceAtMost(originalText.length)
            builder.addStyle(
                SpanStyle(
                    background = Color(0xFFFFF176),
                    color = Color(0xFF1F242C),
                    fontWeight = FontWeight.Bold
                ),
                index,
                endIndex
            )
            startIndex = index + searchQuery.length.coerceAtLeast(1)
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

/**
 * Basic Rich Text Editor component using TextField with AnnotatedString
 * to support font family selection, bold, italic, and underline styling for user content,
 * left, center, right, and justify text alignment, undo / redo history, and search occurrence highlighting.
 */
@Composable
fun RichTextEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    placeholder: String = "Type rich text here... Select words and tap Bold, Italic, or Underline.",
    minLines: Int = 3,
    searchQuery: String = ""
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }
    val availableFonts = listOf("Calibri", "Arial", "Georgia", "Times New Roman", "Courier New", "Roboto")

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0D7DE)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Formatting Toolbar with Undo, Redo, Font Family Dropdown, Style, and Alignments
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F8FA))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Undo Button
                RichFormatButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    isActive = false,
                    enabled = state.canUndo,
                    onClick = { state.undo() },
                    contentDescription = "Undo recent change",
                    testTag = "rich_undo_button"
                )

                // Redo Button
                RichFormatButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    isActive = false,
                    enabled = state.canRedo,
                    onClick = { state.redo() },
                    contentDescription = "Redo recent change",
                    testTag = "rich_redo_button"
                )

                // Toolbar Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color(0xFFD0D7DE))
                )

                // Font Family Dropdown Menu Button
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(6.dp))
                            .clickable { fontMenuExpanded = true }
                            .padding(horizontal = 8.dp)
                            .testTag("rich_font_family_button")
                    ) {
                        Text(
                            text = state.currentFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F242C)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Font Family Menu",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = fontMenuExpanded,
                        onDismissRequest = { fontMenuExpanded = false }
                    ) {
                        availableFonts.forEach { fontName ->
                            val isSelected = fontName.equals(state.currentFontFamily, ignoreCase = true)
                            val previewFont = when (fontName.lowercase()) {
                                "georgia", "times new roman" -> FontFamily.Serif
                                "courier new" -> FontFamily.Monospace
                                else -> FontFamily.Default
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = fontName,
                                            fontFamily = previewFont,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) WordPrimary else Color(0xFF1F242C),
                                            fontSize = 13.sp
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = WordPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    state.applyFontFamily(fontName)
                                    fontMenuExpanded = false
                                },
                                modifier = Modifier.testTag("rich_font_option_${fontName.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Toolbar Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color(0xFFD0D7DE))
                )

                Text(
                    text = "Style:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(end = 2.dp)
                )

                // Bold Button
                RichFormatButton(
                    icon = Icons.Default.FormatBold,
                    isActive = state.isBoldActive,
                    onClick = { state.toggleBold() },
                    contentDescription = "Bold",
                    testTag = "rich_bold_button"
                )

                // Italic Button
                RichFormatButton(
                    icon = Icons.Default.FormatItalic,
                    isActive = state.isItalicActive,
                    onClick = { state.toggleItalic() },
                    contentDescription = "Italic",
                    testTag = "rich_italic_button"
                )

                // Underline Button
                RichFormatButton(
                    icon = Icons.Default.FormatUnderlined,
                    isActive = state.isUnderlineActive,
                    onClick = { state.toggleUnderline() },
                    contentDescription = "Underline",
                    testTag = "rich_underline_button"
                )

                // Clear Formatting Button
                IconButton(
                    onClick = { state.clearFormatting() },
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("rich_clear_button")
                ) {
                    Icon(
                        Icons.Default.FormatClear,
                        contentDescription = "Clear Spans",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Toolbar Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color(0xFFD0D7DE))
                )

                Text(
                    text = "Align:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(end = 2.dp)
                )

                // Align Left Button
                RichFormatButton(
                    icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                    isActive = state.alignment == TextAlignment.LEFT,
                    onClick = { state.updateAlignment(TextAlignment.LEFT) },
                    contentDescription = "Align Left",
                    testTag = "rich_align_left_button"
                )

                // Align Center Button
                RichFormatButton(
                    icon = Icons.Default.FormatAlignCenter,
                    isActive = state.alignment == TextAlignment.CENTER,
                    onClick = { state.updateAlignment(TextAlignment.CENTER) },
                    contentDescription = "Align Center",
                    testTag = "rich_align_center_button"
                )

                // Align Right Button
                RichFormatButton(
                    icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                    isActive = state.alignment == TextAlignment.RIGHT,
                    onClick = { state.updateAlignment(TextAlignment.RIGHT) },
                    contentDescription = "Align Right",
                    testTag = "rich_align_right_button"
                )

                // Align Justify Button
                RichFormatButton(
                    icon = Icons.Default.FormatAlignJustify,
                    isActive = state.alignment == TextAlignment.JUSTIFY,
                    onClick = { state.updateAlignment(TextAlignment.JUSTIFY) },
                    contentDescription = "Justify",
                    testTag = "rich_align_justify_button"
                )

                // Selection Info Pill if text selected
                val sel = state.textFieldValue.selection
                if (!sel.collapsed) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${sel.max - sel.min} chars",
                        fontSize = 10.sp,
                        color = WordPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8F0FE))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Map TextAlignment to Compose TextAlign
            val textAlign = when (state.alignment) {
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.RIGHT -> TextAlign.Right
                TextAlignment.JUSTIFY -> TextAlign.Justify
                else -> TextAlign.Left
            }

            // Rich TextField with AnnotatedString
            TextField(
                value = state.textFieldValue,
                onValueChange = { state.onValueChange(it) },
                visualTransformation = remember(state.spans, searchQuery) {
                    RichTextVisualTransformation(state, searchQuery)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = textAlign,
                    lineHeight = 22.sp
                ),
                placeholder = {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                minLines = minLines,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rich_text_field")
            )
        }
    }
}

@Composable
private fun RichFormatButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    testTag: String,
    enabled: Boolean = true
) {
    val bgColor = when {
        !enabled -> Color(0xFFF3F4F6)
        isActive -> Color(0xFFD2E3FC)
        else -> Color.White
    }
    val borderColor = when {
        !enabled -> Color(0xFFE5E7EB)
        isActive -> WordPrimary
        else -> Color(0xFFD0D7DE)
    }
    val iconTint = when {
        !enabled -> Color(0xFF9CA3AF)
        isActive -> WordPrimaryDark
        else -> Color(0xFF374151)
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
    }
}
