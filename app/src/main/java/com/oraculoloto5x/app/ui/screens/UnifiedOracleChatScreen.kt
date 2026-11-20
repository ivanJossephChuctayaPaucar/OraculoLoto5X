package com.oraculoloto5x.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.ai.HermeticKnowledgeRetriever
import com.oraculoloto5x.app.ai.ParsedCitationsResult
import com.oraculoloto5x.app.data.models.ChatMessage
import com.oraculoloto5x.app.engine.RiskLevel
import com.oraculoloto5x.app.pdf.BookCitation
import com.oraculoloto5x.app.pdf.PdfPageViewerDialog
import com.oraculoloto5x.app.ui.components.BallDetailBottomSheet
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedOracleChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val oracleResult by viewModel.oracleResult.collectAsState()
    val currentHour by viewModel.currentPlanetaryHour.collectAsState()
    val hasSavedPlay by viewModel.hasPlaySavedToday.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val isBallSheetOpen by viewModel.isBallSheetOpen.collectAsState()
    val selectedAnalysis by viewModel.selectedBallForAnalysis.collectAsState()
    val activePdfCitation by viewModel.activePdfCitation.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    var isHeroExpanded by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Auto-colapso al interactuar con el foco o hacer scroll en el chat
    val inputInteractionSource = remember { MutableInteractionSource() }
    val isInputFocused by inputInteractionSource.collectIsFocusedAsState()

    LaunchedEffect(isInputFocused) {
        if (isInputFocused) {
            isHeroExpanded = false
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && listState.firstVisibleItemIndex > 0) {
            isHeroExpanded = false
        }
    }

    // Scroll automático al último mensaje al cambiar la lista o el estado de carga
    LaunchedEffect(chatMessages.size, isChatLoading) {
        val totalItems = chatMessages.size + (if (isChatLoading) 1 else 0)
        if (totalItems > 0) {
            if (chatMessages.isNotEmpty()) {
                isHeroExpanded = false
            }
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val quickSuggestions = remember {
        listOf(
            "¿Por qué salieron estas 6 bolillas?",
            "¿A qué hora me conviene sellar hoy?",
            "Analiza mi jugada según Cheiro y Casa 5",
            "¿Cuál fue el último sorteo de La Tinka?"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── HERO CARD AUTO-COLAPSABLE (6 BOLILLAS + SEMÁFORO + RELOJ) ───────────
        oracleResult?.let { res ->
            val riskColor = when (res.riskAssessment.riskLevel) {
                RiskLevel.VERDE -> EmeraldAccent
                RiskLevel.AMARILLO -> GoldPrimary
                RiskLevel.ROJO -> SemaphoreRed
            }

            AnimatedVisibility(
                visible = isHeroExpanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                // ESTADO EXPANDIDO
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Fila de encabezado con botón para colapsar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "JUGADA MAESTRA DE HOY",
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            IconButton(
                                onClick = { isHeroExpanded = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Colapsar",
                                    tint = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6 Bolillas grandes de La Tinka
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (ball in res.masterPlay) {
                                val isAnchor = ball == res.intentionAnchorBall
                                LotteryBall(
                                    number = ball,
                                    size = 46.dp,
                                    isAnchor = isAnchor,
                                    onClick = { viewModel.openBallSheet(ball) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Línea compacta de contexto (Semáforo + Luna + Hora Cusco)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(riskColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = res.riskAssessment.riskLevel.name,
                                color = riskColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "  ·  ", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "☽ ${res.moonInfo.displaySummary}",
                                color = GoldLight,
                                fontSize = 11.sp
                            )
                            Text(text = "  ·  ", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "🪐 ${currentHour.planet.spanishName} (${currentHour.formattedInterval})",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botones táctiles Recalcular y Guardar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.generateOraclePlay() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recalcular",
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.saveCurrentPlay() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasSavedPlay) EmeraldAccent else GoldPrimary
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasSavedPlay) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasSavedPlay) "Guardada" else "Guardar Jugada",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ESTADO COLAPSADO (Barra ultracompacta de 1 línea para dejar 80%+ de pantalla al chat)
            AnimatedVisibility(
                visible = !isHeroExpanded,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(150)) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Semáforo punto
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 6 bolillas miniatura
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (ball in res.masterPlay) {
                                LotteryBall(
                                    number = ball,
                                    size = 28.dp,
                                    isAnchor = (ball == res.intentionAnchorBall),
                                    onClick = { viewModel.openBallSheet(ball) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón rápido de refrescar
                        IconButton(
                            onClick = { viewModel.generateOraclePlay() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalcular",
                                tint = GoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Botón expandir
                        IconButton(
                            onClick = { isHeroExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir Jugada",
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Mensaje discreto de sincronización si aplica
        syncMessage?.let { msg ->
            Text(
                text = msg,
                color = GoldLight,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        // ── CHAT FEED CON CITAS PDF CLICKEABLES ─────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatMessages.isEmpty() && !isChatLoading) {
                // Bienvenida Hermética cuando no hay historial
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF3B2E15), DarkSurfaceVariant)))
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "✦ Oráculo Hermético Loto 5X",
                        color = GoldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Las 6 bolillas fueron calculadas con 10,000 iteraciones Monte Carlo, distribución de Gauss y los 5 tratados de numerología. Pregúntame sobre la fundamentación, mejores horas o analiza números específicos.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatMessageItem(
                            message = message,
                            onOpenCitation = { citation: BookCitation ->
                                viewModel.openPdfCitation(citation)
                            }
                        )
                    }

                    if (isChatLoading) {
                        item {
                            OracleTypingIndicator()
                        }
                    }
                }
            }
        }

        // ── FILA DE CHIPS DE SUGERENCIA RÁPIDA ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (suggestion in quickSuggestions) {
                SuggestionChip(
                    onClick = {
                        isHeroExpanded = false
                        focusManager.clearFocus()
                        viewModel.sendChatMessage(suggestion)
                    },
                    label = {
                        Text(
                            text = suggestion,
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = DarkSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // ── CAJA DE TEXTO PARA PREGUNTAR AL ORÁCULO ────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f),
                    placeholder = {
                        Text(
                            text = "Pregunta al Oráculo sobre tu jugada...",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    },
                    interactionSource = inputInteractionSource,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BallBorderGold.copy(alpha = 0.4f),
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val text = inputText.trim()
                            if (text.isNotEmpty() && !isChatLoading) {
                                inputText = ""
                                isHeroExpanded = false
                                focusManager.clearFocus()
                                viewModel.sendChatMessage(text)
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = inputText.isNotBlank() && !isChatLoading
                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty() && !isChatLoading) {
                            inputText = ""
                            isHeroExpanded = false
                            focusManager.clearFocus()
                            viewModel.sendChatMessage(text)
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) GoldPrimary else DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = if (canSend) Color.Black else TextSecondary
                    )
                }
            }
        }
    }

    // ── DIÁLOGO DIRECTO DEL VISOR DE PÁGINAS PDF ────────────────────────────────
    activePdfCitation?.let { citation ->
        PdfPageViewerDialog(
            citation = citation,
            onDismiss = { viewModel.closePdfCitation() }
        )
    }

    // ── MODAL BOTTOM SHEET DE DETALLE DE BOLILLA ────────────────────────────────
    if (isBallSheetOpen && selectedAnalysis != null) {
        BallDetailBottomSheet(
            analysis = selectedAnalysis!!,
            anchorBall = oracleResult?.intentionAnchorBall,
            onDismiss = { viewModel.closeBallSheet() }
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onOpenCitation: (BookCitation) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // Burbuja Usuario
            Surface(
                color = Color(0xFF1E2838),
                shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            // Burbuja Oráculo
            val parsed = remember(message.text) {
                HermeticKnowledgeRetriever.parseCitationsFromText(message.text)
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E2413))
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ω",
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = Color(0xFF121824),
                    shape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = parsed.cleanedText,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        // Renderizado de botones interactivos para cada cita PDF encontrada
                        if (parsed.citations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "TRATADOS CITADOS:",
                                color = GoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (citation in parsed.citations) {
                                    AssistChip(
                                        onClick = { onOpenCitation(citation) },
                                        label = {
                                            Text(
                                                text = "📖 ${citation.bookTitle} · Pág. ${citation.pdfPageIndex + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = GoldLight
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFF1C2536)
                                        ),
                                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatMessageTime(message.timestamp),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OracleTypingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E2413))
                .border(1.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ω",
                color = GoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = Color(0xFF121824),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consultando al Oráculo hermético...",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val zone = ZoneId.of("America/Lima")
        val zdt = ZonedDateTime.ofInstant(instant, zone)
        zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        ""
    }
}
