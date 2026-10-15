package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Comunicado
import cl.zaror.alertas.data.Falla
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Maneja fallas del sistema (Encargado/a) y comunicados (Directiva). */
class GestionViewModel(private val repo: Repositorio) : ViewModel() {

    val fallas: StateFlow<List<Falla>> =
        repo.fallas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val comunicados: StateFlow<List<Comunicado>> =
        repo.comunicados.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Fallas ----------
    private val _falla = MutableStateFlow(FallaForm())
    val falla: StateFlow<FallaForm> = _falla.asStateFlow()

    fun onComponente(v: String) = _falla.update { it.copy(componente = v, componenteError = null) }
    fun onPrioridad(v: String) = _falla.update { it.copy(prioridad = v, prioridadError = null) }
    fun onDetalleFalla(v: String) = _falla.update {
        it.copy(detalle = Campo(v, Validaciones.texto(v, "El detalle", 10, 200)))
    }

    fun reportarFalla(autor: String, alTerminar: () -> Unit) {
        val s = _falla.value
        val ec = Validaciones.seleccion(s.componente, "un componente")
        val ep = Validaciones.seleccion(s.prioridad, "una prioridad")
        val ed = Validaciones.texto(s.detalle.valor, "El detalle", 10, 200)
        if (ec != null || ep != null || ed != null) {
            _falla.update {
                it.copy(componenteError = ec, prioridadError = ep, detalle = it.detalle.copy(error = ed))
            }
            return
        }
        viewModelScope.launch {
            repo.insertarFalla(
                Falla(
                    componente = s.componente,
                    detalle = s.detalle.valor.trim(),
                    prioridad = s.prioridad,
                    reportadoPor = autor,
                    fecha = System.currentTimeMillis()
                )
            )
            _falla.value = FallaForm()
            alTerminar()
        }
    }

    fun marcarResuelta(id: Int, resuelta: Boolean) {
        viewModelScope.launch { repo.cambiarResuelta(id, resuelta) }
    }

    // ---------- Comunicados ----------
    private val _aviso = MutableStateFlow(AvisoForm())
    val aviso: StateFlow<AvisoForm> = _aviso.asStateFlow()

    fun onTituloAviso(v: String) = _aviso.update {
        it.copy(titulo = Campo(v, Validaciones.texto(v, "El título", 5, 60)))
    }

    fun onMensajeAviso(v: String) = _aviso.update {
        it.copy(mensaje = Campo(v, Validaciones.texto(v, "El mensaje", 10, 300)))
    }

    fun publicarAviso(autor: String, alTerminar: () -> Unit) {
        val s = _aviso.value
        val et = Validaciones.texto(s.titulo.valor, "El título", 5, 60)
        val em = Validaciones.texto(s.mensaje.valor, "El mensaje", 10, 300)
        if (et != null || em != null) {
            _aviso.update { it.copy(titulo = it.titulo.copy(error = et), mensaje = it.mensaje.copy(error = em)) }
            return
        }
        viewModelScope.launch {
            repo.insertarComunicado(
                Comunicado(
                    titulo = s.titulo.valor.trim(),
                    mensaje = s.mensaje.valor.trim(),
                    autor = autor,
                    fecha = System.currentTimeMillis()
                )
            )
            _aviso.value = AvisoForm()
            alTerminar()
        }
    }
}
