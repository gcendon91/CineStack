package com.gcendon.cinestack.data

import com.gcendon.cinestack.data.remote.MovieDto
import com.gcendon.cinestack.data.remote.RetrofitClient
import com.gcendon.cinestack.domain.Genre
import com.gcendon.cinestack.domain.Movie

class MovieRepository {
    private val api = RetrofitClient.apiService

    suspend fun getPopularMovies(): List<Movie> {
        return api.getPopularMovies(Constants.API_KEY).results.map { it.toDomain() }
    }

    suspend fun getMovieById(movieId: Int): Movie {
        // 1. Pedimos los dos datos que necesitamos (detalles y créditos)
        val dto = api.getMovieDetails(movieId, Constants.API_KEY)
        val credits = api.getMovieCredits(movieId, Constants.API_KEY)

        // 2. Procesamos la lógica específica del detalle
        val directorName = credits.crew.find { it.job == "Director" }?.name ?: "Desconocido"
        val castNames = credits.cast.take(5).joinToString(", ") { it.name }

        // 3. Usamos el mapper para los datos básicos y .copy() para el resto
        return dto.toDomain().copy(
            duration = dto.runtime ?: 0,
            genres = dto.genres?.joinToString(", ") { it.name } ?: "Sin género", // Mapeamos la lista a un solo String
            director = directorName,
            cast = castNames
        )
    }

    suspend fun searchMovies(query: String): List<Movie> {
        return api.searchMovies(Constants.API_KEY, query).results.map { it.toDomain() }
    }

    suspend fun getMoviesByCategory(category: String): List<Movie> {
        val response = when (category) {
            "top_rated" -> api.getTopRatedMovies(Constants.API_KEY)
            "upcoming" -> api.getUpcomingMovies(Constants.API_KEY)
            else -> api.getPopularMovies(Constants.API_KEY)
        }
        return response.results.map { it.toDomain() }
    }

    suspend fun getRecommendations(movieId: Int): List<Movie> {
        return api.getRecommendations(movieId, Constants.API_KEY).results.map { it.toDomain() }
    }

    suspend fun getSimilarMovies(movieId: Int): List<Movie> {
        return api.getSimilarMovies(movieId, Constants.API_KEY).results.map { it.toDomain() }
    }

    suspend fun getGenres(): List<Genre> {
        val response = api.getGenres(Constants.API_KEY)
        return response.genres.map { dto ->
            Genre(id = dto.id, name = dto.name)
        }
    }

    suspend fun getMovieTrailerKey(movieId: Int): String? {
        val response = api.getMovieVideos(movieId, Constants.API_KEY)
        // Buscamos el primer video que cumpla las condiciones
        return response.results.find { it.site == "YouTube" && it.type == "Trailer" }?.key
    }

    suspend fun getMoviesByGenre(genreId: Int): List<Movie> {
        val response = api.discoverMoviesByGenre(Constants.API_KEY, genreId)
        return response.results.map { it.toDomain() }
    }

    private fun MovieDto.toDomain(): Movie {
        return Movie(
            id = this.id,
            title = this.title,
            overview = this.overview,
            posterUrl = "${Constants.IMAGE_BASE_URL}${this.posterPath}",
            rating = this.voteAverage
        )
    }

}