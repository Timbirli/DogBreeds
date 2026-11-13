package com.example.dogbreeds.domain

/** Поиск по названию породы: без учёта регистра, пробелы по краям запроса игнорируются. */
class SearchBreedsUseCase {
    operator fun invoke(breeds: List<Breed>, query: String): List<Breed> {
        val q = query.trim()
        return if (q.isEmpty()) breeds else breeds.filter { it.name.contains(q, ignoreCase = true) }
    }
}
