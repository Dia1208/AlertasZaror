package cl.zaror.alertas.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlertaApi {

    @GET("alertas")
    suspend fun listarAlertas(): List<AlertaDTO>

    @GET("alertas/{id}")
    suspend fun buscarAlerta(@Path("id") id: Long): AlertaDTO

    @POST("alertas")
    suspend fun crearAlerta(
        @Body solicitud: NuevaAlertaRequest
    ): AlertaDTO

    @PUT("alertas/{id}")
    suspend fun actualizarAlerta(
        @Path("id") id: Long,
        @Body solicitud: NuevaAlertaRequest
    ): AlertaDTO

    @DELETE("alertas/{id}")
    suspend fun eliminarAlerta(
        @Path("id") id: Long
    )
}