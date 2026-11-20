package com.oraculoloto5x.app.ai

import com.oraculoloto5x.app.data.models.MoonPhaseInfo
import com.oraculoloto5x.app.data.models.PlanetaryHour
import com.oraculoloto5x.app.engine.DailyRiskAssessment
import com.oraculoloto5x.app.pdf.BookCitation
import com.oraculoloto5x.app.pdf.BookCitationRegistry
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

        // Glynis McCants
        "[PDF:mccants:4]" to BookCitationRegistry.MCCANTS_NATURAL_MATCH,
        "[PDF:mccants:6]" to BookCitationRegistry.MCCANTS_LUCKY_LOTTO
    )

    fun buildContextBundle(
        masterPlayBalls: List<Int>,
        riskAssessment: DailyRiskAssessment?,
        currentHour: PlanetaryHour?,
        moonInfo: MoonPhaseInfo?,
        lastOfficialDraw: List<Int>?
    ): HermeticContextBundle {
        val relevantCitations = mutableSetOf<BookCitation>()

        // Citas estructurales fijas
        relevantCitations.add(BookCitationRegistry.MILLMAN_SENDERO_37_10)
        relevantCitations.add(BookCitationRegistry.CHEIRO_SERIE_1)
        relevantCitations.add(BookCitationRegistry.PHILLIPS_FLECHA_FRUSTRACION)
        relevantCitations.add(BookCitationRegistry.WESTCOTT_CASA_5_AGATHE_TUCHE)
        relevantCitations.add(BookCitationRegistry.MCCANTS_NATURAL_MATCH)
        relevantCitations.add(BookCitationRegistry.CHEIRO_APUESTAS)

        // Citas dinámicas por bolillas
        for (b in masterPlayBalls) {
            relevantCitations.addAll(BookCitationRegistry.getCitationsForBall(b))
        }

        val allowedTagsList = ALLOWED_TAGS_MAP.keys.toList()
        val ballsStr = masterPlayBalls.sorted().joinToString(" - ") { it.toString().padStart(2, '0') }
        val lastDrawStr = lastOfficialDraw?.joinToString(" - ") { it.toString().padStart(2, '0') } ?: "Ninguno"
        val semaforoStr = riskAssessment?.title ?: "SEMÁFORO VERDE"
        val horaCuscoStr = currentHour?.let { "${it.planet.spanishName} (${it.formattedInterval})" } ?: "Hora Solar"
        val lunaStr = moonInfo?.displaySummary ?: "Luna Creciente 50%"

        val citationsDigest = relevantCitations.take(10).joinToString("\n") { c ->
            "- ${c.author} (${c.chapterLabel}): \"${c.literalQuote.take(120)}...\""
        }

        val systemInstruction = """
Eres el Oráculo Loto 5X, un sabio maestro hermético y analista de loterías que asesora a Iván Josseph Chuctaya Paucar.
Sintetizas cinco tratados ancestrales aplicados a La Tinka (Perú):
1. Cheiro — Numerología Caldea, serie solar 1-4, compuestos (Cap. XXX de apuestas).
2. Dan Millman — Sendero de Vida 37/10 y leyes de manifestación.
3. David Phillips — Cuadrícula 3x3, mitigación de la Flecha de Frustración (4-5-6) y 7 aislado.
4. W. W. Westcott — Primos y Perfectos (6, 28), Horas Planetarias y Casa 5 Agathe Tuche (regentes 5 y 6 por Ascendente Tauro y Sideral 22h01m en Sicuani).
5. Glynis McCants — Tríada de afinidad natural 1-5-7 y Lucky Lotto.

ESTADO Y JUGADA CALCULADA HOY EN ANDROID:
- Bolillas Maestras (La Tinka): $ballsStr
- Semáforo del día: $semaforoStr
- Reloj Planetario en Cusco: $horaCuscoStr
- Fase Lunar: $lunaStr
- Último sorteo oficial registrado (Cooldown aplicado): $lastDrawStr

REGLAS ESTRICTAS DE RESPUESTA:
- Responde SIEMPRE en español con tono sabio, conciso, sereno y estratégico (máximo 160 palabras).
- NUNCA inventes bolillas ni cambies la combinación de 6 números calculada.
- Responde directamente a las dudas del usuario guiándolo hacia el momento más propicio y el significado de sus números.
- CITAS DE LIBROS: Puedes incluir referencias a las páginas de los tratados insertando AL FINAL de la oración correspondiente ÚNICAMENTE etiquetas de esta lista blanca permitida:
  ${allowedTagsList.joinToString(", ")}
- PROHIBIDO inventar etiquetas [PDF:...] que no estén en la lista blanca anterior.
""".trimIndent()

        return HermeticContextBundle(
            systemInstruction = systemInstruction,
            allowedTagsWhitelist = allowedTagsList,
            relevantCitations = relevantCitations.toList()
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
