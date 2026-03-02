package com.gcendon.cinestack.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gcendon.cinestack.ui.MovieUiState
import com.gcendon.cinestack.ui.MovieViewModel
import androidx.compose.material.icons.filled.List
import com.gcendon.cinestack.ui.components.MovieCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.font.FontWeight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit
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

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val listState = rememberLazyGridState()
    val currentGenreId by viewModel.currentGenreId.collectAsState()

    //este efecto detecta cuando llegamos al final de la lista actual para cambiar de pagina
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount

                // Si el último que veo es el último que existe (y hay pelis en la lista)
                if (totalItems > 0 && lastVisibleIndex == totalItems - 1) {
                    viewModel.loadNextPage()
                }
            }
    }

    //reinicia la posicion en las vistas si sucede alguno de los cambios de genero, categoria o busqueda
    LaunchedEffect(selectedCategory, searchQuery, currentGenreId) {
        listState.scrollToItem(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- FILA 1: TÍTULO Y ACCIONES PRINCIPALES ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CineStack",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f) // Esto empuja los iconos a la derecha
                )

                // Botón de Tema (Sol/Luna)
                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "Cambiar Tema",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Botón de Favoritos
                IconButton(onClick = onFavoritesClick) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Ver Favoritos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- FILA 2: BUSCADOR Y FILTROS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar película...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- FILA 3: CATEGORÍAS (Chips) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (id, name) ->
                    FilterChip(
                        selected = selectedCategory == id,
                        onClick = { viewModel.onCategorySelected(id) },
                        label = { Text(name) },
                        shape = RoundedCornerShape(50.dp)
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
                            state = listState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
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

}
