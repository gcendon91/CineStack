package com.gcendon.cinestack.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos la instancia de DataStore (solo una vez en el archivo)
private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeManager(private val context: Context) {

    // Definimos la llave para el tema (es como el nombre de la variable en el archivo)
    private val THEME_KEY = booleanPreferencesKey("is_dark_theme")

    // 1. FUNCIÓN PARA LEER: Nos devuelve un Flow que nos dice si es oscuro o no
    // Por defecto ponemos 'true' si el archivo está vacío
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: true
    }

    // 2. FUNCIÓN PARA GUARDAR: Cambia el valor en el disco
    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }
}