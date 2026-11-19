package com.oraculoloto5x.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.engine.PlanetaryClockEngine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object PlanetaryAlarmScheduler {

    fun scheduleFavorableAlarms(context: Context) {
        val dbHelper = LotoDatabaseHelper(context)
        val profile = dbHelper.getUserProfile()
        if (!profile.notificationsEnabled) return

        val engine = PlanetaryClockEngine(profile)
        val today = LocalDate.now()
        val hoursToday = engine.calculatePlanetaryHours(today)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val zoneId = ZoneId.of(profile.timeZone)

        var requestCode = 100

        // 1. Programar alertas 10 minutos antes de cada Hora del Sol o Luna
        for (ph in hoursToday) {
            if (ph.isFavorableWindow) {
                val alertTime = ph.startTime.minusMinutes(10)
                val alertDateTime = LocalDateTime.of(today, alertTime)
                val triggerMillis = alertDateTime.atZone(zoneId).toInstant().toEpochMilli()

                if (triggerMillis > System.currentTimeMillis()) {
                    val intent = Intent(context, OraculoAlarmReceiver::class.java).apply {
                        action = OraculoAlarmReceiver.ACTION_ALERT
                        putExtra(OraculoAlarmReceiver.EXTRA_TITLE, "⚡ Ventana Cósmica: ${ph.planet.spanishName} en Cusco")
                        putExtra(
                            OraculoAlarmReceiver.EXTRA_MESSAGE,
                            "En 10 minutos inicia la ${ph.planet.spanishName} (${ph.formattedInterval}). Serie favorable ${ph.planet.seriesAffinity} para tu perfil natal."
                        )
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode++,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    setExactAlarm(alarmManager, triggerMillis, pendingIntent)
                }
            }
        }

        // 2. Programar alerta 20 minutos antes de cierre de La Tinka si hoy es día de sorteo
        if (today.dayOfWeek in LotteryType.LA_TINKA.drawDays) {
            val cutoff = LotteryType.LA_TINKA.salesCutoffTime // 21:50
            val alertTime = cutoff.minusMinutes(20) // 21:30
            val alertDateTime = LocalDateTime.of(today, alertTime)
            val triggerMillis = alertDateTime.atZone(zoneId).toInstant().toEpochMilli()

            if (triggerMillis > System.currentTimeMillis()) {
                val intent = Intent(context, OraculoAlarmReceiver::class.java).apply {
                    action = OraculoAlarmReceiver.ACTION_ALERT
                    putExtra(OraculoAlarmReceiver.EXTRA_TITLE, "⏰ ¡Alerta La Tinka! Cierre en 20 minutos")
                    putExtra(
                        OraculoAlarmReceiver.EXTRA_MESSAGE,
                        "Faltan 20 minutos para el cierre de ventas (21:50). Verifica tu Jugada Maestra antes del límite."
                    )
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                setExactAlarm(alarmManager, triggerMillis, pendingIntent)
            }
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }
}
