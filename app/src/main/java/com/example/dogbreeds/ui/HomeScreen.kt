package com.example.dogbreeds.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful-обёртка: подключает ViewModel (состояние + одноразовые события) к «глупому» [HomeContent]. */
@Composable
fun HomeScreen(
    viewModel: BreedsViewModel,
    onBreedClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Одноразовые события (SharedFlow) -> Snackbar
    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    HomeContent(
        state = state,
        query = query,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = { viewModel.refresh(manual = true) },
        onRetry = { viewModel.refresh(manual = false) },
        onBreedClick = onBreedClick,
        onFavoritesClick = onFavoritesClick,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

/** Stateless-экран: всё, что он показывает, приходит параметрами — его легко проверять UI-тестами. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    query: String,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBreedClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dog Breeds") },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Избранное")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Поиск по названию породы") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    // Загрузка
                    state.isLoading && state.totalCount == 0 -> ScrollableCenter {
                        CircularProgressIndicator()
                    }

                    // Ошибка (и нечего показать из кэша)
                    state.error != null && state.totalCount == 0 -> ScrollableCenter {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.padding(top = 16.dp)
                            ) { Text("Повторить") }
                        }
                    }

                    // Пусто (после поиска или вообще)
                    state.breeds.isEmpty() -> ScrollableCenter {
                        Text(
                            if (query.isNotBlank()) "Ничего не найдено по запросу «${query.trim()}»"
                            else "Список пуст. Потяните вниз, чтобы обновить.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Успех
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.breeds, key = { it.id }) { breed ->
                            BreedCard(
                                breed = breed,
                                onClick = { onBreedClick(breed.id) },
                                onToggleFavorite = { onToggleFavorite(breed.id) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(250),
                                    fadeOutSpec = tween(250)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Контент по центру, прокручиваемый — чтобы pull-to-refresh работал и в пустых состояниях. */
@Composable
private fun ScrollableCenter(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}
