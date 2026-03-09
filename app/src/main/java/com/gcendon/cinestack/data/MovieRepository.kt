package com.gcendon.cinestack.data

import com.gcendon.cinestack.data.remote.MovieDto
import com.gcendon.cinestack.data.remote.RetrofitClient
import com.gcendon.cinestack.domain.Genre
import com.gcendon.cinestack.domain.Movie

import com.gcendon.cinestack.data.local.dao.MovieDao
import com.gcendon.cinestack.data.local.entities.MovieEntity
import com.gcendon.cinestack.data.remote.CastDto
import com.gcendon.cinestack.data.remote.WatchProviderDto
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val movieDao: MovieDao) {
    private val api = RetrofitClient.apiService

    suspend fun getPopularMovies(page: Int): List<Movie> {
        return api.getPopularMovies(Constants.API_KEY, page = page).results.map { it.toDomain() }
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
            genres = dto.genres?.joinToString(", ") { it.name }
                ?: "Sin género", // Mapeamos la lista a un solo String
            director = directorName,
            cast = castNames
        )
    }

    suspend fun searchMovies(query: String, page: Int): List<Movie> {
        return api.searchMovies(Constants.API_KEY, query, page = page).results.map { it.toDomain() }
    }

    suspend fun getMoviesByCategory(category: String, page: Int): List<Movie> {
        val response = when (category) {
            "top_rated" -> api.getTopRatedMovies(Constants.API_KEY, page = page)
            "upcoming" -> api.getUpcomingMovies(Constants.API_KEY, page = page)
            else -> api.getPopularMovies(Constants.API_KEY, page = page)
        }
        return response.results.map { it.toDomain() }
    }

    suspend fun getRecommendations(movieId: Int, page: Int): List<Movie> {
        return api.getRecommendations(
            movieId,
            Constants.API_KEY,
            page = page
        ).results.map { it.toDomain() }
    }

    suspend fun getSimilarMovies(movieId: Int, page: Int): List<Movie> {
        return api.getSimilarMovies(
            movieId,
            Constants.API_KEY,
            page = page
        ).results.map { it.toDomain() }
    }

    suspend fun getGenres(): List<Genre> {
        val response = api.getGenres(Constants.API_KEY)
        return response.genres.map { dto ->
            Genre(id = dto.id, name = dto.name)
        }
    }

    suspend fun getMovieTrailerKey(movieId: Int): String? {
        val response = api.getMovieVideos(movieId, Constants.API_KEY)

        // 1. Buscamos primero el Trailer oficial
        val officialTrailer = response.results.find {
            it.site == "YouTube" && it.type == "Trailer" && it.official
        }

        // 2. Si no hay oficial, buscamos cualquier Trailer
        val anyTrailer = response.results.find {
            it.site == "YouTube" && it.type == "Trailer"
        }

        // 3. Si sigue sin haber nada, buscamos un Teaser o el primero que venga
        val fallback = response.results.find { it.site == "YouTube" }

        // Devolvemos el mejor que hayamos encontrado (en ese orden)
        return (officialTrailer ?: anyTrailer ?: fallback)?.key
    }

    suspend fun getMoviesByGenre(genreId: Int, page: Int): List<Movie> {
        val response = api.discoverMoviesByGenre(Constants.API_KEY, genreId, page = page)
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

    suspend fun getWatchProviders(movieId: Int): List<WatchProviderDto> {
        // 1. Hacemos el pedido a la API
        val response = api.getWatchProviders(movieId, Constants.API_KEY)

        // 2. Buscamos en el Mapa la clave "AR" (Argentina)
        // Usamos el "safe call" (?) y el operador "elvis" (?:) por seguridad
        val argentinaData = response.results["AR"]

        // 3. Devolvemos solo la lista de 'flatrate' (suscripciones como Netflix, Disney+, etc.)
        // Si no hay datos para Argentina, devolvemos una lista vacía para que la app no explote
        return argentinaData?.flatrate ?: emptyList()
    }

    suspend fun getMovieCast(movieId: Int): List<CastDto> {
        // 1. Pedimos los créditos a la API
        val response = api.getMovieCredits(movieId, Constants.API_KEY)

        // 2. Filtramos: TMDB manda a los actores ordenados por importancia.
        // Nos quedamos con los primeros 10 para no saturar la memoria.
        return response.cast.take(10)
    }

    // --- MÉTODOS LOCALES (FAVORITOS) ---

    // Obtenemos los favoritos como Flow para que la UI se entere de cambios al toque
    fun getFavorites(): Flow<List<MovieEntity>> = movieDao.getAllFavorites()

    suspend fun addFavorite(movie: MovieEntity) = movieDao.insertFavorite(movie)

    suspend fun removeFavorite(movie: MovieEntity) = movieDao.deleteFavorite(movie)

    suspend fun isFavorite(id: Int): Boolean = movieDao.isFavorite(id)

}