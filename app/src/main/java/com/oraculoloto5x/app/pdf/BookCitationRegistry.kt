package com.oraculoloto5x.app.pdf

data class BookCitation(
    val bookId: String,
    val expectedFileName: String,
    val bookTitle: String,
    val author: String,
    val chapterLabel: String,
    val pdfPageIndex: Int,
    val literalQuote: String
)

object BookCitationRegistry {

    // ── MILLMAN ──────────────────────────────────────────────────────────────
    val MILLMAN_SENDERO_37_10 = BookCitation(
        bookId = "MILLMAN",
        expectedFileName = "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
        bookTitle = "The Life You Were Born to Live", author = "Dan Millman",
        chapterLabel = "Sendero 37/10 — El Creador Visionario", pdfPageIndex = 144,
        literalQuote = "Las personas en el sendero 37/10 poseen una mente brillante y creativa, pero deben aprender a confiar en su proceso interior antes de manifestar sus visiones en el mundo."
    )
    val MILLMAN_19_10 = BookCitation(
        bookId = "MILLMAN",
        expectedFileName = "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
        bookTitle = "The Life You Were Born to Live", author = "Dan Millman",
        chapterLabel = "Sendero 19/10 — El Príncipe del Cielo", pdfPageIndex = 132,
        literalQuote = "El 19/10 irradia autoridad solar. Su desafío es equilibrar la independencia creativa con la responsabilidad hacia los demás."
    )
    val MILLMAN_PENSAMIENTO_MAGICO = BookCitation(
        bookId = "MILLMAN",
        expectedFileName = "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
        bookTitle = "The Life You Were Born to Live", author = "Dan Millman",
        chapterLabel = "Pensamiento mágico — Avisos Lotería (Cap. 4)", pdfPageIndex = 76,
        literalQuote = "El pensamiento mágico puede ser una trampa: esperar que el universo provea sin esfuerzo personal. La fe activa requiere acción alineada."
    )
    val MILLMAN_ABUNDANCIA_8 = BookCitation(
        bookId = "MILLMAN",
        expectedFileName = "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
        bookTitle = "The Life You Were Born to Live", author = "Dan Millman",
        chapterLabel = "Abundancia — El 8 (Cap. 8)", pdfPageIndex = 108,
        literalQuote = "El 8 es el número de la abundancia material y el poder ejecutivo. Quienes lo tienen en su sendero deben aprender a gestionar el dinero con sabiduría."
    )
    val MILLMAN_46_10 = BookCitation(
        bookId = "MILLMAN",
        expectedFileName = "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
        bookTitle = "The Life You Were Born to Live", author = "Dan Millman",
        chapterLabel = "Sendero 46/10", pdfPageIndex = 150,
        literalQuote = "El sendero 46/10 combina la estabilidad del 4, la libertad del 6 y la potencia creativa del 10."
    )

