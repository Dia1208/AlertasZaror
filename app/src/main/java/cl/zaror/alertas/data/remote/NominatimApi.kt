package cl.zaror.alertas.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApi {
    @Headers(
        "User-Agent: AlertasZaror/1.0 (Android; uso academico)",
        "Accept-Language: es"
    )
    @GET("reverse")
    suspend fun buscarDireccion(
        @Query("format") formato: String,
        @Query("lat") latitud: Double,
        @Query("lon") longitud: Double,
        @Query("zoom") nivelDetalle: Int,
        @Query("addressdetails") incluirDetalles: Int
    ): DireccionDTO
}