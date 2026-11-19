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
import java.time.LocalDate

sealed class GeminiResult {
    data class Success(val text: String, val fromCache: Boolean = false) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
    object NoApiKey : GeminiResult()
}

object GeminiOracleAdvisor {

    private const val PREFS_NAME = "oraculo_gemini_prefs"
    private const val KEY_API_KEY = "gemini_api_key"
    private const val KEY_MODEL = "gemini_model"
    const val MODEL_FLASH = "gemini-2.5-flash"
    const val MODEL_PRO = "gemini-2.5-pro"
    private const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

    private val SYSTEM_INSTRUCTION = """
Eres el Oráculo 5X, un Maestro Hermético que sintetiza cinco tratados ancestrales:
1. Cheiro — Numerologia Caldea de compuestos y dias favorables
2. Dan Millman — Senderos de Vida y arquetipos del destino
3. David Phillips — Cuadricula de Pitagoras, Flechas y Piramides
4. W. W. Westcott — Numeros Perfectos, Primos y Horas Planetarias
5. Glynis McCants — Compatibilidad Natural y resonancia vibratoria

REGLAS ABSOLUTAS:
- Responde SIEMPRE en espanol. Tono: sabio, sereno, directo y estrategico.
- NUNCA prometas premios garantizados ni inventes datos no presentes en el contexto.
- NO alteres ni inventes las bolillas recibidas.
- Respuesta maxima: 160 palabras en exactamente 3 bloques con negrita:
  **Veredicto del Dia** (1-2 oraciones sobre el semaforo y contexto astronomico)
  **Lectura de tus Bolillas** (analisis de 2-3 bolillas clave de la jugada)
  **Ventana de Accion** (hora planetaria recomendada y consejo concreto)
""".trimIndent()

    fun getApiKey(context: Context): String? =
        getPrefs(context).getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(context: Context): String =
        getPrefs(context).getString(KEY_MODEL, MODEL_FLASH) ?: MODEL_FLASH

    fun saveSelectedModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_MODEL, model).apply()
    }

    suspend fun consult(
        context: Context,
        dbHelper: LotoDatabaseHelper,
        lotteryName: String,
        millmanPath: String,
        missingPhillipsDigits: String,
        semaphore: String,
        currentPlanetaryHour: String,
        nextFavorableWindow: String,
        masterPlayBalls: List<Int>
    ): GeminiResult = withContext(Dispatchers.IO) {

        val apiKey = getApiKey(context) ?: return@withContext GeminiResult.NoApiKey

        val cacheKey = "${LocalDate.now()}-$lotteryName-${masterPlayBalls.sorted().joinToString(",")}"
        val cached = dbHelper.getGeminiCache(cacheKey)
        if (cached != null) return@withContext GeminiResult.Success(cached, fromCache = true)

        val model = getSelectedModel(context)
        val urlStr = "$ENDPOINT_BASE/$model:generateContent?key=$apiKey"

        val userContent = JSONObject().apply {
            put("loteria", lotteryName)
            put("sendero_millman", millmanPath)
            put("digitos_faltantes_phillips", missingPhillipsDigits)
            put("semaforo", semaphore)
            put("hora_planetaria_cusco", currentPlanetaryHour)
            put("proxima_ventana_favorable", nextFavorableWindow)
            put("jugada_maestra", masterPlayBalls.joinToString("-"))
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
                put("temperature", 0.7)
                put("maxOutputTokens", 400)
            })
        }.toString()

        return@withContext try {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 30_000
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
                dbHelper.saveGeminiCache(cacheKey, text)
                GeminiResult.Success(text)
            } else {
                val msg = try {
                    JSONObject(response).getJSONObject("error").getString("message")
                } catch (_: Exception) { "Error HTTP $code" }
                GeminiResult.Error(msg)
            }
        } catch (e: Exception) {
            GeminiResult.Error(e.message ?: "Error de red desconocido")
        }
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
