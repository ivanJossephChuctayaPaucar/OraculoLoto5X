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
import com.oraculoloto5x.app.data.knowledge.PyramidPeak
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun PyramidViewCard(
    pyramids: List<PyramidPeak>,
    currentAge: Int = 35,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, EmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "LAS 4 PIRÁMIDES Y CÚSPIDES DE VIDA (Phillips)",
                color = EmeraldLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Edad actual aproximada: $currentAge años. El ciclo de 9 años rige las cúspides.",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (p in pyramids) {
                    val isActive = currentAge in (p.peakAge - 4)..(p.peakAge + 4)
                    val cardBg = if (isActive) Color(0xFF0F382A) else Color.Black.copy(alpha = 0.4f)
                    val cardBorder = if (isActive) EmeraldAccent else Color(0xFF263345)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cardBg)
                            .border(if (isActive) 1.5.dp else 0.5.dp, cardBorder, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Pico #${p.peakNumber}",
                                color = if (isActive) EmeraldLight else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${p.peakAge} años",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = p.vibrationNumber.toString(),
                                color = if (isActive) GoldLight else TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (isActive) {
                                Text(
                                    text = "ACTIVA",
                                    color = EmeraldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val activePinnacle = pyramids.find { currentAge in (it.peakAge - 4)..(it.peakAge + 4) } ?: pyramids[1]
            Text(
                text = "✦ Cúspide Activa (#${activePinnacle.peakNumber} - Vibración ${activePinnacle.vibrationNumber}): ${activePinnacle.description}. Respalda especialmente bolillas con raíz o compuesto ${activePinnacle.vibrationNumber}.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
