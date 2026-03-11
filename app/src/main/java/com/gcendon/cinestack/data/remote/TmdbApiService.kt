package com.gcendon.cinestack.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX", // <--- Cambiado de es-ES a es-MX
        @Query("page") page: Int
    ): MovieResponse

    // NUEVA FUNCIÓN: El {movie_id} en el URL se reemplaza por el parámetro que le pases
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int, // <--- Esto llena el hueco en el URL
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX"
    ): MovieDto // Usamos el mismo DTO que ya tenemos

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): CreditsResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String, // <--- El texto que escribe el usuario
        @Query("language") language: String = "es-MX",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX",
        @Query("page") page: Int
    ): MovieResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX",
        @Query("page") page: Int
    ): MovieResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX"
    ): GenreResponse // Necesitás crear este DTO simple (val genres: List<Genre>)

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX",
        @Query("page") page: Int
    ): MovieResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX",
        @Query("page") page: Int
    ): MovieResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX"
    ): VideoResponse // DTO con (val results: List<VideoDto>)

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int, // El ID que el usuario elija (ej: 28 para Acción)
        @Query("language") language: String = "es-MX",
        @Query("sort_by") sortBy: String = "popularity.desc", // Para que traiga las mejores primero
        @Query("page") page: Int
    ): MovieResponse

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getWatchProviders(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): WatchProviderResponse

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
        // Nota: Usamos en-US porque casi no hay reseñas en español en TMDB.
    ): ReviewResponse
}