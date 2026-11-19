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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.data.models.PlanetaryHour
import com.oraculoloto5x.app.engine.CutoffAlertStatus
import com.oraculoloto5x.app.ui.theme.*
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PlanetaryCountdownCard(
    currentHour: PlanetaryHour,
    nextFavorable: Pair<PlanetaryHour, Duration>,
    sunTimes: Pair<LocalTime, LocalTime>,
    cutoffStatus: CutoffAlertStatus,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val (sunrise, sunset) = sunTimes
    val (favHour, durationToFav) = nextFavorable

    val hoursLeft = durationToFav.toHours()
    val minutesLeft = durationToFav.toMinutes() % 60

    val countdownText = if (hoursLeft > 0) "${hoursLeft}h ${minutesLeft}m" else "${minutesLeft}m"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(DarkSurfaceVariant, DarkCard)
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Cabecera: Reloj Planetario en Cusco
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RELOJ PLANETARIO CUSCO (100% OFFLINE)",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Amanecer: ${sunrise.format(timeFormatter)} | Ocaso: ${sunset.format(timeFormatter)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentHour.isFavorableWindow) EmeraldDark else Color(0xFF263238))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentHour.isFavorableWindow) "VENTANA AFÍN" else "HORA REGULAR",
                        color = if (currentHour.isFavorableWindow) EmeraldLight else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Según Westcott y Cheiro, las horas regidas por el Sol o la Luna son tus momentos de mayor vibración positiva para sellar o registrar tu jugada antes de que cierren las ventas.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fila central: Hora activa actual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Símbolo del Planeta
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(GoldDark, DarkBackground)
                            )
                        )
                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = currentHour.planet.symbol,
                        color = GoldLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hora de ${currentHour.planet.spanishName} (${currentHour.typeLabel})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vigencia: ${currentHour.formattedInterval}",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentHour.planet.nature,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cuenta regresiva a la próxima ventana favorable (Sol o Luna)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(0.5.dp, GoldLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentHour.isFavorableWindow) "Ventana Favorable en Curso" else "Próxima Ventana Favorable (${favHour.planet.spanishName})",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (currentHour.isFavorableWindow) "Tiempo restante de sintonía" else "Inicia: ${favHour.startTime.format(timeFormatter)}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = countdownText,
                        color = if (currentHour.isFavorableWindow) EmeraldAccent else GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Alerta si hoy es día de sorteo de La Tinka y está cerca de cerrar
            if (cutoffStatus.isDrawDay) {
                Spacer(modifier = Modifier.height(10.dp))
                val bannerBg = if (cutoffStatus.isClosingSoon) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                val bannerBorder = if (cutoffStatus.isClosingSoon) SemaphoreRed else TextSecondary.copy(alpha = 0.5f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerBg)
                        .border(1.dp, bannerBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cutoffStatus.message,
                        color = if (cutoffStatus.isClosingSoon) Color.White else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (cutoffStatus.isClosingSoon) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
