package com.example.dogbreeds.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit-тест №1: модуль поиска пород. */
class SearchBreedsUseCaseTest {

    private val search = SearchBreedsUseCase()
    private val breeds = listOf(
        testBreed(id = 1, name = "Beagle"),
        testBreed(id = 2, name = "Labrador Retriever"),
        testBreed(id = 3, name = "Golden Retriever")
    )

    @Test
    fun blankQuery_returnsAllBreeds() {
        assertEquals(breeds, search(breeds, ""))
        assertEquals(breeds, search(breeds, "   "))
    }

    @Test
    fun query_isCaseInsensitive_matchesSubstring_andIgnoresOuterSpaces() {
        val result = search(breeds, "  RETRIEVER ")

        assertEquals(listOf("Labrador Retriever", "Golden Retriever"), result.map { it.name })
    }

    @Test
    fun query_withoutMatches_returnsEmptyList() {
        assertTrue(search(breeds, "poodle").isEmpty())
    }
}
