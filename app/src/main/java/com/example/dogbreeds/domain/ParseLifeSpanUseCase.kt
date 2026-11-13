package com.example.dogbreeds.domain

/** Средняя продолжительность жизни в годах из строки вида «10 - 12 years»; null, если разобрать нельзя. */
class ParseLifeSpanUseCase {
    operator fun invoke(raw: String): Double? {
        TODO_STUB()
        return null
    }

    // Заглушка для фазы RED: ничего не делает.
    private fun TODO_STUB() = Unit
}
