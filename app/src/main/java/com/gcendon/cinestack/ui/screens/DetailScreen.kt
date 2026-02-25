package com.gcendon.cinestack.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.domain.Movie
import com.gcendon.cinestack.ui.MovieViewModel

@Composable
fun DetailScreen(
    movieId: Int,
    viewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory)
) {
    val context = LocalContext.current

    // Estados de la pantalla
    var movie by remember { mutableStateOf<Movie?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }

    // Estado para saber si es favorita
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(movieId) {
        try {
            // USAMOS EL VIEWMODEL EN LUGAR DEL REPOSITORY
            movie = viewModel.getMovieDetail(movieId)
            trailerKey = viewModel.getTrailer(movieId)

            // Verificamos si ya es favorita al cargar
            isFavorite = viewModel.isMovieFavorite(movieId)
        } catch (e: Exception) {
            Log.e("CineStack", "Error cargando detalle: ${e.message}")
        }
    }

    // UI principal
    movie?.let { peli ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        viewModel.toggleFavorite(peli) // Llama a la función de Room que creamos
                        isFavorite = !isFavorite      // Cambia el color del corazón al toque
                    }) {
                        Icon(
                            // Si isFavorite es true, corazón lleno. Si no, solo el borde.
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

                Spacer(modifier = Modifier.height(24.dp))

                // Ficha Técnica
                InfoSection(label = "Director", value = peli.director)
                Spacer(modifier = Modifier.height(8.dp))
                InfoSection(label = "Elenco", value = peli.cast)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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