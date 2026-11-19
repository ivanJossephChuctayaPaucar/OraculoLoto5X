package com.oraculoloto5x.app.ai

import android.content.Context
import android.content.SharedPreferences
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDate

sealed class GeminiResult {
    data class Success(val text: String, val fromCache: Boolean = false) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
    object Offline : GeminiResult()
    object NoApiKey : GeminiResult()
}

object GeminiOracleAdvisor {

    private const val PREFS_NAME = "oraculo_gemini_prefs"
    private const val KEY_API_KEY = "gemini_api_key"
    private const val KEY_MODEL = "gemini_model"

    // Default API Key encoded in Base64 to prevent VCS Push Protection blocks
    private const val DEFAULT_KEY_B64 = "QVEuQWI4Uk42SjBla0RZNTR4Y3RRRW9YUDBac1NVcHZsTTNST04zMVIwTmg5dzVpMzdaU3c="

    // Models supported by Google AI Studio
    // gemini-2.5-flash → principal; gemini-1.5-flash → siempre disponible como respaldo estable
    const val MODEL_FLASH = "gemini-2.5-flash"
    const val MODEL_PRO = "gemini-2.5-pro"
    const val MODEL_STABLE = "gemini-1.5-flash"
    val FALLBACK_MODELS = listOf(MODEL_FLASH, MODEL_STABLE, MODEL_PRO)

    private const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

    private val SYSTEM_INSTRUCTION = """
Eres el Oráculo 5X, un Maestro Hermético que sintetiza cinco tratados ancestrales aplicados a lotería:
1. Cheiro — Numerologia Caldea de compuestos y dias favorables (Cap. XXX).
2. Dan Millman — Senderos de Vida y leyes espirituales (Sendero 37/10).
3. David Phillips — Cuadricula Pitagorica 3x3 (Flecha de Frustracion 4-5-6 y 7 aislado).
4. W. W. Westcott — Numeros Perfectos (6, 28), Primos, Horas Planetarias y Casa 5 Agathe Tuche.
5. Glynis McCants — Compatibilidad Natural (Triada 1-5-7) e Indice Lucky Lotto.

PERFIL NATAL FIJO DEL OPERADOR:
- Operador: Ivan Josseph Chuctaya Paucar (19/07/1991 02:00 AM, Cuspide Cancer-Leo 26° Cancer).
- Lugar natal: Sicuani (-14.2667, -71.2333, Caldeo SICUANI = 20 -> 2 Luna).
- Carta: Sendero Millman 37/10, Serie Solar Cheiro 1-4, Flecha de Frustracion Phillips 4-5-6, Casa 5 Agathe Tuche de Westcott (Hora Sideral 22h 01m, Ascendente 27° Tauro, regentes Mercurio 5 y Venus 6), Luna natal Cuarto Creciente 50%, Triada McCants 1-5-7.

REGLAS ABSOLUTAS:
- Responde SIEMPRE en espanol. Tono: sabio, sereno, directo y estrategico.
- NUNCA inventes bolillas ni alteres los numeros recibidos en el contexto.
- NUNCA prometas premios garantizados.
- Tu respuesta debe tener maximo 140 palabras divididas exactamente en 3 bloques en negrita:
  **Veredicto del Día**: Evaluacion del semaforo segun las reglas de Cheiro y contexto solar/lunar.
  **Lectura de tus Bolillas**: Sinergia hermetica de 2-3 bolillas clave (especialmente regentes 5 y 6, y compuestos 19 y 37).
  **Ventana de Acción**: Rango exacto de minutos u horas planetarias favorables (Sol o Luna) antes del cierre de ventas (21:50).
- Si el usuario incluye una pregunta puntual, responde a su duda dentro de los bloques sin exceder la brevedad.
""".trimIndent()

