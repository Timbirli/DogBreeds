package com.example.dogbreeds.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseLifeSpanUseCaseTest {

    private val parse = ParseLifeSpanUseCase()

    @Test
    fun range_returnsAverage() {
        assertEquals(11.0, parse("10 - 12 years")!!, 0.001)
    }

    @Test
    fun singleValue_returnsThatValue() {
        assertEquals(12.0, parse("12 years")!!, 0.001)
    }

    @Test
    fun rangeWithoutSpaces_andWithEnDash_isSupported() {
        assertEquals(10.0, parse("9-11 years")!!, 0.001)
        assertEquals(10.0, parse("9–11 years")!!, 0.001)
    }

    @Test
    fun unknownFormat_returnsNull() {
        assertNull(parse(""))
        assertNull(parse("unknown"))
        assertNull(parse("years"))
    }
}
