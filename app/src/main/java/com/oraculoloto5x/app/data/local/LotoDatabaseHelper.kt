package com.oraculoloto5x.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.oraculoloto5x.app.data.models.ChatMessage
import com.oraculoloto5x.app.data.models.DrawResult
import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.data.models.SavedPlay
import com.oraculoloto5x.app.data.models.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LotoDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "oraculo_loto_5x.db"
        private const val DATABASE_VERSION = 3

        // Chat Messages
        const val TABLE_CHAT = "chat_messages"
        const val COL_CHAT_ID = "id"
        const val COL_CHAT_TIMESTAMP = "timestamp"
        const val COL_CHAT_SENDER = "sender"
        const val COL_CHAT_TEXT = "text"
        const val COL_CHAT_BALLS = "related_balls"

        // AI Oracle Cache
        const val TABLE_AI_CACHE = "ai_oracle_cache"

        // Tabla Sorteos Históricos
        const val TABLE_SORTEOS = "sorteos_historicos"
        const val COL_SORTEO_ID = "id"
        const val COL_SORTEO_LOTERIA = "loteria"
        const val COL_SORTEO_FECHA = "fecha"
        const val COL_SORTEO_BOLILLAS = "bolillas"
        const val COL_SORTEO_REGISTRO = "fecha_registro"

        // Tabla Jugadas Guardadas
        const val TABLE_JUGADAS = "jugadas_guardadas"
        const val COL_JUGADA_ID = "id"
        const val COL_JUGADA_LOTERIA = "loteria"
        const val COL_JUGADA_FECHA_GEN = "fecha_generacion"
        const val COL_JUGADA_FECHA_OBJETIVO = "fecha_sorteo_objetivo"
        const val COL_JUGADA_TIPO = "tipo"
        const val COL_JUGADA_BOLILLAS = "bolillas"
        const val COL_JUGADA_SEMAFORO = "semaforo"
        const val COL_JUGADA_HORA_PLANETARIA = "hora_planetaria"
        const val COL_JUGADA_ACIERTOS = "aciertos"
        const val COL_JUGADA_BOLILLAS_ACERTADAS = "bolillas_acertadas"
        const val COL_JUGADA_PREMIO = "premio_estimado"

        // Tabla Perfil Usuario
        const val TABLE_PERFIL = "perfil_usuario"
        const val COL_PERFIL_ID = "id"
        const val COL_PERFIL_NOMBRE_COMPLETO = "nombre_completo"
        const val COL_PERFIL_NOMBRE_USO = "nombre_uso"
        const val COL_PERFIL_FECHA_NACIMIENTO = "fecha_nacimiento"
        const val COL_PERFIL_LATITUD = "latitud"
        const val COL_PERFIL_LONGITUD = "longitud"
        const val COL_PERFIL_ZONA_HORARIA = "zona_horaria"
        const val COL_PERFIL_NOTIFICACIONES = "notificaciones"
        const val COL_PERFIL_LOTERIA_ACTIVA = "loteria_activa"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSorteosTable = """
            CREATE TABLE $TABLE_SORTEOS (
                $COL_SORTEO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SORTEO_LOTERIA TEXT NOT NULL,
                $COL_SORTEO_FECHA TEXT NOT NULL,
                $COL_SORTEO_BOLILLAS TEXT NOT NULL,
                $COL_SORTEO_REGISTRO TEXT NOT NULL
            );
        """.trimIndent()

        val createJugadasTable = """
            CREATE TABLE $TABLE_JUGADAS (
                $COL_JUGADA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_JUGADA_LOTERIA TEXT NOT NULL,
                $COL_JUGADA_FECHA_GEN TEXT NOT NULL,
                $COL_JUGADA_FECHA_OBJETIVO TEXT NOT NULL,
                $COL_JUGADA_TIPO TEXT NOT NULL,
                $COL_JUGADA_BOLILLAS TEXT NOT NULL,
                $COL_JUGADA_SEMAFORO TEXT NOT NULL,
                $COL_JUGADA_HORA_PLANETARIA TEXT NOT NULL,
                $COL_JUGADA_ACIERTOS INTEGER DEFAULT 0,
                $COL_JUGADA_BOLILLAS_ACERTADAS TEXT DEFAULT '',
                $COL_JUGADA_PREMIO TEXT DEFAULT 'Pendiente'
            );
        """.trimIndent()

        val createPerfilTable = """
            CREATE TABLE $TABLE_PERFIL (
                $COL_PERFIL_ID INTEGER PRIMARY KEY,
                $COL_PERFIL_NOMBRE_COMPLETO TEXT NOT NULL,
                $COL_PERFIL_NOMBRE_USO TEXT NOT NULL,
                $COL_PERFIL_FECHA_NACIMIENTO TEXT NOT NULL,
                $COL_PERFIL_LATITUD REAL NOT NULL,
                $COL_PERFIL_LONGITUD REAL NOT NULL,
                $COL_PERFIL_ZONA_HORARIA TEXT NOT NULL,
                $COL_PERFIL_NOTIFICACIONES INTEGER DEFAULT 0,
                $COL_PERFIL_LOTERIA_ACTIVA TEXT DEFAULT 'LA_TINKA'
            );
        """.trimIndent()

        db.execSQL(createSorteosTable)
        db.execSQL(createJugadasTable)
        db.execSQL(createPerfilTable)
        createAiCacheTable(db)
        createChatTable(db)

        // Semilla: Perfil por defecto de Iván Josseph
        val userValues = ContentValues().apply {
            put(COL_PERFIL_ID, 1L)
            put(COL_PERFIL_NOMBRE_COMPLETO, "Ivan Josseph Chuctaya Paucar")
            put(COL_PERFIL_NOMBRE_USO, "Ivan Chuctaya")
            put(COL_PERFIL_FECHA_NACIMIENTO, "1991-07-19")
            put(COL_PERFIL_LATITUD, -13.5319)
            put(COL_PERFIL_LONGITUD, -71.9675)
            put(COL_PERFIL_ZONA_HORARIA, "America/Lima")
            put(COL_PERFIL_NOTIFICACIONES, 0)
            put(COL_PERFIL_LOTERIA_ACTIVA, LotteryType.LA_TINKA.name)
        }
        db.insert(TABLE_PERFIL, null, userValues)

        // Semilla: Últimos 10 sorteos reales de La Tinka
        insertInitialDrawsSeed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe upgrade: preserve existing tables, only add new ones
        if (oldVersion < 2) {
            createAiCacheTable(db)
        }
        if (oldVersion < 3) {
            createChatTable(db)
        }
    }

    private fun createChatTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CHAT (
                $COL_CHAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHAT_TIMESTAMP INTEGER NOT NULL,
                $COL_CHAT_SENDER TEXT NOT NULL,
                $COL_CHAT_TEXT TEXT NOT NULL,
                $COL_CHAT_BALLS TEXT NOT NULL
            );
        """.trimIndent())
    }

    private fun createAiCacheTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_AI_CACHE (
                cache_key TEXT PRIMARY KEY,
                response_text TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun insertInitialDrawsSeed(db: SQLiteDatabase) {
        val seedDraws = listOf(
            Triple(LotteryType.LA_TINKA.name, "2026-11-15", listOf(4, 12, 19, 23, 37, 45)),
            Triple(LotteryType.LA_TINKA.name, "2026-11-11", listOf(7, 14, 21, 28, 33, 50)),
            Triple(LotteryType.LA_TINKA.name, "2026-11-08", listOf(2, 10, 15, 27, 36, 42)),
            Triple(LotteryType.LA_TINKA.name, "2026-11-04", listOf(1, 17, 24, 32, 41, 51)),
            Triple(LotteryType.LA_TINKA.name, "2026-11-01", listOf(5, 19, 23, 30, 37, 46)),
            Triple(LotteryType.LA_TINKA.name, "2026-10-28", listOf(3, 11, 18, 25, 34, 48)),
            Triple(LotteryType.LA_TINKA.name, "2026-10-25", listOf(8, 16, 22, 29, 39, 47)),
            Triple(LotteryType.LA_TINKA.name, "2026-10-21", listOf(6, 14, 20, 31, 40, 52)),
            Triple(LotteryType.LA_TINKA.name, "2026-10-18", listOf(9, 13, 26, 35, 43, 49)),
            Triple(LotteryType.LA_TINKA.name, "2026-10-14", listOf(10, 17, 21, 33, 42, 50))
        )

        for (draw in seedDraws) {
            val values = ContentValues().apply {
                put(COL_SORTEO_LOTERIA, draw.first)
                put(COL_SORTEO_FECHA, draw.second)
                put(COL_SORTEO_BOLILLAS, draw.third.joinToString(","))
                put(COL_SORTEO_REGISTRO, "2026-11-18 20:00:00")
            }
            db.insert(TABLE_SORTEOS, null, values)
        }
    }

    // --- OPERACIONES PERFIL ---
    fun getUserProfile(): UserProfile {
        val db = readableDatabase
        val cursor = db.query(TABLE_PERFIL, null, "$COL_PERFIL_ID = ?", arrayOf("1"), null, null, null)
        var profile = UserProfile()
        if (cursor.moveToFirst()) {
            val fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERFIL_NOMBRE_COMPLETO))
            val commonName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERFIL_NOMBRE_USO))
            val birthDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERFIL_FECHA_NACIMIENTO))
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PERFIL_LATITUD))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PERFIL_LONGITUD))
            val tz = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERFIL_ZONA_HORARIA))
            val notif = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PERFIL_NOTIFICACIONES)) == 1

            profile = UserProfile(
                fullName = fullName,
                commonName = commonName,
                birthDate = LocalDate.parse(birthDateStr),
                latitude = lat,
                longitude = lon,
                timeZone = tz,
                notificationsEnabled = notif
            )
        }
        cursor.close()
        return profile
    }

    fun updateUserProfile(profile: UserProfile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PERFIL_NOMBRE_COMPLETO, profile.fullName)
            put(COL_PERFIL_NOMBRE_USO, profile.commonName)
            put(COL_PERFIL_FECHA_NACIMIENTO, profile.birthDate.toString())
            put(COL_PERFIL_LATITUD, profile.latitude)
            put(COL_PERFIL_LONGITUD, profile.longitude)
            put(COL_PERFIL_ZONA_HORARIA, profile.timeZone)
            put(COL_PERFIL_NOTIFICACIONES, if (profile.notificationsEnabled) 1 else 0)
        }
        db.update(TABLE_PERFIL, values, "$COL_PERFIL_ID = ?", arrayOf("1"))
    }

    // --- OPERACIONES SORTEOS HISTÓRICOS ---
    fun insertDrawResult(lottery: LotteryType, date: LocalDate, balls: List<Int>): Long {
        val db = writableDatabase
        val sortedBalls = balls.sorted()
        val values = ContentValues().apply {
            put(COL_SORTEO_LOTERIA, lottery.name)
            put(COL_SORTEO_FECHA, date.toString())
            put(COL_SORTEO_BOLILLAS, sortedBalls.joinToString(","))
            put(COL_SORTEO_REGISTRO, LocalDate.now().toString())
        }
        val id = db.insert(TABLE_SORTEOS, null, values)

        // Ejecutar Backtest inmediato sobre jugadas existentes
        runBacktestForDraw(lottery, date, sortedBalls)

        return id
    }

    fun getDrawHistory(lottery: LotteryType): List<DrawResult> {
        val db = readableDatabase
        val list = mutableListOf<DrawResult>()
        val cursor = db.query(
            TABLE_SORTEOS,
            null,
            "$COL_SORTEO_LOTERIA = ?",
            arrayOf(lottery.name),
            null,
            null,
            "$COL_SORTEO_FECHA DESC"
        )
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SORTEO_ID))
            val dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SORTEO_FECHA))
            val ballsStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SORTEO_BOLILLAS))
            val regStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SORTEO_REGISTRO))
            val balls = ballsStr.split(",").mapNotNull { it.trim().toIntOrNull() }

            list.add(
                DrawResult(
                    id = id,
                    lotteryType = lottery,
                    drawDate = LocalDate.parse(dateStr),
                    winningBalls = balls,
                    recordedAt = regStr
                )
            )
        }
        cursor.close()
        return list
    }

    fun getHistoricalFrequencies(lottery: LotteryType): Map<Int, Int> {
        val frequencies = mutableMapOf<Int, Int>()
        val draws = getDrawHistory(lottery)
        for (draw in draws) {
            for (ball in draw.winningBalls) {
                frequencies[ball] = (frequencies[ball] ?: 0) + 1
            }
        }
        return frequencies
    }

    // --- OPERACIONES JUGADAS GUARDADAS (BACKTEST) ---
    fun savePlay(play: SavedPlay): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_JUGADA_LOTERIA, play.lotteryType.name)
            put(COL_JUGADA_FECHA_GEN, play.generatedDate.toString())
            put(COL_JUGADA_FECHA_OBJETIVO, play.targetDrawDate.toString())
            put(COL_JUGADA_TIPO, play.playType)
            put(COL_JUGADA_BOLILLAS, play.balls.sorted().joinToString(","))
            put(COL_JUGADA_SEMAFORO, play.riskLevel)
            put(COL_JUGADA_HORA_PLANETARIA, play.planetaryHour)
            put(COL_JUGADA_ACIERTOS, play.matchedCount)
            put(COL_JUGADA_BOLILLAS_ACERTADAS, play.matchedBalls.joinToString(","))
            put(COL_JUGADA_PREMIO, play.estimatedPrize)
        }
        val id = db.insert(TABLE_JUGADAS, null, values)

        // Verificar si ya existe sorteo para esa fecha
        checkSinglePlayBacktest(id, play.lotteryType, play.targetDrawDate, play.balls)

        return id
    }

    fun getSavedPlays(lottery: LotteryType? = null): List<SavedPlay> {
        val db = readableDatabase
        val list = mutableListOf<SavedPlay>()
        val selection = if (lottery != null) "$COL_JUGADA_LOTERIA = ?" else null
        val selectionArgs = if (lottery != null) arrayOf(lottery.name) else null

        val cursor = db.query(
            TABLE_JUGADAS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_JUGADA_FECHA_GEN DESC, $COL_JUGADA_ID DESC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_JUGADA_ID))
            val lotName = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_LOTERIA))
            val genDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_FECHA_GEN))
            val targetDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_FECHA_OBJETIVO))
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_TIPO))
            val ballsStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_BOLILLAS))
            val semaforo = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_SEMAFORO))
            val hora = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_HORA_PLANETARIA))
            val aciertos = cursor.getInt(cursor.getColumnIndexOrThrow(COL_JUGADA_ACIERTOS))
            val acertadasStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_BOLILLAS_ACERTADAS))
            val premio = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_PREMIO))

            val balls = ballsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            val acertadas = if (acertadasStr.isNotBlank()) {
                acertadasStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            } else emptyList()

            list.add(
                SavedPlay(
                    id = id,
                    lotteryType = LotteryType.fromName(lotName),
                    generatedDate = LocalDate.parse(genDateStr),
                    targetDrawDate = LocalDate.parse(targetDateStr),
                    playType = tipo,
                    balls = balls,
                    riskLevel = semaforo,
                    planetaryHour = hora,
                    matchedCount = aciertos,
                    matchedBalls = acertadas,
                    estimatedPrize = premio
                )
            )
        }
        cursor.close()
        return list
    }

    private fun runBacktestForDraw(lottery: LotteryType, drawDate: LocalDate, winningBalls: List<Int>) {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_JUGADAS,
            arrayOf(COL_JUGADA_ID, COL_JUGADA_BOLILLAS, COL_JUGADA_TIPO),
            "$COL_JUGADA_LOTERIA = ? AND $COL_JUGADA_FECHA_OBJETIVO = ?",
            arrayOf(lottery.name, drawDate.toString()),
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val playId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_JUGADA_ID))
            val ballsStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_JUGADA_BOLILLAS))
            val playBalls = ballsStr.split(",").mapNotNull { it.trim().toIntOrNull() }

            val matches = playBalls.filter { it in winningBalls }
            val count = matches.size
            val prize = estimatePrize(lottery, count)

            val updateValues = ContentValues().apply {
                put(COL_JUGADA_ACIERTOS, count)
                put(COL_JUGADA_BOLILLAS_ACERTADAS, matches.joinToString(","))
                put(COL_JUGADA_PREMIO, prize)
            }
            db.update(TABLE_JUGADAS, updateValues, "$COL_JUGADA_ID = ?", arrayOf(playId.toString()))
        }
        cursor.close()
    }

    private fun checkSinglePlayBacktest(playId: Long, lottery: LotteryType, targetDate: LocalDate, playBalls: List<Int>) {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_SORTEOS,
            arrayOf(COL_SORTEO_BOLILLAS),
            "$COL_SORTEO_LOTERIA = ? AND $COL_SORTEO_FECHA = ?",
            arrayOf(lottery.name, targetDate.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val ballsStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SORTEO_BOLILLAS))
            val winning = ballsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            val matches = playBalls.filter { it in winning }
            val count = matches.size
            val prize = estimatePrize(lottery, count)

            val updateValues = ContentValues().apply {
                put(COL_JUGADA_ACIERTOS, count)
                put(COL_JUGADA_BOLILLAS_ACERTADAS, matches.joinToString(","))
                put(COL_JUGADA_PREMIO, prize)
            }
            db.update(TABLE_JUGADAS, updateValues, "$COL_JUGADA_ID = ?", arrayOf(playId.toString()))
        }
        cursor.close()
    }

    private fun estimatePrize(lottery: LotteryType, matches: Int): String {
        return when (lottery) {
            LotteryType.LA_TINKA -> when (matches) {
                6 -> "¡POZO MILLONARIO (6 Aciertos)!"
                5 -> "S/ 50,000 (Gran Acierto 5)"
                4 -> "S/ 500 (4 Aciertos)"
                3 -> "S/ 20 (3 Aciertos)"
                2 -> "Jugada Gratis (2 Aciertos)"
                else -> "Sin premio ($matches aciertos)"
            }
            LotteryType.KABALA -> when (matches) {
                6 -> "¡POZO BUENAZO (6 Aciertos)!"
                5 -> "S/ 5,000 (5 Aciertos)"
                4 -> "S/ 100 (4 Aciertos)"
                3 -> "S/ 5 (3 Aciertos)"
                else -> "Sin premio ($matches aciertos)"
            }
            LotteryType.GANA_DIARIO -> when (matches) {
                5 -> "¡Premio Mayor S/ 100,000!"
                4 -> "S/ 500 (4 Aciertos)"
                3 -> "S/ 25 (3 Aciertos)"
                2 -> "Jugada Gratis (2 Aciertos)"
                else -> "Sin premio ($matches aciertos)"
            }
        }
    }

    // ── Gemini AI Cache ───────────────────────────────────────────────────────

    fun getGeminiCache(cacheKey: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_AI_CACHE, arrayOf("response_text", "timestamp"),
            "cache_key = ?", arrayOf(cacheKey),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            cursor.getString(0).also { cursor.close() }
        } else {
            cursor.close(); null
        }
    }

    fun saveGeminiCache(cacheKey: String, responseText: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("cache_key", cacheKey)
            put("response_text", responseText)
            put("timestamp", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_AI_CACHE, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ── Chat Messages Persistence ─────────────────────────────────────────────

    fun saveChatMessage(message: ChatMessage): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CHAT_TIMESTAMP, message.timestamp)
            put(COL_CHAT_SENDER, message.sender)
            put(COL_CHAT_TEXT, message.text)
            put(COL_CHAT_BALLS, message.relatedBalls)
        }
        return db.insert(TABLE_CHAT, null, values)
    }

    fun getChatHistory(limit: Int = 50): List<ChatMessage> {
        val db = readableDatabase
        val list = mutableListOf<ChatMessage>()
        val cursor = db.query(
            TABLE_CHAT,
            null,
            null,
            null,
            null,
            null,
            "$COL_CHAT_TIMESTAMP ASC",
            limit.toString()
        )
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHAT_ID))
            val ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHAT_TIMESTAMP))
            val sender = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_SENDER))
            val text = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TEXT))
            val balls = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_BALLS))
            list.add(ChatMessage(id = id, timestamp = ts, sender = sender, text = text, relatedBalls = balls))
        }
        cursor.close()
        return list
    }

    fun clearChatHistory() {
        val db = writableDatabase
        db.delete(TABLE_CHAT, null, null)
    }
}
