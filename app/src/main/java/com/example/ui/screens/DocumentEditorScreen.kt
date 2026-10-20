package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BlockType
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.Comment
import com.example.ui.components.CloudSyncSheet
import com.example.ui.components.CollaboratorSheet
import com.example.ui.components.CommentsSheet
import com.example.ui.components.DocumentCanvas
import com.example.ui.components.ExportDialog
import com.example.ui.components.FindReplaceBar
import com.example.ui.components.StatsDialog
import com.example.ui.components.VersionHistoryDialog
import com.example.ui.components.WordRibbon
import com.example.ui.components.WordStatusBar
import com.example.ui.components.WordTopAppBar
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.WordViewModel

@Composable
fun DocumentEditorScreen(
    viewModel: WordViewModel,
    onNavigateBack: () -> Unit
) {
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val cloudSyncInfo by viewModel.cloudSyncInfo.collectAsStateWithLifecycle()
    val collaborators by viewModel.activeCollaborators.collectAsStateWithLifecycle()
    val isLiveCollabActive by viewModel.isLiveCollabActive.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val versions by viewModel.versions.collectAsStateWithLifecycle()

    val documentStats = remember(editorState.blocks) {
        viewModel.getDocumentStats()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        viewModel.saveCurrentDocument()
        onNavigateBack()
    }

    LaunchedEffect(editorState.toastMessage) {
        editorState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            Column {
                WordTopAppBar(
                    title = editorState.title,
                    onTitleChange = { viewModel.updateTitle(it) },
                    canUndo = editorState.canUndo,
                    canRedo = editorState.canRedo,
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onNavigateBack = {
                        viewModel.saveCurrentDocument()
                        onNavigateBack()
                    },
                    cloudSyncInfo = cloudSyncInfo,
                    collaborators = collaborators,
                    isLiveCollabActive = isLiveCollabActive,
                    onOpenModal = { viewModel.setActiveModal(it) },
                    onManualSave = { viewModel.saveCurrentDocument() },
                    onToggleSearch = { viewModel.toggleFindReplace(!editorState.isFindReplaceVisible) }
                )

                WordRibbon(
                    selectedTab = editorState.selectedRibbonTab,
                    onSelectTab = { viewModel.selectRibbonTab(it) },
                    canUndo = editorState.canUndo,
                    canRedo = editorState.canRedo,
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    formatting = editorState.currentFormatting,
                    onToggleBold = { viewModel.toggleBold() },
                    onToggleItalic = { viewModel.toggleItalic() },
                    onToggleUnderline = { viewModel.toggleUnderline() },
                    onToggleStrikethrough = { viewModel.toggleStrikethrough() },
                    onSetFontSize = { viewModel.setFontSize(it) },
                    onSetFontFamily = { viewModel.setFontFamily(it) },
                    onSetTextColor = { viewModel.setTextColor(it) },
                    onSetHighlightColor = { viewModel.setHighlightColor(it) },
                    onSetAlignment = { viewModel.setAlignment(it) },
                    onAdjustIndent = { viewModel.adjustIndent(it) },
                    onChangeBlockType = { viewModel.changeBlockType(it) },
                    onInsertBlock = { viewModel.insertBlock(it) },
                    marginType = editorState.marginType,
                    onSetMarginType = { viewModel.setMarginType(it) },
                    watermarkText = editorState.watermarkText,
                    onSetWatermarkText = { viewModel.setWatermarkText(it) },
                    viewMode = editorState.viewMode,
                    onSetViewMode = { viewModel.setViewMode(it) },
                    zoomPercent = editorState.zoomPercent,
                    onSetZoom = { viewModel.setZoom(it) },
                    onToggleFindReplace = { viewModel.toggleFindReplace(!editorState.isFindReplaceVisible) },
                    onOpenModal = { viewModel.setActiveModal(it) },
                    isLiveCollabActive = isLiveCollabActive,
                    onToggleLiveCollab = { viewModel.toggleLiveCollaboration(it) }
                )

                AnimatedVisibility(visible = editorState.isFindReplaceVisible) {
                    FindReplaceBar(
                        findQuery = editorState.findQuery,
                        onFindQueryChange = { viewModel.setFindQuery(it) },
                        replaceQuery = editorState.replaceQuery,
                        onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                        totalMatchesCount = editorState.totalMatchesCount,
                        currentMatchIndex = editorState.currentMatchIndex,
                        onNextMatch = { viewModel.nextSearchMatch() },
                        onPrevMatch = { viewModel.prevSearchMatch() },
                        onReplaceCurrent = { viewModel.executeReplaceCurrent() },
                        onReplaceAll = { viewModel.executeReplaceAll() },
                        onClose = { viewModel.toggleFindReplace(false) }
                    )
                }
            }
        },
        bottomBar = {
            WordStatusBar(
                stats = documentStats,
                viewMode = editorState.viewMode,
                onSetViewMode = { viewModel.setViewMode(it) },
                zoomPercent = editorState.zoomPercent,
                onSetZoom = { viewModel.setZoom(it) },
                onOpenStats = { viewModel.setActiveModal(ActiveModal.STATS) },
                isSaving = editorState.isSaving
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DocumentCanvas(
                blocks = editorState.blocks,
                activeBlockId = editorState.activeBlockId,
                onSelectBlock = { viewModel.setActiveBlock(it) },
                onUpdateBlockText = { id, text -> viewModel.updateBlockText(id, text) },
                onToggleChecklist = { viewModel.toggleChecklist(it) },
                onUpdateTableCell = { id, r, c, txt -> viewModel.updateTableCell(id, r, c, txt) },
                onAddTableRow = { viewModel.addTableRow(it) },
                onAddTableColumn = { viewModel.addTableColumn(it) },
                onDeleteBlock = { viewModel.deleteBlock(it) },
                onMoveBlock = { id, up -> viewModel.moveBlock(id, up) },
                onInsertBlockAfter = { type, afterId -> viewModel.insertBlock(type, afterId) },
                onSetAlignment = { viewModel.setAlignment(it) },
                collaborators = collaborators,
                isLiveCollabActive = isLiveCollabActive,
                viewMode = editorState.viewMode,
                zoomPercent = editorState.zoomPercent,
                marginType = editorState.marginType,
                watermarkText = editorState.watermarkText,
                searchQuery = editorState.findQuery
            )
        }
    }

    // Modal Sheets
    when (editorState.activeModal) {
        ActiveModal.SHARE -> {
            CollaboratorSheet(
                collaborators = collaborators,
                isLiveCollabActive = isLiveCollabActive,
                onToggleLiveCollab = { viewModel.toggleLiveCollaboration(it) },
                onInvite = { name, email, role -> viewModel.inviteCollaborator(name, email, role) },
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        ActiveModal.CLOUD_SYNC -> {
            CloudSyncSheet(
                syncInfo = cloudSyncInfo,
                onForceSync = { viewModel.forceSyncNow() },
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        ActiveModal.VERSIONS -> {
            VersionHistoryDialog(
                versions = versions,
                onCreateSnapshot = { viewModel.createSnapshot(it) },
                onRestoreVersion = { viewModel.restoreVersion(it) },
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        ActiveModal.STATS -> {
            StatsDialog(
                stats = viewModel.getDocumentStats(),
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        ActiveModal.COMMENTS -> {
            CommentsSheet(
                comments = comments,
                onAddComment = { viewModel.addComment(it) },
                onResolveComment = { id, res -> viewModel.resolveComment(id, res) },
                onReplyComment = { id, replies, text -> viewModel.replyToComment(id, replies, text) },
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        ActiveModal.EXPORT -> {
            ExportDialog(
                title = editorState.title,
                exportedText = viewModel.exportDocumentAsText(),
                onDismiss = { viewModel.setActiveModal(ActiveModal.NONE) }
            )
        }
        else -> Unit
    }
}
