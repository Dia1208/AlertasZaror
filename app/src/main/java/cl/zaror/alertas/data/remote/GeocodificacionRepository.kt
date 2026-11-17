package cl.zaror.alertas.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GeocodificacionRepository(
    private val api: NominatimApi = NominatimRetrofit.api
) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, String>()
    private var ultimaSolicitudMs = 0L

    suspend fun buscarDireccionAproximada(
        latitud: Double,
        longitud: Double
    ): String = mutex.withLock {
        val clave = latitud.toString() + "," + longitud.toString()
        cache[clave]?.let { return@withLock it }

        val ahora = System.currentTimeMillis()
        val esperaMs = 1_000L - (ahora - ultimaSolicitudMs)
        if (ultimaSolicitudMs > 0L && esperaMs > 0L) {
            delay(esperaMs)
        }

        ultimaSolicitudMs = System.currentTimeMillis()
        val respuesta = api.buscarDireccion(
            formato = "jsonv2",
            latitud = latitud,
            longitud = longitud,
            nivelDetalle = 14,
            incluirDetalles = 1
        )

        respuesta.nombreCompleto
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.also { cache[clave] = it }
            ?: throw IllegalStateException("La API no devolvió una dirección")
    }
}