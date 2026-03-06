package com.gcendon.cinestack.data.remote

// 1. El molde para cada plataforma individual (ej: Netflix)
data class WatchProviderDto(
    val provider_id: Int,
    val provider_name: String,
    val logo_path: String // El nombre de la imagen del logo
)

// 2. El molde para lo que hay dentro de un país
data class WatchProviderCountryDto(
    // "flatrate" significa plataformas de suscripción mensual
    val flatrate: List<WatchProviderDto>? = null
)

// 3. El molde principal que recibe toda la respuesta de la API
data class WatchProviderResponse(
    // Es un Mapa donde la "llave" es el país (AR, MX, US) y el valor es el contenido del país
    val results: Map<String, WatchProviderCountryDto>
)