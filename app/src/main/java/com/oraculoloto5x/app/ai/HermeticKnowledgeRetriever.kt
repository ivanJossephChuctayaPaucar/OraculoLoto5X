package com.oraculoloto5x.app.ai

import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.*
import com.oraculoloto5x.app.engine.DailyRiskAssessment
import com.oraculoloto5x.app.engine.OracleGenerationResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

data class HermeticContextBundle(
    val systemInstruction: String
)

object HermeticKnowledgeRetriever {

    fun buildContextBundle(
        userProfile: UserProfile = UserProfile(),
        oracleResult: OracleGenerationResult? = null,
        currentHour: PlanetaryHour? = null,
        allPlanetaryHours: List<PlanetaryHour> = emptyList(),
        lastOfficialDraw: DrawResult? = null,
        historicalFrequencies: Map<Int, Int> = emptyMap(),
        savedPlays: List<SavedPlay> = emptyList(),
        currentTime: LocalTime = LocalTime.now(),
        currentDate: LocalDate = LocalDate.now(),
        masterPlayBalls: List<Int>? = null,
        riskAssessment: DailyRiskAssessment? = null,
        moonInfo: MoonPhaseInfo? = null
    ): HermeticContextBundle {
        val effectiveMasterBalls = oracleResult?.masterPlay ?: masterPlayBalls ?: emptyList()
        val effectiveRisk = oracleResult?.riskAssessment ?: riskAssessment
        val effectiveMoon = oracleResult?.moonInfo ?: moonInfo ?: NatalAstronomicalProfile.getMoonSyncBonus(currentDate.toEpochDay())

        // ── Construcción de secciones del Snapshot Completo (~2 KB) ─────────────────

        // SECCIÓN 1: PERFIL NATAL Y CICLOS ACTIVOS
        val birthDayRed = NumerologyKnowledgeBase.reduceToSingleDigit(userProfile.birthDate.dayOfMonth)
        val birthMonthRed = NumerologyKnowledgeBase.reduceToSingleDigit(userProfile.birthDate.monthValue)
        val yearRed = NumerologyKnowledgeBase.reduceToSingleDigit(currentDate.year)
        val personalYear = NumerologyKnowledgeBase.reduceToSingleDigit(birthDayRed + birthMonthRed + yearRed)
        val personalMonth = NumerologyKnowledgeBase.reduceToSingleDigit(personalYear + currentDate.monthValue)
        val personalDay = NumerologyKnowledgeBase.reduceToSingleDigit(personalMonth + currentDate.dayOfMonth)

        val natalSection = """
1. PERFIL NATAL Y CICLOS ACTIVOS:
- Operador: ${userProfile.fullName} | Nacimiento: 19/07/1991 02:00 AM, Cúspide Cáncer-Leo 26°
- Coordenadas Origen: Sicuani (-14.2667, -71.2333, Gematría Caldea=20->2, armónico lunar 2-7 de Cheiro)
- Astronomía Natal: Hora Sideral 22h 01m, Ascendente 27° Tauro, Casa 5 Agathe Tuche (regentes Mercurio 5 y Venus 6), Luna Natal Cuarto Creciente 50%
- Dan Millman: Sendero de Vida 37/10 (expresión 3, confianza 7, autoridad solar 10)
- David Phillips: Cuadrícula Pitagórica (111, 7, 99; ausentes 4-5-6 Flecha de la Frustración; 7 aislado mitigado)
- Ciclos Personales Hoy ($currentDate): Año Personal $personalYear, Mes Personal $personalMonth, Día Personal $personalDay
- Pirámide Activa Actual: 2da Pirámide = 8 (consolidación material y justicia cósmica a los 35 años)
- Cheiro & McCants: Serie Solar 1-4 con armónicos 2-7; Tríada de afinidad natural 1-5-7
""".trimIndent()

        // SECCIÓN 2: ESTADO ASTRONÓMICO COMPLETO DE HOY (CUSCO)
        val semaforoStr = effectiveRisk?.title ?: "SEMÁFORO VERDE"
        val semaforoRazon = effectiveRisk?.explanation ?: "Vibración armónica solar."
        val horaActualStr = currentHour?.let { "${it.planet.spanishName} (${it.formattedInterval})" } ?: "Hora Solar"
        val moonStr = "${effectiveMoon.displaySummary} (Bono de sincronía: +${effectiveMoon.bonus})"

        // Ventanas favorables restantes antes de las 21:50
        val favorableHours = allPlanetaryHours.filter { it.planet == Planet.SUN || it.planet == Planet.MOON }
        val cutoffSeconds = 21 * 3600 + 50 * 60 // 21:50 cierre La Tinka
        val currentSec = currentTime.toSecondOfDay()
        val remainingFavorable = favorableHours.filter {
            it.endTime.toSecondOfDay() > currentSec && it.startTime.toSecondOfDay() < cutoffSeconds
        }
        val ventanasStr = if (remainingFavorable.isNotEmpty()) {
            remainingFavorable.joinToString("; ") { "${it.planet.spanishName} (${it.formattedInterval})" }
        } else {
            "Sin ventanas restantes antes del cierre (21:50). Todas las del día: " +
                    favorableHours.joinToString("; ") { "${it.planet.spanishName} (${it.formattedInterval})" }
        }

        val astroSection = """
2. ESTADO ASTRONÓMICO COMPLETO DE HOY (CUSCO):
- Fecha y Hora Actual: $currentDate ${currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))}
- Semáforo del Día: $semaforoStr | Razón: $semaforoRazon
- Fase e Iluminación Lunar: $moonStr
- Hora Planetaria Actual: $horaActualStr
- Ventanas Favorables Restantes Hoy (Sol y Luna antes de 21:50): $ventanasStr
""".trimIndent()

        // SECCIÓN 3: RADIOGRAFÍA COMPLETA DE LA JUGADA MAESTRA
        val ballsList = effectiveMasterBalls.sorted()
        val ballsStr = ballsList.joinToString(" - ") { it.toString().padStart(2, '0') }

        val breakdownList = ballsList.map { b ->
            val analysis = oracleResult?.allBallsAnalysis?.find { it.ballNumber == b }
                ?: NumerologyKnowledgeBase.analyzeBall(b, userProfile)
            val scoreStr = String.format(Locale.US, "%.1f", analysis.compositeScore)
            val millmanMatch = if (analysis.millmanMatchesBirthPath) "Sendero 37/10" else "Armónico"
            val westcottNote = when {
                analysis.westcottIsPerfect -> "Perfecto (6,28)"
                analysis.westcottIsPrime -> "Primo sagrado"
                b in setOf(5, 6) || analysis.singleDigit in setOf(5, 6) -> "Regente Casa 5"
                else -> "Compuesto armónico"
            }
            val mccantsNote = if (analysis.mccantsIsNaturalMatch) "Tríada Natural 1-5-7" else analysis.mccantsTriad
            val phillipsNote = if (analysis.phillipsArrowDescription.isNotBlank()) analysis.phillipsArrowDescription else "Equilibrio"

            "* Bolilla ${b.toString().padStart(2, '0')} ($scoreStr pts): Cheiro: ${analysis.cheiroCompoundName}; Millman: $millmanMatch; Phillips: $phillipsNote; Westcott: $westcottNote; McCants: $mccantsNote."
        }.joinToString("\n")

        val sumTotal = ballsList.sum()
        val evens = ballsList.count { it % 2 == 0 }
        val odds = ballsList.size - evens
        val decadesActive = ballsList.map { it / 10 }.distinct().sorted().joinToString(", ") { "${it}x" }
        val primesCount = ballsList.count { it in NumerologyKnowledgeBase.WESTCOTT_PRIMES }
        val perfectsCount = ballsList.count { it in NumerologyKnowledgeBase.WESTCOTT_PERFECTS }

        val wheel9List = oracleResult?.reducedWheel9?.sorted() ?: emptyList()
        val wheel9Str = if (wheel9List.isNotEmpty()) wheel9List.joinToString(" - ") { it.toString().padStart(2, '0') } else "No disponible"
        val backupBalls = wheel9List.filter { it !in ballsList }
        val backupStr = if (backupBalls.isNotEmpty()) backupBalls.joinToString(" - ") { it.toString().padStart(2, '0') } else "Ninguna"
        val anchorStr = oracleResult?.intentionAnchorBall?.let {
            "$it (Gematría Caldea: ${oracleResult.intentionGematriaSum})"
        } ?: "Ninguna ancla manual (combinación armónica pura)"

        val masterSection = """
3. RADIOGRAFÍA COMPLETA DE LA JUGADA MAESTRA (MONTE CARLO 10,000 + GAUSS):
- 6 Bolillas Maestras (La Tinka): $ballsStr
- Desglose por Tratado de cada Bolilla:
$breakdownList
- Métricas Gaussianas Exactas: Suma Total=$sumTotal (rango 100-175) | Paridad=${evens}P/${odds}I | Decenas Activas=[$decadesActive] | Primos=$primesCount, Perfectos=$perfectsCount
- Rueda Reducida (9 bolillas): $wheel9Str | 3 Bolillas de Respaldo: $backupStr
- Bolilla Ancla de Intención: $anchorStr
""".trimIndent()

        // SECCIÓN 4: INTELIGENCIA HISTÓRICA DE LA TINKA
        val lastDrawStr = if (lastOfficialDraw != null) {
            val drawBalls = lastOfficialDraw.winningBalls.sorted().joinToString(" - ") { it.toString().padStart(2, '0') }
            "Fecha: ${lastOfficialDraw.drawDate} | 6 Bolillas: $drawBalls (Penalización de enfriamiento aplicada: -15.0 pts a cada una)"
        } else {
            "Sin sorteo oficial previo registrado en la base de datos local"
        }

        val frequenciesSection = if (historicalFrequencies.isNotEmpty()) {
            val sortedFreq = historicalFrequencies.entries.sortedByDescending { it.value }
            val hot5 = sortedFreq.take(5).joinToString(", ") { "Bolilla ${it.key} (${it.value}x)" }
            val cold5 = sortedFreq.takeLast(5).reversed().joinToString(", ") { "Bolilla ${it.key} (${it.value}x)" }
            "Top 5 Bolillas Calientes: $hot5\nTop 5 Bolillas Frías: $cold5"
        } else {
            "Frecuencias históricas: base inicial sincronizada"
        }

        val backtestSummary = if (savedPlays.isNotEmpty()) {
            val totalSaved = savedPlays.size
            val maxMatched = savedPlays.maxOfOrNull { it.matchedCount } ?: 0
            "Total de jugadas guardadas: $totalSaved | Máximo de aciertos registrado: $maxMatched/6"
        } else {
            "Aún no hay jugadas guardadas para backtesting"
        }

        val historySection = """
4. INTELIGENCIA HISTÓRICA DE LA TINKA (SQLITE):
- Último Sorteo Oficial: $lastDrawStr
- $frequenciesSection
- Resumen de Backtest Local: $backtestSummary
""".trimIndent()

        // SÍNTESIS FINAL DE INSTRUCCIONES
        val systemInstruction = """
Eres el Oráculo Loto 5X, maestro hermético y asesor analítico de lotería para Iván Josseph Chuctaya Paucar en La Tinka (Perú).
A continuación tienes el SNAPSHOT COMPLETO DE TELEMETRÍA con todos los cálculos locales de la app:

$natalSection

$astroSection

$masterSection

$historySection

REGLAS DE RESPUESTA ELABORADA Y HERMÉTICA:
- Tono: maestro hermético erudito, analítico, didáctico y elocuente en español. Explica a fondo cada concepto, cálculo y sincronía con paciencia, profundidad y claridad. Nunca inventar bolillas distintas a las del snapshot ni prometer premios seguros.
- Extensión y profundidad: Desarrolla respuestas ricas, completas y bien articuladas (utiliza Markdown con encabezados ###, viñetas detalladas y párrafos explicativos). No te limites a resúmenes breves: desglosa las razones matemáticas, numerológicas e históricas que respaldan cada cálculo.
- Cuando el usuario pregunte por qué salieron las bolillas, el estado del día o cualquier aspecto de la jugada, elabora una respuesta completa y profunda integrando:
  1) Veredicto Astronómico y Semáforo: El significado de la hora planetaria activa en Cusco, la influencia lunar y la ventana ideal de Sol o Luna antes de las 21:50.
  2) Radiografía Hermética por Bolilla: Explica con detalle cómo convergen los 5 tratados en las bolillas seleccionadas (Cheiro: compuestos caldeos; Millman: propósito de vida 37/10; Phillips: cuadrícula pitagórica y pirámides; Westcott: primos y perfectos; McCants: tríada 1-5-7).
  3) Validación Estadística: Distribución en campana de Gauss, balance par/impar, frecuencias históricas de La Tinka y efecto del filtro de enfriamiento.
  4) Guía Táctica de Jugada: Recomendaciones de juego, rueda reducida de 9 bolillas y enfoque mental.
- Menciona los tratados (Cheiro, Millman, Phillips, Westcott, McCants) por su nombre cuando expliques un cálculo, pero NO uses etiquetas ni referencias a páginas de libros.
""".trimIndent()

        return HermeticContextBundle(systemInstruction = systemInstruction)
    }

    private val LEGACY_TAG_PATTERN = Pattern.compile("\\[PDF:[a-z]+:\\d+\\]")

    /**
     * Elimina etiquetas [PDF:...] heredadas de mensajes antiguos guardados en el historial
     * del chat, para que no se muestren como texto crudo.
     */
    fun stripLegacyCitationTags(rawText: String): String {
        return LEGACY_TAG_PATTERN.matcher(rawText).replaceAll("").replace("  ", " ").trim()
    }
}
