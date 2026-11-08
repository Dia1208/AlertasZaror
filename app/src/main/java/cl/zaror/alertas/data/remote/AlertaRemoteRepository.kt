
package cl.zaror.alertas.data.remote

class AlertaRemoteRepository {

    suspend fun listarAlertas(): List<AlertaDTO> {
        return RetrofitInstance.api.listarAlertas()
    }

}
