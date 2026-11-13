package com.example.dogbreeds.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dogbreeds.domain.Breed
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI-тесты главного экрана. Тестируется stateless [HomeContent]: состояние задаётся параметрами,
 * реакции пользователя проверяются через переданные callback'и — ни сети, ни базы, ни ViewModel не нужно.
 */
@RunWith(AndroidJUnit4::class)
class HomeContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun breed(id: Int, name: String) = Breed(
        id = id,
        name = name,
        temperament = "Friendly",
        origin = null,
        lifeSpan = "10 - 12 years",
        description = "Описание породы $name",
        imageUrl = "",
        isFavorite = false
    )

    private fun show(
        state: HomeUiState,
        onRetry: () -> Unit = {},
        onBreedClick: (Int) -> Unit = {},
        onToggleFavorite: (Int) -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                HomeContent(
                    state = state,
                    query = "",
                    snackbarHostState = SnackbarHostState(),
                    onQueryChange = {},
                    onRefresh = {},
                    onRetry = onRetry,
                    onBreedClick = onBreedClick,
                    onFavoritesClick = {},
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }

    /** UI-тест №1: список пород отображается; нажатия на сердечко и на карточку доходят до callback'ов. */
    @Test
    fun showsBreedList_andReportsFavoriteAndCardClicks() {
        var favoriteId: Int? = null
        var openedId: Int? = null
        show(
            state = HomeUiState(
                breeds = listOf(breed(1, "Beagle"), breed(2, "Labrador Retriever")),
                totalCount = 2,
                isLoading = false
            ),
            onBreedClick = { openedId = it },
            onToggleFavorite = { favoriteId = it }
        )

        composeRule.onNodeWithText("Beagle", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Labrador Retriever", useUnmergedTree = true).assertIsDisplayed()

        // сердечко у второй карточки
        composeRule.onAllNodesWithContentDescription("В избранное", useUnmergedTree = true)[1].performClick()
        composeRule.runOnIdle { assertEquals(2, favoriteId) }

        // клик по карточке открывает породу
        composeRule.onNodeWithText("Beagle", useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertEquals(1, openedId) }
    }

    /** UI-тест №2: при ошибке без кэша показывается сообщение и кнопка «Повторить». */
    @Test
    fun showsErrorWithRetryButton_whenLoadingFailedAndNothingIsCached() {
        var retries = 0
        show(
            state = HomeUiState(
                breeds = emptyList(),
                totalCount = 0,
                isLoading = false,
                error = "Нет подключения к интернету."
            ),
            onRetry = { retries++ }
        )

        composeRule.onNodeWithText("Нет подключения к интернету.").assertIsDisplayed()
        composeRule.onNodeWithText("Повторить").performClick()

        composeRule.runOnIdle { assertEquals(1, retries) }
    }
}
