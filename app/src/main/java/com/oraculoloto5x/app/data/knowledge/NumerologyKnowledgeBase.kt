package com.oraculoloto5x.app.data.knowledge

import com.oraculoloto5x.app.data.models.NumberAnalysis
import com.oraculoloto5x.app.data.models.UserProfile
import java.time.LocalDate

object NumerologyKnowledgeBase {

    // 1. SISTEMA CALDEO (Cheiro - Book of Numbers)
    val CHALDEAN_LETTER_MAP: Map<Char, Int> = mapOf(
        'A' to 1, 'I' to 1, 'J' to 1, 'Q' to 1, 'Y' to 1,
        'B' to 2, 'K' to 2, 'R' to 2,
        'C' to 3, 'G' to 3, 'L' to 3, 'S' to 3,
        'D' to 4, 'M' to 4, 'T' to 4,
        'E' to 5, 'H' to 5, 'N' to 5, 'X' to 5,
        'U' to 6, 'V' to 6, 'W' to 6,
        'O' to 7, 'Z' to 7,
        'F' to 8, 'P' to 8
        // El 9 no se asigna a letras por considerarse sagrado y trascendente
    )

    val FORTUNATE_COMPOUNDS: Set<Int> = setOf(
        10, 14, 15, 17, 19, 21, 23, 24, 27, 32, 33, 36, 37, 41, 42, 45, 46, 50, 51
    )

    val WARNING_COMPOUNDS: Set<Int> = setOf(
        11, 12, 16, 18, 22, 26, 28, 29, 35, 38, 43, 44, 47, 52
    )

