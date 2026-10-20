package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WordPrimary
import com.example.ui.theme.WordPrimaryDark

@Composable
fun WordCraftLogo(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.22f
    val fontSize = (size.value * 0.52f).sp

    Box(
        modifier = modifier
            .size(size)
    ) {
        // Backing document sheet with gradient & subtle border
        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .align(Alignment.BottomEnd)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(cornerRadius * 0.85f))
                .clip(RoundedCornerShape(cornerRadius * 0.85f))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFDCE6F5))
                    )
                )
                .border(0.75.dp, Color(0xFFB8CAE4), RoundedCornerShape(cornerRadius * 0.85f))
        ) {
            // Fold corner indicator
            Box(
                modifier = Modifier
                    .size(size * 0.24f)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 3.dp))
                    .background(Color(0xFFBACDE8))
            )
        }

        // Front blue "W" tile with gradient and drop shadow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.78f)
                .align(Alignment.TopStart)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2673E8), WordPrimary, Color(0xFF0C387A))
                    )
                )
                .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(cornerRadius))
        ) {
            Text(
                text = "W",
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = Color.White
            )
        }
    }
}
