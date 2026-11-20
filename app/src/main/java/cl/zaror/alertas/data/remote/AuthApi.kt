package cl.zaror.alertas.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class RegistroRemotoRequest(
    val correo: String, val clave: String,
    val nombre: String? = null, val telefono: String? = null, val direccion: String? = null
)
data class PerfilRemoto(
    val correo: String, val rol: String,
    val nombre: String? = null, val telefono: String? = null, val direccion: String? = null
)

interface AuthApi {
    @POST("auth/register")
    suspend fun registrar(@Body solicitud: RegistroRemotoRequest): PerfilRemoto

    @GET("auth/me")
    suspend fun perfil(@Header("Authorization") autorizacion: String): PerfilRemoto
}
