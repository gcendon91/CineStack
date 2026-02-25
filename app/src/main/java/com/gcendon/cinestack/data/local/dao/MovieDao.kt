package com.gcendon.cinestack.data.local.dao

import androidx.room.*
import com.gcendon.cinestack.data.local.entities.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Flow nos permite "escuchar" la base de datos en tiempo real.
    // Si agregás una peli, la pantalla se actualiza sola.
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: MovieEntity)

    @Delete
    suspend fun deleteFavorite(movie: MovieEntity)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean
}