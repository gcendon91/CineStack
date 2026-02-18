package com.gcendon.cinestack.domain

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val rating: Double,
    val duration: Int = 0,
    val genres: String = "",
    val director: String = "",
    val cast: String = ""
)