package com.oraculoloto5x.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.engine.RiskLevel
import com.oraculoloto5x.app.ui.theme.*

data class OracleStep(
    val number: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@Composable
fun DailyStepGuideCard(
    riskLevel: RiskLevel?,
    hasFavorableHour: Boolean,
    hasOracleResult: Boolean,
    hasPlaySaved: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val steps = listOf(
        OracleStep(
            number = 1,
            title = "Revisa el Semáforo del Día",
            description = when (riskLevel) {
                RiskLevel.VERDE -> "✓ Día de alta afinidad solar. Condiciones favorables."
                RiskLevel.AMARILLO -> "Día moderado. Juega con prudencia y en hora afín."
                RiskLevel.ROJO -> "⚠ Tensión kármica hoy. Espera una hora del Sol o Luna."
                null -> "Consulta si hoy es un día favorable para tu serie natal."
            },
            icon = Icons.Default.TravelExplore,
            isCompleted = riskLevel != null,
            isActive = riskLevel == null
        ),
        OracleStep(
            number = 2,
            title = "Elige la Hora Planetaria",
            description = if (hasFavorableHour)
                "✓ Estás en o cerca de una ventana del Sol / Luna."
            else
                "Espera la próxima Hora del Sol (☉) o Luna (☽) para comprar tu boleto.",
            icon = Icons.Default.Schedule,
            isCompleted = hasFavorableHour,
            isActive = !hasFavorableHour && riskLevel != null
        ),
        OracleStep(
            number = 3,
            title = "Genera la Jugada Maestra",
            description = if (hasOracleResult)
                "✓ Jugada generada con 10,000 simulaciones Monte Carlo y los 5 tratados."
            else
                "Pulsa el botón dorado para invocar el motor de los 5 tratados herméticos.",
            icon = Icons.Default.AutoAwesome,
            isCompleted = hasOracleResult,
            isActive = !hasOracleResult && riskLevel != null
        ),
        OracleStep(
            number = 4,
            title = "Guarda y Registra",
            description = if (hasPlaySaved)
                "✓ Jugada registrada en SQLite. Cuando salga el sorteo, ingresa el resultado."
            else
                "Pulsa el ícono de guardar ♡ en la Jugada Maestra para hacer Backtest.",
            icon = Icons.Default.BookmarkAdd,
            isCompleted = hasPlaySaved,
            isActive = hasOracleResult && !hasPlaySaved
        )
    )

    val completedCount = steps.count { it.isCompleted }
    val progressFraction = completedCount.toFloat() / steps.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2235), DarkSurface)
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column {
            // Cabecera plegable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GUÍA DEL ORÁCULO — HOY",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$completedCount de ${steps.size} pasos completados",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Indicador circular de progreso
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (completedCount == steps.size) EmeraldDark
                            else Color(0xFF263345)
                        )
                        .border(2.dp, GoldPrimary, CircleShape)
                ) {
                    Text(
                        text = if (completedCount == steps.size) "✓" else "$completedCount/${steps.size}",
                        color = if (completedCount == steps.size) EmeraldLight else GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF263345))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldPrimary, EmeraldAccent)
                            )
                        )
                )
            }

            // Pasos expandibles
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (step in steps) {
                        StepRow(step = step)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: OracleStep) {
    val dotColor = when {
        step.isCompleted -> EmeraldAccent
        step.isActive -> GoldPrimary
        else -> TextMuted
    }

    val textColor = when {
        step.isCompleted -> TextSecondary
        step.isActive -> TextPrimary
        else -> TextMuted
    }

    val bgColor = when {
        step.isActive -> Color(0xFF1E2D45).copy(alpha = 0.8f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = if (step.isActive) 10.dp else 0.dp, vertical = if (step.isActive) 8.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número del paso
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (step.isCompleted) EmeraldDark else Color(0xFF263345))
                .border(1.dp, dotColor, CircleShape)
        ) {
            if (step.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.number.toString(),
                    color = if (step.isActive) GoldLight else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                color = dotColor,
                fontSize = 12.sp,
                fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = step.description,
                color = textColor,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        if (step.isActive) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = GoldPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
