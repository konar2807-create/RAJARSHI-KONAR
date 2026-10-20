package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.DocumentEditorScreen
import com.example.ui.screens.DocumentListScreen
import com.example.ui.theme.WordCraftTheme
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.WordViewModel

enum class AppScreen {
    LIST,
    EDITOR
}

class MainActivity : ComponentActivity() {

    private val viewModel: WordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WordCraftTheme {
                WordCraftApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WordCraftApp(viewModel: WordViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.LIST) }

    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val cloudSyncInfo by viewModel.cloudSyncInfo.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                AppScreen.LIST -> {
                    DocumentListScreen(
                        documents = documents,
                        cloudSyncInfo = cloudSyncInfo,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedFilter = selectedFilter,
                        onSelectFilter = { viewModel.setSelectedFilter(it) },
                        onOpenDocument = { doc ->
                            viewModel.openDocument(doc)
                            currentScreen = AppScreen.EDITOR
                        },
                        onCreateDocument = { template ->
                            viewModel.createDocument(template)
                            currentScreen = AppScreen.EDITOR
                        },
                        onToggleStar = { id, isStarred -> viewModel.toggleDocumentStar(id, isStarred) },
                        onDeleteDocument = { id -> viewModel.deleteDocument(id) },
                        onOpenCloudSync = {
                            viewModel.setActiveModal(ActiveModal.CLOUD_SYNC)
                        }
                    )
                }
                AppScreen.EDITOR -> {
                    DocumentEditorScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = AppScreen.LIST }
                    )
                }
            }
        }
    }
}
