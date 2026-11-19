package com.oraculoloto5x.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.pdf.BookCitation
import com.oraculoloto5x.app.pdf.BookCitationRegistry
import com.oraculoloto5x.app.pdf.NativePdfManager
import com.oraculoloto5x.app.pdf.PdfPageViewerDialog
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.components.PyramidViewCard
import com.oraculoloto5x.app.ui.theme.*
import java.time.LocalDate

@Composable
fun FiveBooksAnalysisScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val oracleResult by viewModel.oracleResult.collectAsState()
    val selectedBallAnalysis by viewModel.selectedBallForAnalysis.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val pdfBooksLinked by viewModel.pdfBooksLinked.collectAsState()

    var activePdfCitation by remember { mutableStateOf<BookCitation?>(null) }

    // SAF folder picker
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            NativePdfManager.saveTreeUri(context, uri)
            viewModel.refreshPdfCount()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPdfCount()
    }

    val phillipsAnalysis = remember(userProfile.birthDate) {
        NumerologyKnowledgeBase.analyzePhillipsGrid(userProfile.birthDate)
    }

    val currentAge = remember(userProfile.birthDate) {
        LocalDate.now().year - userProfile.birthDate.year
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ── BARRA SUPERIOR: TÍTULO Y BOTÓN VINCULAR PDFS ─────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BIBLIOTECA HERMÉTICA (5 TRATADOS)",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Citas textuales y visor de páginas originales",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Botón Vincular PDFs
            FilledTonalButton(
                onClick = { folderPicker.launch(null) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (pdfBooksLinked >= 5) EmeraldDark else Color(0xFF1E2838),
                    contentColor = if (pdfBooksLinked >= 5) EmeraldLight else GoldLight
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (pdfBooksLinked >= 5) Icons.Default.CheckCircle else Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (pdfBooksLinked > 0) "$pdfBooksLinked/5 PDFs" else "Vincular PDFs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── CARRUSEL HORIZONTAL DE LOS 5 LIBROS (ESTILO TOMO ANTIGUO) ─────────
        Text(
            text = "TRATADOS HERMÉTICOS (Toca un libro para abrir su visor):",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(BookCitationRegistry.getBookCovers()) { bookCover ->
                BookTomeCard(
                    citation = bookCover,
                    onClick = { activePdfCitation = bookCover }
                )
            }
        }

        // ── SECCIÓN: TUS ANCLAS NATALES ───────────────────────────────────────
        Text(
            text = "TUS ANCLAS NATALES (Perfil de Iván Josseph Chuctaya):",
            color = GoldPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        // 1. Tarjeta Sendero 37/10 de Dan Millman (Clickable para abrir pág. 144)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1B2436), DarkSurface)
                    )
                )
                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { activePdfCitation = BookCitationRegistry.MILLMAN_SENDERO_37_10 }
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldDark)
                        .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "37/10",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sendero de Vida 37/10 (Dan Millman)",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "📖 Pág. 145",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "El Creador Visionario: mente analítica y creativa que sintetiza conocimiento profundo.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = "Toca para abrir la página 144 del tratado original en PDF",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 2. Tarjeta Casa 5 Agathe Tuche (Westcott p.63) - Clickable para abrir pág. 63
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF142436), DarkSurface)
                    )
                )
                .border(1.dp, EmeraldAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { activePdfCitation = BookCitationRegistry.WESTCOTT_CASA_5_AGATHE_TUCHE }
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldDark)
                        .border(1.dp, EmeraldAccent, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "5 y 6",
                        color = EmeraldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Casa 5: Agathe Tuche (Westcott)",
                            color = EmeraldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "📖 Pág. 63",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Agathe Tuche (\"Buena Fortuna\"): rige esquemas de azar y riqueza especulativa. Regentes Mercurio (5) y Venus (6) derivados de tu Hora Sideral 22h 01m y latitud de Sicuani.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = "Toca para abrir la página 63 del tratado de Westcott en PDF",
                        color = EmeraldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Cuadrícula 3x3 Interactiva de David A. Phillips
        InteractivePhillipsGridCard(
            birthDate = userProfile.birthDate,
            onOpenCitation = { activePdfCitation = it }
        )

        // 3. Visualización de las 4 Pirámides de Vida
        PyramidViewCard(
            pyramids = phillipsAnalysis.pyramids,
            currentAge = currentAge
        )

        // ── INSPECCIÓN DE BOLILLAS GENERADAS ─────────────────────────────────
        val ballsToInspect = remember(oracleResult) {
            val list = mutableListOf<Int>()
            oracleResult?.masterPlay?.let { list.addAll(it) }
            oracleResult?.reducedWheel9?.let { list.addAll(it) }
            list.distinct().sorted()
        }

        if (ballsToInspect.isNotEmpty()) {
            Text(
                text = "AUDITORÍA HERMÉTICA DE BOLILLAS (Toca una para ver detalle):",
                color = GoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (ball in ballsToInspect) {
                    val isSelected = selectedBallAnalysis?.ballNumber == ball
                    LotteryBall(
                        number = ball,
                        size = 46.dp,
                        isSelected = isSelected,
                        onClick = { viewModel.selectBallForAnalysis(ball) }
                    )
                }
            }

            selectedBallAnalysis?.let { analysis ->
                BallDetailedInspectionCard(
                    analysis = analysis,
                    onOpenCitation = { activePdfCitation = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Visor de PDF modal
    activePdfCitation?.let { citation ->
        PdfPageViewerDialog(
            citation = citation,
            onDismiss = { activePdfCitation = null }
        )
    }
}

// ── TARJETA ESTILO TOMO ANTIGUO PARA EL CARRUSEL ──────────────────────────────
@Composable
fun BookTomeCard(
    citation: BookCitation,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(135.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF221F18), Color(0xFF0F0D0A))
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PDF",
                        color = GoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = citation.bookTitle,
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp,
                    maxLines = 3
                )

                Text(
                    text = citation.author,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GoldPrimary.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pág. ${citation.pdfPageIndex + 1}",
                    color = EmeraldAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para abrir",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ── CUADRÍCULA 3X3 INTERACTIVA DE PHILLIPS ───────────────────────────────────
@Composable
fun InteractivePhillipsGridCard(
    birthDate: LocalDate,
    onOpenCitation: (BookCitation) -> Unit
) {
    // Digits of 1991-07-19: 1, 9, 9, 1, 0, 7, 1, 9 -> 1: three times, 7: once, 9: three times. Missing: 2, 3, 4, 5, 6, 8
    val dateDigits = remember(birthDate) {
        val str = "${birthDate.year}${birthDate.monthValue.toString().padStart(2, '0')}${birthDate.dayOfMonth.toString().padStart(2, '0')}"
        str.filter { it != '0' }.map { it.digitToInt() }
    }

    val counts = remember(dateDigits) {
        (1..9).associateWith { d -> dateDigits.count { it == d } }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUADRÍCULA PITAGÓRICA 3×3 (D. A. Phillips)",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toca casillas vacías o números para saltar a su análisis en el PDF",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = { onOpenCitation(BookCitationRegistry.PHILLIPS_CUADRICULA_3X3) }
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Ver libro", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                }
            }

            // Grid Layout:
            // [3] [6] [9]
            // [2] [5] [8]
            // [1] [4] [7]
            val rows = listOf(
                listOf(3, 6, 9),
                listOf(2, 5, 8),
                listOf(1, 4, 7)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (digit in row) {
                            val count = counts[digit] ?: 0
                            val isEmpty = count == 0
                            val isFrustrationArrow = digit in listOf(4, 5, 6) && isEmpty

                            val bgColor = when {
                                count > 0 -> Color(0xFF1B2A3D)
                                isFrustrationArrow -> Color(0xFF3B1F22)
                                else -> Color(0xFF131822)
                            }

                            val borderColor = when {
                                count > 0 -> GoldPrimary
                                isFrustrationArrow -> SemaphoreRed
                                else -> Color(0xFF263345)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable {
                                        when {
                                            digit in listOf(4, 5, 6) -> onOpenCitation(BookCitationRegistry.PHILLIPS_FLECHA_FRUSTRACION)
                                            digit == 7 -> onOpenCitation(BookCitationRegistry.PHILLIPS_7_AISLADO)
                                            else -> onOpenCitation(BookCitationRegistry.PHILLIPS_CUADRICULA_3X3)
                                        }
                                    }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (count > 0) digit.toString().repeat(count) else "($digit)",
                                        color = if (count > 0) GoldLight else if (isFrustrationArrow) Color(0xFFFCA5A5) else TextMuted,
                                        fontSize = if (count > 0) 14.sp else 12.sp,
                                        fontWeight = if (count > 0) FontWeight.Black else FontWeight.Normal
                                    )
                                    Text(
                                        text = if (count > 0) "$count presente" else if (isFrustrationArrow) "Falta (4-5-6)" else "Vacío",
                                        color = if (count > 0) EmeraldLight else if (isFrustrationArrow) SemaphoreRed else TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "⚡ Resalta la Flecha de la Frustración (ausencia de 4-5-6) y el 7 aislado. Toca cada casilla para ver su explicación en el libro de Phillips.",
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

// ── DETALLE DE BOLILLA INSPECCIONADA ──────────────────────────────────────────
@Composable
fun BallDetailedInspectionCard(
    analysis: NumberAnalysis,
    onOpenCitation: (BookCitation) -> Unit
) {
    val citations = remember(analysis.ballNumber) {
        BookCitationRegistry.getCitationsForBall(analysis.ballNumber)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LotteryBall(number = analysis.ballNumber, size = 50.dp, isSelected = true)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bolilla #${analysis.ballNumber}: ${analysis.cheiroCompoundName}",
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Puntaje Compuesto: ${analysis.compositeScore.toInt()} pts | Raíz: ${analysis.singleDigit}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (citations.isNotEmpty()) {
                    IconButton(onClick = { onOpenCitation(citations.first()) }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Ver en PDF", tint = GoldPrimary)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF263345))
            )

            // Resúmenes de tratados
            TreatiseSection("1. Cheiro (Compuestos)", analysis.cheiroExplanation, GoldPrimary)
            TreatiseSection("2. Dan Millman (Senderos)", analysis.millmanExplanation, EmeraldLight)
            TreatiseSection("3. David Phillips (Cuadrícula)", "${analysis.phillipsArrowDescription} ${analysis.phillipsPyramidConnection}", GoldLight)
            TreatiseSection("4. W. W. Westcott (Aritmética)", analysis.westcottExplanation, GoldPrimary)
            TreatiseSection("5. Glynis McCants (Tríada)", analysis.mccantsTriad, EmeraldAccent)
        }
    }
}

@Composable
fun TreatiseSection(
    title: String,
    content: String,
    titleColor: Color
) {
    Column {
        Text(text = title, color = titleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = content, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
    }
}