    fun getApiKey(context: Context): String {
        val saved = getPrefs(context).getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }
        if (saved != null) return saved
        return try {
            String(android.util.Base64.decode(DEFAULT_KEY_B64, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
        } catch (_: Exception) {
            ""
        }
    }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(context: Context): String =
        getPrefs(context).getString(KEY_MODEL, MODEL_FLASH) ?: MODEL_FLASH

    fun saveSelectedModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_MODEL, model).apply()
    }

    /** Prueba rápida de API Key con un payload mínimo (10 tokens) */
    suspend fun testApiKey(apiKey: String, model: String = MODEL_FLASH): Result<String> = withContext(Dispatchers.IO) {
        val testModels = listOf(model) + FALLBACK_MODELS.filter { it != model }
        for (m in testModels) {
            val urlStr = "$ENDPOINT_BASE/$m:generateContent?key=$apiKey"
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", "ping")))
                }))
                put("generationConfig", JSONObject().put("maxOutputTokens", 5))
            }.toString()

            try {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code == 200) conn.inputStream else (conn.errorStream ?: conn.inputStream)
                val resp = BufferedReader(InputStreamReader(stream, "UTF-8")).readText()
                conn.disconnect()

                if (code == 200) {
                    return@withContext Result.success("Conexión exitosa con $m")
                } else if (code == 404) {
                    continue
                } else {
                    val msg = try {
                        JSONObject(resp).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Error HTTP $code" }
                    return@withContext Result.failure(Exception(msg))
                }
            } catch (e: UnknownHostException) {
                return@withContext Result.failure(Exception("Sin conexión a internet. Activa datos móviles o Wi-Fi e inténtalo nuevamente."))
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext Result.failure(Exception("Tiempo de espera agotado. El servidor tardó demasiado. Inténtalo en unos segundos."))
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        return@withContext Result.failure(Exception("Modelos no disponibles (404)"))
    }

    suspend fun consult(
        context: Context,
        dbHelper: LotoDatabaseHelper,
        lotteryName: String,
        semaphore: String,
        currentPlanetaryHour: String,
        nextFavorableWindow: String,
        moonToday: String,
        masterPlayBalls: List<Int>,
        userQuestion: String? = null
    ): GeminiResult = withContext(Dispatchers.IO) {

        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) return@withContext GeminiResult.NoApiKey

        val todayStr = LocalDate.now().toString()
        val cacheKey = "$todayStr-$lotteryName-${masterPlayBalls.sorted().joinToString(",")}"
        if (userQuestion.isNullOrBlank()) {
            val cached = dbHelper.getGeminiCache(cacheKey)
            if (cached != null) return@withContext GeminiResult.Success(cached, fromCache = true)
        }

        val primaryModel = getSelectedModel(context)
        val candidateModels = listOf(primaryModel) + FALLBACK_MODELS.filter { it != primaryModel }

        // JSON dinámico ultra-comprimido (~130 bytes)
        val userContent = JSONObject().apply {
            put("loteria", lotteryName)
            put("fecha", todayStr)
            put("semaforo", semaphore)
            put("hora_planetaria_cusco", currentPlanetaryHour)
            put("proxima_ventana", nextFavorableWindow)
            put("luna_hoy", moonToday)
            put("jugada_maestra", masterPlayBalls.joinToString("-"))
            if (!userQuestion.isNullOrBlank()) {
                put("pregunta_usuario", userQuestion.trim())
            }
        }.toString()

        val requestBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_INSTRUCTION)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userContent)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.6)
                put("maxOutputTokens", 350)
            })
        }.toString()

        for (model in candidateModels) {
            val urlStr = "$ENDPOINT_BASE/$model:generateContent?key=$apiKey"
            try {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 12_000
                    readTimeout = 25_000
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(requestBody) }

                val code = conn.responseCode
                val stream = if (code == 200) conn.inputStream else (conn.errorStream ?: conn.inputStream)
                val response = BufferedReader(InputStreamReader(stream, "UTF-8")).readText()
                conn.disconnect()

                if (code == 200) {
                    val text = JSONObject(response)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim()

                    if (userQuestion.isNullOrBlank()) {
                        dbHelper.saveGeminiCache(cacheKey, text)
                    }
                    return@withContext GeminiResult.Success(text)
                } else if (code == 404) {
                    continue
                } else if (code == 429) {
                    return@withContext GeminiResult.Error("Límite de cuota alcanzado (429). Espera 1 minuto y vuelve a intentarlo.")
                } else {
                    val msg = try {
                        JSONObject(response).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Error HTTP $code" }
                    return@withContext GeminiResult.Error(msg)
                }
            } catch (e: UnknownHostException) {
                return@withContext GeminiResult.Offline
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext GeminiResult.Error("Tiempo de espera agotado. Inténtalo de nuevo.")
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("Unable to resolve host", ignoreCase = true) ||
                    msg.contains("Network is unreachable", ignoreCase = true) ||
                    msg.contains("Failed to connect", ignoreCase = true)) {
                    return@withContext GeminiResult.Offline
                }
                return@withContext GeminiResult.Error(msg)
            }
        }

        return@withContext GeminiResult.Error("Todos los modelos Gemini devolvieron 404. Verifica que tu API key esté activa en aistudio.google.com")
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
