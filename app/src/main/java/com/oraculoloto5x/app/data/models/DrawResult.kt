package com.oraculoloto5x.app.data.models

import java.time.LocalDate

data class DrawResult(
    val id: Long = 0L,
    val lotteryType: LotteryType,
    val drawDate: LocalDate,
    val winningBalls: List<Int>,
    val recordedAt: String = ""
) {
    val formattedBalls: String
        get() = winningBalls.sorted().joinToString(" - ") { it.toString().padStart(2, '0') }
}