    // ── PHILLIPS ─────────────────────────────────────────────────────────────
    val PHILLIPS_CUADRICULA_3X3 = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "La Cuadrícula 3×3 de Pitágoras", pdfPageIndex = 16,
        literalQuote = "La cuadrícula pitagórica revela el mapa de energías innatas: cada dígito del 1 al 9 ocupa una posición específica que refleja fortalezas y áreas de desarrollo."
    )
    val PHILLIPS_7_AISLADO = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "El 7 Aislado — El Filósofo Solitario", pdfPageIndex = 41,
        literalQuote = "Una sola aparición del 7 en la cuadrícula indica una mente filosófica y analítica pero con tendencia al aislamiento."
    )
    val PHILLIPS_FLECHA_FRUSTRACION = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "Flecha de la Frustración (4-5-6)", pdfPageIndex = 60,
        literalQuote = "La ausencia de los dígitos 4, 5 y 6 en la fecha de nacimiento forma la Flecha de la Frustración: tendencia hacia la impaciencia y dificultad para aceptar las limitaciones del plano material."
    )
    val PHILLIPS_REGENTE_10 = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "El Regente del Número 10", pdfPageIndex = 86,
        literalQuote = "El 10 como número regente indica potencial de liderazgo y originalidad. Es el arquetipo del pionero que abre caminos donde otros ven obstáculos."
    )
    val PHILLIPS_CICLO_9 = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "Ciclo de 9 Años", pdfPageIndex = 100,
        literalQuote = "Cada ciclo de 9 años representa una espiral de evolución. Conocer en qué año personal te encuentras permite alinear tus acciones con el flujo natural del tiempo."
    )
    val PHILLIPS_PIRAMIDES = BookCitation(
        bookId = "PHILLIPS",
        expectedFileName = "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
        bookTitle = "The Complete Book of Numerology", author = "David A. Phillips",
        chapterLabel = "Las Pirámides Numéricas", pdfPageIndex = 132,
        literalQuote = "Las pirámides numéricas muestran los picos de energía de tu vida: el primero marca el despertar, el segundo la consolidación y el tercero la maestría."
    )

    // ── CHEIRO ───────────────────────────────────────────────────────────────
    val CHEIRO_SERIE_1 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "La Serie del Sol — 1, 10, 19, 28", pdfPageIndex = 37,
        literalQuote = "Las personas nacidas bajo la influencia del Número 1 —o sus series 10, 19 y 28— están dominadas por el Sol. Son individualistas, creativos y naturalmente líderes."
    )
    val CHEIRO_ALFABETO_CALDEO = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "El Alfabeto Caldeo — Tabla Gematría", pdfPageIndex = 70,
        literalQuote = "En el sistema Caldeo, cada letra tiene un valor numérico del 1 al 8. El 9 se considera sagrado y nunca se asigna a ninguna letra."
    )
    val CHEIRO_COMPUESTOS_10_14 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 10 al 14", pdfPageIndex = 80,
        literalQuote = "El 10 es 'La Rueda de la Fortuna'; el 11 el peligro oculto; el 12 el sacrificio; el 13 la transformación; el 14 la combinación de fuerzas opuestas."
    )
    val CHEIRO_COMPUESTOS_15_18 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 15 al 18", pdfPageIndex = 81,
        literalQuote = "El 15 posee gran poder de magia. El 16 advierte soberbia. El 17 es la Estrella del Mago. El 18 representa las luchas entre la luz y la sombra."
    )
    val CHEIRO_COMPUESTOS_19_23 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 19 al 23", pdfPageIndex = 82,
        literalQuote = "El 19 es conocido como 'El Príncipe del Cielo' o 'El Sol'. Promete éxito, estima y honores. El 21 es 'La Corona del Mago'."
    )
    val CHEIRO_COMPUESTOS_24_30 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 24 al 30", pdfPageIndex = 83,
        literalQuote = "El 24 promete asistencia de superiores. El 27 es 'La Estrella del Mago'. El 30 es el número de la reflexión solitaria."
    )
    val CHEIRO_COMPUESTOS_31_43 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 31 al 43", pdfPageIndex = 84,
        literalQuote = "El 31 indica alma solitaria. El 36 alta creatividad. El 42 y 43 traen desafíos kármicos que forjan la madurez."
    )
    val CHEIRO_COMPUESTOS_44_52 = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Compuestos 44 al 52", pdfPageIndex = 85,
        literalQuote = "El 44 y superiores representan energías de alta vibración. El 52 cierra el ciclo con la síntesis de todas las enseñanzas anteriores."
    )
    val CHEIRO_APUESTAS = BookCitation(
        bookId = "CHEIRO", expectedFileName = "Cheiro's Book of Numbers_text.pdf",
        bookTitle = "Cheiro's Book of Numbers", author = "Cheiro (W. J. Warner)",
        chapterLabel = "Apuestas y Especulación — Cap. XXX", pdfPageIndex = 152,
        literalQuote = "Para las especulaciones, Cheiro recomienda actuar únicamente en los días y horas regidas por el planeta personal del nacimiento, especialmente Sol o Luna."
    )

    // ── WESTCOTT ─────────────────────────────────────────────────────────────
    val WESTCOTT_PRIMOS_PERFECTOS = BookCitation(
        bookId = "WESTCOTT", expectedFileName = "numberstheiroccu1911west.pdf",
        bookTitle = "Numbers: Their Occult Power and Mystic Virtues", author = "W. W. Westcott",
        chapterLabel = "Números Primos y Perfectos — 6, 28", pdfPageIndex = 21,
        literalQuote = "Un número perfecto es aquel cuya suma de divisores propios lo iguala. El 6 y el 28 son los primeros números perfectos: de especial pureza y equilibrio cósmico."
    )
    val WESTCOTT_CUADRADOS_MAGICOS = BookCitation(
        bookId = "WESTCOTT", expectedFileName = "numberstheiroccu1911west.pdf",
        bookTitle = "Numbers: Their Occult Power and Mystic Virtues", author = "W. W. Westcott",
        chapterLabel = "Cuadrados Mágicos", pdfPageIndex = 33,
        literalQuote = "Los cuadrados mágicos son tablas donde la suma de cada fila, columna y diagonal es idéntica. Los siete planetas clásicos tienen cada uno su cuadrado sagrado."
    )
    val WESTCOTT_HORAS_PLANETARIAS = BookCitation(
        bookId = "WESTCOTT", expectedFileName = "numberstheiroccu1911west.pdf",
        bookTitle = "Numbers: Their Occult Power and Mystic Virtues", author = "W. W. Westcott",
        chapterLabel = "Las Horas Planetarias", pdfPageIndex = 49,
        literalQuote = "Las 24 horas del día están gobernadas por los siete planetas en un orden cíclico. Conocer la hora planetaria permite alinear las acciones con la energía cósmica dominante."
    )
    val WESTCOTT_NUMERO_19 = BookCitation(
        bookId = "WESTCOTT", expectedFileName = "numberstheiroccu1911west.pdf",
        bookTitle = "Numbers: Their Occult Power and Mystic Virtues", author = "W. W. Westcott",
        chapterLabel = "El Número 19 — El Sol", pdfPageIndex = 115,
        literalQuote = "El 19 representa el pleno poder del Sol. Es símbolo de éxito, honor y gloria merecidos. Las tradiciones antiguas lo llaman 'La Estrella del Sol'."
    )

    // ── MCCANTS ──────────────────────────────────────────────────────────────
    val MCCANTS_NATURAL_MATCH = BookCitation(
        bookId = "MCCANTS", expectedFileName = "gyhn.pdf",
        bookTitle = "Glynis Has Your Number", author = "Glynis McCants",
        chapterLabel = "Natural Match — Tríada 1-5-7", pdfPageIndex = 4,
        literalQuote = "Los números 1, 5 y 7 forman una tríada de compatibilidad natural: el liderazgo del 1, la libertad del 5 y la espiritualidad del 7 se complementan armoniosamente."
    )
    val MCCANTS_LUCKY_LOTTO = BookCitation(
        bookId = "MCCANTS", expectedFileName = "gyhn.pdf",
        bookTitle = "Glynis Has Your Number", author = "Glynis McCants",
        chapterLabel = "Índice Lucky Lotto", pdfPageIndex = 6,
        literalQuote = "McCants explica que los números que vibran en resonancia con tu número natal tienen mayor afinidad para atraer circunstancias positivas, incluyendo en juegos de azar."
    )

    val ALL_CITATIONS: List<BookCitation> = listOf(
        MILLMAN_SENDERO_37_10, MILLMAN_19_10, MILLMAN_PENSAMIENTO_MAGICO,
        MILLMAN_ABUNDANCIA_8, MILLMAN_46_10,
        PHILLIPS_CUADRICULA_3X3, PHILLIPS_7_AISLADO, PHILLIPS_FLECHA_FRUSTRACION,
        PHILLIPS_REGENTE_10, PHILLIPS_CICLO_9, PHILLIPS_PIRAMIDES,
        CHEIRO_SERIE_1, CHEIRO_ALFABETO_CALDEO, CHEIRO_COMPUESTOS_10_14,
        CHEIRO_COMPUESTOS_15_18, CHEIRO_COMPUESTOS_19_23, CHEIRO_COMPUESTOS_24_30,
        CHEIRO_COMPUESTOS_31_43, CHEIRO_COMPUESTOS_44_52, CHEIRO_APUESTAS,
        WESTCOTT_PRIMOS_PERFECTOS, WESTCOTT_CUADRADOS_MAGICOS,
        WESTCOTT_HORAS_PLANETARIAS, WESTCOTT_NUMERO_19,
        MCCANTS_NATURAL_MATCH, MCCANTS_LUCKY_LOTTO
    )

    fun getCitationsForBall(ball: Int, missingPhillipsDigits: List<Int> = listOf(4, 5, 6)): List<BookCitation> {
        val result = mutableListOf<BookCitation>()
        when (ball) {
            in 1..9        -> { result += CHEIRO_SERIE_1; result += CHEIRO_ALFABETO_CALDEO }
            in 10..14      -> result += CHEIRO_COMPUESTOS_10_14
            in 15..18      -> result += CHEIRO_COMPUESTOS_15_18
            in 19..23      -> result += CHEIRO_COMPUESTOS_19_23
            in 24..30      -> result += CHEIRO_COMPUESTOS_24_30
            in 31..43      -> result += CHEIRO_COMPUESTOS_31_43
            in 44..53      -> result += CHEIRO_COMPUESTOS_44_52
        }
        if (ball == 19) result += WESTCOTT_NUMERO_19
        if (ball == 6 || ball == 28) result += WESTCOTT_PRIMOS_PERFECTOS

        val root = digitRoot(ball)
        if (ball == 19 || root == 1) result += MILLMAN_19_10
        if (root == 8) result += MILLMAN_ABUNDANCIA_8
        if (root == 3 || root == 7) result += MILLMAN_SENDERO_37_10

        val digits = ball.toString().map { it.digitToInt() }
        if (digits.any { it in missingPhillipsDigits }) result += PHILLIPS_FLECHA_FRUSTRACION
        if (ball == 7 || digits.contains(7)) result += PHILLIPS_7_AISLADO
        if (root in listOf(1, 5, 7)) result += MCCANTS_NATURAL_MATCH

        result += WESTCOTT_HORAS_PLANETARIAS
        result += CHEIRO_APUESTAS
        return result.distinctBy { it.bookId + it.pdfPageIndex }
    }

    fun getBookCovers(): List<BookCitation> = listOf(
        CHEIRO_SERIE_1, MILLMAN_SENDERO_37_10, PHILLIPS_CUADRICULA_3X3,
        WESTCOTT_PRIMOS_PERFECTOS, MCCANTS_NATURAL_MATCH
    )

    private fun digitRoot(n: Int): Int {
        var v = n
        while (v >= 10) v = v.toString().sumOf { it.digitToInt() }
        return v
    }
}
