package cl.zaror.alertas.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class FallaDTO(
    val id: Long,
    val componente: String,
    val detalle: String,
    val prioridad: String,
    val reportadoPor: String?,
    val fecha: Long?,
    val resuelta: Boolean
)

data class ComunicadoDTO(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val autor: String?,
    val fecha: Long?
)

data class NuevaFallaRequest(val componente: String, val detalle: String, val prioridad: String)
data class NuevoComunicadoRequest(val titulo: String, val mensaje: String)
data class EstadoFallaRequest(val resuelta: Boolean)

interface GestionApi {
    @GET("fallas") suspend fun listarFallas(): List<FallaDTO>
    @POST("fallas") suspend fun crearFalla(@Body solicitud: NuevaFallaRequest): FallaDTO
    @PUT("fallas/{id}/resuelta")
    suspend fun cambiarEstadoFalla(@Path("id") id: Long, @Body solicitud: EstadoFallaRequest): FallaDTO

    @GET("comunicados") suspend fun listarComunicados(): List<ComunicadoDTO>
    @POST("comunicados") suspend fun crearComunicado(@Body solicitud: NuevoComunicadoRequest): ComunicadoDTO
}
