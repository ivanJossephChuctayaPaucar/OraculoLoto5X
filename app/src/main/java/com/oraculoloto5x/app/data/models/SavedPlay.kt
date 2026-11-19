package com.oraculoloto5x.app.data.models

import java.time.LocalDate

data class SavedPlay(
    val id: Long = 0L,
    val lotteryType: LotteryType,
    val generatedDate: LocalDate,
    val targetDrawDate: LocalDate,
    val playType: String, // "MAESTRA" o "RUEDA_9"
    val balls: List<Int>,
    val riskLevel: String, // "VERDE", "AMARILLO", "ROJO"
    val planetaryHour: String,
    val matchedCount: Int = 0,
    val matchedBalls: List<Int> = emptyList(),
    val estimatedPrize: String = "Pendiente de sorteo"
) {
    val formattedBalls: String
        get() = balls.sorted().joinToString(" - ") { it.toString().padStart(2, '0') }
}
