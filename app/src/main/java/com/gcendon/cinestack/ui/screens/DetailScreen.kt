package com.gcendon.cinestack.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.domain.Movie

@Composable
fun DetailScreen(movieId: Int) {
    // Para no complicarla con otro ViewModel ahora, vamos a usar un estado simple acá
    // (Ojo: En una app pro, esto iría en un DetailViewModel)
    var movie by remember { mutableStateOf<Movie?>(null) }
    val repository = MovieRepository()

    // Buscamos la peli cuando se carga la pantalla
    LaunchedEffect(movieId) {
        try {
            movie = repository.getMovieById(movieId)
        } catch (e: Exception) {
            // Si falla, imprimimos el error en la consola para saber qué pasó
            Log.e("CineStack", "Error cargando peli: ${e.message}")
            // Acá podrías poner un estado de error
        }
    }

    movie?.let { peli ->
        // UNA SOLA COLUMNA para todo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Imagen de cabecera (Poster) - Solo una vez
            AsyncImage(
                model = peli.posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                contentScale = ContentScale.Crop
            )

            // 2. Contenedor de la información (con padding)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = peli.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                InfoSection(label = "Director", value = peli.director)
                Spacer(modifier = Modifier.height(8.dp))
                InfoSection(label = "Elenco", value = peli.cast)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator() // Mientras carga los detalles
    }
}

// Una pequeña función de apoyo para no repetir código de etiquetas
@Composable
fun InfoSection(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}