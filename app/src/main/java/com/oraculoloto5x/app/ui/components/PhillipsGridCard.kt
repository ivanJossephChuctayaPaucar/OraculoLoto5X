package com.oraculoloto5x.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.data.knowledge.PhillipsAnalysis
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun PhillipsGridCard(
    analysis: PhillipsAnalysis,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "CUADRÍCULA PITAGÓRICA 3x3 (David A. Phillips)",
                color = GoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Plano Mental (3-6-9) | Plano Emocional (2-5-8) | Plano Físico (1-4-7)",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Matriz 3x3
            // Fila 1: 3, 6, 9
            // Fila 2: 2, 5, 8
            // Fila 3: 1, 4, 7
            val gridLayout = listOf(
                listOf(3, 6, 9),
                listOf(2, 5, 8),
                listOf(1, 4, 7)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, GoldDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in gridLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (digit in row) {
                            val count = analysis.digitCounts[digit] ?: 0
                            val isPresent = count > 0

                            val cellBg = if (isPresent) Color(0xFF2E394E) else Color(0xFF131720)
                            val cellBorder = if (isPresent) GoldPrimary else Color(0xFF232B3B)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .border(1.dp, cellBorder, RoundedCornerShape(8.dp))
                            ) {
                                if (isPresent) {
                                    val textRep = (1..count).joinToString("") { digit.toString() }
                                    Text(
                                        text = textRep,
                                        color = if (digit == 7) EmeraldLight else GoldLight,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (count > 2) 13.sp else 16.sp
                                    )
                                } else {
                                    Text(
                                        text = "• ($digit)",
                                        color = TextMuted.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Análisis de Flechas y Número Regente
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Número Regente Phillips: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "#${analysis.rulingNumber} (Adaptabilidad y Liderazgo)",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (analysis.arrowOfFrustration) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF451A03))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠ Flecha de la Frustración (Ausencia 4-5-6): Las bolillas 4, 5 y 6 actúan como estabilizadores y balanceadores energéticos.",
                            color = SemaphoreYellow,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (analysis.hasIsolated7) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF064E3B))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✦ 7 Aislado Natal: Gran canal de intuición e introspección profunda. Las bolillas de la tríada {1, 5, 7} despiertan este magnetismo.",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
