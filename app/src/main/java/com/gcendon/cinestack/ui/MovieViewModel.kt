package com.gcendon.cinestack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcendon.cinestack.data.MovieRepository
import com.gcendon.cinestack.domain.Genre
import com.gcendon.cinestack.domain.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gcendon.cinestack.CineStackApp

sealed class MovieUiState {
    object Loading : MovieUiState() // Estado: Cargando...
    data class Success(val movies: List<Movie>) : MovieUiState() // Estado: ¡Acá tenés las pelis!
    data class Error(val message: String) : MovieUiState() // Estado: Explotó algo.
}

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // 1. Cambiamos el tipo de dato: de List<Movie> a MovieUiState
    // Empezamos el estado en "Loading" (Cargando) por defecto
    private val _uiState = MutableStateFlow<MovieUiState>(MovieUiState.Loading)
    val uiState: StateFlow<MovieUiState> = _uiState

    // nueva variable para el texto que escribe el usuario
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Guardamos qué categoría está seleccionada (por defecto "popular")
    private val _selectedCategory = MutableStateFlow("popular")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // Creamos un "trabajo" (Job) para la búsqueda
    private var searchJob: Job? = null

    // Lista de géneros que se cargará una sola vez
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    init {
        fetchMoviesByCategory("popular")
        fetchGenres()
    }

    // Función que llama el Home cuando el usuario escribe
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery

        // Cada vez que el usuario escribe, cancelamos el pedido anterior
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            // En lugar de ir siempre a populares, vamos a la que estaba elegida
            fetchMoviesByCategory(_selectedCategory.value)
        } else {
            _selectedCategory.value = ""
            searchJob = viewModelScope.launch {
                delay(500) // El "reloj" que evita llamadas innecesarias
                searchMovies(newQuery)
            }
        }
    }

    // Función para cuando el usuario toca un Chip/Botón
    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = "" // Si elijo categoría, limpio el buscador
        fetchMoviesByCategory(category)
    }

    private fun fetchMoviesByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val movies = repository.getMoviesByCategory(category)
                _uiState.value = MovieUiState.Success(movies)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val genreList = repository.getGenres()
                _genres.value = genreList
            } catch (e: Exception) {
                // No cambiamos el uiState a Error aquí para no bloquear
                // toda la app si solo fallan los nombres de los géneros.
            }
        }
    }

    fun onGenreSelected(genreId: Int) {
        _searchQuery.value = "" // Si filtro por género, limpio el buscador

        _selectedCategory.value = ""
        viewModelScope.launch {
            _uiState.value = MovieUiState.Loading
            try {
                val movies = repository.getMoviesByGenre(genreId)
                _uiState.value = MovieUiState.Success(movies)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error("No pudimos filtrar por ese género")
            }
        }
    }

    // Nueva función privada para buscar
    private fun searchMovies(query: String) {
        viewModelScope.launch {
            // No ponemos Loading acá para que la pantalla no parpadee tanto al escribir
            try {
                val results = repository.searchMovies(query)
                _uiState.value = MovieUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = MovieUiState.Error("Error al buscar: ${e.message}")
            }
        }
    }

    suspend fun getMovieDetail(id: Int) = repository.getMovieById(id)
    suspend fun getTrailer(id: Int) = repository.getMovieTrailerKey(id)
    suspend fun isMovieFavorite(movieId: Int): Boolean {
        return repository.isFavorite(movieId)
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            if (repository.isFavorite(movie.id)) {
                repository.removeFavorite(movie.toEntity())
            } else {
                repository.addFavorite(movie.toEntity())
            }
        }
    }

    // El "traductor" de Movie (UI) a MovieEntity (Base de Datos)
    private fun Movie.toEntity() = com.gcendon.cinestack.data.local.entities.MovieEntity(
        id = this.id,
        title = this.title,
        posterUrl = this.posterUrl,
        rating = this.rating,
        releaseDate = ""
    )
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // 1. Buscamos la instancia de la App (CineStackApp)
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CineStackApp

                // 2. Usamos el DAO de la base de datos que ya vive en la App
                val dao = application.database.movieDao()

                // 3. Creamos el Repositorio pasándole ese DAO
                val repository = MovieRepository(dao)

                // 4. Devolvemos el ViewModel listo para usar
                return MovieViewModel(repository) as T
            }
        }
    }
}