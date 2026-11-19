package com.oraculoloto5x.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
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
import com.oraculoloto5x.app.data.models.PlanetaryHour
import com.oraculoloto5x.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun HoursAndDaysScreen(
    viewModel: MainViewModel
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val planetaryHours = remember(selectedDate) {
        viewModel.get24PlanetaryHours(selectedDate)
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
                text = "CALENDARIO NUMEROLÓGICO Y HORAS",
                color = GoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cusco, Perú (-13.5319, -71.9675) | Serie natal 1-4",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Calendario mensual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                // Selector de mes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = GoldLight)
                    }

                    val monthName = currentYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES")).uppercase()
                    Text(
                        text = "$monthName ${currentYearMonth.year}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = GoldLight)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Encabezados de días de la semana
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    val daysHeader = listOf("L", "M", "M", "J", "V", "S", "D")
                    for (header in daysHeader) {
                        Text(
                            text = header,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Días del mes
                val firstDayOfMonth = currentYearMonth.atDay(1)
                val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1 // 0 para Lunes
                val daysInMonth = currentYearMonth.lengthOfMonth()

                val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

                val rows = totalCells / 7

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - dayOfWeekOffset + 1

                            if (dayNum in 1..daysInMonth) {
                                val date = currentYearMonth.atDay(dayNum)
                                val isSelected = date == selectedDate

                                val reduced = NumerologyKnowledgeBase.reduceToSingleDigit(dayNum)
                                val dotColor = when (reduced) {
                                    1, 2, 4, 7 -> SemaphoreGreen
                                    3, 5, 6 -> SemaphoreYellow
                                    else -> SemaphoreRed
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GoldDark.copy(alpha = 0.5f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) GoldPrimary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedDate = date }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            color = if (isSelected) GoldLight else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Leyenda de colores del semáforo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = SemaphoreGreen, label = "Verde (1, 2, 4, 7)")
                    LegendItem(color = SemaphoreYellow, label = "Amarillo (3, 5, 6)")
                    LegendItem(color = SemaphoreRed, label = "Rojo (8, 9)")
                }
            }
        }

        // Tabla de las 24 Horas Planetarias para la fecha seleccionada
        val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "ES"))
        Text(
            text = "24 HORAS PLANETARIAS: ${selectedDate.format(dayFormatter).uppercase()}",
            color = GoldLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        // Lista de las 24 horas
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (ph in planetaryHours) {
                PlanetaryHourRow(ph = ph)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun PlanetaryHourRow(ph: PlanetaryHour) {
    val cardBg = if (ph.isFavorableWindow) Color(0xFF0F2E22) else DarkSurface
    val cardBorder = if (ph.isFavorableWindow) EmeraldAccent else Color(0xFF263345)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ph.planet.symbol,
                    color = if (ph.isFavorableWindow) EmeraldLight else GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${ph.planet.spanishName} (${ph.typeLabel})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ph.formattedInterval,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (ph.isFavorableWindow) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VENTANA AFÍN",
                        color = EmeraldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Serie ${ph.planet.seriesAffinity}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
