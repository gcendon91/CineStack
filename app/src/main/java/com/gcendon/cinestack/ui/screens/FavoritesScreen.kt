package com.gcendon.cinestack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcendon.cinestack.ui.MovieViewModel
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.gcendon.cinestack.ui.components.MovieCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory),
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    // Escuchamos la lista de favoritos.
    // Cuando Room cambie, esta variable 'favorites' se actualiza sola.
    val favorites by viewModel.favoriteMovies.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Favoritos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // <--- Al tocar, ejecuta volver
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // La flecha estándar
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Aquí decidiremos qué mostrar según si hay pelis o no
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (favorites.isEmpty()) {
                Text(
                    text = "Aún no tienes favoritos",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 columnas para que se vea igual que el Home
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favorites) { peli ->
                        MovieCard(
                            movie = peli,
                            onClick = { onMovieClick(peli.id) }
                        )
                    }
                }
            }
        }
    }
}