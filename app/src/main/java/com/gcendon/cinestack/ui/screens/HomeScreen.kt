package com.gcendon.cinestack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // ¡Usamos Coil para las fotos!
import com.gcendon.cinestack.domain.Movie
import com.gcendon.cinestack.ui.MovieUiState
import com.gcendon.cinestack.ui.MovieViewModel

@Composable
fun HomeScreen(viewModel: MovieViewModel) {
    // 1. "Escuchamos" el estado del ViewModel.
    // .collectAsState() transforma el flujo de datos en algo que Compose entiende.
    val uiState by viewModel.uiState.collectAsState()

    // 2. Usamos 'when'. Es como el switch/case de escritorio pero obligatorio:
    // tenés que manejar SI O SI todos los estados de la sealed class.
    when (val state = uiState) {
        is MovieUiState.Loading -> {
            // ESTADO ROJO: Mostramos el circulito de carga
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // El "spinner" clásico de Android
            }
        }

        is MovieUiState.Success -> {
            // ESTADO VERDE: Dibujamos la grilla con las pelis que vienen dentro del estado
            val movies = state.movies
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(movies) { movie ->
                    MovieCard(movie)
                }
            }
        }

        is MovieUiState.Error -> {
            // ESTADO AMARILLO/ERROR: Mostramos el mensaje y un botón para reintentar
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "¡Uy! Algo falló", style = MaterialTheme.typography.headlineSmall)
                Text(text = state.message, modifier = Modifier.padding(16.dp))
                Button(onClick = { viewModel.fetchMovies() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}
@Composable
fun MovieCard(movie: Movie) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Le damos un toque de sombra
    ) {
        Column {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Un poco más alta para que se vea mejor el poster
                contentScale = ContentScale.Crop // Esto hace que la imagen llene el espacio sin deformarse
            )

            // Contenedor para el texto con altura fija para que todas las cards midan lo mismo
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .height(50.dp), // Ajustamos este valor según el tamaño de fuente
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, // Permitimos hasta 2 líneas para títulos largos
                    overflow = TextOverflow.Ellipsis, // Si es más largo, mete los "..."
                    lineHeight = 18.sp // Espaciado entre líneas para que no quede amontonado
                )
            }
        }
    }
}