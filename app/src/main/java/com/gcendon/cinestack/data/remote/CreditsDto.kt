package com.gcendon.cinestack.data.remote

import com.google.gson.annotations.SerializedName

data class CreditsResponse(
    @SerializedName("cast") val cast: List<CastDto>,
    @SerializedName("crew") val crew: List<CrewDto>
)

data class CastDto(
    @SerializedName("name") val name: String
)

data class CrewDto(
    @SerializedName("name") val name: String,
    @SerializedName("job") val job: String
)