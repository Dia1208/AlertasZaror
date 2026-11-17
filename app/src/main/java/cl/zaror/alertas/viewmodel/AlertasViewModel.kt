package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Alerta
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import cl.zaror.alertas.data.remote.GeocodificacionRepository
import cl.zaror.alertas.data.remote.NuevaAlertaRequest
import cl.zaror.alertas.domain.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class AlertasViewModel(
    private val repo: Repositorio,
    private val remoto: AlertaRemoteRepository = AlertaRemoteRepository(),
    private val geocodificacion: GeocodificacionRepository = GeocodificacionRepository()
) : ViewModel() {

    val alertas: StateFlow<List<Alerta>> =
        repo.alertas.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun alerta(id: Int): Flow<Alerta?> = repo.alerta(id)

    private val _nueva = MutableStateFlow(NuevaAlertaState())
    val nueva: StateFlow<NuevaAlertaState> = _nueva.asStateFlow()

    fun onTipo(v: String) {
        _nueva.update { it.copy(tipo = v, tipoError = null, errorEnvio = null) }
    }

    fun onDescripcion(v: String) {
        _nueva.update {
            it.copy(
                descripcion = Campo(
                    valor = v,
                    error = Validaciones.texto(v, "La descripción", 10, 200)
                ),
                errorEnvio = null
            )
        }
    }

    /** Redondea a 3 decimales (~100 m) antes de consultar OpenStreetMap. */
    fun onUbicacion(lat: Double, lon: Double) {
        val latAprox = (lat * 1000).roundToLong() / 1000.0
        val lonAprox = (lon * 1000).roundToLong() / 1000.0

        _nueva.update {
            it.copy(
                latitud = latAprox,
                longitud = lonAprox,
                direccionAproximada = null,
                consultandoDireccion = true,
                mensajeUbicacion = null,
                errorEnvio = null
            )
        }

        viewModelScope.launch {
            val direccion = try {
                geocodificacion.buscarDireccionAproximada(latAprox, lonAprox)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }

            _nueva.update { actual ->
                if (actual.latitud != latAprox || actual.longitud != lonAprox) {
                    actual
                } else {
                    actual.copy(
                        direccionAproximada = direccion ?: (
                            "Coordenadas aproximadas: " + latAprox + ", " + lonAprox
                        ),
                        consultandoDireccion = false,
                        mensajeUbicacion = if (direccion == null) {
                            "No se encontró dirección; se usarán coordenadas aproximadas."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun onErrorUbicacion(mensaje: String) {
        _nueva.update {
            it.copy(mensajeUbicacion = mensaje, consultandoDireccion = false)
        }
    }

    fun onFoto(uri: String?) {
        _nueva.update { it.copy(fotoUri = uri) }
    }

    fun activar(autor: String) {
        val formulario = _nueva.value
        val errorTipo = Validaciones.seleccion(formulario.tipo, "un tipo de alerta")
        val errorDescripcion = Validaciones.texto(
            formulario.descripcion.valor,
            "La descripción",
            10,
            200
        )

        if (errorTipo != null || errorDescripcion != null) {
            _nueva.update {
                it.copy(
                    tipoError = errorTipo,
                    descripcion = it.descripcion.copy(error = errorDescripcion)
                )
            }
            return
        }

        if (formulario.enviando || formulario.consultandoDireccion) return

        viewModelScope.launch {
            _nueva.update { it.copy(enviando = true, errorEnvio = null) }
            try {
                val ubicacion = when {
                    formulario.direccionAproximada != null ->
                        formulario.direccionAproximada
                    formulario.latitud != null && formulario.longitud != null ->
                        "Coordenadas aproximadas: " +
                            formulario.latitud + ", " + formulario.longitud
                    else -> "No especificada"
                }

                val respuesta = remoto.crearAlerta(
                    NuevaAlertaRequest(
                        titulo = formulario.tipo,
                        descripcion = formulario.descripcion.valor.trim(),
                        tipo = formulario.tipo,
                        ubicacion = ubicacion,
                        estado = "Activa"
                    )
                )

                repo.insertarAlerta(
                    Alerta(
                        tipo = respuesta.tipo,
                        descripcion = respuesta.descripcion,
                        latitud = formulario.latitud,
                        longitud = formulario.longitud,
                        fotoUri = formulario.fotoUri,
                        autor = autor,
                        fecha = System.currentTimeMillis(),
                        estado = respuesta.estado
                    )
                )

                _nueva.update {
                    it.copy(enviando = false, guardada = true, errorEnvio = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _nueva.update {
                    it.copy(
                        enviando = false,
                        guardada = false,
                        errorEnvio =
                            "No se pudo enviar la alerta. Revisa la conexión y vuelve a intentarlo."
                    )
                }
            }
        }
    }

    fun reiniciar() {
        _nueva.value = NuevaAlertaState()
    }

    fun cambiarEstado(id: Int, estado: String) {
        viewModelScope.launch {
            repo.cambiarEstadoAlerta(id, estado)
        }
    }
}