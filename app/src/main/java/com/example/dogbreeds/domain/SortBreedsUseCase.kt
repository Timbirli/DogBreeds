package com.example.dogbreeds.domain

/** Сортирует породы в заданном порядке. */
class SortBreedsUseCase(
    private val parseLifeSpan: ParseLifeSpanUseCase = ParseLifeSpanUseCase()
) {
    // Заглушка для фазы RED: возвращает список без изменений.
    operator fun invoke(breeds: List<Breed>, order: SortOrder): List<Breed> = breeds
}
