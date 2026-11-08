package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertaRemoteViewModel(
    private val repositorio: AlertaRemoteRepository = AlertaRemoteRepository()
) : ViewModel() {

    private val _alertas = MutableStateFlow<List<AlertaDTO>>(emptyList())
    val alertas: StateFlow<List<AlertaDTO>> = _alertas.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                _error.value = "No se pudieron cargar las alertas"
            }
        }
    }
}