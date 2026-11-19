package com.oraculoloto5x.app.engine

import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.data.models.UserProfile
import java.time.LocalDate
import kotlin.random.Random

data class DailyRiskAssessment(
    val riskLevel: RiskLevel,
    val title: String,
    val explanation: String,
    val actionableAdvice: String,
    val personalDay: Int
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
    val lotteryType: LotteryType
)

class LotteryFusionEngine(
    private val userProfile: UserProfile,
    private val historicalFrequencies: Map<Int, Int> = emptyMap()
) {

    /**
     * Evalúa el Semáforo de Riesgo según el Sistema Caldeo de Cheiro
     * para el día del sorteo frente al perfil natal de Iván (nacido el 19 -> serie 1-4).
     */
    fun assessDailyRisk(targetDate: LocalDate): DailyRiskAssessment {
        val dayOfMonth = targetDate.dayOfMonth
        val reducedDay = NumerologyKnowledgeBase.reduceToSingleDigit(dayOfMonth)

        // Día personal: Mes personal + Día del mes
        // Año personal para 2026 = 1 (19/07 -> 1+7 = 8 + 2026(10->1) = 9)
        val currentYear = targetDate.year
        val yearReduced = NumerologyKnowledgeBase.reduceToSingleDigit(currentYear)
        val birthDayRed = userProfile.primarySingleNumber // 1
        val birthMonthRed = NumerologyKnowledgeBase.reduceToSingleDigit(userProfile.birthDate.monthValue) // 7
        val personalYear = NumerologyKnowledgeBase.reduceToSingleDigit(birthDayRed + birthMonthRed + yearReduced)
        val personalMonth = NumerologyKnowledgeBase.reduceToSingleDigit(personalYear + targetDate.monthValue)
        val personalDay = NumerologyKnowledgeBase.reduceToSingleDigit(personalMonth + targetDate.dayOfMonth)

        return when (reducedDay) {
            1, 2, 4, 7 -> DailyRiskAssessment(
                riskLevel = RiskLevel.VERDE,
                title = "SEMÁFORO VERDE: Alta Afinidad Solar y Lunar",
                explanation = "El día vibra en la serie armónica (${reducedDay}). Según Cheiro, los nacidos el día 19 (serie 1-4) gozan de magnetismo y simpatía universal con los números 1, 2, 4 y 7.",
                actionableAdvice = "Día ideal para consolidar la Jugada Maestra. Las probabilidades energéticas favorecen la sincronía numérica.",
                personalDay = personalDay
            )
            3, 5, 6 -> DailyRiskAssessment(
                riskLevel = RiskLevel.AMARILLO,
                title = "SEMÁFORO AMARILLO: Vibración Moderada / Neutra",
                explanation = "El día vibra con Júpiter (3), Mercurio (5) o Venus (6). Es un flujo compatible pero secundario frente a tu serie natal.",
                actionableAdvice = "Se aconseja jugar con moderación o apoyar la jugada con la Rueda Reducida de 9 bolillas para ampliar cobertura.",
                personalDay = personalDay
            )
            8, 9 -> DailyRiskAssessment(
                riskLevel = RiskLevel.ROJO,
                title = "SEMÁFORO ROJO: Tensión Kármica (Saturno / Marte)",
                explanation = "Cheiro enfatiza rigurosamente que los nacidos bajo la serie 1-4 deben evitar o ser extremadamente cautelosos en días regidos por 8 (Saturno) o 9 (Marte) debido a choques de fuerza y demoras.",
                actionableAdvice = "Alerta de cautela: Si juegas hoy, hazlo en las Horas Planetarias benévolas del Sol o la Luna para amortiguar la disonancia del día.",
                personalDay = personalDay
            )
            else -> DailyRiskAssessment(
                riskLevel = RiskLevel.AMARILLO,
                title = "SEMÁFORO AMARILLO: Día Neutro",
                explanation = "Vibración armónica general.",
                actionableAdvice = "Mantén la concentración en tus números clave.",
                personalDay = personalDay
            )
        }
    }

    /**
     * Motor Híbrido: 10,000 Simulaciones Monte Carlo + Filtro Campana de Gauss + 5 Tratados
     */
    fun generateOraclePlay(
        lotteryType: LotteryType,
        targetDate: LocalDate = LocalDate.now(),
        intentionWord: String = ""
    ): OracleGenerationResult {
        val maxBall = lotteryType.maxBallNumber
        val totalToPick = lotteryType.totalBallsToPick

        // 1. Calcular Gematría de Intención (si existe)
        val intentionSum = if (intentionWord.isNotBlank()) {
            NumerologyKnowledgeBase.calculateChaldeanGematria(intentionWord)
        } else 0

        val anchorBall = if (intentionSum > 0) {
            if (intentionSum in 1..maxBall) intentionSum else ((intentionSum - 1) % maxBall) + 1
        } else null

        // 2. Analizar y puntuar cada bolilla de 1 a maxBall
        val ballAnalyses = (1..maxBall).map { b ->
            val histCount = historicalFrequencies[b] ?: 0
            var analysis = NumerologyKnowledgeBase.analyzeBall(b, userProfile, histCount)
            if (b == anchorBall) {
                // Bono especial por anclaje de intención (+40 puntos)
                analysis = analysis.copy(compositeScore = analysis.compositeScore + 40.0)
            }
            analysis
        }

        val ballScoreMap = ballAnalyses.associate { it.ballNumber to it.compositeScore }

        // 3. Rueda Reducida (Top 9 bolillas de mayor puntaje individual)
        val reducedWheel9 = ballAnalyses
            .sortedByDescending { it.compositeScore }
            .take(9)
            .map { it.ballNumber }
            .sorted()

        // 4. Monte Carlo (10,000 iteraciones con Campana de Gauss)
        val candidateBalls = (1..maxBall).toList()
        var bestPlay = reducedWheel9.take(totalToPick).sorted()
        var bestPlayScore = Double.NEGATIVE_INFINITY

        val random = Random(System.currentTimeMillis())

        // Pesos para muestreo
        val minScore = ballAnalyses.minOf { it.compositeScore }
        val adjustedWeights = ballAnalyses.associate {
            it.ballNumber to (it.compositeScore - minScore + 5.0)
        }
        val totalWeight = adjustedWeights.values.sum()

        // Función para elegir una bolilla ponderada
        fun pickWeightedBall(available: Set<Int>): Int {
            val subTotalWeight = available.sumOf { adjustedWeights[it] ?: 1.0 }
            var r = random.nextDouble() * subTotalWeight
            for (ball in available) {
                val w = adjustedWeights[ball] ?: 1.0
                if (r <= w) return ball
                r -= w
            }
            return available.random(random)
        }

        val iterations = 10_000
        for (i in 0 until iterations) {
            val picked = mutableSetOf<Int>()

            // Si hay bolilla comodín de intención, incluirla en 50% de las simulaciones
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

            val playList = picked.toList()

            // --- FILTROS DE CAMPANA DE GAUSS ---
            // 1. Suma Gaussiana
            val sum = playList.sum()
            if (sum !in lotteryType.minGaussianSum..lotteryType.maxGaussianSum) continue

            // 2. Balance Par / Impar
            val evens = playList.count { it % 2 == 0 }
            val odds = totalToPick - evens
            val isBalancedParity = if (totalToPick == 6) {
                evens in 2..4 // 2-4, 3-3, 4-2
            } else {
                evens in 2..3 // 2-3 o 3-2 para 5 bolillas
            }
            if (!isBalancedParity) continue

            // 3. Distribución de decenas
            val decadesCount = playList.map { it / 10 }.distinct().size
            if (decadesCount < lotteryType.minDistinctDecades) continue

            // 4. Calcular Aptitud Compuesta de la combinación
            var comboScore = playList.sumOf { ballScoreMap[it] ?: 0.0 }

            // Bonificaciones combinatorias
            // Presencia de al menos 1 número maestro o afortunado
            if (playList.any { it in NumerologyKnowledgeBase.FORTUNATE_COMPOUNDS }) comboScore += 20.0
            // Presencia de al menos 1 número de la serie 1-4
            if (playList.any { NumerologyKnowledgeBase.reduceToSingleDigit(it) in listOf(1, 4) }) comboScore += 15.0
            // Penalización si contiene más de un número de advertencia
            val warningCount = playList.count { it in NumerologyKnowledgeBase.WARNING_COMPOUNDS }
            if (warningCount > 1) comboScore -= 40.0

            if (comboScore > bestPlayScore) {
                bestPlayScore = comboScore
                bestPlay = playList.sorted()
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
            lotteryType = lotteryType
        )
    }
}
