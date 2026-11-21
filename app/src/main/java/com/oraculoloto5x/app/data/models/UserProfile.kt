package com.oraculoloto5x.app.data.models

import java.time.LocalDate

data class UserProfile(
    val id: Long = 1L,
    val fullName: String = "",
    val commonName: String = "",
    val birthDate: LocalDate = LocalDate.of(2000, 1, 1),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timeZone: String = "America/Lima",
    val notificationsEnabled: Boolean = false
) {
    /**
     * Día de nacimiento en número (19 para Ivan)
     */
    val birthDayNumber: Int
        get() = birthDate.dayOfMonth

    /**
     * Reducción simple del día de nacimiento (19 -> 1+9 = 10 -> 1)
     */
    val primarySingleNumber: Int
        get() {
            var n = birthDayNumber
            while (n > 9) {
                var s = 0
                var temp = n
                while (temp > 0) {
                    s += temp % 10
                    temp /= 10
                }
                n = s
            }
            return n
        }
}
