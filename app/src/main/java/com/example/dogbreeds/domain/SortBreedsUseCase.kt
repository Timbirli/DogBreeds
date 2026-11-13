package com.example.dogbreeds.domain

/** Сортирует породы в заданном порядке. */
class SortBreedsUseCase(
    private val parseLifeSpan: ParseLifeSpanUseCase = ParseLifeSpanUseCase()
) {
    operator fun invoke(breeds: List<Breed>, order: SortOrder): List<Breed> {
        if (order == SortOrder.NAME_ASC) {
            return breeds.sortedBy { it.name.lowercase() }
        }
        if (order == SortOrder.NAME_DESC) {
            return breeds.sortedByDescending { it.name.lowercase() }
        }
        // LIFESPAN_DESC
        val withLifeSpan = breeds.filter { parseLifeSpan(it.lifeSpan) != null }
            .sortedWith(
                compareByDescending<Breed> { parseLifeSpan(it.lifeSpan) }
                    .thenBy { it.name.lowercase() }
            )
        val withoutLifeSpan = breeds.filter { parseLifeSpan(it.lifeSpan) == null }
            .sortedBy { it.name.lowercase() }
        return withLifeSpan + withoutLifeSpan
    }
}
