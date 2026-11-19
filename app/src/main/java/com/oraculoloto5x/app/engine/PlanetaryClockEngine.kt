package com.oraculoloto5x.app.engine

import com.oraculoloto5x.app.data.models.LotteryType
import com.oraculoloto5x.app.data.models.Planet
import com.oraculoloto5x.app.data.models.PlanetaryHour
import com.oraculoloto5x.app.data.models.UserProfile
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoField
import kotlin.math.*

class PlanetaryClockEngine(private val userProfile: UserProfile) {

    /**
     * Calcula la hora exacta de amanecer y atardecer en Cusco (-13.5319, -71.9675)
     * utilizando ecuaciones solares estándar de la NOAA (100% offline).
     */
    fun calculateSunTimes(date: LocalDate): Pair<LocalTime, LocalTime> {
        val lat = userProfile.latitude
        val lon = userProfile.longitude
        val dayOfYear = date.get(ChronoField.DAY_OF_YEAR)

        // Fracción anual en radianes (gamma)
        val gamma = 2.0 * PI / 365.0 * (dayOfYear - 1)

        // Ecuación del Tiempo (eqtime en minutos)
        val eqtime = 229.18 * (
                0.000075 +
                        0.001868 * cos(gamma) -
                        0.032077 * sin(gamma) -
                        0.014615 * cos(2.0 * gamma) -
                        0.040849 * sin(2.0 * gamma)
                )

        // Declinación Solar (declin en radianes)
        val declin = 0.006918 -
                0.399912 * cos(gamma) +
                0.070257 * sin(gamma) -
                0.006758 * cos(2.0 * gamma) +
                0.000907 * sin(2.0 * gamma) -
                0.002697 * cos(3.0 * gamma) +
                0.00148 * sin(3.0 * gamma)

        val latRad = Math.toRadians(lat)
        val zenith = Math.toRadians(90.833) // Cénit solar considerando refracción atmosférica

        // Coseno del ángulo horario
        val cosHourAngle = (cos(zenith) - sin(latRad) * sin(declin)) / (cos(latRad) * cos(declin))

        // Si cosHourAngle se desborda en latitudes extremas (no en Cusco a -13.5)
        val hourAngle = Math.toDegrees(acos(cosHourAngle.coerceIn(-1.0, 1.0)))

        // Cálculo de minutos UTC desde la medianoche
        val sunriseUtcMinutes = 720.0 - 4.0 * lon - eqtime - hourAngle * 4.0
        val sunsetUtcMinutes = 720.0 - 4.0 * lon - eqtime + hourAngle * 4.0

        // Cusco está en UTC-5 (300 minutos de retraso)
        val timezoneOffsetMinutes = -5 * 60

        fun toLocalTime(minutesUtc: Double): LocalTime {
            var localMinutes = (minutesUtc + timezoneOffsetMinutes).roundToInt()
            while (localMinutes < 0) localMinutes += 1440
            while (localMinutes >= 1440) localMinutes -= 1440
            val h = localMinutes / 60
            val m = localMinutes % 60
            return LocalTime.of(h, m)
        }

        return toLocalTime(sunriseUtcMinutes) to toLocalTime(sunsetUtcMinutes)
    }

