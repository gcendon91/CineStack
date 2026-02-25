package com.gcendon.cinestack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class MovieEntity(
    @PrimaryKey val id: Int, // El ID de TMDB nos sirve como clave única
    val title: String,
    val posterUrl: String,
    val rating: Double,
    val releaseDate: String
)