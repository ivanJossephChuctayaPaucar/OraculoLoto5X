package com.oraculoloto5x.app.ai

import android.content.Context
import android.content.SharedPreferences
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

    // Models supported by Google AI Studio
    const val MODEL_FLASH_35 = "gemini-3.5-flash"
    const val MODEL_FLASH_20 = "gemini-2.0-flash"
    const val MODEL_FLASH_15 = "gemini-1.5-flash"
    const val MODEL_FLASH_LATEST = "gemini-flash-latest"

    // Alias de compatibilidad
    const val MODEL_FLASH_25 = MODEL_FLASH_LATEST
    const val MODEL_FLASH_38 = MODEL_FLASH_35

    val FALLBACK_MODELS = listOf(MODEL_FLASH_35, MODEL_FLASH_LATEST, MODEL_FLASH_20, MODEL_FLASH_15)

    private const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

    fun getApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() } ?: ""
    }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(context: Context): String {
        val model = getPrefs(context).getString(KEY_MODEL, MODEL_FLASH_35) ?: MODEL_FLASH_35
        return when (model) {
            MODEL_FLASH_35, "gemini-3.5-flash" -> MODEL_FLASH_35
            MODEL_FLASH_20, "gemini-2.0-flash" -> MODEL_FLASH_20
            MODEL_FLASH_15, "gemini-1.5-flash" -> MODEL_FLASH_15
            MODEL_FLASH_LATEST, "gemini-flash-latest" -> MODEL_FLASH_LATEST
            else -> MODEL_FLASH_35
        }
    }

    fun saveSelectedModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_MODEL, model).apply()
    }

    /** Prueba rápida de API Key con un payload mínimo */
    suspend fun testApiKey(apiKey: String, model: String = MODEL_FLASH_35): Result<String> = withContext(Dispatchers.IO) {
        val testModel = when (model) {
            MODEL_FLASH_20 -> MODEL_FLASH_20
            MODEL_FLASH_15 -> MODEL_FLASH_15
            MODEL_FLASH_LATEST -> MODEL_FLASH_LATEST
            else -> MODEL_FLASH_35
        }
        val candidateModels = listOf(testModel, MODEL_FLASH_35, MODEL_FLASH_LATEST, MODEL_FLASH_20, MODEL_FLASH_15).distinct()

        for (i in candidateModels.indices) {
            val m = candidateModels[i]
            val isFallback = (i > 0)
            val urlStr = "$ENDPOINT_BASE/$m:generateContent?key=$apiKey"
            val generationConfig = JSONObject().apply {
                put("maxOutputTokens", 15)
            }
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", "ping")))
                }))
                put("generationConfig", generationConfig)
            }.toString()

            var conn: HttpURLConnection? = null
            try {
                conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 20_000
                    readTimeout = 60_000
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code == 200) conn.inputStream else (conn.errorStream ?: conn.inputStream)
                val resp = BufferedReader(InputStreamReader(stream, "UTF-8")).readText()

                if (code == 200) {
                    return@withContext Result.success("Conexión exitosa con $m")
                } else if (code == 404 || code == 429) {
                    if (!isFallback && i + 1 < candidateModels.size) {
                        continue
                    }
                    if (code == 429) {
                        return@withContext Result.failure(Exception("Cuota gratuita temporalmente alcanzada (HTTP 429). Espera 1 minuto."))
                    } else {
                        return@withContext Result.failure(Exception("Modelo no disponible (HTTP 404)."))
                    }
                } else if (code == 402) {
                    return@withContext Result.failure(Exception("Créditos agotados (HTTP 402). Genera una API Key gratuita en AI Studio (elige 'Create in new project')."))
                } else if (code == 400 || code == 403) {
                    val errorMessage = try {
                        JSONObject(resp).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Clave no autorizada o restringida" }
                    return@withContext Result.failure(Exception("API Key inválida o restringida (HTTP $code): $errorMessage"))
                } else {
                    val errorMessage = try {
                        JSONObject(resp).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Error HTTP $code" }
                    return@withContext Result.failure(Exception("Error HTTP $code: $errorMessage"))
                }
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext Result.failure(Exception("Tiempo de espera agotado (45s). Revisa tu señal móvil o prueba nuevamente."))
            } catch (e: UnknownHostException) {
                return@withContext Result.failure(Exception("Sin conexión a internet o DNS bloqueado."))
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("Unable to resolve host", ignoreCase = true) ||
                    msg.contains("Network is unreachable", ignoreCase = true) ||
                    msg.contains("Failed to connect", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Sin conexión a internet o DNS bloqueado."))
                }
                return@withContext Result.failure(Exception("Error de conexión: $msg"))
            } finally {
                conn?.disconnect()
            }
        }
        return@withContext Result.failure(Exception("No se pudo verificar la API Key."))
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
        val candidateModels = listOf(primaryModel, MODEL_FLASH_35, MODEL_FLASH_LATEST, MODEL_FLASH_20, MODEL_FLASH_15).distinct()

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

        for (i in candidateModels.indices) {
            val model = candidateModels[i]
            val isFallback = (i > 0)
            val urlStr = "$ENDPOINT_BASE/$model:generateContent?key=$apiKey"

            val generationConfig = JSONObject().apply {
                put("temperature", 0.6)
                put("maxOutputTokens", 4096)
            }

            val requestBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", contentsArray)
                put("generationConfig", generationConfig)
            }.toString()

            var conn: HttpURLConnection? = null
            try {
                conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 20_000
                    readTimeout = 60_000
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(requestBody) }

                val code = conn.responseCode
                val stream = if (code == 200) conn.inputStream else (conn.errorStream ?: conn.inputStream)
                val response = BufferedReader(InputStreamReader(stream, "UTF-8")).readText()

                if (code == 200) {
                    val text = JSONObject(response)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim()

                    return@withContext GeminiResult.Success(text)
                } else if (code == 404 || code == 429) {
                    if (!isFallback && i + 1 < candidateModels.size) {
                        continue
                    }
                    if (code == 429) {
                        return@withContext GeminiResult.Error("Cuota gratuita temporalmente alcanzada (HTTP 429). Espera 1 minuto.")
                    } else {
                        return@withContext GeminiResult.Error("Modelo no disponible (HTTP 404).")
                    }
                } else if (code == 402) {
                    return@withContext GeminiResult.Error("Créditos agotados (HTTP 402). Genera una API Key gratuita en AI Studio (elige 'Create in new project').")
                } else if (code == 400 || code == 403) {
                    val errorMessage = try {
                        JSONObject(response).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Clave no autorizada o parámetros inválidos" }
                    return@withContext GeminiResult.Error("API Key inválida o restringida (HTTP $code): $errorMessage")
                } else {
                    val errorMessage = try {
                        JSONObject(response).getJSONObject("error").getString("message")
                    } catch (_: Exception) { "Error de servidor" }
                    return@withContext GeminiResult.Error("Error HTTP $code: $errorMessage")
                }
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext GeminiResult.Error("Tiempo de espera agotado (45s). Revisa tu señal móvil o prueba nuevamente.")
            } catch (e: UnknownHostException) {
                return@withContext GeminiResult.Error("Sin conexión a internet o DNS bloqueado.")
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("Unable to resolve host", ignoreCase = true) ||
                    msg.contains("Network is unreachable", ignoreCase = true) ||
                    msg.contains("Failed to connect", ignoreCase = true)) {
                    return@withContext GeminiResult.Error("Sin conexión a internet o DNS bloqueado.")
                }
                return@withContext GeminiResult.Error("Error de conexión: $msg")
            } finally {
                conn?.disconnect()
            }
        }

        return@withContext GeminiResult.Error("No se pudo obtener respuesta del Oráculo.")
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
