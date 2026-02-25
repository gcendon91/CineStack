package com.gcendon.cinestack

import android.app.Application
import androidx.room.Room
import com.gcendon.cinestack.data.local.AppDatabase

class CineStackApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "cinestack_database" // Nombre físico del archivo en el celular
        ).build()
    }
}