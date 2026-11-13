package com.example.dogbreeds.domain

/**
 * Средняя продолжительность жизни в годах из строки вида «10 - 12 years»
 * (диапазон -> среднее, одно число -> оно само); null, если чисел в строке нет.
 */
class ParseLifeSpanUseCase {

    operator fun invoke(raw: String): Double? {
        val years = NUMBER.findAll(raw).take(2).map { it.value.replace(',', '.').toDouble() }.toList()
        return years.takeIf { it.isNotEmpty() }?.average()
    }

    private companion object {
        val NUMBER = Regex("""\d+(?:[.,]\d+)?""")
    }
}
