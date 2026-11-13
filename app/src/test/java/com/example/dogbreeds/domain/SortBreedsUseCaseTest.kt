package com.example.dogbreeds.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SortBreedsUseCaseTest {

    private val sort = SortBreedsUseCase()

    private fun names(breeds: List<Breed>) = breeds.map { it.name }

    @Test
    fun nameAsc_ignoresCase() {
        val breeds = listOf(testBreed(1, "labrador"), testBreed(2, "Beagle"), testBreed(3, "akita"))

        assertEquals(listOf("akita", "Beagle", "labrador"), names(sort(breeds, SortOrder.NAME_ASC)))
    }

    @Test
    fun nameDesc_isReverseOfNameAsc() {
        val breeds = listOf(testBreed(1, "labrador"), testBreed(2, "Beagle"), testBreed(3, "akita"))

        assertEquals(listOf("labrador", "Beagle", "akita"), names(sort(breeds, SortOrder.NAME_DESC)))
    }

    @Test
    fun lifespanDesc_putsLongerLivedFirst_andUnknownLast() {
        val breeds = listOf(
            testBreed(1, "A", lifeSpan = "10 - 12 years"), // 11
            testBreed(2, "B", lifeSpan = "14 - 16 years"), // 15
            testBreed(3, "C", lifeSpan = ""),               // неизвестно
            testBreed(4, "D", lifeSpan = "12 years")        // 12
        )

        assertEquals(listOf("B", "D", "A", "C"), names(sort(breeds, SortOrder.LIFESPAN_DESC)))
    }

    @Test
    fun lifespanDesc_withEqualLifespan_sortsByName() {
        val breeds = listOf(
            testBreed(1, "Zeta", lifeSpan = "10 years"),
            testBreed(2, "Alpha", lifeSpan = "10 years")
        )

        assertEquals(listOf("Alpha", "Zeta"), names(sort(breeds, SortOrder.LIFESPAN_DESC)))
    }

    @Test
    fun emptyList_returnsEmptyList() {
        assertTrue(sort(emptyList(), SortOrder.NAME_ASC).isEmpty())
    }
}
