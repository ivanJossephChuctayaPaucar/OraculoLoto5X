package com.oraculoloto5x.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
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
import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.pdf.BookCitation
import com.oraculoloto5x.app.pdf.BookCitationRegistry
import com.oraculoloto5x.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BallDetailBottomSheet(
    analysis: NumberAnalysis,
    anchorBall: Int?,
    onDismiss: () -> Unit
) {
    val citations = remember(analysis.ballNumber) {
        BookCitationRegistry.getCitationsForBall(analysis.ballNumber)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoldPrimary.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera con bolilla grande
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LotteryBall(
                        number = analysis.ballNumber,
                        size = 60.dp,
                        isSelected = true,
                        isAnchor = analysis.ballNumber == anchorBall
                    )
                    Column {
                        Text(
                            text = analysis.cheiroCompoundName,
                            color = GoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Bolilla #${analysis.ballNumber} · Raíz Caldea: ${analysis.singleDigit}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (analysis.cheiroIsFortunate) PillTag("AFORTUNADO", EmeraldAccent, EmeraldDark)
                            if (analysis.cheiroIsWarning) PillTag("ADVERTENCIA", SemaphoreRed, Color(0xFF7F1D1D))
                            if (analysis.westcottIsPrime) PillTag("PRIMO PURO", GoldPrimary, Color(0xFF78350F))
                            if (analysis.westcottIsPerfect) PillTag("PERFECTO", GoldLight, Color(0xFF78350F))
                            if (analysis.mccantsIsNaturalMatch) PillTag("TRÍADA NATAL", EmeraldLight, EmeraldDark)
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            // Puntuación visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Puntaje Compuesto Total", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${analysis.compositeScore.toInt()} / 300 pts",
                            color = GoldLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Separador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF263345))
            )

            // Citas directas de tratados herméticos
            Text(
                text = "CITAS LITERALES DE TRATADOS HERMÉTICOS",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (citations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (citation in citations) {
                        CitationCard(citation = citation)
                    }
                }
            }

            // Separador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF263345))
            )

            // Los 5 Tratados en tarjetas horizontales deslizables
            Text(
                text = "RESUMEN HERMÉTICO POR TRATADO",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            val treatises = listOf(
                TreatiseInfo(
                    "I. Cheiro",
                    "Book of Numbers",
                    analysis.cheiroExplanation,
                    if (analysis.cheiroIsFortunate) EmeraldAccent else if (analysis.cheiroIsWarning) SemaphoreRed else GoldPrimary,
                    pageHint = "Cap. II-IV · Pág. 20-85"
                ),
                TreatiseInfo(
                    "II. Dan Millman",
                    "The Life You Were Born to Live",
                    analysis.millmanExplanation,
                    if (analysis.millmanMatchesBirthPath) EmeraldAccent else TextSecondary,
                    pageHint = "Sendero 37/10 · Pág. 290-305"
                ),
                TreatiseInfo(
                    "III. D. A. Phillips",
                    "The Complete Book of Numerology",
                    "${analysis.phillipsArrowDescription} ${analysis.phillipsPyramidConnection}",
                    if (analysis.phillipsFillsMissingDigit) EmeraldAccent else GoldPrimary,
                    pageHint = "Cap. 7-9 · Cuadrícula 3×3"
                ),
                TreatiseInfo(
                    "IV. W. W. Westcott",
                    "Numbers: Their Occult Power",
                    analysis.westcottExplanation,
                    GoldPrimary,
                    pageHint = "Cap. V-VIII · Aritmética Hermética"
                ),
                TreatiseInfo(
                    "V. Glynis McCants",
                    "Glynis Has Your Number",
                    analysis.mccantsTriad,
                    if (analysis.mccantsIsNaturalMatch) EmeraldAccent else TextSecondary,
                    pageHint = "Cap. 3 · Coincidencia Natural"
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (treatise in treatises) {
                    TreatiseCard(treatise = treatise)
                }
            }
        }
    }
}

@Composable
fun CitationCard(
    citation: BookCitation
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF162032), Color(0xFF0F1724))
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${citation.author} · ${citation.bookTitle}",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Ref. pág. ${citation.pdfPageIndex + 1}",
                    color = EmeraldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "«${citation.literalQuote}»",
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = citation.chapterLabel,
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

data class TreatiseInfo(
    val shortTitle: String,
    val fullTitle: String,
    val content: String,
    val accentColor: Color,
    val pageHint: String
)

@Composable
fun TreatiseCard(treatise: TreatiseInfo) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2235), DarkBackground)
                )
            )
            .border(1.dp, treatise.accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = treatise.shortTitle,
                color = treatise.accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = treatise.fullTitle,
                color = TextSecondary,
                fontSize = 10.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(treatise.accentColor.copy(alpha = 0.3f))
            )
            Text(
                text = treatise.content,
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Text(
                text = "📖 ${treatise.pageHint}",
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun PillTag(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
