package com.oraculoloto5x.app.data.models

import java.time.LocalDate
import java.time.LocalTime

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
         * Cálculo offline de fase lunar actual (sin internet)
         * Ciclo sinódico: 29.53058867 días desde 2000-01-06 (Epoch day 10962)
         */
        fun getMoonSyncBonus(targetDateEpochDay: Long): Pair<String, Double> {
            val synodicMonth = 29.53058867
            val daysSinceRef = (targetDateEpochDay - 10962).toDouble()
            val phaseAge = ((daysSinceRef % synodicMonth) + synodicMonth) % synodicMonth

            return when {
                // Ventana de Cuarto Creciente (días 5.5 a 9.5 -> ~50% iluminada, resonancia natal exacta)
                phaseAge in 5.5..9.5 -> "Resonancia Exacta: Cuarto Creciente Natal (+2.0)" to 2.0
                // Toda la fase creciente (días 1.0 a 14.7 -> Westcott p.62)
                phaseAge in 1.0..14.7 -> "Luna Creciente Activa (+1.0)" to 1.0
                else -> "Luna Menguante (Consolidación)" to 0.0
            }
        }
    }
}
