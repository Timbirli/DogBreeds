package com.example.dogbreeds.domain

/** Превращает строку темперамента «Friendly, Loyal, Alert» в список черт. */
class ParseTemperamentUseCase {
    operator fun invoke(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
