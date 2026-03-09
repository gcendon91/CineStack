package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

data class CreditsResponse(
    @SerializedName("cast") val cast: List<CastDto>,
    @SerializedName("crew") val crew: List<CrewDto>
)

data class CastDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String, // El nombre del personaje
    @SerializedName("profile_path") val profilePath: String? // La foto del actor
)

data class CrewDto(
    @SerializedName("name") val name: String,
    @SerializedName("job") val job: String
)