package com.oraculoloto5x.app.ai

import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.models.*
import com.oraculoloto5x.app.engine.DailyRiskAssessment
import com.oraculoloto5x.app.engine.OracleGenerationResult
import com.oraculoloto5x.app.pdf.BookCitation
import com.oraculoloto5x.app.pdf.BookCitationRegistry
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

data class HermeticContextBundle(
    val systemInstruction: String,
    val allowedTagsWhitelist: List<String>,
    val relevantCitations: List<BookCitation>
)

data class ParsedCitationsResult(
    val cleanedText: String,
    val citations: List<BookCitation>
)

object HermeticKnowledgeRetriever {

    // Lista blanca cerrada de etiquetas válidas para evitar alucinaciones
    val ALLOWED_TAGS_MAP: Map<String, BookCitation> = mapOf(
        // Cheiro
        "[PDF:cheiro:37]" to BookCitationRegistry.CHEIRO_SERIE_1,
        "[PDF:cheiro:70]" to BookCitationRegistry.CHEIRO_ALFABETO_CALDEO,
        "[PDF:cheiro:80]" to BookCitationRegistry.CHEIRO_COMPUESTOS_10_14,
        "[PDF:cheiro:81]" to BookCitationRegistry.CHEIRO_COMPUESTOS_15_18,
        "[PDF:cheiro:82]" to BookCitationRegistry.CHEIRO_COMPUESTOS_19_23,
        "[PDF:cheiro:83]" to BookCitationRegistry.CHEIRO_COMPUESTOS_24_30,
        "[PDF:cheiro:84]" to BookCitationRegistry.CHEIRO_COMPUESTOS_31_43,
        "[PDF:cheiro:85]" to BookCitationRegistry.CHEIRO_COMPUESTOS_44_52,
        "[PDF:cheiro:152]" to BookCitationRegistry.CHEIRO_APUESTAS,

        // Dan Millman
        "[PDF:millman:144]" to BookCitationRegistry.MILLMAN_SENDERO_37_10,
        "[PDF:millman:132]" to BookCitationRegistry.MILLMAN_19_10,
        "[PDF:millman:76]" to BookCitationRegistry.MILLMAN_PENSAMIENTO_MAGICO,
        "[PDF:millman:108]" to BookCitationRegistry.MILLMAN_ABUNDANCIA_8,
        "[PDF:millman:150]" to BookCitationRegistry.MILLMAN_46_10,

        // David Phillips
        "[PDF:phillips:16]" to BookCitationRegistry.PHILLIPS_CUADRICULA_3X3,
        "[PDF:phillips:41]" to BookCitationRegistry.PHILLIPS_7_AISLADO,
        "[PDF:phillips:60]" to BookCitationRegistry.PHILLIPS_FLECHA_FRUSTRACION,
        "[PDF:phillips:86]" to BookCitationRegistry.PHILLIPS_REGENTE_10,
        "[PDF:phillips:100]" to BookCitationRegistry.PHILLIPS_CICLO_9,
        "[PDF:phillips:132]" to BookCitationRegistry.PHILLIPS_PIRAMIDES,

        // W. W. Westcott
        "[PDF:westcott:21]" to BookCitationRegistry.WESTCOTT_PRIMOS_PERFECTOS,
        "[PDF:westcott:33]" to BookCitationRegistry.WESTCOTT_CUADRADOS_MAGICOS,
        "[PDF:westcott:49]" to BookCitationRegistry.WESTCOTT_HORAS_PLANETARIAS,
        "[PDF:westcott:63]" to BookCitationRegistry.WESTCOTT_CASA_5_AGATHE_TUCHE,
        "[PDF:westcott:115]" to BookCitationRegistry.WESTCOTT_NUMERO_19,

        // Glynis McCants (Índices válidos: gyhn.pdf tiene 13 páginas, pág 4 y 6)
        "[PDF:mccants:4]" to BookCitationRegistry.MCCANTS_NATURAL_MATCH,
        "[PDF:mccants:6]" to BookCitationRegistry.MCCANTS_LUCKY_LOTTO
    )

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

