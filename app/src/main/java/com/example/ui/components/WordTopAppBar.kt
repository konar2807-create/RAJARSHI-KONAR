package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncInfo
import com.example.data.model.Collaborator
import com.example.data.model.SyncStatus
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark
import com.example.ui.viewmodel.ActiveModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTopAppBar(
    title: String,
    onTitleChange: (String) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNavigateBack: () -> Unit,
    cloudSyncInfo: CloudSyncInfo,
    collaborators: List<Collaborator>,
    isLiveCollabActive: Boolean,
    onOpenModal: (ActiveModal) -> Unit,
    onManualSave: () -> Unit,
    onToggleSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingTitle by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Rotation animation for cloud sync spinner
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "sync_rotation"
    )

    Surface(
        color = WordPrimaryDark,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            TopAppBar(
                title = {
                    if (isEditingTitle) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("doc_title_input")
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isEditingTitle = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("doc_title_header")
                        ) {
                            WordCraftLogo(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Documents",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }

                    // Search Button
                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search in document",
                            tint = Color.White
                        )
                    }

                    // Cloud Sync Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onOpenModal(ActiveModal.CLOUD_SYNC) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("cloud_sync_badge")
                    ) {
                        when (cloudSyncInfo.status) {
                            SyncStatus.SYNCED -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Synced", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            SyncStatus.SYNCING -> {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing with cloud",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(rotation)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Syncing...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            SyncStatus.OFFLINE -> {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Mode",
                                    tint = Color(0xFFEF9A9A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Offline", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            SyncStatus.PENDING_CHANGES -> {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Pending changes",
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${cloudSyncInfo.pendingChangesCount} pend", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Collaborator Avatars Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onOpenModal(ActiveModal.SHARE) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("collaborators_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Collaborators",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        // Stacked avatar circles
                        collaborators.take(2).forEach { col ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(col.avatarColorHex))
                            } catch (_: Exception) {
                                WordPrimary
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color.White, CircleShape)
                            ) {
                                Text(
                                    text = col.name.take(1),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        // Pulsing online indicator dot if live collab active
                        if (isLiveCollabActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF69F0AE))
                            )
                        }
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("menu_more_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save Document Now") },
                                onClick = {
                                    menuExpanded = false
                                    onManualSave()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = WordPrimary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Find & Replace in Document") },
                                onClick = {
                                    menuExpanded = false
                                    onToggleSearch()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = WordPrimary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Word Count & Statistics") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenModal(ActiveModal.STATS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Version History") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenModal(ActiveModal.VERSIONS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Document Comments") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenModal(ActiveModal.COMMENTS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export & Share") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenModal(ActiveModal.EXPORT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cloud Sync & Storage") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenModal(ActiveModal.CLOUD_SYNC)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WordPrimaryDark,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}
