package com.gcendon.cinestack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.domain.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    // El estado de la UI: una lista de películas
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                val movieList = repository.getPopularMovies()
                _movies.value = movieList
            } catch (e: Exception) {
                // Por ahora solo ignoramos el error, luego pondremos un estado de error
            }
        }
    }
}