package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import cl.zaror.alertas.data.remote.NuevaAlertaRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertaRemoteViewModel(
    private val repositorio: AlertaRemoteRepository = AlertaRemoteRepository(),
    private val local: cl.zaror.alertas.data.Repositorio? = null
) : ViewModel() {

    private val _alertas = MutableStateFlow<List<AlertaDTO>>(emptyList())
    val alertas: StateFlow<List<AlertaDTO>> = _alertas.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando.asStateFlow()

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        viewModelScope.launch {
            try {
                _error.value = null
                _alertas.value = repositorio.listarAlertas()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las alertas. Revisa la conexión."
            }
        }
    }

    fun cambiarEstado(alerta: AlertaDTO) {
        val nuevoEstado =
            if (alerta.estado.equals("Activa", ignoreCase = true)) {
                "Resuelta"
            } else {
                "Activa"
            }

        val solicitud = NuevaAlertaRequest(
            titulo = alerta.titulo,
            descripcion = alerta.descripcion,
            tipo = alerta.tipo,
            ubicacion = alerta.ubicacion,
            estado = nuevoEstado
        )

        viewModelScope.launch {
            _procesando.value = true
            _error.value = null

            try {
                val respuesta = repositorio.actualizarAlerta(alerta.id, solicitud)
                local?.actualizarRemota(alerta.id, respuesta.estado)
                cargarAlertas()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo actualizar la alerta."
            } finally {
                _procesando.value = false
            }
        }
    }

    fun eliminarAlerta(id: Long) {
        viewModelScope.launch {
            _procesando.value = true
            _error.value = null

            try {
                repositorio.eliminarAlerta(id)
                local?.eliminarRemota(id)
                _alertas.value = _alertas.value.filterNot { it.id == id }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo eliminar la alerta."
            } finally {
                _procesando.value = false
            }
        }
    }
}