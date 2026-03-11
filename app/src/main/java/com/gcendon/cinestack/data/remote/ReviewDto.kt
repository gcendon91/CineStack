package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    @SerializedName("results") val results: List<ReviewDto>
)

data class ReviewDto(
    @SerializedName("author") val author: String,
    @SerializedName("content") val content: String,
    @SerializedName("url") val url: String, // Por si queremos un link a la reseña completa
    @SerializedName("author_details") val authorDetails: AuthorDetailsDto
)

data class AuthorDetailsDto(
    @SerializedName("rating") val rating: Float? // Algunas reseñas tienen puntaje, otras no
)