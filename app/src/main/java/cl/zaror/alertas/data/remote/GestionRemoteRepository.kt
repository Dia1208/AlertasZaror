package cl.zaror.alertas.data.remote

class GestionRemoteRepository(private val api: GestionApi = RetrofitInstance.gestionApi) {
    suspend fun fallas() = api.listarFallas()
    suspend fun comunicados() = api.listarComunicados()
    suspend fun crearFalla(solicitud: NuevaFallaRequest) = api.crearFalla(solicitud)
    suspend fun resolverFalla(id: Long, resuelta: Boolean) =
        api.cambiarEstadoFalla(id, EstadoFallaRequest(resuelta))
    suspend fun crearComunicado(solicitud: NuevoComunicadoRequest) = api.crearComunicado(solicitud)
}
