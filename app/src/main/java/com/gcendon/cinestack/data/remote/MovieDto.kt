package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("results") val results: List<MovieDto>
)

data class MovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("runtime") val runtime: Int?, // Viene en minutos
    @SerializedName("genres") val genres: List<GenreDto>?
)

data class GenreDto(
    @SerializedName("name") val name: String
)