    /**
     * Planeta regente del día de la semana
     */
    fun getDayRuler(dayOfWeek: DayOfWeek): Planet {
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> Planet.SUN
            DayOfWeek.MONDAY -> Planet.MOON
            DayOfWeek.TUESDAY -> Planet.MARS
            DayOfWeek.WEDNESDAY -> Planet.MERCURY
            DayOfWeek.THURSDAY -> Planet.JUPITER
            DayOfWeek.FRIDAY -> Planet.VENUS
            DayOfWeek.SATURDAY -> Planet.SATURN
        }
    }

    /**
     * Genera las 24 horas planetarias completas del día (12 diurnas y 12 nocturnas)
     * siguiendo la Rueda Caldea de los Planetas.
     */
    fun calculatePlanetaryHours(date: LocalDate): List<PlanetaryHour> {
        val (sunrise, sunset) = calculateSunTimes(date)
        val nextDate = date.plusDays(1)
        val (nextSunrise, _) = calculateSunTimes(nextDate)

        val dayRuler = getDayRuler(date.dayOfWeek)
        val chaldeanOrder = Planet.CHALDEAN_ORDER
        val startIndex = chaldeanOrder.indexOf(dayRuler)

        val sunriseSeconds = sunrise.toSecondOfDay()
        val sunsetSeconds = sunset.toSecondOfDay()
        val nextSunriseSeconds = nextSunrise.toSecondOfDay() + 86400

        val dayDurationSec = (sunsetSeconds - sunriseSeconds).toDouble()
        val nightDurationSec = (nextSunriseSeconds - sunsetSeconds).toDouble()

        val dayHourLengthSec = dayDurationSec / 12.0
        val nightHourLengthSec = nightDurationSec / 12.0

        val hours = mutableListOf<PlanetaryHour>()

        // 12 Horas Diurnas
        for (i in 0 until 12) {
            val planet = chaldeanOrder[(startIndex + i) % chaldeanOrder.size]
            val startSec = (sunriseSeconds + i * dayHourLengthSec).roundToInt() % 86400
            val endSec = (sunriseSeconds + (i + 1) * dayHourLengthSec).roundToInt() % 86400

            val isFavorable = planet == Planet.SUN || planet == Planet.MOON

            hours.add(
                PlanetaryHour(
                    hourIndex = i + 1,
                    planet = planet,
                    isDaytime = true,
                    startTime = LocalTime.ofSecondOfDay(startSec.toLong()),
                    endTime = LocalTime.ofSecondOfDay(endSec.toLong()),
                    isFavorableWindow = isFavorable
                )
            )
        }

        // 12 Horas Nocturnas
        for (i in 0 until 12) {
            val planet = chaldeanOrder[(startIndex + 12 + i) % chaldeanOrder.size]
            val sSec = (sunsetSeconds + i * nightHourLengthSec).roundToInt()
            val eSec = (sunsetSeconds + (i + 1) * nightHourLengthSec).roundToInt()

            val normStart = (sSec % 86400).toLong()
            val normEnd = (eSec % 86400).toLong()

            val isFavorable = planet == Planet.SUN || planet == Planet.MOON

            hours.add(
                PlanetaryHour(
                    hourIndex = 13 + i,
                    planet = planet,
                    isDaytime = false,
                    startTime = LocalTime.ofSecondOfDay(normStart),
                    endTime = LocalTime.ofSecondOfDay(normEnd),
                    isFavorableWindow = isFavorable
                )
            )
        }

        return hours
    }

    /**
     * Obtiene la hora planetaria activa en este instante
     */
    fun getCurrentPlanetaryHour(date: LocalDate = LocalDate.now(), time: LocalTime = LocalTime.now()): PlanetaryHour {
        val hours = calculatePlanetaryHours(date)
        val nowSec = time.toSecondOfDay()

        for (ph in hours) {
            val s = ph.startTime.toSecondOfDay()
            val e = ph.endTime.toSecondOfDay()
            if (s < e) {
                if (nowSec in s until e) return ph
            } else {
                // Cruza la medianoche (23:xx a 00:xx)
                if (nowSec >= s || nowSec < e) return ph
            }
        }
        return hours.first()
    }

    /**
     * Calcula los minutos que faltan para la próxima ventana favorable (Sol o Luna)
     */
    fun getTimeToNextFavorableWindow(date: LocalDate = LocalDate.now(), time: LocalTime = LocalTime.now()): Pair<PlanetaryHour, Duration> {
        val hoursToday = calculatePlanetaryHours(date)
        val hoursTomorrow = calculatePlanetaryHours(date.plusDays(1))
        val allHours = hoursToday + hoursTomorrow

        val nowSec = time.toSecondOfDay()

        // Buscar una hora favorable que inicie en el futuro inmediato
        for (ph in allHours) {
            if (ph.isFavorableWindow) {
                val s = ph.startTime.toSecondOfDay()
                val e = ph.endTime.toSecondOfDay()
                // Si estamos dentro de ella
                if ((s < e && nowSec in s until e) || (s > e && (nowSec >= s || nowSec < e))) {
                    val remainingSec = if (e > nowSec) e - nowSec else (86400 - nowSec + e)
                    return ph to Duration.ofSeconds(remainingSec.toLong())
                }
                // Si empieza después
                if (s > nowSec) {
                    val waitSec = s - nowSec
                    return ph to Duration.ofSeconds(waitSec.toLong())
                }
            }
        }

        // Fallback
        val firstFav = allHours.first { it.isFavorableWindow }
        return firstFav to Duration.ofHours(1)
    }

    /**
     * Alerta de cierre de ventas de La Tinka (Miércoles y Domingos a las 21:50)
     */
    fun checkLaTinkaCutoffStatus(date: LocalDate = LocalDate.now(), time: LocalTime = LocalTime.now()): CutoffAlertStatus {
        val isTinkaDay = date.dayOfWeek in LotteryType.LA_TINKA.drawDays
        if (!isTinkaDay) {
            return CutoffAlertStatus(
                isDrawDay = false,
                isClosingSoon = false,
                isClosed = false,
                message = "Hoy no hay sorteo de La Tinka (próximo miércoles o domingo)."
            )
        }

        val cutoff = LotteryType.LA_TINKA.salesCutoffTime // 21:50
        val diffMinutes = Duration.between(time, cutoff).toMinutes()

        return when {
            diffMinutes < 0 -> CutoffAlertStatus(
                isDrawDay = true,
                isClosingSoon = false,
                isClosed = true,
                message = "Ventas cerradas para el sorteo de hoy (Cierre 21:50)."
            )
            diffMinutes in 0..30 -> CutoffAlertStatus(
                isDrawDay = true,
                isClosingSoon = true,
                isClosed = false,
                minutesRemaining = diffMinutes,
                message = "¡ALERTA CRÍTICA! Faltan solo $diffMinutes minutos para el cierre de La Tinka (21:50)."
            )
            diffMinutes in 31..90 -> CutoffAlertStatus(
                isDrawDay = true,
                isClosingSoon = true,
                isClosed = false,
                minutesRemaining = diffMinutes,
                message = "Atención: Quedan $diffMinutes minutos para el cierre de ventas de La Tinka (21:50)."
            )
            else -> CutoffAlertStatus(
                isDrawDay = true,
                isClosingSoon = false,
                isClosed = false,
                minutesRemaining = diffMinutes,
                message = "Sorteo activo hoy. Cierre de ventas a las 21:50."
            )
        }
    }
}

data class CutoffAlertStatus(
    val isDrawDay: Boolean,
    val isClosingSoon: Boolean,
    val isClosed: Boolean,
    val minutesRemaining: Long = 0L,
    val message: String
)
