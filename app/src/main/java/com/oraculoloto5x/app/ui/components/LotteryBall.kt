package com.oraculoloto5x.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun LotteryBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isSelected: Boolean = false,
    isAnchor: Boolean = false,
    isMatched: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradientBrush = when {
        isMatched -> Brush.radialGradient(
            colors = listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF064E3B))
        )
        isAnchor -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFDF73), Color(0xFFD4AF37), Color(0xFF92400E))
        )
        isSelected -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFDF73), Color(0xFFD4AF37), Color(0xFF78350F))
        )
        else -> Brush.radialGradient(
            colors = listOf(Color(0xFF2A3245), Color(0xFF181F2E), Color(0xFF0D121B))
        )
    }

    val borderColor = when {
        isMatched -> EmeraldAccent
        isAnchor -> GoldLight
        isSelected -> GoldPrimary
        else -> BallBorderGold.copy(alpha = 0.4f)
    }

    val textColor = when {
        isMatched -> Color.White
        isAnchor || isSelected -> Color.White
        else -> TextPrimary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isSelected || isAnchor || isMatched) 8.dp else 4.dp,
                shape = CircleShape,
                ambientColor = if (isAnchor || isSelected) GoldPrimary else Color.Black
            )
            .clip(CircleShape)
            .background(gradientBrush)
            .border(
                width = if (isSelected || isAnchor || isMatched) 2.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number.toString().padStart(2, '0'),
                fontSize = (size.value * 0.42).sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (isAnchor) {
                Text(
                    text = "★",
                    fontSize = (size.value * 0.20).sp,
                    color = GoldLight,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
