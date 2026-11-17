package cl.zaror.alertas.data.remote

class AlertaRemoteRepository(
    private val api: AlertaApi = RetrofitInstance.api
) {
    suspend fun listarAlertas(): List<AlertaDTO> =
        api.listarAlertas()

    suspend fun crearAlerta(solicitud: NuevaAlertaRequest): AlertaDTO =
        api.crearAlerta(solicitud)

    suspend fun actualizarAlerta(
        id: Long,
        solicitud: NuevaAlertaRequest
    ): AlertaDTO =
        api.actualizarAlerta(id, solicitud)

    suspend fun eliminarAlerta(id: Long) {
        api.eliminarAlerta(id)
    }
}