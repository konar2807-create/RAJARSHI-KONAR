package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncInfo
import com.example.data.model.SyncStatus
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncSheet(
    syncInfo: CloudSyncInfo,
    onForceSync: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    val lastSyncStr = timeFormat.format(Date(syncInfo.lastSyncedTimestamp))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F0FE))
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = WordPrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cloud Synchronization",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WordPrimaryDark
                        )
                        Text(
                            text = "WordCraft Cloud Drive (${syncInfo.connectedAccountEmail})",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Status Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (syncInfo.status) {
                        SyncStatus.SYNCED -> Color(0xFFE8F5E9)
                        SyncStatus.SYNCING -> Color(0xFFFFF8E1)
                        SyncStatus.OFFLINE -> Color(0xFFFFEBEE)
                        SyncStatus.PENDING_CHANGES -> Color(0xFFFFF3E0)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = when (syncInfo.status) {
                            SyncStatus.SYNCED -> Icons.Default.CheckCircle
                            SyncStatus.SYNCING -> Icons.Default.Sync
                            SyncStatus.OFFLINE -> Icons.Default.CloudOff
                            SyncStatus.PENDING_CHANGES -> Icons.Default.Sync
                        },
                        contentDescription = null,
                        tint = when (syncInfo.status) {
                            SyncStatus.SYNCED -> Color(0xFF2E7D32)
                            SyncStatus.SYNCING -> Color(0xFFF57F17)
                            SyncStatus.OFFLINE -> Color(0xFFC62828)
                            SyncStatus.PENDING_CHANGES -> Color(0xFFE65100)
                        },
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (syncInfo.status) {
                                SyncStatus.SYNCED -> "All Changes Synced"
                                SyncStatus.SYNCING -> "Synchronizing with Cloud..."
                                SyncStatus.OFFLINE -> "Offline Mode Active"
                                SyncStatus.PENDING_CHANGES -> "Pending Local Edits"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when (syncInfo.status) {
                                SyncStatus.SYNCED -> "Last synchronized at $lastSyncStr"
                                SyncStatus.SYNCING -> "Uploading revisions and delta packets"
                                SyncStatus.OFFLINE -> "Edits saved locally. Will sync when back online."
                                SyncStatus.PENDING_CHANGES -> "${syncInfo.pendingChangesCount} changes queued to push"
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563)
                        )
                    }

                    Button(
                        onClick = onForceSync,
                        enabled = syncInfo.status != SyncStatus.OFFLINE,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                        modifier = Modifier.testTag("force_sync_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Storage quota card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp), tint = WordPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud Storage Quota", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = String.format(Locale.US, "%.2f MB / 15.00 GB", syncInfo.storageUsedMb),
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (syncInfo.storageUsedMb / syncInfo.totalStorageMb).coerceIn(0.01f, 1f) },
                        color = WordPrimary,
                        trackColor = Color(0xFFE5E7EB),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline Mode Toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AirplanemodeActive,
                            contentDescription = null,
                            tint = if (syncInfo.status == SyncStatus.OFFLINE) Color(0xFFD32F2F) else Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Simulate Offline Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Test editing without internet connection; changes queue locally", fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                    }

                    Switch(
                        checked = syncInfo.status == SyncStatus.OFFLINE,
                        onCheckedChange = { onToggleOfflineMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD32F2F)),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "RECENT SYNC ACTIVITY LOG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Sync log
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                items(syncInfo.syncLog) { logEntry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WordPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = logEntry,
                            fontSize = 11.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }
}
