package com.example.dogbreeds.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogbreeds.domain.ParseTemperamentUseCase

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(viewModel: BreedDetailViewModel, onBack: () -> Unit) {
    val breed by viewModel.breed.collectAsStateWithLifecycle()
    val images by viewModel.images.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(breed?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        val b = breed
        if (b == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            ) {
                Gallery(images)

                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        b.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    InfoRow("Происхождение", b.origin ?: "—")
                    InfoRow("Продолжительность жизни", b.lifeSpan.ifBlank { "—" })

                    val traits = remember(b.temperament) { ParseTemperamentUseCase()(b.temperament) }
                    if (traits.isNotEmpty()) {
                        Text("Темперамент", style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            traits.forEach { SuggestionChip(onClick = {}, label = { Text(it) }) }
                        }
                    }

                    Text("Описание", style = MaterialTheme.typography.titleSmall)
                    Text(
                        b.description ?: "Описание для этой породы отсутствует.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    AnimatedContent(targetState = b.isFavorite, label = "favoriteButton") { fav ->
                        val content: @Composable () -> Unit = {
                            Icon(
                                if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (fav) "В избранном" else "В избранное")
                        }
                        if (fav) {
                            FilledTonalButton(
                                onClick = viewModel::toggleFavorite,
                                modifier = Modifier.fillMaxWidth()
                            ) { content() }
                        } else {
                            Button(
                                onClick = viewModel::toggleFavorite,
                                modifier = Modifier.fillMaxWidth()
                            ) { content() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

/** Галерея фото породы (HorizontalPager). images == null -> идёт загрузка. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(images: List<String>?) {
    val height = 280.dp
    when {
        images == null -> Box(Modifier.fillMaxWidth().height(height), Alignment.Center) {
            CircularProgressIndicator()
        }

        images.isEmpty() -> Box(Modifier.fillMaxWidth().height(height), Alignment.Center) {
            Text("🐶", fontSize = 64.sp)
        }

        else -> {
            val pagerState = rememberPagerState(pageCount = { images.size })
            Box(Modifier.fillMaxWidth().height(height)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    BreedImage(url = images[page], modifier = Modifier.fillMaxSize())
                }
                if (images.size > 1) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
