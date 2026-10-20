package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.CalloutType
import com.example.data.model.Collaborator
import com.example.data.model.DocumentBlock
import com.example.data.model.TextAlignment
import com.example.data.model.ViewMode
import com.example.ui.theme.WordCanvasBg
import com.example.ui.theme.WordPaper
import com.example.ui.theme.WordPaperBorder
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark

@Composable
fun DocumentCanvas(
    blocks: List<DocumentBlock>,
    activeBlockId: String?,
    onSelectBlock: (String) -> Unit,
    onUpdateBlockText: (String, String) -> Unit,
    onToggleChecklist: (String) -> Unit,
    onUpdateTableCell: (String, Int, Int, String) -> Unit,
    onAddTableRow: (String) -> Unit,
    onAddTableColumn: (String) -> Unit,
    onDeleteBlock: (String) -> Unit,
    onMoveBlock: (String, Boolean) -> Unit,
    onInsertBlockAfter: (BlockType, String) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit = {},
    collaborators: List<Collaborator>,
    isLiveCollabActive: Boolean,
    viewMode: ViewMode,
    zoomPercent: Int,
    marginType: String,
    watermarkText: String,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val pagePadding = when (marginType) {
        "NARROW" -> 16.dp
        "WIDE" -> 36.dp
        else -> 24.dp // NORMAL
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (viewMode == ViewMode.PRINT_LAYOUT) WordCanvasBg else WordPaper)
            .verticalScroll(scrollState),
        contentAlignment = Alignment.TopCenter
    ) {
        // Document Page Container
        Surface(
            color = WordPaper,
            shadowElevation = if (viewMode == ViewMode.PRINT_LAYOUT) 4.dp else 0.dp,
            shape = if (viewMode == ViewMode.PRINT_LAYOUT) RoundedCornerShape(4.dp) else RoundedCornerShape(0.dp),
            modifier = Modifier
                .padding(vertical = if (viewMode == ViewMode.PRINT_LAYOUT) 16.dp else 0.dp)
                .widthIn(max = 680.dp)
                .fillMaxWidth(if (viewMode == ViewMode.PRINT_LAYOUT) 0.94f else 1f)
                .border(
                    width = if (viewMode == ViewMode.PRINT_LAYOUT) 1.dp else 0.dp,
                    color = WordPaperBorder,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(modifier = Modifier.padding(pagePadding)) {
                // Watermark overlay if configured
                if (watermarkText.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = watermarkText,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0x15000000),
                            modifier = Modifier.rotate(-30f)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Document Header / Page indicator in Print Layout
                    if (viewMode == ViewMode.PRINT_LAYOUT) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "WordCraft Cloud Document",
                                fontSize = 10.sp,
                                color = Color(0xFF8C95A6),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Page 1 of 1",
                                fontSize = 10.sp,
                                color = Color(0xFF8C95A6)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFECEFF5), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Render blocks
                    blocks.forEachIndexed { index, block ->
                        val isActive = block.id == activeBlockId

                        // Check if any collaborator is near this block
                        val activeCollab = if (isLiveCollabActive) {
                            collaborators.getOrNull(index % (collaborators.size + 1))
                        } else null

                        BlockItem(
                            block = block,
                            isActive = isActive,
                            collaborator = if (isLiveCollabActive && (index == 1 || index == 2)) activeCollab else null,
                            searchQuery = searchQuery,
                            isMatchFocus = (isActive && searchQuery.isNotBlank()),
                            onSelect = { onSelectBlock(block.id) },
                            onUpdateText = { onUpdateBlockText(block.id, it) },
                            onToggleChecklist = { onToggleChecklist(block.id) },
                            onUpdateTableCell = { r, c, txt -> onUpdateTableCell(block.id, r, c, txt) },
                            onAddTableRow = { onAddTableRow(block.id) },
                            onAddTableColumn = { onAddTableColumn(block.id) },
                            onDelete = { onDeleteBlock(block.id) },
                            onMoveUp = { onMoveBlock(block.id, true) },
                            onMoveDown = { onMoveBlock(block.id, false) },
                            onInsertAfter = { type -> onInsertBlockAfter(type, block.id) },
                            onSetAlignment = onSetAlignment
                        )
                    }

                    // Bottom Quick Block Adder
                    Spacer(modifier = Modifier.height(16.dp))
                    QuickAddBlockBar(
                        onAddBlock = { onInsertBlockAfter(it, blocks.lastOrNull()?.id ?: "") }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockItem(
    block: DocumentBlock,
    isActive: Boolean,
    collaborator: Collaborator?,
    searchQuery: String = "",
    isMatchFocus: Boolean = false,
    onSelect: () -> Unit,
    onUpdateText: (String) -> Unit,
    onToggleChecklist: () -> Unit,
    onUpdateTableCell: (Int, Int, String) -> Unit,
    onAddTableRow: () -> Unit,
    onAddTableColumn: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onInsertAfter: (BlockType) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Color(0x0A185ABD) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) WordPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Floating Collaborator Presence Chip
        if (collaborator != null) {
            val collabColor = try {
                Color(android.graphics.Color.parseColor(collaborator.avatarColorHex))
            } catch (_: Exception) {
                WordPrimary
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(collabColor.copy(alpha = 0.12f))
                    .border(1.dp, collabColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(collabColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${collaborator.name} ${if (collaborator.isTyping) "• typing..." else "• viewing"}",
                    color = collabColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Render block by type
        when (block.type) {
            BlockType.DIVIDER -> {
                HorizontalDivider(
                    color = Color(0xFFD0D7DE),
                    thickness = 1.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            BlockType.CHECK_LIST -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onToggleChecklist,
                        modifier = Modifier.size(32.dp).testTag("checklist_toggle")
                    ) {
                        Icon(
                            imageVector = if (block.formatting.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Toggle task",
                            tint = if (block.formatting.isChecked) WordPrimary else Color(0xFF6B7280)
                        )
                    }
                    BlockTextEditor(
                        text = block.text,
                        formatting = block.formatting.copy(
                            strikethrough = block.formatting.isChecked || block.formatting.strikethrough
                        ),
                        onTextChange = onUpdateText,
                        searchQuery = searchQuery,
                        isMatchFocus = isMatchFocus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            BlockType.BULLET_LIST -> {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "•",
                        fontSize = (block.formatting.fontSizeSp + 4).sp,
                        fontWeight = FontWeight.Bold,
                        color = WordPrimary,
                        modifier = Modifier.padding(start = (block.formatting.indentLevel * 16).dp, end = 8.dp)
                    )
                    BlockTextEditor(
                        text = block.text,
                        formatting = block.formatting,
                        onTextChange = onUpdateText,
                        searchQuery = searchQuery,
                        isMatchFocus = isMatchFocus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            BlockType.NUMBERED_LIST -> {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1.",
                        fontSize = block.formatting.fontSizeSp.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WordPrimary,
                        modifier = Modifier.padding(start = (block.formatting.indentLevel * 16).dp, end = 8.dp)
                    )
                    BlockTextEditor(
                        text = block.text,
                        formatting = block.formatting,
                        onTextChange = onUpdateText,
                        searchQuery = searchQuery,
                        isMatchFocus = isMatchFocus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            BlockType.QUOTE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(WordPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BlockTextEditor(
                        text = block.text,
                        formatting = block.formatting.copy(italic = true),
                        onTextChange = onUpdateText,
                        searchQuery = searchQuery,
                        isMatchFocus = isMatchFocus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            BlockType.CALLOUT -> {
                CalloutBox(
                    block = block,
                    onUpdateText = onUpdateText,
                    searchQuery = searchQuery,
                    isMatchFocus = isMatchFocus
                )
            }
            BlockType.TABLE -> {
                TableBlockEditor(
                    block = block,
                    onUpdateCell = onUpdateTableCell,
                    onAddRow = onAddTableRow,
                    onAddColumn = onAddTableColumn,
                    searchQuery = searchQuery,
                    isMatchFocus = isMatchFocus
                )
            }
            BlockType.RICH_TEXT -> {
                val richState = remember(block.id) {
                    RichTextState(
                        initialText = block.text,
                        initialSpans = listOf(
                            RichSpan(0, minOf(block.text.length, 12), RichSpanType.BOLD)
                        ),
                        initialAlignment = block.formatting.alignment
                    )
                }
                RichTextEditor(
                    state = richState,
                    searchQuery = searchQuery,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.runtime.LaunchedEffect(richState.textFieldValue.text) {
                    if (richState.textFieldValue.text != block.text) {
                        onUpdateText(richState.textFieldValue.text)
                    }
                }
                androidx.compose.runtime.LaunchedEffect(richState.alignment) {
                    if (richState.alignment != block.formatting.alignment) {
                        onSetAlignment(richState.alignment)
                    }
                }
            }
            else -> {
                // Regular Paragraph, Heading, Title, Subtitle
                BlockTextEditor(
                    text = block.text,
                    formatting = block.formatting,
                    onTextChange = onUpdateText,
                    searchQuery = searchQuery,
                    isMatchFocus = isMatchFocus,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Action handles when block is active
        if (isActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                // Quick Alignment Toolbar Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onSetAlignment(TextAlignment.LEFT) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.FormatAlignLeft,
                            contentDescription = "Align Left",
                            tint = if (block.formatting.alignment == TextAlignment.LEFT) WordPrimary else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onSetAlignment(TextAlignment.CENTER) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatAlignCenter,
                            contentDescription = "Align Center",
                            tint = if (block.formatting.alignment == TextAlignment.CENTER) WordPrimary else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onSetAlignment(TextAlignment.RIGHT) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.FormatAlignRight,
                            contentDescription = "Align Right",
                            tint = if (block.formatting.alignment == TextAlignment.RIGHT) WordPrimary else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onSetAlignment(TextAlignment.JUSTIFY) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatAlignJustify,
                            contentDescription = "Justify",
                            tint = if (block.formatting.alignment == TextAlignment.JUSTIFY) WordPrimary else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Block movement & delete handles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp).testTag("delete_block_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Block", tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockTextEditor(
    text: String,
    formatting: BlockFormatting,
    onTextChange: (String) -> Unit,
    searchQuery: String = "",
    isMatchFocus: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = try {
        Color(android.graphics.Color.parseColor(formatting.textColorHex))
    } catch (_: Exception) {
        Color.Black
    }

    val highlightColor = try {
        Color(android.graphics.Color.parseColor(formatting.highlightColorHex))
    } catch (_: Exception) {
        Color.Transparent
    }

    val textAlign = when (formatting.alignment) {
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.Right
        TextAlignment.JUSTIFY -> TextAlign.Justify
        else -> TextAlign.Left
    }

    val textDecoration = when {
        formatting.underline && formatting.strikethrough -> TextDecoration.combine(
            listOf(TextDecoration.Underline, TextDecoration.LineThrough)
        )
        formatting.underline -> TextDecoration.Underline
        formatting.strikethrough -> TextDecoration.LineThrough
        else -> TextDecoration.None
    }

    val fontFamily = when (formatting.fontFamily.lowercase()) {
        "georgia", "times new roman" -> FontFamily.Serif
        "courier new" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    val style = TextStyle(
        color = textColor,
        fontSize = formatting.fontSizeSp.sp,
        fontWeight = if (formatting.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (formatting.italic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = textDecoration,
        textAlign = textAlign,
        fontFamily = fontFamily,
        background = highlightColor,
        lineHeight = (formatting.fontSizeSp * formatting.lineSpacing).sp
    )

    val visualTransformation = remember(searchQuery, isMatchFocus) {
        if (searchQuery.isNotBlank()) {
            SearchHighlightVisualTransformation(
                query = searchQuery,
                highlightColor = if (isMatchFocus) Color(0xFFFFB300) else Color(0xFFFFF176)
            )
        } else {
            VisualTransformation.None
        }
    }

    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        textStyle = style,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(WordPrimary),
        modifier = modifier
            .padding(start = (formatting.indentLevel * 16).dp)
            .fillMaxWidth()
            .testTag("block_text_field")
    )
}

@Composable
private fun CalloutBox(
    block: DocumentBlock,
    onUpdateText: (String) -> Unit,
    searchQuery: String = "",
    isMatchFocus: Boolean = false
) {
    val (icon, bg, borderCol, title) = when (block.calloutType) {
        CalloutType.TIP -> Quad(Icons.Default.Lightbulb, Color(0xFFE8F5E9), Color(0xFF4CAF50), "Tip")
        CalloutType.WARNING -> Quad(Icons.Default.Warning, Color(0xFFFFF3E0), Color(0xFFFF9800), "Warning")
        CalloutType.NOTE -> Quad(Icons.Default.FormatQuote, Color(0xFFF3E5F5), Color(0xFF9C27B0), "Note")
        else -> Quad(Icons.Default.Info, Color(0xFFE3F2FD), WordPrimary, "Information")
    }

    val visualTransformation = remember(searchQuery, isMatchFocus) {
        if (searchQuery.isNotBlank()) {
            SearchHighlightVisualTransformation(
                query = searchQuery,
                highlightColor = if (isMatchFocus) Color(0xFFFFB300) else Color(0xFFFFF176)
            )
        } else {
            VisualTransformation.None
        }
    }

    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = borderCol,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderCol
                )
                Spacer(modifier = Modifier.height(2.dp))
                BasicTextField(
                    value = block.text,
                    onValueChange = onUpdateText,
                    visualTransformation = visualTransformation,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF263238)
                    ),
                    cursorBrush = SolidColor(borderCol),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TableBlockEditor(
    block: DocumentBlock,
    onUpdateCell: (Int, Int, String) -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    searchQuery: String = "",
    isMatchFocus: Boolean = false
) {
    val visualTransformation = remember(searchQuery, isMatchFocus) {
        if (searchQuery.isNotBlank()) {
            SearchHighlightVisualTransformation(
                query = searchQuery,
                highlightColor = if (isMatchFocus) Color(0xFFFFB300) else Color(0xFFFFF176)
            )
        } else {
            VisualTransformation.None
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(6.dp))
    ) {
        block.tableRows.forEachIndexed { rowIdx, row ->
            val isHeader = rowIdx == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isHeader) Color(0xFFF3F5F9) else Color.White)
            ) {
                row.forEachIndexed { colIdx, cellText ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color(0xFFE2E6ED))
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = cellText,
                            onValueChange = { onUpdateCell(rowIdx, colIdx, it) },
                            visualTransformation = visualTransformation,
                            textStyle = TextStyle(
                                fontSize = if (isHeader) 13.sp else 12.sp,
                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                color = if (isHeader) WordPrimaryDark else Color(0xFF222222)
                            ),
                            cursorBrush = SolidColor(WordPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("table_cell_${rowIdx}_$colIdx")
                        )
                    }
                }
            }
        }

        // Table controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFBFC))
                .padding(4.dp)
        ) {
            Text(
                text = "+ Add Row",
                fontSize = 11.sp,
                color = WordPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAddRow() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("add_table_row_btn")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+ Add Column",
                fontSize = 11.sp,
                color = WordPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAddColumn() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("add_table_col_btn")
            )
        }
    }
}

@Composable
private fun QuickAddBlockBar(
    onAddBlock: (BlockType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF6F8FA))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Insert:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        )

        QuickAddChip(label = "+ Text", onClick = { onAddBlock(BlockType.PARAGRAPH) })
        QuickAddChip(label = "+ Rich Text", onClick = { onAddBlock(BlockType.RICH_TEXT) })
        QuickAddChip(label = "+ Heading", onClick = { onAddBlock(BlockType.HEADING_1) })
        QuickAddChip(label = "+ Table", onClick = { onAddBlock(BlockType.TABLE) })
        QuickAddChip(label = "+ Task", onClick = { onAddBlock(BlockType.CHECK_LIST) })
        QuickAddChip(label = "+ Note", onClick = { onAddBlock(BlockType.CALLOUT) })
    }
}

@Composable
private fun QuickAddChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WordPrimary)
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

class SearchHighlightVisualTransformation(
    private val query: String,
    private val highlightColor: Color = Color(0xFFFFF176),
    private val textColor: Color = Color(0xFF1F242C)
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (query.isBlank()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val originalText = text.text
        val builder = AnnotatedString.Builder(text)
        var startIndex = 0

        while (startIndex < originalText.length) {
            val index = originalText.indexOf(query, startIndex, ignoreCase = true)
            if (index == -1) break
            val endIndex = (index + query.length).coerceAtMost(originalText.length)
            builder.addStyle(
                SpanStyle(
                    background = highlightColor,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                ),
                index,
                endIndex
            )
            startIndex = index + query.length.coerceAtLeast(1)
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
