package com.example.dogbreeds.domain

/** Средняя продолжительность жизни в годах из строки вида «10 - 12 years»; null, если разобрать нельзя. */
class ParseLifeSpanUseCase {
    operator fun invoke(raw: String): Double? {
        val text = raw.replace('–', '-').replace('—', '-')
        if (text.contains("-")) {
            val parts = text.split("-")
            val a = parts[0].filter { it.isDigit() }.toDoubleOrNull()
            val b = parts[1].filter { it.isDigit() }.toDoubleOrNull()
            if (a != null && b != null) {
                return (a + b) / 2
            }
            return null
        } else {
            val a = text.filter { it.isDigit() }.toDoubleOrNull()
            return a
        }
    }
}
