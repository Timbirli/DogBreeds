package com.example.dogbreeds.domain

/** Сортирует породы в заданном порядке. */
class SortBreedsUseCase(
    private val parseLifeSpan: ParseLifeSpanUseCase = ParseLifeSpanUseCase()
) {
    operator fun invoke(breeds: List<Breed>, order: SortOrder): List<Breed> =
        breeds.sortedWith(comparatorFor(order))

    private fun comparatorFor(order: SortOrder): Comparator<Breed> = when (order) {
        SortOrder.NAME_ASC -> byName
        SortOrder.NAME_DESC -> byName.reversed()
        // null (неизвестно) в естественном порядке меньше любого числа, поэтому при убывании оказывается в конце
        SortOrder.LIFESPAN_DESC -> compareByDescending<Breed> { parseLifeSpan(it.lifeSpan) }.then(byName)
    }

    private companion object {
        val byName: Comparator<Breed> = compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
}
