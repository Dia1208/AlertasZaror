package cl.zaror.alertas.data.remote

import com.google.gson.annotations.SerializedName

data class DireccionDTO(
    @SerializedName("display_name")
    val nombreCompleto: String
)