package com.gcendon.cinestack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // ¡Usamos Coil para las fotos!
import com.gcendon.cinestack.domain.Movie
import com.gcendon.cinestack.ui.MovieUiState
import com.gcendon.cinestack.ui.MovieViewModel
import androidx.compose.material.icons.filled.List

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    // 1. "Escuchamos" el estado del ViewModel.
    // .collectAsState() transforma el flujo de datos en algo que Compose entiende.
    val uiState by viewModel.uiState.collectAsState()
    //traigo lo que el usuario escribe en la search bar
    val searchQuery by viewModel.searchQuery.collectAsState()
    //obtengo la categoria seleccionada
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Lista de categorías para los botoncitos
    val categories = listOf(
        "popular" to "Populares",
        "top_rated" to "Mejor Valoradas",
        "upcoming" to "Próximas"
    )

    val sheetState = rememberModalBottomSheetState() // Controla la animación
    var showSheet by remember { mutableStateOf(false) } // Controla si se ve o no

    // Traemos los géneros del ViewModel
    val genres by viewModel.genres.collectAsState()


    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp), // Ajustamos padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.weight(1f), // CAMBIO CLAVE: weight(1f) para que deje espacio al botón
                placeholder = { Text("Buscar película...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // El nuevo botón de filtro
            IconButton(onClick = { showSheet = true }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Filtros",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        // FILA DE CATEGORÍAS (Chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()), // Por si no entran todos los botones
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (id, name) ->
                FilterChip(
                    selected = selectedCategory == id,
                    onClick = { viewModel.onCategorySelected(id) },
                    label = { Text(name) },
                    shape = RoundedCornerShape(50.dp) // Bien redondeados
                )
            }
        }

        // 2. Usamos 'when'. Es como el switch/case de escritorio pero obligatorio:
        // tenés que manejar SI O SI todos los estados de la sealed class.
        //Usamos un Box con weight(1f) para que "empuje" la barra hacia arriba
        Box(modifier = Modifier.weight(1f)) {
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
                            MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
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
                        Text(
                            text = "¡Uy! Algo falló",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(text = state.message, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.onCategorySelected(selectedCategory) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                // Contenido de la cortina
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Seleccionar Género",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Grilla o lista de géneros
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp) // Para que no ocupe toda la pantalla
                    ) {
                        items(genres) { genre ->
                            FilterChip(
                                selected = false, // Podríamos guardar cuál está seleccionado después
                                onClick = {
                                    viewModel.onGenreSelected(genre.id)
                                    showSheet = false // Cerramos la cortina al elegir
                                },
                                label = { Text(genre.name) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp) // Esquinas un poco más redondeadas
    ) {
        Column {
            // Usamos un Box para poder encimar el puntaje sobre la imagen
            Box {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                // Badge de puntuación (arriba a la derecha)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(bottomStart = 12.dp), // Solo redondeamos una esquina
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Color amarillo "estrella"
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (movie.rating > 0) "%.1f".format(movie.rating) else "S/P", // S/P = Sin Puntaje
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Título de la película
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .height(50.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
