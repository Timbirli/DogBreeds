package com.example.dogbreeds.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit-тест №5: модуль выбора адреса картинки. */
class ResolveImageUrlUseCaseTest {

    private val resolve = ResolveImageUrlUseCase()

    @Test
    fun prefersDirectUrl() {
        assertEquals(
            "https://img/direct.jpg",
            resolve("https://img/direct.jpg", "abc")
        )
    }

    @Test
    fun withoutUrl_buildsAddressFromReferenceId() {
        assertEquals("https://cdn2.thedogapi.com/images/abc.jpg", resolve(null, "abc"))
        assertEquals("https://cdn2.thedogapi.com/images/abc.jpg", resolve("  ", "abc"))
    }

    @Test
    fun withoutAnything_returnsEmptyString() {
        assertEquals("", resolve(null, null))
        assertEquals("", resolve("", ""))
    }
}
