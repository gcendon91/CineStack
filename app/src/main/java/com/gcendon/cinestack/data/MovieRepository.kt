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
}