    val COMPOUND_MEANINGS: Map<Int, Pair<String, String>> = mapOf(
        10 to ("La Rueda de la Fortuna" to "Éxito, honor y confianza. Elevación y cumplimiento de planes."),
        11 to ("El León Amordazado" to "Advertencia de peligros ocultos y pruebas provocadas por otros."),
        12 to ("El Sacrificio" to "Ansiedad y sacrificios. Alerta contra engaños o falsas expectativas."),
        13 to ("La Regeneración" to "Cambio radical y transformación súbita. Poder si se comprende."),
        14 to ("Movimiento y Desafío Fértil" to "Combinación de fuerzas. Excelente para ganancias financieras si se actúa con prudencia."),
        15 to ("El Mago y la Elocuencia" to "Magnetismo personal y atracción de dádivas materiales."),
        16 to ("La Torre Fulminada" to "Advertencia severa de accidentes o colapso de planes mal fundamentados."),
        17 to ("La Estrella de los Magos" to "Inmortalidad, paz espiritual y esperanza suprema. Muy afortunado."),
        18 to ("Conflicto y Traición" to "Luchas internas y peligro de desengaños o disputas familiares."),
        19 to ("El Príncipe del Cielo" to "El Sol radiante. Triunfo absoluto, felicidad, estima y plenitud de destino."),
        20 to ("El Despertar" to "Llamado a nuevos proyectos y trascendencia de propósitos materiales."),
        21 to ("La Corona de los Magos" to "Victoria total, avance ininterrumpido y elevación."),
        22 to ("El Falso Paraíso" to "Advertencia contra ilusiones y falsas amistades."),
        23 to ("La Estrella Real del León" to "Promesa inequívoca de triunfo, protección y favores de personas influyentes."),
        24 to ("Asociación Afortunada" to "Ganancias mediante alianzas sólidas y favores afectivos."),
        25 to ("La Sabiduría por la Experiencia" to "Victoria tras sortear obstáculos iniciales con discernimiento."),
        26 to ("Pérdida por Asociación" to "Advertencia grave contra sociedades y contratos imprudentes."),
        27 to ("El Cetro del Mando" to "Autoridad, intelecto brillante y liderazgo creador."),
        28 to ("El Cordero Confiado" to "Peligro de pérdida legal o financiera por confiar ciegamente."),
        29 to ("Incertidumbres y Engaños" to "Pruebas de carácter ante deslealtades ocultas."),
        30 to ("El Pensador" to "Poder mental superior, desapego de lo efímero."),
        31 to ("El Solitario" to "Aislamiento intelectual y autosuficiencia profunda."),
        32 to ("Conexión y Éxito Colectivo" to "Poder de convocar y movilizar grupos. Gran fortuna comercial."),
        33 to ("Protección Universal" to "Maestría espiritual y respaldo cósmico benigno."),
        34 to ("Fuerza tras la Prueba" to "Crecimiento firme nacido de la disciplina."),
        35 to ("Advertencia Financiera" to "Riesgo de errores en inversiones o despilfarro."),
        36 to ("Genio y Autoridad" to "Capacidad ejecutiva y reconocimiento en el arte o ciencia."),
        37 to ("Alianza Afortunada" to "Armonía en el amor, fidelidad y prosperidad en sociedades."),
        38 to ("Ruptura y Desengaño" to "Inestabilidad en acuerdos afectivos y de negocios."),
        39 to ("Filosofía y Mente" to "Comprensión abstracta y sabiduría reflexiva."),
        40 to ("Retiro Estratégico" to "Paciencia y preparación en silencio."),
        41 to ("Liderazgo y Empresa" to "Iniciativa arrolladora para nuevos negocios."),
        42 to ("Afecto y Fortuna" to "Suerte en la vida social y relaciones beneficiosas."),
        43 to ("Fracaso y Dispersión" to "Tendencia a proyectos inconclusos o pérdidas por precipitación."),
        44 to ("Carga Pesada" to "Esfuerzo desmedido con frutos tardíos. Prudencia."),
        45 to ("Victoria Comercial" to "Éxito resonante en comercio y finanzas."),
        46 to ("Alianza Fértil de Rango" to "Progreso por vínculos de alta consideración."),
        47 to ("Decepción Afectiva" to "Riesgo de penas morales y quebrantos inesperados."),
        48 to ("Labor y Servicio" to "Trabajo arduo que requiere constancia inquebrantable."),
        49 to ("Prueba de Voluntad" to "Retos kármicos que demandan firmeza moral."),
        50 to ("Claridad y Movimiento" to "Brillo intelectual, facilidad de palabra y viajes venturosos."),
        51 to ("Victoria del Guerrero" to "Avance repentino y triunfo sobre adversarios."),
        52 to ("Doble Traición" to "Alerta extrema de colapso por confiar en falsos aliados."),
        53 to ("Vigilancia Suprema" to "Protección elevada que exige pureza de intención.")
    )

