package com.example.data.model

import java.util.UUID

enum class BlockType {
    PARAGRAPH,
    TITLE,
    SUBTITLE,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    BULLET_LIST,
    NUMBERED_LIST,
    CHECK_LIST,
    CALLOUT,
    TABLE,
    DIVIDER,
    QUOTE,
    RICH_TEXT
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

enum class CalloutType {
    INFO,
    TIP,
    WARNING,
    NOTE
}

data class BlockFormatting(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSizeSp: Int = 16,
    val fontFamily: String = "Calibri",
    val textColorHex: String = "#1F242C",
    val highlightColorHex: String = "#00000000",
    val alignment: TextAlignment = TextAlignment.LEFT,
    val lineSpacing: Float = 1.15f,
    val indentLevel: Int = 0,
    val isChecked: Boolean = false
)

data class DocumentBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val text: String = "",
    val formatting: BlockFormatting = BlockFormatting(),
    val tableRows: List<List<String>> = emptyList(), // For TABLE type
    val calloutType: CalloutType = CalloutType.INFO // For CALLOUT type
)

data class Collaborator(
    val id: String,
    val name: String,
    val email: String,
    val avatarColorHex: String,
    val role: String = "Editor",
    val status: String = "Online",
    val activeBlockId: String? = null,
    val isOnline: Boolean = true,
    val isTyping: Boolean = false
)

data class CommentReply(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorColorHex: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val blockId: String? = null,
    val selectedText: String = "",
    val authorName: String,
    val authorColorHex: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE,
    PENDING_CHANGES
}

data class CloudSyncInfo(
    val status: SyncStatus = SyncStatus.SYNCED,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val isAutoSyncEnabled: Boolean = true,
    val pendingChangesCount: Int = 0,
    val storageUsedMb: Float = 18.4f,
    val totalStorageMb: Float = 15360f, // 15 GB
    val connectedAccountEmail: String = "user@wordcraft.cloud",
    val syncLog: List<String> = listOf("Initial sync complete", "Cloud replica verified")
)

data class DocumentStats(
    val words: Int = 0,
    val characters: Int = 0,
    val charactersNoSpaces: Int = 0,
    val paragraphs: Int = 0,
    val pages: Int = 1,
    val readingTimeMinutes: Int = 1
)

enum class ViewMode {
    PRINT_LAYOUT,   // Paper page with margins, header, page breaks
    MOBILE_FLOW     // Continuous mobile responsive canvas
}

data class DocumentThemeOption(
    val id: String,
    val name: String,
    val primaryColorHex: String,
    val paperColorHex: String,
    val fontName: String
)
