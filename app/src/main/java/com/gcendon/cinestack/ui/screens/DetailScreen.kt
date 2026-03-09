package com.gcendon.cinestack.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gcendon.cinestack.data.Constants
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.data.remote.WatchProviderDto
import com.gcendon.cinestack.domain.Movie
import com.gcendon.cinestack.ui.MovieViewModel
import com.gcendon.cinestack.ui.components.MovieCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: Int,
    viewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory),
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    val context = LocalContext.current

    // Estados de la pantalla
    var movie by remember { mutableStateOf<Movie?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }

    // Estado para saber si es favorita
    var isFavorite by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val similarMovies by viewModel.similarMovies.collectAsState()

    // Observamos los proveedores desde el ViewModel
    val providers by viewModel.watchProviders.collectAsState()

    val castList by viewModel.cast.collectAsState()

    LaunchedEffect(movieId) {
        try {
            movie = viewModel.getMovieDetail(movieId)
            trailerKey = viewModel.getTrailer(movieId)
            isFavorite = viewModel.isMovieFavorite(movieId)

            viewModel.fetchSimilarMovies(movieId)
            viewModel.fetchWatchProviders(movieId)
            viewModel.fetchMovieCast(movieId)

        } catch (e: Exception) {
            Log.e("CineStack", "Error cargando detalle: ${e.message}")
        }
    }

    // UI principal
    movie?.let { peli ->
        Scaffold(
            topBar = {
            TopAppBar(
                title = { Text("Detalle de Película") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // Usamos el parámetro que agregamos
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
            snackbarHost = { SnackbarHost(snackbarHostState) } // El lugar donde aparecen los avisos
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera: Poster
                AsyncImage(
                    model = peli.posterUrl,
                    contentDescription = peli.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    contentScale = ContentScale.Crop
                )

                // Contenido: Información detallada
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = peli.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f) // Esto hace que el título ocupe el espacio y no empuje al corazón fuera de la pantalla
                        )

                        IconButton(onClick = {
                            viewModel.toggleFavorite(peli)
                            isFavorite = !isFavorite

                            scope.launch { // Necesitamos un 'scope' porque mostrar el aviso es una tarea que lleva tiempo (animación)
                                snackbarHostState.showSnackbar(
                                    message = if (isFavorite) "❤️ ¡Guardada en favoritos!" else "💔 Eliminada de favoritos",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color.Red else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fila de Info Rápida (Rating y Duración)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐ ${"%.1f".format(peli.rating)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "🕒 ${peli.duration} min",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = peli.genres,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Botón de Tráiler (Solo si existe key)
                    trailerKey?.let { key ->
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$key")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VER TRÁILER OFICIAL", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Sinopsis
                    Text(
                        text = "Sinopsis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = peli.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    WatchProvidersRow(providers = providers)

                    CastRow(cast = castList)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ficha Técnica
                    InfoSection(label = "Director", value = peli.director)
                    //Spacer(modifier = Modifier.height(8.dp))
                    //InfoSection(label = "Elenco", value = peli.cast)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Solo mostramos la sección si realmente hay películas similares
                    if (similarMovies.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = "Películas similares",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Usamos LazyRow para que se pueda scrollear de costado (tipo Netflix)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp) // Espacio al final para que no quede pegado
                        ) {
                            items(similarMovies) { peliSim ->
                                // Reutilizamos el MovieCard que creamos ayer
                                Box(modifier = Modifier.width(160.dp)) {
                                    MovieCard(
                                        movie = peliSim,
                                        onClick = { onMovieClick(peliSim.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun InfoSection(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun WatchProvidersRow(providers: List<WatchProviderDto>) {
    Column(modifier = Modifier.padding(16.dp)) {
        // El título lo dejamos fijo para que el usuario sepa qué estamos buscando
        Text(
            text = "Disponible en Argentina:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (providers.isNotEmpty()) {
            // Si hay plataformas, mostramos la fila de logos
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(providers) { provider ->
                    AsyncImage(
                        model = "${Constants.IMAGE_BASE_URL}${provider.logo_path}",
                        contentDescription = provider.provider_name,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        } else {
            // Si la lista está vacía, mostramos el mensaje de aviso
            Text(
                text = "No disponible actualmente en plataformas de streaming.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Color más suave (grisáceo)
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CastRow(cast: List<com.gcendon.cinestack.data.remote.CastDto>) {
    if (cast.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                text = "Reparto Principal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cast) { actor ->
                    ActorItem(actor)
                }
            }
        }
    }
}

@Composable
fun ActorItem(actor: com.gcendon.cinestack.data.remote.CastDto) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        AsyncImage(
            model = if (actor.profilePath != null)
                "${Constants.IMAGE_BASE_URL}${actor.profilePath}"
            else
                "https://www.themoviedb.org/assets/2/v4/glyphicons/basic/glyphicons-basic-4-user-grey-d8fe357375fc6e57a6e82ef91398a5d7c921711b0c11503504886943e9efd472.svg",
            contentDescription = actor.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(85.dp)
                .clip(androidx.compose.foundation.shape.CircleShape) // Foto redonda
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = actor.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Text(
            text = actor.character,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}