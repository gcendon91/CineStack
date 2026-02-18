package com.gcendon.cinestack.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX", // <--- Cambiado de es-ES a es-MX
        @Query("page") page: Int = 1
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
}