package com.oraculoloto5x.app.engine

import com.oraculoloto5x.app.data.models.DrawResult
import java.time.LocalDate

class StatisticsEngine(private val draws: List<DrawResult>) {

    private val sortedDraws = draws.sortedByDescending { it.drawDate }

    val lastDraw: DrawResult? = sortedDraws.firstOrNull()
    val lastDrawDate: LocalDate? = lastDraw?.drawDate
    val totalDraws: Int = draws.size

    data class BallFrequency(
        val ball: Int,
        val count: Int,
        val lastSeen: LocalDate?,
        val daysSinceLastSeen: Long? = null
    )

    fun ballFrequencies(maxBall: Int): List<BallFrequency> {
        val counts = mutableMapOf<Int, Int>()
        val lastSeen = mutableMapOf<Int, LocalDate>()

        for (d in draws) {
            for (b in d.winningBalls) {
                counts[b] = (counts[b] ?: 0) + 1
                if (lastSeen[b] == null || d.drawDate.isAfter(lastSeen[b]!!)) {
                    lastSeen[b] = d.drawDate
                }
            }
        }

        val today = LocalDate.now()
        return (1..maxBall).map { ball ->
            val count = counts[ball] ?: 0
            val ls = lastSeen[ball]
            BallFrequency(
                ball = ball,
                count = count,
                lastSeen = ls,
                daysSinceLastSeen = if (ls != null) java.time.temporal.ChronoUnit.DAYS.between(ls, today) else null
            )
        }.sortedByDescending { it.count }
    }

    data class HotColdAnalysis(
        val hot: List<BallFrequency>,
        val cold: List<BallFrequency>,
        val overdue: List<BallFrequency>
    )

    fun hotColdAnalysis(maxBall: Int): HotColdAnalysis {
        val freqs = ballFrequencies(maxBall)
        val hot = freqs.take(5)
        val cold = freqs.filter { it.count > 0 }.takeLast(5).reversed()
        val overdue = freqs
            .filter { it.daysSinceLastSeen != null }
            .sortedByDescending { it.daysSinceLastSeen }
            .take(5)
        return HotColdAnalysis(hot, cold, overdue)
    }

    data class ParityStats(
        val evenCount: Int,
        val oddCount: Int,
        val evenPct: Double,
        val oddPct: Double
    )

    fun parityAnalysis(): ParityStats {
        var evens = 0
        var odds = 0
        for (d in draws) {
            for (b in d.winningBalls) {
                if (b % 2 == 0) evens++ else odds++
            }
        }
        val total = evens + odds
        return ParityStats(
            evenCount = evens,
            oddCount = odds,
            evenPct = if (total == 0) 0.0 else evens * 100.0 / total,
            oddPct = if (total == 0) 0.0 else odds * 100.0 / total
        )
    }

    data class SumStats(
        val minSum: Int,
        val maxSum: Int,
        val avgSum: Double,
        val medianSum: Double,
        val mostCommonSum: Int,
        val mostCommonSumCount: Int
    )

    fun sumAnalysis(): SumStats? {
        if (draws.isEmpty()) return null
        val sums = draws.map { it.winningBalls.sum() }.sorted()
        val minSum = sums.first()
        val maxSum = sums.last()
        val avgSum = sums.average()

        val medianSum = if (sums.size % 2 == 1) {
            sums[sums.size / 2].toDouble()
        } else {
            (sums[sums.size / 2 - 1] + sums[sums.size / 2]) / 2.0
        }

        val sumCounts = sums.groupingBy { it }.eachCount()
        val mostCommonEntry = sumCounts.maxByOrNull { it.value }
        val mostCommonSum = mostCommonEntry?.key ?: sums.first()
        val mostCommonSumCount = mostCommonEntry?.value ?: 1

        return SumStats(minSum, maxSum, avgSum, medianSum, mostCommonSum, mostCommonSumCount)
    }

    data class DecadeStats(
        val decade: Int,
        val range: String,
        val count: Int,
        val pct: Double
    )

    fun decadeAnalysis(maxBall: Int): List<DecadeStats> {
        val totalBalls = draws.size * 6.0
        val decades = mutableMapOf<Int, Int>()

        for (d in draws) {
            for (b in d.winningBalls) {
                val dec = ((b - 1) / 10) + 1
                decades[dec] = (decades[dec] ?: 0) + 1
            }
        }

        val maxDecade = ((maxBall - 1) / 10) + 1
        return (1..maxDecade).map { dec ->
            val from = (dec - 1) * 10 + 1
            val to = if (dec * 10 > maxBall) maxBall else dec * 10
            val count = decades[dec] ?: 0
            DecadeStats(
                decade = dec,
                range = "$from - $to",
                count = count,
                pct = if (totalBalls == 0.0) 0.0 else count * 100.0 / totalBalls
            )
        }
    }

    fun lastDrawBalls(): List<Int> = lastDraw?.winningBalls ?: emptyList()

    data class FullReport(
        val totalDraws: Int,
        val lastDrawDate: LocalDate?,
        val lastDrawBalls: List<Int>,
        val hotCold: HotColdAnalysis,
        val parity: ParityStats,
        val sums: SumStats?,
        val decades: List<DecadeStats>
    )

    fun fullReport(maxBall: Int): FullReport {
        return FullReport(
            totalDraws = totalDraws,
            lastDrawDate = lastDrawDate,
            lastDrawBalls = lastDrawBalls(),
            hotCold = hotColdAnalysis(maxBall),
            parity = parityAnalysis(),
            sums = sumAnalysis(),
            decades = decadeAnalysis(maxBall)
        )
    }

    companion object {
        fun fromDraws(draws: List<DrawResult>) = StatisticsEngine(draws)
    }
}

/**
 * Convierte las estadísticas en ajustes de peso para LotteryFusionEngine.
 * - Bolillas calientes: +peso
 * - Bolillas frías: -peso
 * - Bolillas sobrepasadas (mucho sin salir): +peso
 * - Último sorteo: enfriamiento (-peso)
 */
fun StatisticsEngine.ballWeightAdjustments(maxBall: Int): Map<Int, Double> {
    val adjustments = mutableMapOf<Int, Double>()
    val report = fullReport(maxBall)

    for (b in 1..maxBall) adjustments[b] = 1.0

    val avgCount = report.totalDraws * 6.0 / maxBall
    for (f in report.hotCold.hot) {
        val bonus = if (f.count > avgCount) 0.2 else 0.1
        adjustments[f.ball] = (adjustments[f.ball] ?: 1.0) + bonus
    }

    for (f in report.hotCold.cold) {
        adjustments[f.ball] = (adjustments[f.ball] ?: 1.0) - 0.1
    }

    for (f in report.hotCold.overdue) {
        adjustments[f.ball] = (adjustments[f.ball] ?: 1.0) + 0.15
    }

    for (b in report.lastDrawBalls) {
        adjustments[b] = (adjustments[b] ?: 1.0) - 0.3
    }

    return adjustments
}