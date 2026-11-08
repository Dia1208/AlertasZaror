
package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertaRemoteViewModel : ViewModel() {

    private val repositorio = AlertaRemoteRepository()

    private val _alertas = MutableStateFlow<List<AlertaDTO>>(emptyList())
    val alertas: StateFlow<List<AlertaDTO>> = _alertas

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        viewModelScope.launch {
            try {
                _error.value = null
                _alertas.value = repositorio.listarAlertas()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las alertas"
            }
        }
    }
}
