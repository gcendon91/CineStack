package com.gcendon.cinestack.data

import com.gcendon.cinestack.data.remote.RetrofitClient
import com.gcendon.cinestack.domain.Movie

class MovieRepository {
    private val api = RetrofitClient.apiService

    suspend fun getPopularMovies(): List<Movie> {
        val response = api.getPopularMovies(Constants.API_KEY)

        // Convertimos los DTOs (datos sucios) a Movies (datos limpios)
        return response.results.map { dto ->
            Movie(
                id = dto.id,
                title = dto.title,
                overview = dto.overview,
                // Construimos la URL completa de la imagen
                posterUrl = "${Constants.IMAGE_BASE_URL}${dto.posterPath}",
                rating = dto.voteAverage
            )
        }
    }

    suspend fun getMovieById(movieId: Int): Movie {
        // 1. Llamamos a los detalles (duración, géneros)
        val dto = api.getMovieDetails(movieId, Constants.API_KEY)

        // 2. Llamamos a los créditos (actores, director)
        val credits = api.getMovieCredits(movieId, Constants.API_KEY)

        // 3. Filtramos al director entre todo el equipo técnico (crew)
        val directorName = credits.crew.find { it.job == "Director" }?.name ?: "Desconocido"

        // 4. Tomamos los primeros 5 actores y los separamos por coma
        val castNames = credits.cast.take(5).joinToString(", ") { it.name }

        // 5. Devolvemos la Movie con ABSOLUTAMENTE TODO
        return Movie(
            id = dto.id,
            title = dto.title,
            overview = dto.overview,
            posterUrl = "${Constants.IMAGE_BASE_URL}${dto.posterPath}",
            rating = dto.voteAverage,
            duration = dto.runtime ?: 0,
            genres = dto.genres?.joinToString(", ") { it.name } ?: "Sin género",
            director = directorName,
            cast = castNames
        )
    }
}