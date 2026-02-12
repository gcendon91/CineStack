package com.gcendon.cinestack.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-MX", // <--- Cambiado de es-ES a es-MX
        @Query("page") page: Int = 1
    ): MovieResponse
}