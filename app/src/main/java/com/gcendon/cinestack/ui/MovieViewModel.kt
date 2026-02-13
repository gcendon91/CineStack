package com.gcendon.cinestack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.domain.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MovieUiState {
    object Loading : MovieUiState() // Estado: Cargando...
    data class Success(val movies: List<Movie>) : MovieUiState() // Estado: ¡Acá tenés las pelis!
    data class Error(val message: String) : MovieUiState() // Estado: Explotó algo.
}
class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    // 1. Cambiamos el tipo de dato: de List<Movie> a MovieUiState
    // Empezamos el estado en "Loading" (Cargando) por defecto
    private val _uiState = MutableStateFlow<MovieUiState>(MovieUiState.Loading)
    val uiState: StateFlow<MovieUiState> = _uiState

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            // 2. Antes de pedir los datos, nos aseguramos de estar en estado Loading
            _uiState.value = MovieUiState.Loading

            try {
                val movieList = repository.getPopularMovies()
                // 3. Si todo sale bien, pasamos al estado Success y le "metemos" la lista
                _uiState.value = MovieUiState.Success(movieList)
            } catch (e: Exception) {
                // 4. Si algo falla, pasamos al estado Error con el mensaje
                _uiState.value = MovieUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}