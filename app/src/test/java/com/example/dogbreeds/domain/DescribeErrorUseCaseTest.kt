package com.example.dogbreeds.domain

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit-тест №3: модуль описания ошибок для пользователя. */
class DescribeErrorUseCaseTest {

    private val describe = DescribeErrorUseCase()

    @Test
    fun ioException_meansNoInternet() {
        assertEquals("Нет подключения к интернету.", describe(IOException("timeout")))
    }

    @Test
    fun serverException_containsHttpCode() {
        assertEquals("Ошибка сервера (503).", describe(ServerException(503)))
    }

    @Test
    fun otherError_usesItsMessage() {
        assertEquals("Ошибка: boom.", describe(IllegalStateException("boom")))
    }

    @Test
    fun otherError_withoutMessage_usesPlaceholder() {
        assertEquals("Ошибка: неизвестная.", describe(RuntimeException()))
    }
}
