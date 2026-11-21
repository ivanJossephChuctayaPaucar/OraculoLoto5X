package com.oraculoloto5x.app.engine

import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.data.models.MoonPhaseInfo
import com.oraculoloto5x.app.data.models.NatalAstronomicalProfile
import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.data.models.UserProfile
import java.time.LocalDate
import kotlin.random.Random

data class DailyRiskAssessment(
    val riskLevel: RiskLevel,
    val title: String,
    val explanation: String,
    val actionableAdvice: String,
    val personalDay: Int,
    val moonInfo: MoonPhaseInfo? = null
)

enum class RiskLevel {
    VERDE, AMARILLO, ROJO
}

data class OracleGenerationResult(
    val masterPlay: List<Int>,
    val reducedWheel9: List<Int>,
    val intentionAnchorBall: Int?,
    val intentionGematriaSum: Int,
    val riskAssessment: DailyRiskAssessment,
    val allBallsAnalysis: List<NumberAnalysis>,
    val targetDrawDate: LocalDate,
    val lotteryType: LotteryType,
    val moonInfo: MoonPhaseInfo,
    val house5RulersDescription: String = "Casa 5 Agathe Tuche: Mercurio (5) y Venus (6)"
)

class LotteryFusionEngine(
    private val userProfile: UserProfile,
    private val historicalFrequencies: Map<Int, Int> = emptyMap(),
    private val lastOfficialDrawBalls: List<Int> = emptyList(),
    private val natalAstro: NatalAstronomicalProfile = NatalAstronomicalProfile.DEFAULT,
    private val weightAdjustments: Map<Int, Double> = emptyMap()
) {

    /**
     * Evalúa el Semáforo de Riesgo según el Sistema Caldeo de Cheiro
     * y los ciclos lunares de Westcott (resonancia con Cuarto Creciente natal al 50%).
     */
    fun assessDailyRisk(targetDate: LocalDate): DailyRiskAssessment {
        val dayOfMonth = targetDate.dayOfMonth
        val reducedDay = NumerologyKnowledgeBase.reduceToSingleDigit(dayOfMonth)

        val currentYear = targetDate.year
        val yearReduced = NumerologyKnowledgeBase.reduceToSingleDigit(currentYear)
        val birthDayRed = userProfile.primarySingleNumber
        val birthMonthRed = NumerologyKnowledgeBase.reduceToSingleDigit(userProfile.birthDate.monthValue)
        val personalYear = NumerologyKnowledgeBase.reduceToSingleDigit(birthDayRed + birthMonthRed + yearReduced)
        val personalMonth = NumerologyKnowledgeBase.reduceToSingleDigit(personalYear + targetDate.monthValue)
        val personalDay = NumerologyKnowledgeBase.reduceToSingleDigit(personalMonth + targetDate.dayOfMonth)

        val moonInfo = NatalAstronomicalProfile.getMoonSyncBonus(targetDate.toEpochDay())

        val baseAssessment = when (reducedDay) {
            1, 2, 4, 7 -> DailyRiskAssessment(
                riskLevel = RiskLevel.VERDE,
                title = "SEMÁFORO VERDE: Alta Afinidad Solar y Lunar",
                explanation = "El día vibra en la serie armónica (${reducedDay}). Según Cheiro, los nacidos el día 19 (serie 1-4) gozan de simpatía universal con los números 1, 2, 4 y 7. ☽ ${moonInfo.displaySummary}.",
                actionableAdvice = "Día ideal para consolidar la Jugada Maestra. Las probabilidades energéticas favorecen la sincronía numérica.",
                personalDay = personalDay,
                moonInfo = moonInfo
            )
            3, 5, 6 -> {
                val elevatedLevel = if (moonInfo.isExactNatalResonance) RiskLevel.VERDE else RiskLevel.AMARILLO
                DailyRiskAssessment(
                    riskLevel = elevatedLevel,
                    title = if (elevatedLevel == RiskLevel.VERDE) "SEMÁFORO VERDE: Impulso de Resonancia Lunar Natal" else "SEMÁFORO AMARILLO: Vibración Moderada / Neutra",
                    explanation = "El día vibra con Júpiter (3), Mercurio (5) o Venus (6). Los regentes 5 y 6 activan tu Casa 5 (Agathe Tuche de Westcott). ☽ ${moonInfo.displaySummary}.",
                    actionableAdvice = if (elevatedLevel == RiskLevel.VERDE) "La coincidencia con tu fase lunar natal potencia los regentes de azar." else "Se aconseja jugar con moderación apoyado en la Rueda Reducida de 9 bolillas.",
                    personalDay = personalDay,
                    moonInfo = moonInfo
                )
            }
            8, 9 -> DailyRiskAssessment(
                riskLevel = RiskLevel.ROJO,
                title = "SEMÁFORO ROJO: Tensión Kármica (Saturno / Marte)",
                explanation = "Cheiro enfatiza rigurosamente cautela en días regidos por 8 o 9 para la serie 1-4. ☽ ${moonInfo.displaySummary}.",
                actionableAdvice = "Si juegas hoy, hazlo en las Horas Planetarias benévolas del Sol o la Luna para amortiguar la disonancia del día.",
                personalDay = personalDay,
                moonInfo = moonInfo
            )
            else -> DailyRiskAssessment(
                riskLevel = RiskLevel.AMARILLO,
                title = "SEMÁFORO AMARILLO: Día Neutro",
                explanation = "Vibración armónica general. ☽ ${moonInfo.displaySummary}.",
                actionableAdvice = "Mantén la concentración en tus números clave.",
                personalDay = personalDay,
                moonInfo = moonInfo
            )
        }

        return baseAssessment
    }

    /**
     * Motor Híbrido Astro-Numerológico:
     * 10,000 Simulaciones Monte Carlo suavizadas + Filtros Gaussianos + Anti-Consecutivos + Cooldown
     */
    fun generateOraclePlay(
        lotteryType: LotteryType,
        targetDate: LocalDate = LocalDate.now(),
        intentionWord: String = ""
    ): OracleGenerationResult {
        val maxBall = lotteryType.maxBallNumber
        val totalToPick = lotteryType.totalBallsToPick

        // 1. Calcular Gematría de Intención
        val intentionSum = if (intentionWord.isNotBlank()) {
            NumerologyKnowledgeBase.calculateChaldeanGematria(intentionWord)
        } else 0

        val anchorBall = if (intentionSum > 0) {
            if (intentionSum in 1..maxBall) intentionSum else ((intentionSum - 1) % maxBall) + 1
        } else null

        // 2. Cálculo de fase lunar actual
        val moonInfo = NatalAstronomicalProfile.getMoonSyncBonus(targetDate.toEpochDay())

        // 3. Analizar y puntuar cada bolilla de 1 a maxBall
        val ballAnalyses = (1..maxBall).map { b ->
            val histCount = historicalFrequencies[b] ?: 0
            var analysis = NumerologyKnowledgeBase.analyzeBall(b, userProfile, histCount)

            // A) Bonificador Casa 5 Agathe Tuche (Westcott p.63): Regentes 5 y 6
            val root = NumerologyKnowledgeBase.reduceToSingleDigit(b)
            if (b in natalAstro.house5AgatheTucheRulers || root in natalAstro.house5AgatheTucheRulers) {
                val house5Bonus = 22.0 * (1.0 + moonInfo.bonus * 0.25)
                analysis = analysis.copy(compositeScore = analysis.compositeScore + house5Bonus)
            }

            // B) Bonificador geográfico natal Sicuani = 20 -> 2 (Cheiro Cap. XXIX)
            if (b == natalAstro.chaldeanPlaceNumber || root == 2) {
                analysis = analysis.copy(compositeScore = analysis.compositeScore + 15.0)
            }

            // C) Filtro de Enfriamiento (Cooldown): penalización de -15.0 a bolillas del sorteo inmediato anterior
            if (b in lastOfficialDrawBalls) {
                analysis = analysis.copy(compositeScore = analysis.compositeScore - 15.0)
            }

            // D) Bono especial por anclaje de intención
            if (b == anchorBall) {
                analysis = analysis.copy(compositeScore = analysis.compositeScore + 40.0)
            }

            analysis
        }

        val ballScoreMap = ballAnalyses.associate { it.ballNumber to it.compositeScore }

        // 4. Rueda Reducida (Top 9 bolillas de mayor puntaje individual)
        val reducedWheel9 = ballAnalyses
            .sortedByDescending { it.compositeScore }
            .take(9)
            .map { it.ballNumber }
            .sorted()

        // 5. Monte Carlo (10,000 iteraciones con pesos suavizados)
        val candidateBalls = (1..maxBall).toList()
        var bestPlay = reducedWheel9.take(totalToPick).sorted()
        var bestPlayScore = Double.NEGATIVE_INFINITY

        val random = Random(System.currentTimeMillis())

        // Curva de suavizado: evita monopolización de las mismas bolillas bonificadas
        val adjustedWeights = ballAnalyses.associate {
            val baseWeight = 1.0 + (it.compositeScore.coerceAtLeast(0.0) / 80.0)
            val statAdjustment = weightAdjustments[it.ballNumber] ?: 1.0
            it.ballNumber to (baseWeight * statAdjustment)
        }

        fun pickWeightedBall(available: Set<Int>): Int {
            val subTotalWeight = available.sumOf { adjustedWeights[it] ?: 1.0 }
            val r = random.nextDouble() * subTotalWeight
            var cumulative = 0.0
            for (ball in available) {
                val w = adjustedWeights[ball] ?: 1.0
                cumulative += w
                if (r <= cumulative) return ball
            }
            return available.last()
        }

        fun hasThreeOrMoreConsecutive(sortedList: List<Int>): Boolean {
            for (idx in 0 until sortedList.size - 2) {
                if (sortedList[idx] + 1 == sortedList[idx + 1] && sortedList[idx + 1] + 1 == sortedList[idx + 2]) {
                    return true
                }
            }
            return false
        }

        val iterations = 10_000
        for (i in 0 until iterations) {
            val picked = mutableSetOf<Int>()

            if (anchorBall != null && random.nextBoolean()) {
                picked.add(anchorBall)
            }

            val remainingPool = candidateBalls.toMutableSet()
            remainingPool.removeAll(picked)

            while (picked.size < totalToPick && remainingPool.isNotEmpty()) {
                val ball = pickWeightedBall(remainingPool)
                picked.add(ball)
                remainingPool.remove(ball)
            }

            if (picked.size != totalToPick) continue

            val sortedPlay = picked.toList().sorted()

            // --- FILTRO ANTI-CONSECUTIVOS: Descartar si hay 3 o más consecutivos seguidos (ej. 4-5-6 o 18-19-20) ---
            if (hasThreeOrMoreConsecutive(sortedPlay)) continue

            // --- FILTROS DE CAMPANA DE GAUSS ---
            val sum = sortedPlay.sum()
            if (sum !in lotteryType.minGaussianSum..lotteryType.maxGaussianSum) continue

            val evens = sortedPlay.count { it % 2 == 0 }
            val isBalancedParity = if (totalToPick == 6) {
                evens in 2..4
            } else {
                evens in 2..3
            }
            if (!isBalancedParity) continue

            val decadesCount = sortedPlay.map { it / 10 }.distinct().size
            if (decadesCount < lotteryType.minDistinctDecades) continue

            var comboScore = sortedPlay.sumOf { ballScoreMap[it] ?: 0.0 }

            if (sortedPlay.any { it in NumerologyKnowledgeBase.FORTUNATE_COMPOUNDS }) comboScore += 20.0
            if (sortedPlay.any { NumerologyKnowledgeBase.reduceToSingleDigit(it) in listOf(1, 4) }) comboScore += 15.0
            if (sortedPlay.any { it in setOf(5, 6, 23, 14, 32, 41, 15, 24, 33, 42) }) comboScore += 18.0

            val warningCount = sortedPlay.count { it in NumerologyKnowledgeBase.WARNING_COMPOUNDS }
            if (warningCount > 1) comboScore -= 40.0

            if (comboScore > bestPlayScore) {
                bestPlayScore = comboScore
                bestPlay = sortedPlay
            }
        }

        val riskAssessment = assessDailyRisk(targetDate)

        return OracleGenerationResult(
            masterPlay = bestPlay,
            reducedWheel9 = reducedWheel9,
            intentionAnchorBall = anchorBall,
            intentionGematriaSum = intentionSum,
            riskAssessment = riskAssessment,
            allBallsAnalysis = ballAnalyses.sortedByDescending { it.compositeScore },
            targetDrawDate = targetDate,
            lotteryType = lotteryType,
            moonInfo = moonInfo,
            house5RulersDescription = "Casa 5 Agathe Tuche: Mercurio (5) y Venus (6)"
        )
    }
}
