package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.local.DocumentJsonHelper
import com.example.data.local.VersionEntity
import com.example.data.local.WordCraftDatabase
import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.CalloutType
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.Comment
import com.example.data.model.CommentReply
import com.example.data.model.DocumentBlock
import com.example.data.model.DocumentStats
import com.example.data.model.TextAlignment
import com.example.data.model.ViewMode
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class RibbonTab {
    HOME,
    INSERT,
    LAYOUT,
    REVIEW,
    VIEW
}

enum class ActiveModal {
    NONE,
    SHARE,
    CLOUD_SYNC,
    VERSIONS,
    COMMENTS,
    STATS,
    TABLE_BUILDER,
    THEMES,
    EXPORT
}

data class SearchMatch(
    val blockId: String,
    val startIndex: Int,
    val endIndex: Int
)

data class EditorUiState(
    val documentId: String = "",
    val title: String = "Untitled Document",
    val blocks: List<DocumentBlock> = emptyList(),
    val activeBlockId: String? = null,
    val selectedRibbonTab: RibbonTab = RibbonTab.HOME,
    val currentFormatting: BlockFormatting = BlockFormatting(),
    val viewMode: ViewMode = ViewMode.PRINT_LAYOUT,
    val zoomPercent: Int = 100,
    val marginType: String = "NORMAL",
    val watermarkText: String = "",
    val activeModal: ActiveModal = ActiveModal.NONE,
    val isFindReplaceVisible: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val totalMatchesCount: Int = 0,
    val currentMatchIndex: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val activeThemeHex: String = "#185ABD",
    val toastMessage: String? = null
)