        // ── 1. Citas relevantes y lista blanca de etiquetas ─────────────────────────
        val coreWhitelistTags = listOf(
            "[PDF:cheiro:82]",
            "[PDF:cheiro:152]",
            "[PDF:millman:144]",
            "[PDF:phillips:60]",
            "[PDF:westcott:63]",
            "[PDF:mccants:4]",
            "[PDF:mccants:6]"
        )

        val dynamicTags = mutableSetOf<String>()
        val citationsSet = mutableSetOf<BookCitation>()

        // Agregar citas fijas de la lista blanca
        for (tag in coreWhitelistTags) {
            ALLOWED_TAGS_MAP[tag]?.let { citationsSet.add(it) }
        }

        // Agregar citas dinámicas base-0 de las 6 bolillas activas
        for (b in effectiveMasterBalls) {
            val ballCitations = BookCitationRegistry.getCitationsForBall(b)
            for (c in ballCitations) {
                val tag = "[PDF:${c.bookId.lowercase()}:${c.pdfPageIndex}]"
                if (ALLOWED_TAGS_MAP.containsKey(tag)) {
                    dynamicTags.add(tag)
                    citationsSet.add(c)
                }
            }
        }

        val allowedTagsList = (coreWhitelistTags + dynamicTags).distinct()

        // ── 2. Construcción de secciones del Snapshot Completo (~2 KB) ──────────────

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
            it.endSecond > currentSec && it.startSecond < cutoffSeconds
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

REGLAS ESTRICTAS DE RESPUESTA:
- Tono: analítico, claro, hermético y directo en español. Nunca inventar bolillas distintas a las del snapshot ni prometer premios seguros.
- Si el usuario pide un resumen general o pregunta "¿Por qué salieron estas 6 bolillas?", sintetiza de forma ordenada (máx. 220 palabras) integrando:
  1) Veredicto y mejor ventana horaria de hoy.
  2) Sinergia de las 6 bolillas con los 5 libros y la estadística de La Tinka.
  3) Consejo práctico de ejecución.
- Si el usuario hace una pregunta específica (sobre una bolilla, una hora, el último sorteo o un autor), responde directo al grano usando los datos exactos del snapshot e inserta únicamente etiquetas válidas de la lista blanca [PDF:libro:pagina].
- CITAS DE LIBROS: Puedes incluir referencias a las páginas de los tratados insertando AL FINAL de la oración correspondiente ÚNICAMENTE etiquetas de esta lista blanca permitida:
  ${allowedTagsList.joinToString(", ")}
- PROHIBIDO inventar etiquetas [PDF:...] que no estén en la lista blanca anterior.
""".trimIndent()

        return HermeticContextBundle(
            systemInstruction = systemInstruction,
            allowedTagsWhitelist = allowedTagsList,
            relevantCitations = citationsSet.toList()
        )
    }

    private val PDF_TAG_PATTERN = Pattern.compile("\\[PDF:([a-z]+):(\\d+)\\]")

    /**
     * Extrae las etiquetas válidas y limpia el texto para visualización,
     * retornando el texto limpio y la lista de citas reales.
     */
    fun parseCitationsFromText(rawText: String): ParsedCitationsResult {
        val matcher = PDF_TAG_PATTERN.matcher(rawText)
        val citations = mutableListOf<BookCitation>()

        while (matcher.find()) {
            val fullTag = matcher.group(0)
            ALLOWED_TAGS_MAP[fullTag]?.let {
                if (!citations.contains(it)) {
                    citations.add(it)
                }
            }
        }

        // Limpiar las etiquetas [PDF:...] del texto visible para que se lean de forma limpia
        val cleanedText = matcher.replaceAll("").replace("  ", " ").trim()
        return ParsedCitationsResult(cleanedText, citations)
    }
}
