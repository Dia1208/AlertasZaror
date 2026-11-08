
package cl.zaror.alertas.data.remote

import retrofit2.http.GET

interface AlertaApi {

    @GET("alertas")
    suspend fun listarAlertas(): List<AlertaDTO>

}
