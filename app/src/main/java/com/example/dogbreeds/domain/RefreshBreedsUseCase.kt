package com.example.dogbreeds.domain

sealed interface RefreshOutcome {
    data object Success : RefreshOutcome

    /** [hasCachedData] — есть ли в локальной БД данные, которые можно показать вместо ошибки. */
    data class Failure(val message: String, val hasCachedData: Boolean) : RefreshOutcome
}

/** Обновляет данные с сервера и сообщает, чем всё закончилось (с учётом офлайн-кэша). */
class RefreshBreedsUseCase(
    private val repository: BreedRepository,
    private val describeError: DescribeErrorUseCase = DescribeErrorUseCase()
) {
    suspend operator fun invoke(): RefreshOutcome {
        val failure = repository.refresh().exceptionOrNull() ?: return RefreshOutcome.Success
        return RefreshOutcome.Failure(
            message = describeError(failure),
            hasCachedData = repository.cachedCount() > 0
        )
    }
}
