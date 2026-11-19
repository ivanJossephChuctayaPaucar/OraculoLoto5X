package com.oraculoloto5x.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.ai.GeminiOracleAdvisor
import com.oraculoloto5x.app.ai.GeminiResult
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.engine.RiskLevel
import com.oraculoloto5x.app.ui.components.BallDetailBottomSheet
import com.oraculoloto5x.app.ui.components.DailyStepGuideCard
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.components.PlanetaryCountdownCard
import com.oraculoloto5x.app.ui.components.SemaphoreCard
import com.oraculoloto5x.app.ui.theme.*

@Composable
fun TodayOracleScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val oracleResult by viewModel.oracleResult.collectAsState()
    val currentHour by viewModel.currentPlanetaryHour.collectAsState()
    val nextFavorable by viewModel.nextFavorableWindow.collectAsState()
    val sunTimes by viewModel.sunTimes.collectAsState()
    val cutoffStatus by viewModel.cutoffStatus.collectAsState()
    val intentionWord by viewModel.intentionWord.collectAsState()
    val selectedLottery by viewModel.selectedLottery.collectAsState()
    val isBallSheetOpen by viewModel.isBallSheetOpen.collectAsState()
    val selectedAnalysis by viewModel.selectedBallForAnalysis.collectAsState()
    val hasPlaySavedToday by viewModel.hasPlaySavedToday.collectAsState()

    // Gemini AI states
    val geminiResult by viewModel.geminiResult.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()

    // Advanced tools collapsible state
    var showAdvancedTools by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Guía interactiva de pasos (Fase 1)
        DailyStepGuideCard(
            riskLevel = oracleResult?.riskAssessment?.riskLevel,
            hasFavorableHour = currentHour.isFavorableWindow,
            hasOracleResult = oracleResult != null,
            hasPlaySaved = hasPlaySavedToday
        )

        // ── PASO 1: STATUS DEL DÍA Y RELOJ PLANETARIO CUSCO ──────────────────
        oracleResult?.riskAssessment?.let { assessment ->
            SemaphoreCard(assessment = assessment)
        }

        PlanetaryCountdownCard(
            currentHour = currentHour,
            nextFavorable = nextFavorable,
            sunTimes = sunTimes,
            cutoffStatus = cutoffStatus
        )

        // ── PASO 2: HERO CARD - JUGADA MAESTRA ──────────────────────────────
        oracleResult?.let { res ->
            val riskBorderColor = when (res.riskAssessment.riskLevel) {
                RiskLevel.VERDE -> EmeraldAccent
                RiskLevel.AMARILLO -> GoldPrimary
                RiskLevel.ROJO -> SemaphoreRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2638), DarkSurface)
                        )
                    )
                    .border(1.5.dp, riskBorderColor, RoundedCornerShape(18.dp))
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
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Toca cualquier bolilla para ver su origen en los 5 libros",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.saveCurrentPlay() }
                        ) {
                            Icon(
                                imageVector = if (hasPlaySavedToday) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                                contentDescription = "Guardar Jugada",
                                tint = if (hasPlaySavedToday) EmeraldAccent else GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Esferas doradas de la jugada maestra
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
                                onClick = { viewModel.openBallSheet(ball) }
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botones de acción principales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.generateOraclePlay() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recalcular", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.saveCurrentPlay() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (hasPlaySavedToday) EmeraldDark else Color(0xFF263345),
                                contentColor = if (hasPlaySavedToday) EmeraldLight else GoldLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasPlaySavedToday) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasPlaySavedToday) "Guardada ✓" else "Guardar Jugada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ── ACORDEÓN HERRAMIENTAS AVANZADAS (Rueda 9 + Gematría) ─────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, Color(0xFF263345), RoundedCornerShape(14.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedTools = !showAdvancedTools }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "HERRAMIENTAS AVANZADAS (Rueda 9 y Gematría)",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showAdvancedTools) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(
                    visible = showAdvancedTools,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Palabra/Intención Gematría
                        Column {
                            Text(
                                text = "Palabra de Intención (Gematría Caldea):",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = intentionWord,
                                onValueChange = { viewModel.setIntentionWord(it) },
                                placeholder = { Text("Ej. PROSPERIDAD, ABUNDANCIA...", color = TextMuted, fontSize = 12.sp) },
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
                                Text(
                                    text = "Valor Caldeo: $gematriaSum  ·  Bolilla ancla: #$anchor",
                                    color = EmeraldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        // Rueda reducida 9 bolillas
                        oracleResult?.let { res ->
                            Column {
                                Text(
                                    text = "Rueda Reducida (Top 9 Bolillas Afines):",
                                    color = EmeraldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val chunked = res.reducedWheel9.chunked(5)
                                for (row in chunked) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        for (ball in row) {
                                            LotteryBall(
                                                number = ball,
                                                size = 42.dp,
                                                onClick = { viewModel.openBallSheet(ball) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── PASO 3: TARJETA DEL ORÁCULO IA (GOOGLE GEMINI) ───────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF142033), Color(0xFF0F1726))
                    )
                )
                .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "ORÁCULO HERMÉTICO GEMINI",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = GeminiOracleAdvisor.getSelectedModel(context),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "Sintetiza la sabiduría de los 5 tratados con tu perfil natal, la hora astronómica en Cusco y tus bolillas de hoy.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Botón de consulta
                Button(
                    onClick = { viewModel.consultGemini() },
                    enabled = !isGeminiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground,
                        disabledContainerColor = GoldDark,
                        disabledContentColor = DarkBackground.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isGeminiLoading) {
                        CircularProgressIndicator(
                            color = DarkBackground,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sintetizando tratados con Gemini...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consultar al Oráculo Gemini", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                // Resultado o mensajes de estado
                when (val res = geminiResult) {
                    is GeminiResult.Success -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C121D))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (res.fromCache) {
                                    Text(
                                        text = "⚡ Respuesta leída desde caché local offline",
                                        color = EmeraldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = res.text,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                    is GeminiResult.NoApiKey -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF263345))
                                .clickable { viewModel.setProfileDialogOpen(true) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "API Key no configurada",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Toca aquí para configurar tu clave de Google AI Studio en Ajustes.",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    is GeminiResult.Error -> {
                        Text(
                            text = "⚠ ${res.message}",
                            color = SemaphoreRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    null -> {
                        // Sin consulta aún
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal BottomSheet de detalle de bolilla
    if (isBallSheetOpen && selectedAnalysis != null) {
        BallDetailBottomSheet(
            analysis = selectedAnalysis!!,
            anchorBall = oracleResult?.intentionAnchorBall,
            onDismiss = { viewModel.closeBallSheet() }
        )
    }
}
