package com.oraculoloto5x.app.data.sync

import android.content.Context
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import com.oraculoloto5x.app.data.models.LotteryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

sealed class SyncResult {
    data class Success(val newDrawsCount: Int, val latestDate: String) : SyncResult()
    object AlreadyUpToDate : SyncResult()
    data class Error(val message: String) : SyncResult()
    object Offline : SyncResult()
}

object TinkaOnlineSyncService {

    private const val SYNC_URL =
        "https://raw.githubusercontent.com/ivanJossephChuctayaPaucar/OraculoLoto5X/main/data/tinka_history.json"

    suspend fun syncTinkaHistory(
        context: Context,
        dbHelper: LotoDatabaseHelper
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(SYNC_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("Accept", "application/json")
                useCaches = false
            }

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                connection.disconnect()
                return@withContext SyncResult.Error("Error HTTP $responseCode al conectar con el servidor de sorteos")
            }

            val jsonText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use {
                it.readText()
            }
            connection.disconnect()

            val rootJson = JSONObject(jsonText)
            val drawsArray = rootJson.optJSONArray("draws") ?: return@withContext SyncResult.AlreadyUpToDate

            // Obtener fechas ya registradas en la tabla sorteos_historicos
            val existingHistory = dbHelper.getDrawHistory(LotteryType.LA_TINKA)
            val existingDates = existingHistory.map { it.drawDate.toString() }.toSet()

            var insertedCount = 0
            var latestDate = ""

            for (i in 0 until drawsArray.length()) {
                val drawObj = drawsArray.getJSONObject(i)
                val dateStr = drawObj.getString("date")
                val ballsArray = drawObj.getJSONArray("balls")

                if (i == 0) latestDate = dateStr

                if (!existingDates.contains(dateStr)) {
                    val ballsList = mutableListOf<Int>()
                    for (b in 0 until ballsArray.length()) {
                        ballsList.add(ballsArray.getInt(b))
                    }

                    if (ballsList.size == 6 && ballsList.all { it in 1..53 }) {
                        val parsedDate = LocalDate.parse(dateStr)
                        dbHelper.insertDrawResult(LotteryType.LA_TINKA, parsedDate, ballsList)
                        insertedCount++
                    }
                }
            }

            if (insertedCount > 0) {
                SyncResult.Success(insertedCount, latestDate)
            } else {
                SyncResult.AlreadyUpToDate
            }
        } catch (e: java.net.UnknownHostException) {
            SyncResult.Offline
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Fallo de conexión")
        }
    }
}
