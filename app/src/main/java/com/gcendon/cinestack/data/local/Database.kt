package com.gcendon.cinestack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gcendon.cinestack.data.local.dao.MovieDao
import com.gcendon.cinestack.data.local.entities.MovieEntity

@Database(entities = [MovieEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}