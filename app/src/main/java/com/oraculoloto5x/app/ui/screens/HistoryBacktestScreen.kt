package com.oraculoloto5x.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.data.models.DrawResult
import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.data.models.SavedPlay
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.theme.*
import java.time.LocalDate

@Composable
fun HistoryBacktestScreen(
    viewModel: MainViewModel
) {
    val savedPlays by viewModel.savedPlays.collectAsState()
    val drawHistory by viewModel.drawHistory.collectAsState()
    val selectedLottery by viewModel.selectedLottery.collectAsState()
    val isAddDrawOpen by viewModel.isAddDrawDialogOpen.collectAsState()

    val totalPlays = savedPlays.size
    val maxAciertos = savedPlays.maxOfOrNull { it.matchedCount } ?: 0
    val totalWithPrize = savedPlays.count { it.matchedCount >= 2 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HISTORIAL Y BACKTESTING",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rendimiento real de los 5 tratados en SQLite",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { viewModel.setAddDrawDialogOpen(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Sorteo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tarjetas KPI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Jugadas",
                value = totalPlays.toString(),
                subtitle = "Guardadas",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Mayor Acierto",
                value = "$maxAciertos",
                subtitle = "Bolillas",
                modifier = Modifier.weight(1f),
                valueColor = if (maxAciertos >= 4) EmeraldLight else GoldLight
            )
            KpiCard(
                title = "Con Aciertos",
                value = "$totalWithPrize",
                subtitle = "≥ 2 Bolillas",
                modifier = Modifier.weight(1f)
            )
        }

        // Sección: Jugadas Auditadas (Backtesting)
        Text(
            text = "JUGADAS AUDITADAS FRENTE A RESULTADOS REALES",
            color = GoldLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        if (savedPlays.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no has guardado jugadas. Ve a la pestaña 'Hoy' y pulsa el icono de guardar en la Jugada Maestra.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (play in savedPlays) {
                    SavedPlayBacktestCard(play = play)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sección: Historial Oficial de Sorteos
        Text(
            text = "SORTEOS OFICIALES REGISTRADOS (${selectedLottery.displayName})",
            color = GoldLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (draw in drawHistory) {
                OfficialDrawCard(draw = draw)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (isAddDrawOpen) {
        AddDrawDialog(
            lotteryType = selectedLottery,
            onDismiss = { viewModel.setAddDrawDialogOpen(false) },
            onSave = { date, balls ->
                viewModel.insertOfficialDraw(selectedLottery, date, balls)
                viewModel.setAddDrawDialogOpen(false)
            }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: Color = GoldLight
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = TextSecondary, fontSize = 10.sp)
            Text(text = value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun SavedPlayBacktestCard(play: SavedPlay) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (play.matchedCount >= 3) EmeraldAccent else GoldDark.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sorteo: ${play.targetDrawDate} (${play.lotteryType.displayName})",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (play.matchedCount >= 3) EmeraldDark else Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${play.matchedCount} ACIERTOS",
                        color = if (play.matchedCount >= 3) EmeraldLight else GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bolillas de la jugada, resaltando en esmeralda las acertadas
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (ball in play.balls) {
                    val isMatched = ball in play.matchedBalls
                    LotteryBall(
                        number = ball,
                        size = 38.dp,
                        isMatched = isMatched
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Premio: ${play.estimatedPrize}",
                    color = if (play.matchedCount >= 3) EmeraldLight else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Generada en Hora de ${play.planetaryHour}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun OfficialDrawCard(draw: DrawResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(0.5.dp, Color(0xFF263345), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fecha: ${draw.drawDate}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = draw.formattedBalls,
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${draw.winningBalls.size} bolillas",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun AddDrawDialog(
    lotteryType: LotteryType,
    onDismiss: () -> Unit,
    onSave: (LocalDate, List<Int>) -> Unit
) {
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) }
    var ballsInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldAccent, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REGISTRAR RESULTADO OFICIAL",
                    color = EmeraldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${lotteryType.displayName} (${lotteryType.totalBallsToPick} bolillas del 1 al ${lotteryType.maxBallNumber})",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ballsInput,
                    onValueChange = { ballsInput = it },
                    label = { Text("Bolillas ganadoras (separadas por coma)") },
                    placeholder = { Text("Ej. 4, 12, 19, 23, 37, 45") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage, color = SemaphoreRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val parsedDate = try {
                                LocalDate.parse(dateStr.trim())
                            } catch (e: Exception) {
                                errorMessage = "Formato de fecha inválido (use AAAA-MM-DD)"
                                return@Button
                            }

                            val balls = ballsInput
                                .split(",", " ", "-", ";")
                                .mapNotNull { it.trim().toIntOrNull() }
                                .distinct()

                            if (balls.size != lotteryType.totalBallsToPick) {
                                errorMessage = "Debe ingresar exactamente ${lotteryType.totalBallsToPick} bolillas distintas."
                                return@Button
                            }

                            if (balls.any { it !in 1..lotteryType.maxBallNumber }) {
                                errorMessage = "Las bolillas deben estar entre 1 y ${lotteryType.maxBallNumber}."
                                return@Button
                            }

                            onSave(parsedDate, balls)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAccent,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
