package com.example.data.repository

import com.example.data.local.CommentEntity
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.local.DocumentJsonHelper
import com.example.data.local.VersionEntity
import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.CalloutType
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.Comment
import com.example.data.model.CommentReply
import com.example.data.model.DocumentBlock
import com.example.data.model.DocumentStats
import com.example.data.model.SyncStatus
import com.example.data.model.TextAlignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class DocumentRepository(
    private val dao: DocumentDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    // Cloud Sync State
    private val _cloudSyncInfo = MutableStateFlow(CloudSyncInfo())
    val cloudSyncInfo: StateFlow<CloudSyncInfo> = _cloudSyncInfo.asStateFlow()

    // Real-time Collaborators for the active document
    private val _activeCollaborators = MutableStateFlow<List<Collaborator>>(getDefaultCollaborators())
    val activeCollaborators: StateFlow<List<Collaborator>> = _activeCollaborators.asStateFlow()

    // Live collaboration enabled state
    private val _isLiveCollabActive = MutableStateFlow(true)
    val isLiveCollabActive: StateFlow<Boolean> = _isLiveCollabActive.asStateFlow()

    init {
        // Start background simulated peer activity
        startCollaborationHeartbeat()
    }

    val allDocuments: Flow<List<DocumentEntity>> = dao.getAllDocuments()

    fun getDocument(id: String): Flow<DocumentEntity?> = dao.getDocumentById(id)

    suspend fun getDocumentDirect(id: String): DocumentEntity? = dao.getDocumentByIdDirect(id)

    fun getComments(documentId: String): Flow<List<Comment>> {
        return dao.getCommentsForDocument(documentId).map { entities ->
            entities.map { entity ->
                Comment(
                    id = entity.id,
                    documentId = entity.documentId,
                    blockId = entity.blockId,
                    selectedText = entity.selectedText,
                    authorName = entity.authorName,
                    authorColorHex = entity.authorColorHex,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    resolved = entity.resolved,
                    replies = DocumentJsonHelper.jsonToReplies(entity.repliesJson)
                )
            }
        }
    }

    fun getVersions(documentId: String): Flow<List<VersionEntity>> {
        return dao.getVersionsForDocument(documentId)
    }

    suspend fun saveDocument(
        id: String,
        title: String,
        blocks: List<DocumentBlock>,
        category: String = "General",
        themeColorHex: String = "#185ABD",
        watermarkText: String = "",
        marginType: String = "NORMAL"
    ) {
        val stats = calculateStats(blocks)
        val json = DocumentJsonHelper.blocksToJson(blocks)
        val existing = dao.getDocumentByIdDirect(id)

        val doc = DocumentEntity(
            id = id,
            title = title.ifBlank { "Untitled Document" },
            blocksJson = json,
            lastModified = System.currentTimeMillis(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            authorName = existing?.authorName ?: "You",
            authorEmail = existing?.authorEmail ?: "user@wordcraft.cloud",
            isStarred = existing?.isStarred ?: false,
            themeColorHex = themeColorHex,
            category = category,
            syncStatus = if (_cloudSyncInfo.value.status == SyncStatus.OFFLINE) "OFFLINE" else "SYNCED",
            cloudRevision = (existing?.cloudRevision ?: 0) + 1,
            wordCount = stats.words,
            pageCount = stats.pages,
            collaboratorCount = _activeCollaborators.value.size,
            watermarkText = watermarkText,
            marginType = marginType
        )
        dao.insertDocument(doc)

        // Trigger cloud sync
        if (_cloudSyncInfo.value.isAutoSyncEnabled && _cloudSyncInfo.value.status != SyncStatus.OFFLINE) {
            triggerCloudSync("Document '$title' modified")
        } else if (_cloudSyncInfo.value.status == SyncStatus.OFFLINE) {
            _cloudSyncInfo.update {
                it.copy(
                    pendingChangesCount = it.pendingChangesCount + 1,
                    syncLog = listOf("Queued offline change: $title") + it.syncLog.take(9)
                )
            }
        }
    }

    suspend fun createVersionSnapshot(documentId: String, summary: String = "Manual snapshot") {
        val doc = dao.getDocumentByIdDirect(documentId) ?: return
        val currentVersions = dao.getDocumentByIdDirect(documentId) // placeholder check
        val versionNumber = System.currentTimeMillis().toInt() % 1000 + 1
        val version = VersionEntity(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            versionNumber = versionNumber,
            label = "Version $versionNumber",
            timestamp = System.currentTimeMillis(),
            authorName = "You",
            summary = summary,
            blocksSnapshotJson = doc.blocksJson
        )
        dao.insertVersion(version)
    }

    suspend fun restoreVersion(documentId: String, version: VersionEntity) {
        val doc = dao.getDocumentByIdDirect(documentId) ?: return
        val blocks = DocumentJsonHelper.jsonToBlocks(version.blocksSnapshotJson)
        saveDocument(
            id = documentId,
            title = doc.title,
            blocks = blocks,
            category = doc.category,
            themeColorHex = doc.themeColorHex,
            watermarkText = doc.watermarkText,
            marginType = doc.marginType
        )
    }

    suspend fun toggleStar(id: String, isStarred: Boolean) {
        dao.updateStarred(id, isStarred)
    }

    suspend fun deleteDocument(id: String) {
        dao.deleteDocumentById(id)
    }

    suspend fun addComment(
        documentId: String,
        blockId: String?,
        selectedText: String,
        content: String
    ) {
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            blockId = blockId,
            selectedText = selectedText,
            authorName = "You",
            authorColorHex = "#185ABD",
            content = content,
            timestamp = System.currentTimeMillis(),
            resolved = false,
            repliesJson = "[]"
        )
        dao.insertComment(comment)
        triggerCloudSync("Added comment")
    }

    suspend fun resolveComment(commentId: String, resolved: Boolean) {
        dao.updateCommentResolved(commentId, resolved)
    }

    suspend fun replyToComment(commentId: String, currentReplies: List<CommentReply>, content: String) {
        val newReply = CommentReply(
            authorName = "You",
            authorColorHex = "#185ABD",
            content = content
        )
        val updatedReplies = currentReplies + newReply
        val json = DocumentJsonHelper.repliesToJson(updatedReplies)
        dao.updateCommentReplies(commentId, json)
    }

    fun toggleLiveCollab(active: Boolean) {
        _isLiveCollabActive.value = active
    }

    fun toggleOfflineMode() {
        val current = _cloudSyncInfo.value.status
        if (current == SyncStatus.OFFLINE) {
            // Reconnecting to cloud
            _cloudSyncInfo.update {
                it.copy(
                    status = SyncStatus.SYNCING,
                    syncLog = listOf("Reconnecting to WordCraft Cloud Drive...") + it.syncLog.take(9)
                )
            }
            scope.launch {
                delay(1200)
                _cloudSyncInfo.update {
                    it.copy(
                        status = SyncStatus.SYNCED,
                        lastSyncedTimestamp = System.currentTimeMillis(),
                        pendingChangesCount = 0,
                        syncLog = listOf("Offline changes synced to cloud successfully!") + it.syncLog.take(9)
                    )
                }
            }
        } else {
            // Going offline
            _cloudSyncInfo.update {
                it.copy(
                    status = SyncStatus.OFFLINE,
                    syncLog = listOf("Switched to Offline Mode (airplane safe)") + it.syncLog.take(9)
                )
            }
        }
    }

    fun triggerCloudSync(reason: String = "Manual Sync") {
        if (_cloudSyncInfo.value.status == SyncStatus.OFFLINE) return

        scope.launch {
            _cloudSyncInfo.update {
                it.copy(
                    status = SyncStatus.SYNCING,
                    syncLog = listOf("Cloud sync started: $reason") + it.syncLog.take(9)
                )
            }
            delay(800) // Simulated network round-trip
            _cloudSyncInfo.update {
                it.copy(
                    status = SyncStatus.SYNCED,
                    lastSyncedTimestamp = System.currentTimeMillis(),
                    pendingChangesCount = 0,
                    storageUsedMb = it.storageUsedMb + 0.05f,
                    syncLog = listOf("Synced to WordCraft Cloud ($reason)") + it.syncLog.take(9)
                )
            }
        }
    }

    fun addCollaborator(name: String, email: String, role: String) {
        val colors = listOf("#0E7A0D", "#9C27B0", "#E65100", "#00838F", "#C2185B", "#3949AB")
        val color = colors[(_activeCollaborators.value.size) % colors.size]
        val newCollab = Collaborator(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            avatarColorHex = color,
            role = role,
            status = "Invited - Active now",
            isOnline = true
        )
        _activeCollaborators.update { it + newCollab }
        triggerCloudSync("Invited $name ($email)")
    }

    private fun startCollaborationHeartbeat() {
        scope.launch {
            while (true) {
                delay(9000)
                if (_isLiveCollabActive.value && _cloudSyncInfo.value.status != SyncStatus.OFFLINE) {
                    // Update collaborators typing/cursor statuses realistically
                    _activeCollaborators.update { list ->
                        list.mapIndexed { index, col ->
                            when (index) {
                                0 -> col.copy(status = "Reviewing page 1", isTyping = false)
                                1 -> col.copy(status = "Editing paragraph 2...", isTyping = true)
                                2 -> col.copy(status = "Added a comment in Section 1", isTyping = false)
                                else -> col
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDefaultCollaborators(): List<Collaborator> {
        return listOf(
            Collaborator(
                id = "collab_sarah",
                name = "Sarah Connor",
                email = "sarah.connor@office.corp",
                avatarColorHex = "#0E7A0D",
                role = "Editor",
                status = "Active in Section 2",
                isOnline = true,
                isTyping = false
            ),
            Collaborator(
                id = "collab_alex",
                name = "Alex Morgan",
                email = "alex.morgan@design.io",
                avatarColorHex = "#9C27B0",
                role = "Editor",
                status = "Viewing document",
                isOnline = true,
                isTyping = true
            ),
            Collaborator(
                id = "collab_elena",
                name = "Elena Rostova",
                email = "elena.r@global.net",
                avatarColorHex = "#E65100",
                role = "Commenter",
                status = "Left comment 5m ago",
                isOnline = true,
                isTyping = false
            )
        )
    }

    fun calculateStats(blocks: List<DocumentBlock>): DocumentStats {
        var wordCount = 0
        var charCount = 0
        var charNoSpace = 0
        var paragraphCount = 0

        for (block in blocks) {
            when (block.type) {
                BlockType.TABLE -> {
                    for (row in block.tableRows) {
                        for (cell in row) {
                            val words = cell.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                            wordCount += words.size
                            charCount += cell.length
                            charNoSpace += cell.replace("\\s".toRegex(), "").length
                        }
                    }
                    paragraphCount += block.tableRows.size
                }
                BlockType.DIVIDER -> {}
                else -> {
                    val text = block.text.trim()
                    if (text.isNotBlank()) {
                        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
                        wordCount += words.size
                        charCount += text.length
                        charNoSpace += text.replace("\\s".toRegex(), "").length
                        paragraphCount++
                    }
                }
            }
        }

        val pages = maxOf(1, (wordCount / 350) + 1)
        val readingTime = maxOf(1, (wordCount / 200))

        return DocumentStats(
            words = wordCount,
            characters = charCount,
            charactersNoSpaces = charNoSpace,
            paragraphs = paragraphCount,
            pages = pages,
            readingTimeMinutes = readingTime
        )
    }

    // Pre-built document templates
    fun getTemplates(): List<Pair<String, List<DocumentBlock>>> {
        return listOf(
            "Blank Document" to listOf(
                DocumentBlock(
                    type = BlockType.TITLE,
                    text = "Untitled Document",
                    formatting = BlockFormatting(fontSizeSp = 26, bold = true, alignment = TextAlignment.LEFT)
                ),
                DocumentBlock(
                    type = BlockType.PARAGRAPH,
                    text = "Start typing your rich document here with real-time cloud sync and collaborative editing...",
                    formatting = BlockFormatting(fontSizeSp = 16)
                )
            ),
            "Project Proposal" to listOf(
                DocumentBlock(
                    type = BlockType.TITLE,
                    text = "Project Proposal: NextGen Cloud Platform",
                    formatting = BlockFormatting(fontSizeSp = 28, bold = true, textColorHex = "#185ABD")
                ),
                DocumentBlock(
                    type = BlockType.SUBTITLE,
                    text = "Prepared by Engineering & Product Strategy Team | Confidential",
                    formatting = BlockFormatting(fontSizeSp = 14, italic = true, textColorHex = "#5F6876")
                ),
                DocumentBlock(type = BlockType.DIVIDER),
                DocumentBlock(
                    type = BlockType.HEADING_1,
                    text = "1. Executive Summary",
                    formatting = BlockFormatting(fontSizeSp = 20, bold = true, textColorHex = "#104A9C")
                ),
                DocumentBlock(
                    type = BlockType.CALLOUT,
                    text = "This proposal outlines our high-availability cloud architecture designed for 99.999% uptime, distributed sync, and sub-10ms document latency across mobile and web clients.",
                    calloutType = CalloutType.TIP
                ),
                DocumentBlock(
                    type = BlockType.HEADING_2,
                    text = "2. Key Objectives & Milestones",
                    formatting = BlockFormatting(fontSizeSp = 18, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.CHECK_LIST,
                    text = "Deploy multi-region cloud synchronization clusters",
                    formatting = BlockFormatting(isChecked = true)
                ),
                DocumentBlock(
                    type = BlockType.CHECK_LIST,
                    text = "Implement real-time operational transformation & collaborative cursors",
                    formatting = BlockFormatting(isChecked = true)
                ),
                DocumentBlock(
                    type = BlockType.CHECK_LIST,
                    text = "Execute end-to-end security audit and penetration testing",
                    formatting = BlockFormatting(isChecked = false)
                ),
                DocumentBlock(
                    type = BlockType.HEADING_2,
                    text = "3. Resource Allocation & Timeline",
                    formatting = BlockFormatting(fontSizeSp = 18, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.TABLE,
                    tableRows = listOf(
                        listOf("Phase", "Duration", "Budget", "Owner"),
                        listOf("Architecture & Specs", "4 Weeks", "$45,000", "Alex M."),
                        listOf("Core Sync Engine", "8 Weeks", "$110,000", "Sarah C."),
                        listOf("UI Ribbon & Mobile", "6 Weeks", "$75,000", "Elena R."),
                        listOf("Global Rollout", "3 Weeks", "$30,000", "David C.")
                    )
                ),
                DocumentBlock(
                    type = BlockType.QUOTE,
                    text = "\"Collaboration is the engine of high-velocity software delivery.\" — Tech Review 2026",
                    formatting = BlockFormatting(italic = true, textColorHex = "#2B579A")
                )
            ),
            "Meeting Minutes" to listOf(
                DocumentBlock(
                    type = BlockType.TITLE,
                    text = "Executive Sprint Planning Meeting",
                    formatting = BlockFormatting(fontSizeSp = 26, bold = true, textColorHex = "#185ABD")
                ),
                DocumentBlock(
                    type = BlockType.SUBTITLE,
                    text = "Date: October 19, 2026 | Attendees: Sarah, Alex, Elena, David",
                    formatting = BlockFormatting(fontSizeSp = 14, italic = true)
                ),
                DocumentBlock(type = BlockType.DIVIDER),
                DocumentBlock(
                    type = BlockType.HEADING_1,
                    text = "Agenda & Discussion Points",
                    formatting = BlockFormatting(fontSizeSp = 20, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.BULLET_LIST,
                    text = "Review Q3 synchronization performance benchmarks and edge latency."
                ),
                DocumentBlock(
                    type = BlockType.BULLET_LIST,
                    text = "Sign-off on WordCraft ribbon toolbar layout and mobile ergonomics."
                ),
                DocumentBlock(
                    type = BlockType.BULLET_LIST,
                    text = "Evaluate conflict resolution algorithms for simultaneous offline edits."
                ),
                DocumentBlock(
                    type = BlockType.HEADING_1,
                    text = "Decisions & Action Items",
                    formatting = BlockFormatting(fontSizeSp = 20, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.CHECK_LIST,
                    text = "Sarah to finalize automatic version snapshots before next Friday.",
                    formatting = BlockFormatting(isChecked = true)
                ),
                DocumentBlock(
                    type = BlockType.CHECK_LIST,
                    text = "Alex to publish the new typography and styles palette.",
                    formatting = BlockFormatting(isChecked = false)
                )
            ),
            "Weekly Status Report" to listOf(
                DocumentBlock(
                    type = BlockType.TITLE,
                    text = "Engineering Weekly Status Report",
                    formatting = BlockFormatting(fontSizeSp = 26, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.CALLOUT,
                    text = "Overall Status: GREEN. All sprint deliverable goals reached with zero critical defects.",
                    calloutType = CalloutType.INFO
                ),
                DocumentBlock(
                    type = BlockType.HEADING_1,
                    text = "Accomplishments",
                    formatting = BlockFormatting(fontSizeSp = 20, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.BULLET_LIST,
                    text = "Achieved 99.98% successful sync operations across active test sessions."
                ),
                DocumentBlock(
                    type = BlockType.BULLET_LIST,
                    text = "Reduced memory footprint during large table and formatted document rendering."
                ),
                DocumentBlock(
                    type = BlockType.HEADING_1,
                    text = "Upcoming Priorities",
                    formatting = BlockFormatting(fontSizeSp = 20, bold = true)
                ),
                DocumentBlock(
                    type = BlockType.NUMBERED_LIST,
                    text = "Ship real-time comment threads with collaborator avatar chips."
                ),
                DocumentBlock(
                    type = BlockType.NUMBERED_LIST,
                    text = "Expand document export to HTML, Markdown, and formatted plain text."
                )
            )
        )
    }

    suspend fun createInitialSeedDocuments() {
        val count = dao.getAllDocuments()
        // Check if DB is empty
        val existing = dao.getDocumentByIdDirect("doc_proposal_seed")
        if (existing == null) {
            val templates = getTemplates()
            // Proposal
            val proposalBlocks = templates.find { it.first == "Project Proposal" }?.second ?: emptyList()
            saveDocument(
                id = "doc_proposal_seed",
                title = "Project Proposal: NextGen Cloud Platform",
                blocks = proposalBlocks,
                category = "Work",
                themeColorHex = "#185ABD"
            )
            // Add a comment to the proposal
            addComment(
                documentId = "doc_proposal_seed",
                blockId = null,
                selectedText = "sub-10ms document latency",
                content = "Elena: Benchmarks confirmed sub-8ms latency in our Asian-Pacific nodes!"
            )

            // Meeting Minutes
            val minutesBlocks = templates.find { it.first == "Meeting Minutes" }?.second ?: emptyList()
            saveDocument(
                id = "doc_minutes_seed",
                title = "Executive Sprint Planning Meeting",
                blocks = minutesBlocks,
                category = "Meeting",
                themeColorHex = "#0E7A0D"
            )

            // Weekly Report
            val reportBlocks = templates.find { it.first == "Weekly Status Report" }?.second ?: emptyList()
            saveDocument(
                id = "doc_report_seed",
                title = "Engineering Weekly Status Report",
                blocks = reportBlocks,
                category = "Report",
                themeColorHex = "#2B579A"
            )
        }
    }
}
