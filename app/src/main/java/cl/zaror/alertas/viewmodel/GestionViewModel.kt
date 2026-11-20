package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Comunicado
import cl.zaror.alertas.data.Falla
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.remote.GestionRemoteRepository
import cl.zaror.alertas.data.remote.NuevaFallaRequest
import cl.zaror.alertas.data.remote.NuevoComunicadoRequest
import cl.zaror.alertas.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

/** Maneja fallas del sistema (Encargado/a) y comunicados (Directiva). */
class GestionViewModel(
    private val repo: Repositorio,
    private val remoto: GestionRemoteRepository = GestionRemoteRepository()
) : ViewModel() {

    val fallas: StateFlow<List<Falla>> =
        repo.fallas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val comunicados: StateFlow<List<Comunicado>> =
        repo.comunicados.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun actualizarFallas() {
        viewModelScope.launch {
            try {
                repo.sincronizarFallas(remoto.fallas())
                _error.value = null
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "No se pudieron actualizar las fallas." }
        }
    }

    fun actualizarComunicados() {
        viewModelScope.launch {
            try {
                repo.sincronizarComunicados(remoto.comunicados())
                _error.value = null
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "No se pudieron actualizar los comunicados." }
        }
    }

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
          try {
            val respuesta = remoto.crearFalla(NuevaFallaRequest(s.componente,
                s.detalle.valor.trim(), s.prioridad))
            val copiaGuardada = try {
                repo.insertarFalla(
                    Falla(
                        componente = respuesta.componente,
                        detalle = respuesta.detalle,
                        prioridad = respuesta.prioridad,
                        reportadoPor = respuesta.reportadoPor ?: autor,
                        fecha = respuesta.fecha ?: System.currentTimeMillis(),
                        idRemoto = respuesta.id
                    )
                )
                true
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { false }
            _falla.value = FallaForm()
            _error.value = if (copiaGuardada) null
                else "La falla llegó al servidor. Actualiza para recuperar la copia local."
            alTerminar()
          } catch (e: CancellationException) { throw e }
          catch (_: Exception) { _error.value = "No se pudo enviar la falla al servidor." }
        }
    }

    fun marcarResuelta(falla: Falla, resuelta: Boolean) {
        viewModelScope.launch {
            try {
                falla.idRemoto?.let { remoto.resolverFalla(it, resuelta) }
                repo.cambiarResuelta(falla.id, resuelta)
                _error.value = null
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "No se pudo actualizar la falla." }
        }
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
          try {
            val respuesta = remoto.crearComunicado(NuevoComunicadoRequest(
                s.titulo.valor.trim(), s.mensaje.valor.trim()))
            val copiaGuardada = try {
                repo.insertarComunicado(
                    Comunicado(
                        titulo = respuesta.titulo,
                        mensaje = respuesta.mensaje,
                        autor = respuesta.autor ?: autor,
                        fecha = respuesta.fecha ?: System.currentTimeMillis(),
                        idRemoto = respuesta.id
                    )
                )
                true
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { false }
            _aviso.value = AvisoForm()
            _error.value = if (copiaGuardada) null
                else "El comunicado llegó al servidor. Actualiza para recuperar la copia local."
            alTerminar()
          } catch (e: CancellationException) { throw e }
          catch (_: Exception) { _error.value = "No se pudo publicar el comunicado." }
        }
    }
}
