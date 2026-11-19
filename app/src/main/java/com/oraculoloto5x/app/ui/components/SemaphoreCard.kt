package com.oraculoloto5x.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.engine.DailyRiskAssessment
import com.oraculoloto5x.app.engine.RiskLevel
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun SemaphoreCard(
    assessment: DailyRiskAssessment,
    modifier: Modifier = Modifier
) {
    val (primaryColor, gradientColors, icon) = when (assessment.riskLevel) {
        RiskLevel.VERDE -> Triple(
            SemaphoreGreen,
            listOf(Color(0xFF064E3B).copy(alpha = 0.5f), DarkSurface),
            Icons.Default.CheckCircle
        )
        RiskLevel.AMARILLO -> Triple(
            SemaphoreYellow,
            listOf(Color(0xFF78350F).copy(alpha = 0.5f), DarkSurface),
            Icons.Default.Info
        )
        RiskLevel.ROJO -> Triple(
            SemaphoreRed,
            listOf(Color(0xFF7F1D1D).copy(alpha = 0.5f), DarkSurface),
            Icons.Default.Warning
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(1.dp, primaryColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assessment.title,
                        color = primaryColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Día Personal Activo: #${assessment.personalDay}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = assessment.explanation,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "Consejo: ",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assessment.actionableAdvice,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
