package com.oraculoloto5x.app.data.models

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos

data class MoonPhaseInfo(
    val title: String,
    val bonus: Double,
    val illuminationPct: Int,
    val isWaxing: Boolean,
    val isExactNatalResonance: Boolean
) {
    val displaySummary: String
        get() = "Luna hoy: $illuminationPct% · ${if (isExactNatalResonance) "Cuarto Creciente (Resonancia 50% Natal)" else if (isWaxing) "Creciente" else "Menguante"}"
}

/**
 * Perfil Astro-Numerológico Natal de Alta Precisión
 * Calibrado para Ivan Josseph Chuctaya Paucar
 * Lugar natal: Sicuani (-14.2667, -71.2333) | 19/07/1991 02:00 AM
 * Ubicación actual de juego: Cusco (-13.5319, -71.9675)
 */
data class NatalAstronomicalProfile(
    val fullName: String = "Ivan Josseph Chuctaya Paucar",
    val birthDate: LocalDate = LocalDate.of(1991, 7, 19),
    val birthTime: LocalTime = LocalTime.of(2, 0),
    val birthPlace: String = "Sicuani, Cusco",
    val birthLat: Double = -14.2667,         // 14°16' S
    val birthLon: Double = -71.2333,         // 71°14' O
    val currentCity: String = "Cusco",
    val currentLat: Double = -13.5319,
    val currentLon: Double = -71.9675,
    val chaldeanPlaceNumber: Int = 20,       // S-I-C-U-A-N-I = 3+1+3+6+1+5+1 = 20 -> 2 (Armónico lunar 2-7 de Cheiro)
    val siderealTimeHours: Double = 22.0167, // 22h 01m -> Ascendente 27° Tauro / Casa 5 Virgo-Libra
    val house5AgatheTucheRulers: Set<Int> = setOf(5, 6), // Mercurio (5) y Venus (6) según Westcott p.63
    val natalMoonIllumination: Double = 0.50, // Cuarto Creciente (50% iluminada al nacer)
    val isNatalMoonWaxing: Boolean = true,
    val solarCusp: String = "Cáncer-Leo (26° Cáncer, 2:00 AM - Phillips Cap. 12)"
) {
    companion object {
        val DEFAULT = NatalAstronomicalProfile()

        /**
         * Cálculo offline de fase lunar e iluminación porcentual real
         * Ciclo sinódico estándar: 29.53058867 días desde Epoch 10962 (2000-01-06)
         */
        fun getMoonSyncBonus(targetDateEpochDay: Long): MoonPhaseInfo {
            val synodicMonth = 29.53058867
            val daysSinceRef = (targetDateEpochDay - 10962).toDouble()
            val phaseAge = ((daysSinceRef % synodicMonth) + synodicMonth) % synodicMonth

            val angleRad = Math.toRadians((phaseAge / synodicMonth) * 360.0)
            val illuminationPct = (((1.0 - cos(angleRad)) / 2.0) * 100.0).toInt().coerceIn(0, 100)
            val isWaxing = phaseAge < (synodicMonth / 2.0)

            // Resonancia con el 50% Cuarto Creciente natal (ventana 45%..55% creciente o días 5.5..9.5)
            val isExactNatalResonance = isWaxing && (illuminationPct in 45..55 || phaseAge in 5.5..9.5)

            val (title, bonus) = when {
                isExactNatalResonance ->
                    "Resonancia Exacta: Cuarto Creciente Natal ($illuminationPct%) [+2.0]" to 2.0
                isWaxing ->
                    "Luna Creciente Activa ($illuminationPct%) [+1.0]" to 1.0
                else ->
                    "Luna Menguante ($illuminationPct%) [Consolidación]" to 0.0
            }

            return MoonPhaseInfo(
                title = title,
                bonus = bonus,
                illuminationPct = illuminationPct,
                isWaxing = isWaxing,
                isExactNatalResonance = isExactNatalResonance
            )
        }
    }
}
