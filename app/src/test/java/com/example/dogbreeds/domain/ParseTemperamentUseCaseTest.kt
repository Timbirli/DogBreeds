package com.example.dogbreeds.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit-тест №2: модуль разбора темперамента. */
class ParseTemperamentUseCaseTest {

    private val parse = ParseTemperamentUseCase()

    @Test
    fun splitsByComma_andTrimsEachTrait() {
        assertEquals(
            listOf("Friendly", "Loyal", "Alert"),
            parse("Friendly,  Loyal ,Alert")
        )
    }

    @Test
    fun blankString_returnsEmptyList() {
        assertTrue(parse("").isEmpty())
        assertTrue(parse("   ").isEmpty())
    }

    @Test
    fun skipsEmptySegments() {
        assertEquals(listOf("Calm", "Smart"), parse("Calm,, ,Smart,"))
    }
}
