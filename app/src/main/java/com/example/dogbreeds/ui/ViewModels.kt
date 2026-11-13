package com.example.dogbreeds.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.domain.Breed
import com.example.dogbreeds.domain.BreedRepository
import com.example.dogbreeds.domain.RefreshBreedsUseCase
import com.example.dogbreeds.domain.RefreshOutcome
import com.example.dogbreeds.domain.SearchBreedsUseCase
import kotlinx.coroutines.CoroutineDispatcher
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

data class HomeUiState(
    val breeds: List<Breed> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,      // первичная загрузка
    val isRefreshing: Boolean = false,  // pull-to-refresh
    val error: String? = null
)

/**
 * Общая ViewModel для главного экрана и экрана избранного.
 * Зависимости (репозиторий, use case'ы, диспетчер) передаются через конструктор — это позволяет
 * подставлять fake-реализации и тестовый диспетчер в тестах.
 */
class BreedsViewModel(
    private val repository: BreedRepository,
    private val searchBreeds: SearchBreedsUseCase = SearchBreedsUseCase(),
    private val refreshBreeds: RefreshBreedsUseCase = RefreshBreedsUseCase(repository),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

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
        HomeUiState(
            breeds = searchBreeds(all, query),
            totalCount = all.size,
            isLoading = status.isLoading,
            isRefreshing = status.isRefreshing,
            error = status.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val favorites: StateFlow<List<Breed>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refresh(manual: Boolean = false) {
        viewModelScope.launch(ioDispatcher) {
            _status.update { it.copy(isRefreshing = manual, error = null) }
            when (val outcome = refreshBreeds()) {
                RefreshOutcome.Success ->
                    _status.update { it.copy(isLoading = false, isRefreshing = false, error = null) }

                is RefreshOutcome.Failure -> {
                    if (outcome.hasCachedData) {
                        _events.emit("${outcome.message} Показаны сохранённые данные.")
                    }
                    _status.update {
                        it.copy(isLoading = false, isRefreshing = false, error = outcome.message)
                    }
                }
            }
        }
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch(ioDispatcher) { repository.toggleFavorite(id) }
    }
}

/** ViewModel экрана деталей конкретной породы. */
class BreedDetailViewModel(
    private val repository: BreedRepository,
    private val breedId: Int,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val breed: StateFlow<Breed?> = repository.observeBreed(breedId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _images = MutableStateFlow<List<String>?>(null) // null = загрузка
    val images: StateFlow<List<String>?> = _images.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val b = repository.observeBreed(breedId).filterNotNull().first()
            _images.value = repository.getBreedImages(breedId, b.imageUrl)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch(ioDispatcher) { repository.toggleFavorite(breedId) }
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
