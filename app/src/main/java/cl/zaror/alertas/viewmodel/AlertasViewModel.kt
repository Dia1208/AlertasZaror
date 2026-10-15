package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Alerta
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.domain.Validaciones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class AlertasViewModel(private val repo: Repositorio) : ViewModel() {

    val alertas: StateFlow<List<Alerta>> =
        repo.alertas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun alerta(id: Int): Flow<Alerta?> = repo.alerta(id)

    private val _nueva = MutableStateFlow(NuevaAlertaState())
    val nueva: StateFlow<NuevaAlertaState> = _nueva.asStateFlow()

    fun onTipo(v: String) = _nueva.update { it.copy(tipo = v, tipoError = null) }

    fun onDescripcion(v: String) = _nueva.update {
        it.copy(descripcion = Campo(v, Validaciones.texto(v, "La descripción", 10, 200)))
    }

    /** Se redondea a 3 decimales (~100 m): punto APROXIMADO del sector, protege la privacidad. */
    fun onUbicacion(lat: Double, lon: Double) = _nueva.update {
        it.copy(
            latitud = (lat * 1000).roundToLong() / 1000.0,
            longitud = (lon * 1000).roundToLong() / 1000.0,
            mensajeUbicacion = null
        )
    }

    fun onErrorUbicacion(mensaje: String) = _nueva.update { it.copy(mensajeUbicacion = mensaje) }
    fun onFoto(uri: String?) = _nueva.update { it.copy(fotoUri = uri) }

    fun activar(autor: String) {
        val s = _nueva.value
        val errTipo = Validaciones.seleccion(s.tipo, "un tipo de alerta")
        val errDesc = Validaciones.texto(s.descripcion.valor, "La descripción", 10, 200)
        if (errTipo != null || errDesc != null) {
            _nueva.update { it.copy(tipoError = errTipo, descripcion = it.descripcion.copy(error = errDesc)) }
            return
        }
        viewModelScope.launch {
            repo.insertarAlerta(
                Alerta(
                    tipo = s.tipo,
                    descripcion = s.descripcion.valor.trim(),
                    latitud = s.latitud,
                    longitud = s.longitud,
                    fotoUri = s.fotoUri,
                    autor = autor,
                    fecha = System.currentTimeMillis()
                )
            )
            _nueva.update { it.copy(guardada = true) }
        }
    }

    fun reiniciar() { _nueva.value = NuevaAlertaState() }

    fun cambiarEstado(id: Int, estado: String) {
        viewModelScope.launch { repo.cambiarEstadoAlerta(id, estado) }
    }
}