class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WordCraftDatabase.getDatabase(application)
    private val repository = DocumentRepository(database.documentDao(), viewModelScope)

    // Document List State
    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cloudSyncInfo: StateFlow<CloudSyncInfo> = repository.cloudSyncInfo
    val activeCollaborators: StateFlow<List<Collaborator>> = repository.activeCollaborators
    val isLiveCollabActive: StateFlow<Boolean> = repository.isLiveCollabActive

    // Search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Editor UI State
    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<List<DocumentBlock>>()
    private val redoStack = mutableListOf<List<DocumentBlock>>()

    // Comments & Versions for active document
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _versions = MutableStateFlow<List<VersionEntity>>(emptyList())
    val versions: StateFlow<List<VersionEntity>> = _versions.asStateFlow()

    private var autoSaveJob: Job? = null
    private var observeCommentsJob: Job? = null
    private var observeVersionsJob: Job? = null

    init {
        // Seed initial templates if empty
        viewModelScope.launch {
            repository.createInitialSeedDocuments()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun openDocument(doc: DocumentEntity) {
        val blocks = DocumentJsonHelper.jsonToBlocks(doc.blocksJson)
        undoStack.clear()
        redoStack.clear()

        _editorState.update {
            it.copy(
                documentId = doc.id,
                title = doc.title,
                blocks = blocks,
                activeBlockId = blocks.firstOrNull()?.id,
                currentFormatting = blocks.firstOrNull()?.formatting ?: BlockFormatting(),
                marginType = doc.marginType,
                watermarkText = doc.watermarkText,
                activeThemeHex = doc.themeColorHex,
                canUndo = false,
                canRedo = false,
                activeModal = ActiveModal.NONE
            )
        }

        observeDocumentData(doc.id)
    }

    fun createDocument(templateTitle: String? = null) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val template = repository.getTemplates().find { it.first == templateTitle }
            val (title, blocks) = if (template != null) {
                template.first to template.second
            } else {
                "Document ${allDocuments.value.size + 1}" to listOf(
                    DocumentBlock(
                        type = BlockType.TITLE,
                        text = "Document Title",
                        formatting = BlockFormatting(fontSizeSp = 26, bold = true)
                    ),
                    DocumentBlock(
                        type = BlockType.PARAGRAPH,
                        text = "Tap here to start writing...",
                        formatting = BlockFormatting(fontSizeSp = 16)
                    )
                )
            }

            repository.saveDocument(
                id = id,
                title = title,
                blocks = blocks
            )
            repository.createVersionSnapshot(id, "Initial Draft created")

            val doc = repository.getDocumentDirect(id)
            if (doc != null) {
                openDocument(doc)
            }
        }
    }

    private fun observeDocumentData(docId: String) {
        observeCommentsJob?.cancel()
        observeCommentsJob = viewModelScope.launch {
            repository.getComments(docId).collect { list ->
                _comments.value = list
            }
        }

        observeVersionsJob?.cancel()
        observeVersionsJob = viewModelScope.launch {
            repository.getVersions(docId).collect { list ->
                _versions.value = list
            }
        }
    }

    fun selectRibbonTab(tab: RibbonTab) {
        _editorState.update { it.copy(selectedRibbonTab = tab) }
    }

    fun setActiveModal(modal: ActiveModal) {
        _editorState.update { it.copy(activeModal = modal) }
    }

    fun setViewMode(mode: ViewMode) {
        _editorState.update { it.copy(viewMode = mode) }
    }

    fun setZoom(percent: Int) {
        _editorState.update { it.copy(zoomPercent = percent.coerceIn(50, 200)) }
    }

    fun setMarginType(type: String) {
        _editorState.update { it.copy(marginType = type) }
        scheduleAutoSave()
    }

    fun setWatermarkText(text: String) {
        _editorState.update { it.copy(watermarkText = text) }
        scheduleAutoSave()
    }

    fun updateTitle(newTitle: String) {
        _editorState.update { it.copy(title = newTitle) }
        scheduleAutoSave()
    }

    fun setActiveBlock(blockId: String) {
        val block = _editorState.value.blocks.find { it.id == blockId }
        _editorState.update {
            it.copy(
                activeBlockId = blockId,
                currentFormatting = block?.formatting ?: it.currentFormatting
            )
        }
    }

    fun updateBlockText(blockId: String, newText: String) {
        val currentBlock = _editorState.value.blocks.find { it.id == blockId }
        if (currentBlock?.text == newText) return
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(text = newText) else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    fun toggleChecklist(blockId: String) {
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) {
                    val fmt = block.formatting.copy(isChecked = !block.formatting.isChecked)
                    block.copy(formatting = fmt)
                } else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    fun updateTableCell(blockId: String, rowIdx: Int, colIdx: Int, text: String) {
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId && block.type == BlockType.TABLE) {
                    val newRows = block.tableRows.mapIndexed { r, row ->
                        if (r == rowIdx) {
                            row.mapIndexed { c, cell -> if (c == colIdx) text else cell }
                        } else row
                    }
                    block.copy(tableRows = newRows)
                } else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    fun addTableRow(blockId: String) {
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId && block.type == BlockType.TABLE) {
                    val colCount = block.tableRows.firstOrNull()?.size ?: 3
                    val newRow = List(colCount) { "Data" }
                    block.copy(tableRows = block.tableRows + listOf(newRow))
                } else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    fun addTableColumn(blockId: String) {
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId && block.type == BlockType.TABLE) {
                    val newRows = block.tableRows.mapIndexed { idx, row ->
                        row + if (idx == 0) "Column ${row.size + 1}" else "Value"
                    }
                    block.copy(tableRows = newRows)
                } else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    fun insertBlock(type: BlockType, afterBlockId: String? = null, customText: String = "") {
        pushUndo()
        val defaultText = when (type) {
            BlockType.TITLE -> "Document Title"
            BlockType.SUBTITLE -> "Document Subtitle"
            BlockType.HEADING_1 -> "Heading 1"
            BlockType.HEADING_2 -> "Heading 2"
            BlockType.HEADING_3 -> "Heading 3"
            BlockType.BULLET_LIST -> "List item"
            BlockType.NUMBERED_LIST -> "Step description"
            BlockType.CHECK_LIST -> "Action item task"
            BlockType.CALLOUT -> "Important note or insight goes here."
            BlockType.QUOTE -> "Quote citation text"
            BlockType.DIVIDER -> ""
            BlockType.TABLE -> ""
            BlockType.RICH_TEXT -> "This is a rich text editor component. Select words and tap Bold, Italic, or Underline to format!"
            BlockType.PARAGRAPH -> "Enter paragraph content..."
        }

        val defaultFormatting = when (type) {
            BlockType.TITLE -> BlockFormatting(fontSizeSp = 26, bold = true, textColorHex = "#185ABD")
            BlockType.SUBTITLE -> BlockFormatting(fontSizeSp = 14, italic = true, textColorHex = "#5F6876")
            BlockType.HEADING_1 -> BlockFormatting(fontSizeSp = 20, bold = true, textColorHex = "#104A9C")
            BlockType.HEADING_2 -> BlockFormatting(fontSizeSp = 18, bold = true)
            BlockType.HEADING_3 -> BlockFormatting(fontSizeSp = 16, bold = true)
            BlockType.QUOTE -> BlockFormatting(fontSizeSp = 15, italic = true, textColorHex = "#2B579A")
            BlockType.RICH_TEXT -> BlockFormatting(fontSizeSp = 15)
            else -> BlockFormatting(fontSizeSp = 16)
        }

        val tableRows = if (type == BlockType.TABLE) {
            listOf(
                listOf("Header 1", "Header 2", "Header 3"),
                listOf("Item A", "Description", "$100"),
                listOf("Item B", "Description", "$250")
            )
        } else emptyList()

        val newBlock = DocumentBlock(
            type = type,
            text = customText.ifBlank { defaultText },
            formatting = defaultFormatting,
            tableRows = tableRows,
            calloutType = if (type == BlockType.CALLOUT) CalloutType.INFO else CalloutType.INFO
        )

        _editorState.update { state ->
            val list = state.blocks.toMutableList()
            val targetIdx = if (afterBlockId != null) {
                list.indexOfFirst { it.id == afterBlockId }
            } else {
                list.indexOfFirst { it.id == state.activeBlockId }
            }

            if (targetIdx >= 0 && targetIdx < list.size) {
                list.add(targetIdx + 1, newBlock)
            } else {
                list.add(newBlock)
            }
            state.copy(blocks = list, activeBlockId = newBlock.id, currentFormatting = newBlock.formatting)
        }
        scheduleAutoSave()
    }

    fun deleteBlock(blockId: String) {
        if (_editorState.value.blocks.size <= 1) return
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.filterNot { it.id == blockId }
            state.copy(
                blocks = updated,
                activeBlockId = updated.firstOrNull()?.id
            )
        }
        scheduleAutoSave()
    }

    fun moveBlock(blockId: String, up: Boolean) {
        pushUndo()
        _editorState.update { state ->
            val list = state.blocks.toMutableList()
            val idx = list.indexOfFirst { it.id == blockId }
            if (idx == -1) return@update state
            val targetIdx = if (up) idx - 1 else idx + 1
            if (targetIdx in list.indices) {
                val item = list.removeAt(idx)
                list.add(targetIdx, item)
            }
            state.copy(blocks = list)
        }
        scheduleAutoSave()
    }

    // Formatting operations
    fun toggleBold() {
        applyFormattingUpdate { it.copy(bold = !it.bold) }
    }

    fun toggleItalic() {
        applyFormattingUpdate { it.copy(italic = !it.italic) }
    }

    fun toggleUnderline() {
        applyFormattingUpdate { it.copy(underline = !it.underline) }
    }

    fun toggleStrikethrough() {
        applyFormattingUpdate { it.copy(strikethrough = !it.strikethrough) }
    }

    fun setFontSize(sizeSp: Int) {
        applyFormattingUpdate { it.copy(fontSizeSp = sizeSp.coerceIn(8, 72)) }
    }

    fun setFontFamily(family: String) {
        applyFormattingUpdate { it.copy(fontFamily = family) }
    }

    fun setTextColor(colorHex: String) {
        applyFormattingUpdate { it.copy(textColorHex = colorHex) }
    }

    fun setHighlightColor(colorHex: String) {
        applyFormattingUpdate { it.copy(highlightColorHex = colorHex) }
    }

    fun setAlignment(alignment: TextAlignment) {
        applyFormattingUpdate { it.copy(alignment = alignment) }
    }

    fun setLineSpacing(spacing: Float) {
        applyFormattingUpdate { it.copy(lineSpacing = spacing) }
    }

    fun adjustIndent(increase: Boolean) {
        applyFormattingUpdate {
            val newLvl = if (increase) (it.indentLevel + 1).coerceAtMost(4) else (it.indentLevel - 1).coerceAtLeast(0)
            it.copy(indentLevel = newLvl)
        }
    }

    fun changeBlockType(newType: BlockType) {
        val activeId = _editorState.value.activeBlockId ?: return
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == activeId) {
                    val fmt = when (newType) {
                        BlockType.TITLE -> block.formatting.copy(fontSizeSp = 26, bold = true, textColorHex = "#185ABD")
                        BlockType.SUBTITLE -> block.formatting.copy(fontSizeSp = 14, italic = true, textColorHex = "#5F6876")
                        BlockType.HEADING_1 -> block.formatting.copy(fontSizeSp = 20, bold = true, textColorHex = "#104A9C")
                        BlockType.HEADING_2 -> block.formatting.copy(fontSizeSp = 18, bold = true)
                        BlockType.HEADING_3 -> block.formatting.copy(fontSizeSp = 16, bold = true)
                        BlockType.QUOTE -> block.formatting.copy(fontSizeSp = 15, italic = true, textColorHex = "#2B579A")
                        else -> block.formatting.copy(fontSizeSp = 16)
                    }
                    block.copy(type = newType, formatting = fmt)
                } else block
            }
            state.copy(blocks = updated)
        }
        scheduleAutoSave()
    }

    private fun applyFormattingUpdate(updateFn: (BlockFormatting) -> BlockFormatting) {
        val activeId = _editorState.value.activeBlockId ?: return
        pushUndo()
        _editorState.update { state ->
            var updatedFmt = state.currentFormatting
            val updatedBlocks = state.blocks.map { block ->
                if (block.id == activeId) {
                    val newFmt = updateFn(block.formatting)
                    updatedFmt = newFmt
                    block.copy(formatting = newFmt)
                } else block
            }
            state.copy(blocks = updatedBlocks, currentFormatting = updatedFmt)
        }
        scheduleAutoSave()
    }

    // Undo / Redo
    private fun pushUndo() {
        undoStack.add(_editorState.value.blocks)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
        _editorState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(_editorState.value.blocks)
        _editorState.update {
            it.copy(
                blocks = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
        scheduleAutoSave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(_editorState.value.blocks)
        _editorState.update {
            it.copy(
                blocks = next,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000)
            saveCurrentDocument()
        }
    }

    fun saveCurrentDocument() {
        val state = _editorState.value
        if (state.documentId.isBlank()) return
        viewModelScope.launch {
            _editorState.update { it.copy(isSaving = true) }
            repository.saveDocument(
                id = state.documentId,
                title = state.title,
                blocks = state.blocks,
                marginType = state.marginType,
                watermarkText = state.watermarkText,
                themeColorHex = state.activeThemeHex
            )
            _editorState.update { it.copy(isSaving = false) }
        }
    }

    // Find and Replace Search Functionality
    fun toggleFindReplace(show: Boolean) {
        _editorState.update { it.copy(isFindReplaceVisible = show) }
        if (show && _editorState.value.findQuery.isNotBlank()) {
            setFindQuery(_editorState.value.findQuery)
        }
    }

    fun setFindQuery(query: String) {
        val matches = calculateMatches(query, _editorState.value.blocks)
        val activeBlock = matches.firstOrNull()?.blockId ?: _editorState.value.activeBlockId
        _editorState.update {
            it.copy(
                findQuery = query,
                totalMatchesCount = matches.size,
                currentMatchIndex = if (matches.isNotEmpty()) 0 else -1,
                activeBlockId = activeBlock
            )
        }
    }

    private fun calculateMatches(query: String, blocks: List<DocumentBlock>): List<SearchMatch> {
        if (query.isBlank()) return emptyList()
        val result = mutableListOf<SearchMatch>()
        blocks.forEach { block ->
            val text = when (block.type) {
                BlockType.TABLE -> block.tableRows.flatten().joinToString(" ")
                else -> block.text
            }
            var idx = 0
            while (idx < text.length) {
                val found = text.indexOf(query, idx, ignoreCase = true)
                if (found == -1) break
                result.add(SearchMatch(block.id, found, found + query.length))
                idx = found + query.length.coerceAtLeast(1)
            }
        }
        return result
    }

    fun nextSearchMatch() {
        val query = _editorState.value.findQuery
        if (query.isBlank()) return
        val matches = calculateMatches(query, _editorState.value.blocks)
        if (matches.isEmpty()) return
        val nextIdx = (_editorState.value.currentMatchIndex + 1) % matches.size
        val match = matches[nextIdx]
        _editorState.update {
            it.copy(
                currentMatchIndex = nextIdx,
                activeBlockId = match.blockId,
                totalMatchesCount = matches.size
            )
        }
    }

    fun prevSearchMatch() {
        val query = _editorState.value.findQuery
        if (query.isBlank()) return
        val matches = calculateMatches(query, _editorState.value.blocks)
        if (matches.isEmpty()) return
        val cur = _editorState.value.currentMatchIndex
        val prevIdx = if (cur <= 0) matches.size - 1 else cur - 1
        val match = matches[prevIdx]
        _editorState.update {
            it.copy(
                currentMatchIndex = prevIdx,
                activeBlockId = match.blockId,
                totalMatchesCount = matches.size
            )
        }
    }

    fun setReplaceQuery(query: String) {
        _editorState.update { it.copy(replaceQuery = query) }
    }

    fun executeReplaceCurrent() {
        val q = _editorState.value.findQuery
        val r = _editorState.value.replaceQuery
        if (q.isBlank()) return
        val matches = calculateMatches(q, _editorState.value.blocks)
        if (matches.isEmpty()) return
        val idx = _editorState.value.currentMatchIndex.coerceIn(0, matches.lastIndex)
        val targetMatch = matches[idx]

        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == targetMatch.blockId) {
                    val original = block.text
                    val matchIdx = original.indexOf(q, ignoreCase = true)
                    if (matchIdx != -1) {
                        val newText = original.replaceRange(matchIdx, matchIdx + q.length, r)
                        block.copy(text = newText)
                    } else block
                } else block
            }
            state.copy(blocks = updated, toastMessage = "Replaced match with '$r'")
        }
        setFindQuery(q)
        scheduleAutoSave()
    }

    fun executeReplaceAll() {
        val q = _editorState.value.findQuery
        val r = _editorState.value.replaceQuery
        if (q.isBlank()) return
        val prevCount = _editorState.value.totalMatchesCount
        pushUndo()
        _editorState.update { state ->
            val updated = state.blocks.map { block ->
                when (block.type) {
                    BlockType.TABLE -> {
                        val newRows = block.tableRows.map { row ->
                            row.map { it.replace(q, r, ignoreCase = true) }
                        }
                        block.copy(tableRows = newRows)
                    }
                    else -> block.copy(text = block.text.replace(q, r, ignoreCase = true))
                }
            }
            state.copy(
                blocks = updated,
                toastMessage = "Replaced $prevCount occurrence${if (prevCount == 1) "" else "s"} of '$q'"
            )
        }
        setFindQuery(q)
        scheduleAutoSave()
    }

    // Comments
    fun addComment(text: String, selectedSnippet: String = "") {
        val docId = _editorState.value.documentId
        if (docId.isBlank() || text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(
                documentId = docId,
                blockId = _editorState.value.activeBlockId,
                selectedText = selectedSnippet.ifBlank { "Section feedback" },
                content = text
            )
            _editorState.update { it.copy(toastMessage = "Comment posted") }
        }
    }

    fun resolveComment(commentId: String, resolved: Boolean) {
        viewModelScope.launch {
            repository.resolveComment(commentId, resolved)
        }
    }

    fun replyToComment(commentId: String, currentReplies: List<CommentReply>, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.replyToComment(commentId, currentReplies, text)
        }
    }

    // Collaboration & Sharing
    fun toggleLiveCollaboration(active: Boolean) {
        repository.toggleLiveCollab(active)
        _editorState.update {
            it.copy(toastMessage = if (active) "Live peer collaboration active" else "Live collaboration paused")
        }
    }

    fun inviteCollaborator(name: String, email: String, role: String) {
        repository.addCollaborator(name, email, role)
        _editorState.update { it.copy(toastMessage = "Invitation sent to $email ($role)") }
    }

    // Cloud Sync
    fun forceSyncNow() {
        repository.triggerCloudSync("Manual trigger from user")
        _editorState.update { it.copy(toastMessage = "Synchronizing with WordCraft Cloud...") }
    }

    fun toggleOfflineMode() {
        repository.toggleOfflineMode()
    }

    // Versions
    fun createSnapshot(summary: String = "Manual checkpoint") {
        viewModelScope.launch {
            repository.createVersionSnapshot(_editorState.value.documentId, summary)
            _editorState.update { it.copy(toastMessage = "Saved new version snapshot") }
        }
    }

    fun restoreVersion(version: VersionEntity) {
        viewModelScope.launch {
            repository.restoreVersion(_editorState.value.documentId, version)
            val doc = repository.getDocumentDirect(_editorState.value.documentId)
            if (doc != null) {
                openDocument(doc)
                _editorState.update { it.copy(toastMessage = "Restored ${version.label}") }
            }
        }
    }

    // Document Management
    fun toggleDocumentStar(id: String, isStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleStar(id, !isStarred)
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            if (_editorState.value.documentId == id) {
                _editorState.update { EditorUiState() }
            }
        }
    }

    fun getDocumentStats(): DocumentStats {
        return repository.calculateStats(_editorState.value.blocks)
    }

    fun exportDocumentAsText(): String {
        val sb = StringBuilder()
        sb.appendLine(_editorState.value.title.uppercase())
        sb.appendLine("=".repeat(40))
        for (b in _editorState.value.blocks) {
            when (b.type) {
                BlockType.TITLE -> sb.appendLine("# ${b.text}\n")
                BlockType.SUBTITLE -> sb.appendLine("## ${b.text}\n")
                BlockType.HEADING_1 -> sb.appendLine("### ${b.text}\n")
                BlockType.HEADING_2 -> sb.appendLine("#### ${b.text}\n")
                BlockType.BULLET_LIST -> sb.appendLine("• ${b.text}")
                BlockType.NUMBERED_LIST -> sb.appendLine("1. ${b.text}")
                BlockType.CHECK_LIST -> sb.appendLine("[${if (b.formatting.isChecked) "x" else " "}] ${b.text}")
                BlockType.CALLOUT -> sb.appendLine("> [NOTE] ${b.text}\n")
                BlockType.QUOTE -> sb.appendLine("> \"${b.text}\"\n")
                BlockType.DIVIDER -> sb.appendLine("-".repeat(30) + "\n")
                BlockType.TABLE -> {
                    for (row in b.tableRows) {
                        sb.appendLine(row.joinToString(" | "))
                    }
                    sb.appendLine()
                }
                else -> sb.appendLine("${b.text}\n")
            }
        }
        return sb.toString()
    }

    fun clearToast() {
        _editorState.update { it.copy(toastMessage = null) }
    }
}
