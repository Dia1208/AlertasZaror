package cl.zaror.alertas.data.remote

class AlertaRemoteRepository(
    private val api: AlertaApi = RetrofitInstance.api
) {
    suspend fun listarAlertas(): List<AlertaDTO> {
        return api.listarAlertas()
    }
}