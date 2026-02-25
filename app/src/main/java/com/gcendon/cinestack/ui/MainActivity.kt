package com.gcendon.cinestack.ui

import android.os.Bundle
import android.util.Log // Importamos Log para que sea más profesional que println
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gcendon.cinestack.data.Constants
import com.gcendon.cinestack.data.remote.RetrofitClient
import com.gcendon.cinestack.ui.screens.DetailScreen
import com.gcendon.cinestack.ui.screens.HomeScreen
import com.gcendon.cinestack.ui.theme.CineStackTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineStackTheme {
                // 1. El NavController es el "Chofer": él sabe cómo ir de A a B.
                val navController = rememberNavController()
                val viewModel: MovieViewModel = viewModel(factory = MovieViewModel.Factory)

                //Observamos en qué pantalla estamos para saber si mostrar la flecha
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Definimos la barra superior
                        TopAppBar(
                            title = { Text("CineStack") },
                            navigationIcon = {
                                // Si la ruta empieza con "detail", mostramos la flecha
                                if (currentRoute?.startsWith("detail") == true) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Volver"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ){ innerPadding ->
                    // 2. El NavHost es el "Mapa": define qué rutas existen.
                    NavHost(
                        navController = navController,
                        startDestination = "home", // Empezamos en la lista
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        // Ruta 1: Home
                        composable("home") {
                            HomeScreen(viewModel = viewModel, onMovieClick = { movieId ->
                                // Cuando tocan una peli, navegamos al detalle pasando el ID
                                navController.navigate("detail/$movieId")
                            })
                        }

                        // Ruta 2: Detalle (fijate cómo definimos el argumento en la URL)
                        composable("detail/{movieId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("movieId")?.toInt() ?: 0
                            DetailScreen(movieId = id)
                        }
                    }
                }
            }
        }
    }
}
