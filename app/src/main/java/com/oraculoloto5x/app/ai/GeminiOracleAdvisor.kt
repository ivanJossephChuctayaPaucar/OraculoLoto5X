package com.oraculoloto5x.app.ai

import android.content.Context
import android.content.SharedPreferences
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import com.oraculoloto5x.app.data.models.ChatMessage
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

    // Default API Key encoded in Base64
    private const val DEFAULT_KEY_B64 = "QVEuQWI4Uk42SjBla0RZNTR4Y3RRRW9YUDBac1NVcHZsTTNST04zMVIwTmg5dzVpMzdaU3c="

    // Models supported by Google AI Studio
    // Prioridad: gemini-2.5-flash (respaldo gemini-3.8-flash, gemini-flash-latest, gemini-2.5-pro)
    const val MODEL_FLASH_25 = "gemini-2.5-flash"
    const val MODEL_PRO_25 = "gemini-2.5-pro"
    const val MODEL_FLASH_38 = "gemini-3.8-flash"
    const val MODEL_FLASH_LATEST = "gemini-flash-latest"

    val FALLBACK_MODELS = listOf(MODEL_FLASH_25, MODEL_FLASH_38, MODEL_FLASH_LATEST, MODEL_PRO_25)

    private const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

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
        getPrefs(context).getString(KEY_MODEL, MODEL_FLASH_25) ?: MODEL_FLASH_25

    fun saveSelectedModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_MODEL, model).apply()
    }

    /** Prueba rápida de API Key con un payload mínimo (10 tokens) */
    suspend fun testApiKey(apiKey: String, model: String = MODEL_FLASH_25): Result<String> = withContext(Dispatchers.IO) {
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
                return@withContext Result.failure(Exception("Sin conexión a internet. Activa datos o Wi-Fi."))
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext Result.failure(Exception("Tiempo de espera agotado. Inténtalo de nuevo."))
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        return@withContext Result.failure(Exception("Modelos no disponibles (404)"))
    }

    /**
     * Chat Multi-Turno con memoria de contexto hermético RAG y ventana deslizante
     * de los últimos 6 mensajes para control estricto de tokens y megabytes.
     */
    suspend fun chatTurn(
        context: Context,
        systemInstruction: String,
        history: List<ChatMessage>,
        newQuestion: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) return@withContext GeminiResult.NoApiKey

        val primaryModel = getSelectedModel(context)
        val candidateModels = listOf(primaryModel) + FALLBACK_MODELS.filter { it != primaryModel }

        // Ventana deslizante de los últimos 6 mensajes
        val recentHistory = history.takeLast(6)
        val contentsArray = JSONArray()

        for (msg in recentHistory) {
            val role = if (msg.isFromUser) "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
            })
        }

        // Agregar la nueva pregunta actual del usuario
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", newQuestion.trim())))
        })

        val requestBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.6)
                put("maxOutputTokens", 450)
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

        return@withContext GeminiResult.Error("No se pudo conectar con el servicio de Gemini (404)")
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
