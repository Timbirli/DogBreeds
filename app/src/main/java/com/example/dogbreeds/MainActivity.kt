package com.example.dogbreeds

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dogbreeds.ui.BreedDetailViewModel
import com.example.dogbreeds.ui.BreedDetailViewModelFactory
import com.example.dogbreeds.ui.BreedsViewModel
import com.example.dogbreeds.ui.BreedsViewModelFactory
import com.example.dogbreeds.ui.DetailScreen
import com.example.dogbreeds.ui.FavoritesScreen
import com.example.dogbreeds.ui.HomeScreen
import kotlinx.serialization.Serializable

// ---- Ключи экранов Navigation 3 ----
@Serializable data object Home : NavKey
@Serializable data object Favorites : NavKey
@Serializable data class Detail(val breedId: Int) : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDependencies.init(this)
        setContent {
            DogBreedsTheme { DogBreedsRoot() }
        }
    }
}

@Composable
private fun DogBreedsRoot() {
    val repository = AppDependencies.repository
    val backStack = rememberNavBackStack(Home)

    // Общая ViewModel для списка и избранного (живёт в рамках Activity)
    val breedsViewModel: BreedsViewModel = viewModel(
        factory = remember { BreedsViewModelFactory(repository) }
    )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    viewModel = breedsViewModel,
                    onBreedClick = { id -> backStack.add(Detail(id)) },
                    onFavoritesClick = { backStack.add(Favorites) }
                )
            }
            entry<Favorites> {
                FavoritesScreen(
                    viewModel = breedsViewModel,
                    onBreedClick = { id -> backStack.add(Detail(id)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Detail> { key ->
                val detailViewModel: BreedDetailViewModel = viewModel(
                    key = "breed_${key.breedId}",
                    factory = BreedDetailViewModelFactory(repository, key.breedId)
                )
                DetailScreen(
                    viewModel = detailViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@Composable
private fun DogBreedsTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
