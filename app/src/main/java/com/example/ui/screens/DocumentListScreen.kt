package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.components.WordCraftLogo
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DocumentEntity
import com.example.data.model.CloudSyncInfo
import com.example.data.model.SyncStatus
import com.example.ui.theme.WordCanvasBg
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    documents: List<DocumentEntity>,
    cloudSyncInfo: CloudSyncInfo,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onOpenDocument: (DocumentEntity) -> Unit,
    onCreateDocument: (String?) -> Unit,
    onToggleStar: (String, Boolean) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onOpenCloudSync: () -> Unit
) {
    val filters = listOf("All", "Starred", "Work", "Meeting", "Report")
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val filteredDocs = documents.filter { doc ->
        val matchesSearch = doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.authorName.contains(searchQuery, ignoreCase = true) ||
                doc.category.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Starred" -> doc.isStarred
            "All" -> true
            else -> doc.category.equals(selectedFilter, ignoreCase = true)
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            Surface(
                color = WordPrimaryDark,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WordCraftLogo(size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "WordCraft",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cloud Sync & Real-Time Collaboration",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        },
                        actions = {
                            // Cloud status badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { onOpenCloudSync() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("dashboard_sync_badge")
                            ) {
                                Icon(
                                    imageVector = when (cloudSyncInfo.status) {
                                        SyncStatus.SYNCED -> Icons.Default.CloudDone
                                        SyncStatus.OFFLINE -> Icons.Default.CloudOff
                                        else -> Icons.Default.Sync
                                    },
                                    contentDescription = null,
                                    tint = if (cloudSyncInfo.status == SyncStatus.SYNCED) Color(0xFF81C784) else Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (cloudSyncInfo.status == SyncStatus.OFFLINE) "Offline" else "Cloud",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = WordPrimaryDark,
                            titleContentColor = Color.White
                        )
                    )

                    // Search input
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search documents, reports, proposals...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White.copy(alpha = 0.4f),
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("doc_search_bar")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateDocument(null) },
                containerColor = WordPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("new_doc_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Document", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = WordCanvasBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // New Document Templates Carousel
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "START A NEW DOCUMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    TemplateCard(
                        title = "Blank Document",
                        color = WordPrimary,
                        onClick = { onCreateDocument(null) },
                        testTag = "template_blank"
                    )
                    TemplateCard(
                        title = "Project Proposal",
                        color = Color(0xFF0F3E85),
                        onClick = { onCreateDocument("Project Proposal") },
                        testTag = "template_proposal"
                    )
                    TemplateCard(
                        title = "Meeting Minutes",
                        color = Color(0xFF0E7A0D),
                        onClick = { onCreateDocument("Meeting Minutes") },
                        testTag = "template_minutes"
                    )
                    TemplateCard(
                        title = "Weekly Status Report",
                        color = Color(0xFF2B579A),
                        onClick = { onCreateDocument("Weekly Status Report") },
                        testTag = "template_report"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) WordPrimary else Color.White)
                                .border(1.dp, if (isSelected) WordPrimary else Color(0xFFD0D7DE), RoundedCornerShape(16.dp))
                                .clickable { onSelectFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_$filter")
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF374151)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "RECENT DOCUMENTS (${filteredDocs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Documents List
            if (filteredDocs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFB0BAC8), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No documents found", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Create your first document using the + button above.", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    DocumentCardItem(
                        doc = doc,
                        dateFormat = dateFormat,
                        onOpen = { onOpenDocument(doc) },
                        onToggleStar = { onToggleStar(doc.id, doc.isStarred) },
                        onDelete = { onDeleteDocument(doc.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun TemplateCard(
    title: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(130.dp)
            .height(100.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.12f))
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DocumentCardItem(
    doc: DocumentEntity,
    dateFormat: SimpleDateFormat,
    onOpen: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("doc_item_${doc.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Document icon with theme color
            WordCraftLogo(size = 42.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(doc.lastModified)),
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text("•", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    Text(
                        text = "${doc.wordCount} words",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )

                    // Collaborator chip if present
                    if (doc.collaboratorCount > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F0FE))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(11.dp), tint = WordPrimary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${doc.collaboratorCount}", fontSize = 10.sp, color = WordPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Cloud synced tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (doc.syncStatus == "SYNCED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (doc.syncStatus == "SYNCED") "Cloud Synced" else "Offline",
                            fontSize = 9.sp,
                            color = if (doc.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFE65100),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Star button
            IconButton(
                onClick = onToggleStar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (doc.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star",
                    tint = if (doc.isStarred) Color(0xFFFFA000) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Document", color = Color(0xFFD32F2F)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                        }
                    )
                }
            }
        }
    }
}