    // 2. HERMÉTICA DE WESTCOTT (Números primos y perfectos hasta 53)
    val WESTCOTT_PRIMES: Set<Int> = setOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53
    )

    val WESTCOTT_PERFECTS: Set<Int> = setOf(6, 28)

    val TRIANGULAR_NUMBERS: Set<Int> = setOf(1, 3, 6, 10, 15, 21, 28, 36, 45)

    // 3. COINCIDENCIA NATURAL (Glynis McCants)
    val MCCANTS_MIND_TRIAD: Set<Int> = setOf(1, 5, 7)
    val MCCANTS_BUSINESS_TRIAD: Set<Int> = setOf(2, 4, 8)
    val MCCANTS_CREATIVE_TRIAD: Set<Int> = setOf(3, 6, 9)

    /**
     * Calcula la Gematría Caldea de una palabra o frase
     */
    fun calculateChaldeanGematria(text: String): Int {
        val clean = text.uppercase().filter { it in 'A'..'Z' }
        if (clean.isEmpty()) return 0
        return clean.sumOf { CHALDEAN_LETTER_MAP[it] ?: 0 }
    }

    /**
     * Reduce un número a un solo dígito (salvo si se desea conservar maestros 10, 11, 22)
     */
    fun reduceToSingleDigit(number: Int, keepMaster: Boolean = false): Int {
        var n = number
        if (keepMaster && (n == 10 || n == 11 || n == 22 || n == 33)) return n
        while (n > 9) {
            var sum = 0
            var temp = n
            while (temp > 0) {
                sum += temp % 10
                temp /= 10
            }
            n = sum
        }
        return n
    }

    /**
     * Calcula el Camino de Vida Compuesto según Dan Millman
     * Para 1991-07-19: 1+9+9+1 + 0+7 + 1+9 = 37 -> 37/10
     */
    fun calculateMillmanLifePath(birthDate: LocalDate): Pair<Int, Int> {
        val y = birthDate.year
        val m = birthDate.monthValue
        val d = birthDate.dayOfMonth

        fun sumDigits(value: Int): Int {
            var v = value
            var s = 0
            while (v > 0) {
                s += v % 10
                v /= 10
            }
            return s
        }

        val totalSum = sumDigits(y) + sumDigits(m) + sumDigits(d)
        val reduced = reduceToSingleDigit(totalSum, keepMaster = true)
        return totalSum to reduced
    }

    /**
     * Analiza la Cuadrícula Pitagórica 3x3 de David A. Phillips para una fecha de nacimiento
     * Filas:
     * 3, 6, 9 (Mente)
     * 2, 5, 8 (Alma / Sentimientos)
     * 1, 4, 7 (Físico)
     */
    fun analyzePhillipsGrid(birthDate: LocalDate): PhillipsAnalysis {
        val dateString = String.format("%02d%02d%04d", birthDate.dayOfMonth, birthDate.monthValue, birthDate.year)
        val digitCounts = mutableMapOf<Int, Int>()
        for (i in 1..9) digitCounts[i] = 0

        dateString.forEach { c ->
            val digit = c.digitToIntOrNull()
            if (digit != null && digit in 1..9) {
                digitCounts[digit] = (digitCounts[digit] ?: 0) + 1
            }
        }

        val missingDigits = (1..9).filter { (digitCounts[it] ?: 0) == 0 }

        // Flechas ausentes
        val arrowOfFrustration = (digitCounts[4] ?: 0) == 0 && (digitCounts[5] ?: 0) == 0 && (digitCounts[6] ?: 0) == 0
        val arrowOfHypersensitivity = (digitCounts[2] ?: 0) == 0 && (digitCounts[5] ?: 0) == 0 && (digitCounts[8] ?: 0) == 0
        val arrowOfSkepticism = (digitCounts[3] ?: 0) == 0 && (digitCounts[5] ?: 0) == 0 && (digitCounts[7] ?: 0) == 0

        // Flechas presentes
        val arrowOfDetermination = (digitCounts[1] ?: 0) > 0 && (digitCounts[5] ?: 0) > 0 && (digitCounts[9] ?: 0) > 0
        val arrowOfIntellect = (digitCounts[3] ?: 0) > 0 && (digitCounts[6] ?: 0) > 0 && (digitCounts[9] ?: 0) > 0
        val arrowOfPracticality = (digitCounts[1] ?: 0) > 0 && (digitCounts[4] ?: 0) > 0 && (digitCounts[7] ?: 0) > 0

        val hasIsolated7 = (digitCounts[7] ?: 0) > 0 &&
                (digitCounts[4] ?: 0) == 0 && (digitCounts[5] ?: 0) == 0 && (digitCounts[8] ?: 0) == 0

        // Número Regente (Ruling Number)
        val sumAll = dateString.map { it.digitToInt() }.sum()
        val rulingNumber = reduceToSingleDigit(sumAll, keepMaster = true)

        // 4 Pirámides (Pinnacles)
        val baseAge = 36 - (if (rulingNumber == 10) 10 else rulingNumber)
        val reducedDay = reduceToSingleDigit(birthDate.dayOfMonth)
        val reducedMonth = reduceToSingleDigit(birthDate.monthValue)
        val reducedYear = reduceToSingleDigit(birthDate.year)

        val pinnacle1 = reduceToSingleDigit(reducedMonth + reducedDay, keepMaster = true) // Edad baseAge
        val pinnacle2 = reduceToSingleDigit(reducedDay + reducedYear, keepMaster = true) // Edad baseAge + 9
        val pinnacle3 = reduceToSingleDigit(pinnacle1 + pinnacle2, keepMaster = true) // Edad baseAge + 18
        val pinnacle4 = reduceToSingleDigit(reducedMonth + reducedYear, keepMaster = true) // Edad baseAge + 27

        return PhillipsAnalysis(
            digitCounts = digitCounts,
            missingDigits = missingDigits,
            arrowOfFrustration = arrowOfFrustration,
            arrowOfHypersensitivity = arrowOfHypersensitivity,
            arrowOfSkepticism = arrowOfSkepticism,
            arrowOfDetermination = arrowOfDetermination,
            arrowOfIntellect = arrowOfIntellect,
            arrowOfPracticality = arrowOfPracticality,
            hasIsolated7 = hasIsolated7,
            rulingNumber = rulingNumber,
            pyramids = listOf(
                PyramidPeak(1, baseAge, pinnacle1, "Mes + Día: Aprendizaje cimiento"),
                PyramidPeak(2, baseAge + 9, pinnacle2, "Día + Año: Expresión y afirmación creativa"),
                PyramidPeak(3, baseAge + 18, pinnacle3, "Cúspide 1 + Cúspide 2: Trascendencia y maestría"),
                PyramidPeak(4, baseAge + 27, pinnacle4, "Mes + Año: Cosecha y servicio universal")
            )
        )
    }

    /**
     * Realiza el análisis completo de una bolilla frente a los 5 tratados y el perfil
     */
    fun analyzeBall(ball: Int, user: UserProfile, historicalCount: Int = 0): NumberAnalysis {
        val single = reduceToSingleDigit(ball)
        val isFortunate = ball in FORTUNATE_COMPOUNDS
        val isWarning = ball in WARNING_COMPOUNDS
        val compoundInfo = COMPOUND_MEANINGS[ball]
        val compoundName = compoundInfo?.first ?: (if (ball <= 9) "Dígito Simple $ball" else "Compuesto $ball")
        val cheiroExpl = compoundInfo?.second ?: "Vibración caldea base asociada al número $single."

        // Millman
        val (pathSum, pathReduced) = calculateMillmanLifePath(user.birthDate)
        val matchesMillman = ball == pathSum || ball == pathReduced || ball == 37 || ball == 10 || ball == 19 || single == 1 || single == 3 || single == 7
        val millmanExpl = if (matchesMillman) {
            "Sintoniza con el sendero 37/10 de Iván (expresión 3, confianza 7 y liderazgo 10)."
        } else {
            "Número secundario en el esquema del propósito vital."
        }

        // Phillips
        val phillips = analyzePhillipsGrid(user.birthDate)
        val fillsMissing = ball in phillips.missingDigits || single in phillips.missingDigits
        val phillipsArrow = when {
            single in listOf(4, 5, 6) && phillips.arrowOfFrustration ->
                "Compensa y equilibra la Flecha de la Frustración (4-5-6 ausente)."
            single in listOf(2, 5, 8) && phillips.arrowOfHypersensitivity ->
                "Aporta solidez a la Flecha de la Sensibilidad (2-5-8 ausente)."
            single == 7 ->
                "Resuena con el 7 aislado natal, sublimando pruebas en visión mística."
            else -> "Alineado con los planos de la cuadrícula pitagórica."
        }

        // Pirámide activa (Para Iván con 35 años cumplidos en 2026, la cúspide 2 está activa: valor 3)
        val currentAge = LocalDate.now().year - user.birthDate.year
        val activePinnacle = phillips.pyramids.find { currentAge in (it.peakAge - 4)..(it.peakAge + 4) }
            ?: phillips.pyramids[1] // Por defecto Cúspide 2 (35 años)
        val pyramidConn = if (single == activePinnacle.vibrationNumber || ball == activePinnacle.vibrationNumber) {
            "¡Coincidencia exacta con la Pirámide de Vida activa a los 35 años (Cúspide #${activePinnacle.peakNumber}: vibración ${activePinnacle.vibrationNumber})!"
        } else {
            "Vibración complementaria a la etapa actual de vida."
        }

        // Westcott
        val isPrime = ball in WESTCOTT_PRIMES
        val isPerfect = ball in WESTCOTT_PERFECTS
        val westcottExpl = when {
            isPerfect -> "Número Perfecto hermético (armonía divina y equilibrio cósmico absoluto)."
            isPrime -> "Número Primo puro (mónada indivisible, vibración arquetípica pura)."
            ball in TRIANGULAR_NUMBERS -> "Número Triangular hermético (fuerza geométrica sagrada)."
            else -> "Número compuesto derivado."
        }

        // McCants
        val isNaturalMatch = single in MCCANTS_MIND_TRIAD // Para Iván nacido el 19 -> 1: Tríada de la Mente (1, 5, 7)
        val mccantsTriad = when (single) {
            in MCCANTS_MIND_TRIAD -> "Tríada de la Mente (1, 5, 7) - Coincidencia Natural Suprema"
            in MCCANTS_BUSINESS_TRIAD -> "Tríada de Negocios (2, 4, 8) - Compatible / Material"
            else -> "Tríada Creativa (3, 6, 9) - Reto / Expresión"
        }

        // Cálculo de Puntuación Compuesta
        var score = 50.0 // Base
        if (isFortunate) score += 35.0
        if (isWarning) score -= 30.0
        if (single in listOf(1, 4)) score += 20.0
        if (single in listOf(2, 7)) score += 15.0
        if (single in listOf(8, 9)) score -= 15.0

        if (matchesMillman) score += 25.0
        if (fillsMissing) score += 15.0
        if (single == activePinnacle.vibrationNumber) score += 20.0
        if (isPrime) score += 15.0
        if (isPerfect) score += 30.0
        if (isNaturalMatch) score += 25.0

        // Frecuencia histórica (hasta 20 puntos adicionales)
        score += (historicalCount * 2.5).coerceAtMost(20.0)

        return NumberAnalysis(
            ballNumber = ball,
            cheiroCompoundName = compoundName,
            cheiroIsFortunate = isFortunate,
            cheiroIsWarning = isWarning,
            cheiroSingleRoot = single,
            cheiroExplanation = cheiroExpl,
            millmanExplanation = millmanExpl,
            millmanMatchesBirthPath = matchesMillman,
            phillipsFillsMissingDigit = fillsMissing,
            phillipsArrowDescription = phillipsArrow,
            phillipsPyramidConnection = pyramidConn,
            westcottIsPrime = isPrime,
            westcottIsPerfect = isPerfect,
            westcottExplanation = westcottExpl,
            mccantsTriad = mccantsTriad,
            mccantsIsNaturalMatch = isNaturalMatch,
            historicalFrequencyCount = historicalCount,
            compositeScore = score
        )
    }
}

data class PhillipsAnalysis(
    val digitCounts: Map<Int, Int>,
    val missingDigits: List<Int>,
    val arrowOfFrustration: Boolean,
    val arrowOfHypersensitivity: Boolean,
    val arrowOfSkepticism: Boolean,
    val arrowOfDetermination: Boolean,
    val arrowOfIntellect: Boolean,
    val arrowOfPracticality: Boolean,
    val hasIsolated7: Boolean,
    val rulingNumber: Int,
    val pyramids: List<PyramidPeak>
)

data class PyramidPeak(
    val peakNumber: Int,
    val peakAge: Int,
    val vibrationNumber: Int,
    val description: String
)
