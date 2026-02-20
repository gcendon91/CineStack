package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

// Este es el que recibe el JSON completo de la API
data class GenreResponse(
    @SerializedName("genres")
    val genres: List<GenreDto>
)

// Este es el objeto individual
data class GenreDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)