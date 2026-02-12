package com.gcendon.cinestack.domain

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String, // Aquí ya tendremos la URL completa de la imagen
    val rating: Double
)