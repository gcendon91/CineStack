package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("results")
    val results: List<VideoDto>
)

data class VideoDto(
    @SerializedName("key")
    val key: String,      // El ID de YouTube
    @SerializedName("site")
    val site: String,     // Debe ser "YouTube"
    @SerializedName("type")
    val type: String      // Debe ser "Trailer"
)