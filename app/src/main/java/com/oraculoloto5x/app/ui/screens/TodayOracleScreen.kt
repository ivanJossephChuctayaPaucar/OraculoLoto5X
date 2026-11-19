package com.oraculoloto5x.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.components.PlanetaryCountdownCard
import com.oraculoloto5x.app.ui.components.SemaphoreCard
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun TodayOracleScreen(
    viewModel: MainViewModel,
    onNavigateToAnalysis: (Int) -> Unit
) {
    val oracleResult by viewModel.oracleResult.collectAsState()
    val currentHour by viewModel.currentPlanetaryHour.collectAsState()
    val nextFavorable by viewModel.nextFavorableWindow.collectAsState()
    val sunTimes by viewModel.sunTimes.collectAsState()
    val cutoffStatus by viewModel.cutoffStatus.collectAsState()
    val intentionWord by viewModel.intentionWord.collectAsState()
    val selectedLottery by viewModel.selectedLottery.collectAsState()

    var showSaveToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Semáforo de Riesgo Diario
        oracleResult?.riskAssessment?.let { assessment ->
            SemaphoreCard(assessment = assessment)
        }

        // 2. Reloj Planetario en Cusco con cuenta regresiva
        PlanetaryCountdownCard(
            currentHour = currentHour,
            nextFavorable = nextFavorable,
            sunTimes = sunTimes,
            cutoffStatus = cutoffStatus
        )

        // 3. Palabra/Intención del Día (Gematría Caldea)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "PALABRA O INTENCIÓN DEL DÍA (Gematría Caldea)",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Escribe una intención para anclar una bolilla comodín en la simulación",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = intentionWord,
                    onValueChange = { viewModel.setIntentionWord(it) },
                    placeholder = { Text("Ej. ABUNDANCIA, PROSPERIDAD...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (intentionWord.isNotBlank()) {
                    val gematriaSum = NumerologyKnowledgeBase.calculateChaldeanGematria(intentionWord)
                    val maxBall = selectedLottery.maxBallNumber
                    val anchor = if (gematriaSum in 1..maxBall) gematriaSum else ((gematriaSum - 1) % maxBall) + 1

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Valor Caldeo: $gematriaSum",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Bolilla Ancla Comodín: #$anchor",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Botón de Generación de Jugada Oráculo (Monte Carlo 10,000 iteraciones)
        Button(
            onClick = {
                viewModel.generateOraclePlay()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = DarkBackground
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONSULTAR ORÁCULO (10,000 MONTE CARLO)",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 5. Jugada Maestra
        oracleResult?.let { res ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2638), DarkSurface)
                        )
                    )
                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "JUGADA MAESTRA: ${selectedLottery.displayName}",
                                color = GoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Filtrada por Campana de Gauss y 5 Tratados",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.saveCurrentPlay()
                                showSaveToast = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Guardar Jugada",
                                tint = GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bolillas de la jugada maestra
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (ball in res.masterPlay) {
                            val isAnchor = ball == res.intentionAnchorBall
                            LotteryBall(
                                number = ball,
                                size = 50.dp,
                                isAnchor = isAnchor,
                                onClick = { onNavigateToAnalysis(ball) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Métricas de Gauss
                    val sum = res.masterPlay.sum()
                    val evens = res.masterPlay.count { it % 2 == 0 }
                    val odds = res.masterPlay.size - evens
                    val decades = res.masterPlay.map { it / 10 }.distinct().size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Suma: $sum", color = TextPrimary, fontSize = 11.sp)
                        Text(text = "Par/Imp: $evens-$odds", color = TextPrimary, fontSize = 11.sp)
                        Text(text = "Decenas: $decades distintas", color = TextPrimary, fontSize = 11.sp)
                    }

                    if (showSaveToast) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Jugada registrada en SQLite para Backtesting",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 6. Rueda Reducida (9 Bolillas de Mayor Puntaje)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, EmeraldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "RUEDA REDUCIDA (TOP 9 BOLILLAS AFINES)",
                        color = EmeraldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Combinatoria recomendada para jugadas múltiples o sistemas",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Renderizado de las 9 bolillas
                    val chunked = res.reducedWheel9.chunked(5)
                    for (row in chunked) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            for (ball in row) {
                                LotteryBall(
                                    number = ball,
                                    size = 44.dp,
                                    onClick = { onNavigateToAnalysis(ball) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
