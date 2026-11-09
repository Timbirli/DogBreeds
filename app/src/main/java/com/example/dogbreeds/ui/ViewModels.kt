package com.example.dogbreeds.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.data.BreedRepository
import com.example.dogbreeds.data.local.BreedEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class HomeUiState(
    val breeds: List<BreedEntity> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,      // первичная загрузка
    val isRefreshing: Boolean = false,  // pull-to-refresh
    val error: String? = null
)

/** Общая ViewModel для главного экрана и экрана избранного. */
class BreedsViewModel(private val repository: BreedRepository) : ViewModel() {

    private data class Status(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val error: String? = null
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _status = MutableStateFlow(Status())

    private val _events = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    /** Список пород из Room, отфильтрованный по поисковому запросу. */
    val uiState: StateFlow<HomeUiState> = combine(repository.breeds, _query, _status) { all, query, status ->
        val q = query.trim()
        HomeUiState(
            breeds = if (q.isEmpty()) all else all.filter { it.name.contains(q, ignoreCase = true) },
            totalCount = all.size,
            isLoading = status.isLoading,
            isRefreshing = status.isRefreshing,
            error = status.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val favorites: StateFlow<List<BreedEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refresh(manual: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _status.update { it.copy(isRefreshing = manual, error = null) }
            val result = repository.refresh()
            val failure = result.exceptionOrNull()
            if (failure != null) {
                val message = describe(failure)
                if (repository.cachedCount() > 0) {
                    _events.emit("$message Показаны сохранённые данные.")
                }
                _status.update { it.copy(isLoading = false, isRefreshing = false, error = message) }
            } else {
                _status.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
            }
        }
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch(Dispatchers.IO) { repository.toggleFavorite(id) }
    }

    private fun describe(e: Throwable): String = when (e) {
        is IOException -> "Нет подключения к интернету."
        is HttpException -> "Ошибка сервера (${e.code()})."
        else -> "Ошибка: ${e.message ?: "неизвестная"}."
    }
}

/** ViewModel экрана деталей конкретной породы. */
class BreedDetailViewModel(
    private val repository: BreedRepository,
    private val breedId: Int
) : ViewModel() {

    val breed: StateFlow<BreedEntity?> = repository.observeBreed(breedId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _images = MutableStateFlow<List<String>?>(null) // null = загрузка
    val images: StateFlow<List<String>?> = _images.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val b = repository.observeBreed(breedId).filterNotNull().first()
            _images.value = repository.getBreedImages(breedId, b.imageUrl)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch(Dispatchers.IO) { repository.toggleFavorite(breedId) }
    }
}

class BreedsViewModelFactory(private val repository: BreedRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BreedsViewModel::class.java))
        return BreedsViewModel(repository) as T
    }
}

class BreedDetailViewModelFactory(
    private val repository: BreedRepository,
    private val breedId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BreedDetailViewModel::class.java))
        return BreedDetailViewModel(repository, breedId) as T
    }
}
