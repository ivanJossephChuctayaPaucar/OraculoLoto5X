package com.oraculoloto5x.app.data.models

import java.time.DayOfWeek
import java.time.LocalTime

enum class LotteryType(
    val displayName: String,
    val totalBallsToPick: Int,
    val maxBallNumber: Int,
    val drawDays: Set<DayOfWeek>,
    val salesCutoffTime: LocalTime,
    val minGaussianSum: Int,
    val maxGaussianSum: Int,
    val minDistinctDecades: Int
) {
    LA_TINKA(
        displayName = "La Tinka",
        totalBallsToPick = 6,
        maxBallNumber = 53,
        drawDays = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY),
        salesCutoffTime = LocalTime.of(21, 50),
        minGaussianSum = 135,
        maxGaussianSum = 195,
        minDistinctDecades = 4
    ),
    KABALA(
        displayName = "Kábala",
        totalBallsToPick = 6,
        maxBallNumber = 40,
        drawDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
        salesCutoffTime = LocalTime.of(20, 50),
        minGaussianSum = 100,
        maxGaussianSum = 150,
        minDistinctDecades = 3
    ),
    GANA_DIARIO(
        displayName = "Gana Diario",
        totalBallsToPick = 5,
        maxBallNumber = 35,
        drawDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        ),
        salesCutoffTime = LocalTime.of(20, 30),
        minGaussianSum = 70,
        maxGaussianSum = 110,
        minDistinctDecades = 3
    );

    companion object {
        fun fromName(name: String): LotteryType {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: LA_TINKA
        }
    }
}
