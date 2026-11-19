package com.oraculoloto5x.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.MainViewModel
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.ui.components.LotteryBall
import com.oraculoloto5x.app.ui.components.PhillipsGridCard
import com.oraculoloto5x.app.ui.components.PyramidViewCard
import com.oraculoloto5x.app.ui.theme.*
import java.time.LocalDate

@Composable
fun FiveBooksAnalysisScreen(
    viewModel: MainViewModel
) {
    val oracleResult by viewModel.oracleResult.collectAsState()
    val selectedBallAnalysis by viewModel.selectedBallForAnalysis.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

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

        // Cabecera
        Column {
            Text(
                text = "LOS 5 TRATADOS DE SABIDURÍA",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cheiro • Millman • Phillips • Westcott • McCants",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Selector horizontal de bolillas recomendadas
        val ballsToInspect = remember(oracleResult) {
            val list = mutableListOf<Int>()
            oracleResult?.masterPlay?.let { list.addAll(it) }
            oracleResult?.reducedWheel9?.let { list.addAll(it) }
            list.distinct().sorted()
        }

        if (ballsToInspect.isNotEmpty()) {
            Text(
                text = "Toca una bolilla para auditar su justificación hermética:",
                color = TextSecondary,
                fontSize = 11.sp
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
        }

        // Desglose de la bolilla seleccionada
        selectedBallAnalysis?.let { analysis ->
            BallDetailedInspectionCard(analysis = analysis)
        }

        // Visualización de la Cuadrícula 3x3 de David A. Phillips
        PhillipsGridCard(analysis = phillipsAnalysis)

        // Visualización de las 4 Pirámides de Vida
        PyramidViewCard(
            pyramids = phillipsAnalysis.pyramids,
            currentAge = currentAge
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BallDetailedInspectionCard(analysis: NumberAnalysis) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(1.5.dp, GoldPrimary, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Fila superior: Bolilla + Nombre Compuesto + Puntaje
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LotteryBall(number = analysis.ballNumber, size = 52.dp, isSelected = true)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bolilla #${analysis.ballNumber}: ${analysis.cheiroCompoundName}",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Raíz Simple: ${analysis.singleDigit} | Puntaje Compuesto: ${analysis.compositeScore.toInt()} pts",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (analysis.cheiroIsFortunate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF064E3B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "AFORTUNADO", color = EmeraldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (analysis.cheiroIsWarning) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF7F1D1D))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "ADVERTENCIA", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Divider(color = Color(0xFF263345), thickness = 1.dp)

            // 1. Cheiro
            TreatiseSection(
                title = "1. Cheiro (Book of Numbers)",
                content = analysis.cheiroExplanation,
                tag = "Vibración Raíz #${analysis.cheiroSingleRoot}",
                tagColor = GoldPrimary
            )

            // 2. Dan Millman
            TreatiseSection(
                title = "2. Dan Millman (Camino de Vida 37/10)",
                content = analysis.millmanExplanation,
                tag = if (analysis.millmanMatchesBirthPath) "Afinidad Sendero Directo" else "Secundario",
                tagColor = if (analysis.millmanMatchesBirthPath) EmeraldLight else TextSecondary
            )

            // 3. David A. Phillips
            TreatiseSection(
                title = "3. David A. Phillips (Cuadrícula y Pirámides)",
                content = "${analysis.phillipsArrowDescription}\n${analysis.phillipsPyramidConnection}",
                tag = if (analysis.phillipsFillsMissingDigit) "Equilibra Ausencias" else "Alineado",
                tagColor = EmeraldLight
            )

            // 4. W. Wynn Westcott
            TreatiseSection(
                title = "4. W. Wynn Westcott (Aritmética Hermética)",
                content = analysis.westcottExplanation,
                tag = when {
                    analysis.westcottIsPerfect -> "NÚMERO PERFECTO (6, 28)"
                    analysis.westcottIsPrime -> "PRIMO PURO"
                    else -> "Compuesto"
                },
                tagColor = GoldLight
            )

            // 5. Glynis McCants
            TreatiseSection(
                title = "5. Glynis McCants (Coincidencia Natural)",
                content = analysis.mccantsTriad,
                tag = if (analysis.mccantsIsNaturalMatch) "COINCIDENCIA NATURAL (1-5-7)" else "Compatible",
                tagColor = if (analysis.mccantsIsNaturalMatch) EmeraldAccent else TextSecondary
            )
        }
    }
}

@Composable
fun TreatiseSection(
    title: String,
    content: String,
    tag: String,
    tagColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tag,
                color = tagColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
