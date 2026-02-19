# 🎬 CineStack

Explorador de películas utilizando la API de TMDB. Una implementación funcional y robusta para practicar arquitectura y componentes modernos en Android.

### 🌟 Características principales:
* 🔍 **Buscador Inteligente:** Búsqueda de películas por título en tiempo real con actualización dinámica de la grilla.
* 🎭 **Ficha Técnica Completa:** Visualización detallada de información incluyendo director, elenco principal, duración y géneros.
* ⭐ **Rating Formateado:** Implementación de lógica para redondeo de puntuaciones y visualización clara mediante componentes de Material 3.
* 🏗️ **Arquitectura:** Separación clara de responsabilidades mediante los patrones **MVVM** (Model-View-ViewModel) y **Repositorio**.

### 🛠️ Stack Tecnológico:
* **Kotlin + Coroutines:** Manejo de hilos y asincronismo para el consumo de datos sin bloquear la interfaz.
* **Jetpack Compose:** UI declarativa nativa para una interfaz moderna y fluida.
* **Retrofit:** Cliente para el consumo de la API REST de *The Movie Database* (TMDB).
* **Coil:** Librería para la carga asíncrona y eficiente de pósters e imágenes.
* **ViewModel & State Management:** Gestión de estados de pantalla mediante `StateFlow` (Loading, Success, Error).

---

# 🎬 CineStack (English)

Movie explorer using the TMDB API. A functional and robust implementation for practicing modern Android components and architecture.

### 🌟 Key Features:
* 🔍 **Smart Search:** Instant movie search by title with dynamic grid updates.
* 🎭 **Full Movie Info:** Detailed view including director, main cast, runtime, and genres.
* ⭐ **Formatted Rating:** Custom logic for score rounding and clear visualization using Material 3 components.
* 🏗️ **Architecture:** Clean layer separation using **MVVM** (Model-View-ViewModel) and **Repository** patterns.

### 🛠️ Tech Stack:
* **Kotlin + Coroutines:** Asynchronous programming and thread management.
* **Jetpack Compose:** Native declarative UI for a modern look and feel.
* **Retrofit:** REST API client for data consumption from *The Movie Database* (TMDB).
* **Coil:** Asynchronous image loading and management for movie posters.
* **ViewModel & State Management:** Screen state handling using `StateFlow` (Loading, Success, Error).

---

## 🔧 Configuración / Setup

### 🇦🇷 Español
Para que la app funcione, necesitás una API Key de [The Movie Database (TMDB)](https://www.themoviedb.org/).
1. Cloná el repositorio.
2. Agregá tu clave en `Constants.kt`:
```kotlin
object Constants {
    const val API_KEY = "TU_API_KEY_AQUÍ"
}
```

### 🇺🇸 English
To run this app, you need an API Key from [The Movie Database (TMDB)](https://www.themoviedb.org/).
1. Clone the repository.
2. Add your key in `Constants.kt`:
```kotlin
object Constants {
    const val API_KEY = "YOUR_API_KEY_HERE"
}
```

---

## 🏗️ Estructura del Proyecto / Project Structure

### 🇦🇷 Español
* **`data/`**: Contiene la configuración de la API (Retrofit), los DTOs para la transferencia de datos y la implementación del Repositorio.
* **`domain/`**: Clases de datos (Modelos de dominio) que representan la lógica de negocio limpia de la aplicación.
* **`ui/`**: Archivos de interfaz (Compose), ViewModels para la gestión del estado y configuración de navegación.

### 🇺🇸 English
* **`data/`**: API configuration (Retrofit), DTOs, and Repository implementation.
* **`domain/`**: Data classes (Domain models) representing pure business logic.
* **`ui/`**: UI screens (Compose), ViewModels, and navigation setup.

