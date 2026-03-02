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
import com.gcendon.cinestack.data.local.entities.MovieEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class MovieUiState {
    object Loading : MovieUiState() // Estado: Cargando...
    data class Success(val movies: List<Movie>) : MovieUiState() // Estado: ¡Acá tenés las pelis!
    data class Error(val message: String) : MovieUiState() // Estado: Explotó algo.
}

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // Variables para controlar la paginacion
    private var currentPage = 1
    private var isFetching = false
    private val allMoviesList = mutableListOf<Movie>()
    private val _currentGenreId = MutableStateFlow<Int?>(null)
    val currentGenreId: StateFlow<Int?> = _currentGenreId

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

    // Convertimos el Flow de Room en un StateFlow que la UI pueda entender
    val favoriteMovies: StateFlow<List<Movie>> = repository.getFavorites()
        .map { entities ->
            // Convertimos la lista de 'MovieEntity' de la DB a 'Movie' de la UI
            entities.map { it.toDomain() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _similarMovies = MutableStateFlow<List<Movie>>(emptyList())
    val similarMovies: StateFlow<List<Movie>> = _similarMovies

    //tema oscuro por defecto
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    init {
        fetchMoviesByCategory("popular", isNewCategory = true)
        fetchGenres()
    }

    // Función que llama el Home cuando el usuario escribe
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            fetchMoviesByCategory(_selectedCategory.value, isNewCategory = true)
        } else {
            _selectedCategory.value = ""
            // IMPORTANTE: Al empezar una búsqueda nueva, reseteamos la página
            currentPage = 1
            searchJob = viewModelScope.launch {
                delay(500)
                searchMovies(newQuery)
            }
        }
    }

    // Función para cuando el usuario toca un Chip/Botón
    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
        fetchMoviesByCategory(category, isNewCategory = true)
    }

    fun fetchMoviesByCategory(category: String, isNewCategory: Boolean = false) {
        if (isFetching) return // Si ya estamos cargando, no hacemos nada

        viewModelScope.launch {
            if (isNewCategory) {
                currentPage = 1
                allMoviesList.clear()
                _uiState.value = MovieUiState.Loading
            }

            isFetching = true
            try {
                // Pedimos la página actual al repositorio
                val newMovies = repository.getMoviesByCategory(category, currentPage)

                // AGREGAMOS las nuevas a la lista que ya teníamos
                allMoviesList.addAll(newMovies)

                // Emitimos una COPIA de la lista completa (toList() es clave para que Compose detecte el cambio)
                _uiState.value = MovieUiState.Success(allMoviesList.toList())

                // Preparamos la página para la próxima vez
                currentPage++
            } catch (e: Exception) {
                // Solo mostramos error si es la primera página.
                // Si falló la página 5, mejor no romper la pantalla y dejar lo que ya había.
                if (currentPage == 1) {
                    _uiState.value = MovieUiState.Error(e.message ?: "Error de conexión")
                }
            } finally {
                isFetching = false
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
        _searchQuery.value = ""
        _selectedCategory.value = "" // Limpiamos categoría para saber que manda el género
        _currentGenreId.value = genreId     // Guardamos el género actual

        // RESETEAMOS para la nueva lista de género
        currentPage = 1
        allMoviesList.clear()
        _uiState.value = MovieUiState.Loading

        fetchMoviesByGenre(genreId)
    }
    // Creamos esta función de apoyo para manejar la carga (Igual que la de categorías)
    private fun fetchMoviesByGenre(genreId: Int) {
        if (isFetching) return

        viewModelScope.launch {
            isFetching = true
            try {
                // Pasamos el genreId Y la página actual
                val newMovies = repository.getMoviesByGenre(genreId, currentPage)

                allMoviesList.addAll(newMovies)
                _uiState.value = MovieUiState.Success(allMoviesList.toList())

                currentPage++
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _uiState.value = MovieUiState.Error("No pudimos filtrar por ese género")
                }
            } finally {
                isFetching = false
            }
        }
    }

    // Nueva función privada para buscar
    private fun searchMovies(query: String) {
        if (isFetching) return

        viewModelScope.launch {
            // Si es la primera vez que buscamos este texto, reseteamos todo
            if (currentPage == 1) {
                allMoviesList.clear()
                // No ponemos Loading para que no parpadee, pero limpiamos la lista
            }

            isFetching = true
            try {
                // Pasamos la query Y la página actual
                val results = repository.searchMovies(query, currentPage)

                allMoviesList.addAll(results)
                _uiState.value = MovieUiState.Success(allMoviesList.toList())

                currentPage++
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _uiState.value = MovieUiState.Error("Error al buscar: ${e.message}")
                }
            } finally {
                isFetching = false
            }
        }
    }

    fun fetchSimilarMovies(movieId: Int) {
        viewModelScope.launch {
            try {
                // Simplemente le pasamos la página 1
                val movies = repository.getSimilarMovies(movieId, page = 1)
                _similarMovies.value = movies
            } catch (e: Exception) {
                _similarMovies.value = emptyList()
            }
        }
    }
    fun loadNextPage() {
        val query = searchQuery.value
        val category = selectedCategory.value
        val genreId = currentGenreId.value

        if (query.isNotEmpty()) {
            // Si el usuario está buscando algo, cargamos más resultados de esa búsqueda
            searchMovies(query)
        } else if (category.isNotEmpty()) {
            // Si no hay búsqueda pero hay categoría (Popular, etc)
            fetchMoviesByCategory(category)
        } else if (genreId != null) {
            // Si no hay nada de lo anterior pero hay un género elegido
            fetchMoviesByGenre(genreId)
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

    // El "traductor" de Movie (UI) a MovieEntity (Base de Datos), es para guardar
    private fun Movie.toEntity() = com.gcendon.cinestack.data.local.entities.MovieEntity(
        id = this.id,
        title = this.title,
        posterUrl = this.posterUrl,
        rating = this.rating,
        releaseDate = ""
    )

    // con este leo las movies favoritas guardadas
    private fun MovieEntity.toDomain(): Movie {
        return Movie(
            id = this.id,
            title = this.title,
            posterUrl = this.posterUrl,
            rating = this.rating,
            // Los campos que no guardamos en la DB los ponemos por defecto
            duration = 0,
            genres = "",
            overview = "",
            director = "",
            cast = ""
        )
    }

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