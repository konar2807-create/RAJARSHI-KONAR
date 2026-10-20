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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark

/**
 * Interactive Find and Replace search bar that supports:
 * - Real-time word and phrase search highlighting across document
 * - Total match count and current match index indicator chip
 * - Next and previous match navigation
 * - Expandable Replace drawer with Replace Current and Replace All
 */
@Composable
fun FindReplaceBar(
    findQuery: String,
    onFindQueryChange: (String) -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    totalMatchesCount: Int,
    currentMatchIndex: Int,
    onNextMatch: () -> Unit,
    onPrevMatch: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isReplaceExpanded by remember { mutableStateOf(replaceQuery.isNotEmpty()) }

    Surface(
        color = Color(0xFFF8FAFC),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Main Search Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = findQuery,
                    onValueChange = onFindQueryChange,
                    placeholder = { Text("Find words or phrases in document...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = WordPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (findQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onFindQueryChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = WordPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("find_input_field")
                )

                // Match count indicator badge
                if (findQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (totalMatchesCount > 0) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                            )
                            .border(
                                1.dp,
                                if (totalMatchesCount > 0) Color(0xFFFCD34D) else Color(0xFFFCA5A5),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("search_match_count_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (totalMatchesCount > 0) {
                                "${currentMatchIndex + 1} of $totalMatchesCount"
                            } else {
                                "0 found"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalMatchesCount > 0) Color(0xFF92400E) else Color(0xFFB91C1C)
                        )
                    }
                }

                // Previous Match Button
                IconButton(
                    onClick = onPrevMatch,
                    enabled = totalMatchesCount > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (totalMatchesCount > 0) Color.White else Color(0xFFF1F5F9))
                        .border(
                            1.dp,
                            if (totalMatchesCount > 0) Color(0xFFCBD5E1) else Color(0xFFE2E8F0),
                            RoundedCornerShape(6.dp)
                        )
                        .testTag("search_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Previous match",
                        tint = if (totalMatchesCount > 0) WordPrimaryDark else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Next Match Button
                IconButton(
                    onClick = onNextMatch,
                    enabled = totalMatchesCount > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (totalMatchesCount > 0) Color.White else Color(0xFFF1F5F9))
                        .border(
                            1.dp,
                            if (totalMatchesCount > 0) Color(0xFFCBD5E1) else Color(0xFFE2E8F0),
                            RoundedCornerShape(6.dp)
                        )
                        .testTag("search_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Next match",
                        tint = if (totalMatchesCount > 0) WordPrimaryDark else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Toggle Replace Drawer Button
                IconButton(
                    onClick = { isReplaceExpanded = !isReplaceExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isReplaceExpanded) Color(0xFFD2E3FC) else Color.White)
                        .border(
                            1.dp,
                            if (isReplaceExpanded) WordPrimary else Color(0xFFCBD5E1),
                            RoundedCornerShape(6.dp)
                        )
                        .testTag("toggle_replace_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Toggle Replace Drawer",
                        tint = if (isReplaceExpanded) WordPrimary else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("search_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable Replace Section
            AnimatedVisibility(visible = isReplaceExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = onReplaceQueryChange,
                            placeholder = { Text("Replace with...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FindReplace,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = WordPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("replace_input_field")
                        )

                        // Replace Current occurrence button
                        OutlinedButton(
                            onClick = onReplaceCurrent,
                            enabled = totalMatchesCount > 0 && findQuery.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("replace_current_button")
                        ) {
                            Text("Replace", fontSize = 12.sp)
                        }

                        // Replace All occurrences button
                        Button(
                            onClick = onReplaceAll,
                            enabled = totalMatchesCount > 0 && findQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = WordPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("replace_all_button")
                        ) {
                            Text("Replace All", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
