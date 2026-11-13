package com.example.dogbreeds.domain

import java.io.IOException

/** Человекочитаемое описание ошибки для пользователя. */
class DescribeErrorUseCase {
    operator fun invoke(error: Throwable): String = when (error) {
        is IOException -> "Нет подключения к интернету."
        is ServerException -> "Ошибка сервера (${error.code})."
        else -> "Ошибка: ${error.message ?: "неизвестная"}."
    }
}
