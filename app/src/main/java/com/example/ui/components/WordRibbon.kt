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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.TextAlignment
import com.example.data.model.ViewMode
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordRibbonBg
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.RibbonTab

@Composable
fun WordRibbon(
    selectedTab: RibbonTab,
    onSelectTab: (RibbonTab) -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    formatting: BlockFormatting,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onSetFontSize: (Int) -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetTextColor: (String) -> Unit,
    onSetHighlightColor: (String) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit,
    onAdjustIndent: (Boolean) -> Unit,
    onChangeBlockType: (BlockType) -> Unit,
    onInsertBlock: (BlockType) -> Unit,
    marginType: String,
    onSetMarginType: (String) -> Unit,
    watermarkText: String,
    onSetWatermarkText: (String) -> Unit,
    viewMode: ViewMode,
    onSetViewMode: (ViewMode) -> Unit,
    zoomPercent: Int,
    onSetZoom: (Int) -> Unit,
    onToggleFindReplace: () -> Unit,
    onOpenModal: (ActiveModal) -> Unit,
    isLiveCollabActive: Boolean,
    onToggleLiveCollab: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = RibbonTab.values()

    Surface(
        color = WordRibbonBg,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Tab Row (Home, Insert, Layout, Review, View)
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 8.dp,
                containerColor = Color.White,
                contentColor = WordPrimary,
                indicator = { tabPositions ->
                    if (selectedTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = WordPrimary,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTab(tab) },
                        text = {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) WordPrimary else Color(0xFF5F6876)
                            )
                        },
                        modifier = Modifier.testTag("ribbon_tab_${tab.name.lowercase()}")
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E6ED))

            // Ribbon Tools Strip for Selected Tab
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                when (selectedTab) {
                    RibbonTab.HOME -> HomeRibbonContent(
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        formatting = formatting,
                        onToggleBold = onToggleBold,
                        onToggleItalic = onToggleItalic,
                        onToggleUnderline = onToggleUnderline,
                        onToggleStrikethrough = onToggleStrikethrough,
                        onSetFontSize = onSetFontSize,
                        onSetFontFamily = onSetFontFamily,
                        onSetTextColor = onSetTextColor,
                        onSetHighlightColor = onSetHighlightColor,
                        onSetAlignment = onSetAlignment,
                        onAdjustIndent = onAdjustIndent,
                        onChangeBlockType = onChangeBlockType,
                        onToggleFindReplace = onToggleFindReplace
                    )
                    RibbonTab.INSERT -> InsertRibbonContent(
                        onInsertBlock = onInsertBlock,
                        onOpenModal = onOpenModal
                    )
                    RibbonTab.LAYOUT -> LayoutRibbonContent(
                        marginType = marginType,
                        onSetMarginType = onSetMarginType,
                        watermarkText = watermarkText,
                        onSetWatermarkText = onSetWatermarkText
                    )
                    RibbonTab.REVIEW -> ReviewRibbonContent(
                        onToggleFindReplace = onToggleFindReplace,
                        onOpenModal = onOpenModal,
                        isLiveCollabActive = isLiveCollabActive,
                        onToggleLiveCollab = onToggleLiveCollab
                    )
                    RibbonTab.VIEW -> ViewRibbonContent(
                        viewMode = viewMode,
                        onSetViewMode = onSetViewMode,
                        zoomPercent = zoomPercent,
                        onSetZoom = onSetZoom
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeRibbonContent(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    formatting: BlockFormatting,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    onSetFontSize: (Int) -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetTextColor: (String) -> Unit,
    onSetHighlightColor: (String) -> Unit,
    onSetAlignment: (TextAlignment) -> Unit,
    onAdjustIndent: (Boolean) -> Unit,
    onChangeBlockType: (BlockType) -> Unit,
    onToggleFindReplace: () -> Unit
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }
    var textColorMenuExpanded by remember { mutableStateOf(false) }
    var highlightMenuExpanded by remember { mutableStateOf(false) }
    var stylesMenuExpanded by remember { mutableStateOf(false) }

    val fontFamilies = listOf("Calibri", "Arial", "Georgia", "Times New Roman", "Roboto", "Courier New")

    // Undo / Redo toolbar buttons
    RibbonIconButton(
        icon = Icons.AutoMirrored.Filled.Undo,
        isEnabled = canUndo,
        onClick = onUndo,
        contentDescription = "Undo recent change",
        testTag = "ribbon_undo_button"
    )

    RibbonIconButton(
        icon = Icons.AutoMirrored.Filled.Redo,
        isEnabled = canRedo,
        onClick = onRedo,
        contentDescription = "Redo recent change",
        testTag = "ribbon_redo_button"
    )

    RibbonIconButton(
        icon = Icons.Default.Search,
        isEnabled = true,
        onClick = onToggleFindReplace,
        contentDescription = "Find in document",
        testTag = "home_ribbon_search_button"
    )

    RibbonDivider()

    // Font family dropdown
    Box {
        RibbonChipButton(
            label = formatting.fontFamily,
            trailingIcon = Icons.Default.ArrowDropDown,
            onClick = { fontMenuExpanded = true },
            testTag = "font_family_button"
        )
        DropdownMenu(
            expanded = fontMenuExpanded,
            onDismissRequest = { fontMenuExpanded = false }
        ) {
            fontFamilies.forEach { font ->
                val isSelected = font.equals(formatting.fontFamily, ignoreCase = true)
                val previewFont = when (font.lowercase()) {
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
                                text = font,
                                fontFamily = previewFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WordPrimary else Color(0xFF222222),
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
                        onSetFontFamily(font)
                        fontMenuExpanded = false
                    },
                    modifier = Modifier.testTag("ribbon_font_${font.lowercase().replace(" ", "_")}")
                )
            }
        }
    }

    // Font size controls
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD8DDE6), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = { onSetFontSize(formatting.fontSizeSp - 2) },
            modifier = Modifier.size(28.dp).testTag("font_size_minus")
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Font Size", modifier = Modifier.size(16.dp))
        }

        Text(
            text = "${formatting.fontSizeSp}pt",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        IconButton(
            onClick = { onSetFontSize(formatting.fontSizeSp + 2) },
            modifier = Modifier.size(28.dp).testTag("font_size_plus")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Font Size", modifier = Modifier.size(16.dp))
        }
    }

    RibbonDivider()

    // B / I / U / S
    RibbonToggleButton(
        icon = Icons.Default.FormatBold,
        isSelected = formatting.bold,
        onClick = onToggleBold,
        contentDescription = "Bold",
        testTag = "bold_button"
    )

    RibbonToggleButton(
        icon = Icons.Default.FormatItalic,
        isSelected = formatting.italic,
        onClick = onToggleItalic,
        contentDescription = "Italic",
        testTag = "italic_button"
    )

    RibbonToggleButton(
        icon = Icons.Default.FormatUnderlined,
        isSelected = formatting.underline,
        onClick = onToggleUnderline,
        contentDescription = "Underline",
        testTag = "underline_button"
    )

    RibbonToggleButton(
        icon = Icons.Default.FormatStrikethrough,
        isSelected = formatting.strikethrough,
        onClick = onToggleStrikethrough,
        contentDescription = "Strikethrough",
        testTag = "strikethrough_button"
    )

    RibbonDivider()

    // Text Color
    Box {
        IconButton(
            onClick = { textColorMenuExpanded = true },
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .testTag("text_color_button")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FormatColorText, contentDescription = "Text Color", modifier = Modifier.size(18.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp, 3.dp)
                        .background(
                            try { Color(android.graphics.Color.parseColor(formatting.textColorHex)) }
                            catch (_: Exception) { Color.Black }
                        )
                )
            }
        }

        DropdownMenu(
            expanded = textColorMenuExpanded,
            onDismissRequest = { textColorMenuExpanded = false }
        ) {
            val colors = listOf(
                "#1F242C" to "Black",
                "#185ABD" to "Word Blue",
                "#D32F2F" to "Dark Red",
                "#388E3C" to "Forest Green",
                "#F57C00" to "Deep Amber",
                "#7B1FA2" to "Royal Purple"
            )
            colors.forEach { (hex, name) ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                        )
                    },
                    text = { Text(name) },
                    onClick = {
                        onSetTextColor(hex)
                        textColorMenuExpanded = false
                    }
                )
            }
        }
    }

    // Highlight Color
    Box {
        IconButton(
            onClick = { highlightMenuExpanded = true },
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .testTag("highlight_color_button")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FormatColorFill, contentDescription = "Highlight", modifier = Modifier.size(18.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp, 3.dp)
                        .background(
                            try { Color(android.graphics.Color.parseColor(formatting.highlightColorHex)) }
                            catch (_: Exception) { Color.Transparent }
                        )
                )
            }
        }

        DropdownMenu(
            expanded = highlightMenuExpanded,
            onDismissRequest = { highlightMenuExpanded = false }
        ) {
            val highlights = listOf(
                "#00000000" to "No Color",
                "#FFF59D" to "Yellow Highlighter",
                "#A5D6A7" to "Green Highlighter",
                "#80DEEA" to "Cyan Highlighter",
                "#FFCC80" to "Orange Highlighter",
                "#F48FB1" to "Pink Highlighter"
            )
            highlights.forEach { (hex, name) ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
                        )
                    },
                    text = { Text(name) },
                    onClick = {
                        onSetHighlightColor(hex)
                        highlightMenuExpanded = false
                    }
                )
            }
        }
    }

    RibbonDivider()

    // Alignment
    RibbonToggleButton(
        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
        isSelected = formatting.alignment == TextAlignment.LEFT,
        onClick = { onSetAlignment(TextAlignment.LEFT) },
        contentDescription = "Align Left",
        testTag = "align_left_button"
    )
    RibbonToggleButton(
        icon = Icons.Default.FormatAlignCenter,
        isSelected = formatting.alignment == TextAlignment.CENTER,
        onClick = { onSetAlignment(TextAlignment.CENTER) },
        contentDescription = "Align Center",
        testTag = "align_center_button"
    )
    RibbonToggleButton(
        icon = Icons.AutoMirrored.Filled.FormatAlignRight,
        isSelected = formatting.alignment == TextAlignment.RIGHT,
        onClick = { onSetAlignment(TextAlignment.RIGHT) },
        contentDescription = "Align Right",
        testTag = "align_right_button"
    )
    RibbonToggleButton(
        icon = Icons.Default.FormatAlignJustify,
        isSelected = formatting.alignment == TextAlignment.JUSTIFY,
        onClick = { onSetAlignment(TextAlignment.JUSTIFY) },
        contentDescription = "Justify",
        testTag = "align_justify_button"
    )

    RibbonDivider()

    // Indent
    IconButton(
        onClick = { onAdjustIndent(false) },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.FormatIndentDecrease, contentDescription = "Decrease Indent", modifier = Modifier.size(18.dp))
    }
    IconButton(
        onClick = { onAdjustIndent(true) },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.FormatIndentIncrease, contentDescription = "Increase Indent", modifier = Modifier.size(18.dp))
    }

    RibbonDivider()

    // Block Styles Dropdown
    Box {
        RibbonChipButton(
            label = "Styles",
            icon = Icons.Default.Title,
            onClick = { stylesMenuExpanded = true },
            testTag = "styles_dropdown_button"
        )
        DropdownMenu(
            expanded = stylesMenuExpanded,
            onDismissRequest = { stylesMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Normal Text") },
                onClick = {
                    onChangeBlockType(BlockType.PARAGRAPH)
                    stylesMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Title", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WordPrimary) },
                onClick = {
                    onChangeBlockType(BlockType.TITLE)
                    stylesMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Subtitle", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                onClick = {
                    onChangeBlockType(BlockType.SUBTITLE)
                    stylesMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 1", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                onClick = {
                    onChangeBlockType(BlockType.HEADING_1)
                    stylesMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 2", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                onClick = {
                    onChangeBlockType(BlockType.HEADING_2)
                    stylesMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Blockquote", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                onClick = {
                    onChangeBlockType(BlockType.QUOTE)
                    stylesMenuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun InsertRibbonContent(
    onInsertBlock: (BlockType) -> Unit,
    onOpenModal: (ActiveModal) -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table",
        onClick = { onInsertBlock(BlockType.TABLE) },
        testTag = "insert_table_button"
    )

    RibbonActionButton(
        icon = Icons.Default.FormatBold,
        label = "Rich Text",
        onClick = { onInsertBlock(BlockType.RICH_TEXT) },
        testTag = "insert_rich_text_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Lightbulb,
        label = "Callout Box",
        onClick = { onInsertBlock(BlockType.CALLOUT) },
        testTag = "insert_callout_button"
    )

    RibbonActionButton(
        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
        label = "Bullet List",
        onClick = { onInsertBlock(BlockType.BULLET_LIST) }
    )

    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Numbered",
        onClick = { onInsertBlock(BlockType.NUMBERED_LIST) }
    )

    RibbonActionButton(
        icon = Icons.Default.Checklist,
        label = "Checklist",
        onClick = { onInsertBlock(BlockType.CHECK_LIST) }
    )

    RibbonActionButton(
        icon = Icons.Default.HorizontalRule,
        label = "Divider",
        onClick = { onInsertBlock(BlockType.DIVIDER) }
    )

    RibbonActionButton(
        icon = Icons.Default.AddComment,
        label = "New Comment",
        onClick = { onOpenModal(ActiveModal.COMMENTS) }
    )
}

@Composable
private fun LayoutRibbonContent(
    marginType: String,
    onSetMarginType: (String) -> Unit,
    watermarkText: String,
    onSetWatermarkText: (String) -> Unit
) {
    var marginMenuExpanded by remember { mutableStateOf(false) }
    var watermarkMenuExpanded by remember { mutableStateOf(false) }

    // Margins
    Box {
        RibbonChipButton(
            label = "Margins: $marginType",
            icon = Icons.Default.ViewAgenda,
            onClick = { marginMenuExpanded = true }
        )
        DropdownMenu(
            expanded = marginMenuExpanded,
            onDismissRequest = { marginMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Normal (1.0 in)") },
                onClick = {
                    onSetMarginType("NORMAL")
                    marginMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Narrow (0.5 in)") },
                onClick = {
                    onSetMarginType("NARROW")
                    marginMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Wide (1.5 in)") },
                onClick = {
                    onSetMarginType("WIDE")
                    marginMenuExpanded = false
                }
            )
        }
    }

    // Watermark
    Box {
        RibbonChipButton(
            label = if (watermarkText.isBlank()) "Watermark: None" else "Watermark: $watermarkText",
            icon = Icons.Default.Bookmark,
            onClick = { watermarkMenuExpanded = true }
        )
        DropdownMenu(
            expanded = watermarkMenuExpanded,
            onDismissRequest = { watermarkMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onSetWatermarkText("")
                    watermarkMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("DRAFT") },
                onClick = {
                    onSetWatermarkText("DRAFT")
                    watermarkMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("CONFIDENTIAL") },
                onClick = {
                    onSetWatermarkText("CONFIDENTIAL")
                    watermarkMenuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("FINAL COPY") },
                onClick = {
                    onSetWatermarkText("FINAL COPY")
                    watermarkMenuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun ReviewRibbonContent(
    onToggleFindReplace: () -> Unit,
    onOpenModal: (ActiveModal) -> Unit,
    isLiveCollabActive: Boolean,
    onToggleLiveCollab: (Boolean) -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.Description,
        label = "Word Count",
        onClick = { onOpenModal(ActiveModal.STATS) },
        testTag = "review_stats_button"
    )

    RibbonActionButton(
        icon = Icons.Default.FindReplace,
        label = "Find & Replace",
        onClick = onToggleFindReplace,
        testTag = "find_replace_ribbon_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Comment,
        label = "Comments",
        onClick = { onOpenModal(ActiveModal.COMMENTS) },
        testTag = "review_comments_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Group,
        label = if (isLiveCollabActive) "Peer Sync (ON)" else "Peer Sync (OFF)",
        onClick = { onToggleLiveCollab(!isLiveCollabActive) },
        isSelected = isLiveCollabActive,
        testTag = "toggle_peer_sync_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Save,
        label = "Snapshot Version",
        onClick = { onOpenModal(ActiveModal.VERSIONS) },
        testTag = "review_versions_button"
    )
}

@Composable
private fun ViewRibbonContent(
    viewMode: ViewMode,
    onSetViewMode: (ViewMode) -> Unit,
    zoomPercent: Int,
    onSetZoom: (Int) -> Unit
) {
    RibbonActionButton(
        icon = Icons.Default.Pageview,
        label = "Print Layout",
        onClick = { onSetViewMode(ViewMode.PRINT_LAYOUT) },
        isSelected = viewMode == ViewMode.PRINT_LAYOUT,
        testTag = "print_layout_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Smartphone,
        label = "Mobile Flow",
        onClick = { onSetViewMode(ViewMode.MOBILE_FLOW) },
        isSelected = viewMode == ViewMode.MOBILE_FLOW,
        testTag = "mobile_flow_button"
    )

    RibbonDivider()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD8DDE6), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = { onSetZoom(zoomPercent - 25) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
        }

        Text(
            text = "$zoomPercent%",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        IconButton(
            onClick = { onSetZoom(zoomPercent + 25) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun RibbonToggleButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    testTag: String = ""
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFFD2E3FC) else Color.White)
            .border(
                1.dp,
                if (isSelected) WordPrimary else Color(0xFFD8DDE6),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) WordPrimary else Color(0xFF333333),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RibbonChipButton(
    label: String,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD8DDE6), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = WordPrimary)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF222222))
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun RibbonActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFFD2E3FC) else Color.White)
            .border(1.dp, if (isSelected) WordPrimary else Color(0xFFD8DDE6), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isSelected) WordPrimary else Color(0xFF444444)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) WordPrimary else Color(0xFF222222)
        )
    }
}

@Composable
private fun RibbonDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color(0xFFD8DDE6))
    )
}

@Composable
private fun RibbonIconButton(
    icon: ImageVector,
    isEnabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    testTag: String = ""
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isEnabled) Color.White else Color(0xFFF3F4F6))
            .border(
                1.dp,
                if (isEnabled) Color(0xFFD8DDE6) else Color(0xFFE5E7EB),
                RoundedCornerShape(6.dp)
            )
            .clickable(enabled = isEnabled) { onClick() }
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isEnabled) Color(0xFF333333) else Color(0xFFB0B7C3),
            modifier = Modifier.size(18.dp)
        )
    }
}
