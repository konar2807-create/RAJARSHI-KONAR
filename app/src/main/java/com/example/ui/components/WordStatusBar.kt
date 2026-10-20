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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentStats
import com.example.data.model.ViewMode
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark

/**
 * Word-style Bottom Status Bar displaying:
 * - Real-time word count and character count indicator
 * - Page count and reading time
 * - Quick stats tap target to open detailed Document Statistics
 * - Document save status (Saved / Saving...)
 * - View mode switcher (Print Layout vs Mobile Flow)
 * - Zoom controls (- / percentage / +)
 */
@Composable
fun WordStatusBar(
    stats: DocumentStats,
    viewMode: ViewMode,
    onSetViewMode: (ViewMode) -> Unit,
    zoomPercent: Int,
    onSetZoom: (Int) -> Unit,
    onOpenStats: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "save_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "save_rotation"
    )

    Surface(
        color = WordPrimaryDark,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 10.dp)
        ) {
            // Left Group: Page info & Real-time Word / Character Count Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState())
            ) {
                // Page Number
                Text(
                    text = "Page 1 of ${stats.pages}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("status_bar_page_count")
                )

                Text(
                    text = "•",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )

                // Real-time Word & Character Count Interactive Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .clickable { onOpenStats() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("status_bar_stats_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Document Statistics",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))

                    // Word Count
                    Text(
                        text = "${stats.words} ${if (stats.words == 1) "word" else "words"}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("status_bar_word_count")
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "|",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    // Character Count
                    Text(
                        text = "${stats.characters} chars",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("status_bar_char_count")
                    )
                }

                // Save status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    if (isSaving) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Saving document...",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier
                                .size(12.dp)
                                .rotate(rotation)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Saving...",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Saved",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Saved",
                            color = Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Group: View Mode Switcher & Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // View Mode Toggle (Print Layout / Mobile Flow)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(2.dp)
                        .testTag("status_bar_view_mode")
                ) {
                    // Print Layout Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == ViewMode.PRINT_LAYOUT) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onSetViewMode(ViewMode.PRINT_LAYOUT) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pageview,
                            contentDescription = "Print Layout",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Mobile Flow Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == ViewMode.MOBILE_FLOW) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onSetViewMode(ViewMode.MOBILE_FLOW) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = "Mobile View",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Zoom Out Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSetZoom(zoomPercent - 10) }
                        .testTag("status_bar_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Zoom Percentage Display
                Text(
                    text = "${zoomPercent}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSetZoom(100) }
                        .padding(horizontal = 2.dp)
                        .testTag("status_bar_zoom_text")
                )

                // Zoom In Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSetZoom(zoomPercent + 10) }
                        .testTag("status_bar_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
