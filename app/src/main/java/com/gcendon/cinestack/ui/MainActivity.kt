package com.gcendon.cinestack.ui

import android.os.Bundle
import android.util.Log // Importamos Log para que sea más profesional que println
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.gcendon.cinestack.data.Constants
import com.gcendon.cinestack.data.remote.RetrofitClient
import com.gcendon.cinestack.ui.screens.HomeScreen
import com.gcendon.cinestack.ui.theme.CineStackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineStackTheme {
                val viewModel = MovieViewModel()
                // El Scaffold ayuda a manejar los espacios (Edge-to-Edge)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Le pasamos el padding al HomeScreen para que no se pegue a los bordes
                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen(viewModel)
                    }
                }
            }
        }
    }
}
