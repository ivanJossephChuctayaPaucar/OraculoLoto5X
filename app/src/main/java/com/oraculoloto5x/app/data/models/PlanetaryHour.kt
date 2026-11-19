package com.oraculoloto5x.app.data.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Planet(
    val spanishName: String,
    val symbol: String,
    val seriesAffinity: Set<Int>, // afinidad con series numerológicas
    val nature: String
) {
    SUN("Sol", "☉", setOf(1, 4), "Luz, vitalidad, oro, éxito supremo"),
    MOON("Luna", "☽", setOf(2, 7), "Intuición, magnetismo, plata, visión interior"),
    MARS("Marte", "♂", setOf(9), "Fuerza, energía impulsiva, combate"),
    MERCURY("Mercurio", "☿", setOf(5), "Inteligencia, comercio, rapidez, cálculo"),
    JUPITER("Júpiter", "♃", setOf(3), "Expansión, fortuna mayor, sabiduría"),
    VENUS("Venus", "♀", setOf(6), "Armonía, belleza, atracción, gracia"),
    SATURN("Saturno", "♄", setOf(8), "Disciplina, karma, lentitud, prudencia");

    companion object {
        val CHALDEAN_ORDER = listOf(SATURN, JUPITER, MARS, SUN, VENUS, MERCURY, MOON)
    }
}

data class PlanetaryHour(
    val hourIndex: Int, // 1 a 24
    val planet: Planet,
    val isDaytime: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isFavorableWindow: Boolean // Para Iván: horas del Sol (1-4) o de la Luna (2-7)
) {
    val formattedInterval: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return "${startTime.format(formatter)} - ${endTime.format(formatter)}"
        }

    val typeLabel: String
        get() = if (isDaytime) "Hora Diurna #$hourIndex" else "Hora Nocturna #${hourIndex - 12}"
}
