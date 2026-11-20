package cl.zaror.alertas.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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

    private val _errorSincronizacion = MutableStateFlow<String?>(null)
    val errorSincronizacion = _errorSincronizacion.asStateFlow()

    fun actualizar() {
        viewModelScope.launch {
            try {
                repo.sincronizarAlertas(remoto.listarAlertas())
                _errorSincronizacion.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _errorSincronizacion.value = "No se pudieron actualizar las alertas del servidor."
            }
        }
    }

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

    fun onFoto(contexto: Context, uri: Uri) {
        _nueva.update { it.copy(fotoUri = uri.toString(), procesandoFoto = true, errorEnvio = null) }
        viewModelScope.launch {
            try {
                val codificada = withContext(Dispatchers.IO) {
                    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contexto.contentResolver.openInputStream(uri).use { entrada ->
                        BitmapFactory.decodeStream(entrada, null, limites)
                    }
                    val opciones = BitmapFactory.Options().apply {
                        inSampleSize = maxOf(1, maxOf(limites.outWidth, limites.outHeight) / 1024)
                    }
                    contexto.contentResolver.openInputStream(uri).use { entrada ->
                        val original = BitmapFactory.decodeStream(entrada, null, opciones)
                            ?: error("No se pudo leer la fotografía")
                        val escala = minOf(1f, 1024f / maxOf(original.width, original.height))
                        val bitmap = if (escala < 1f) Bitmap.createScaledBitmap(
                            original, (original.width * escala).toInt(),
                            (original.height * escala).toInt(), true
                        ) else original
                        val salida = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, salida)
                        Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
                    }
                }
                _nueva.update {
                    if (it.fotoUri == uri.toString())
                        it.copy(fotoBase64 = codificada, procesandoFoto = false)
                    else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _nueva.update { it.copy(fotoUri = null, fotoBase64 = null,
                    procesandoFoto = false, errorEnvio = "No se pudo preparar la fotografía") }
            }
        }
    }

    fun quitarFoto() {
        _nueva.update { it.copy(fotoUri = null, fotoBase64 = null, procesandoFoto = false) }
    }

    private var copiaPendiente: Alerta? = null

    fun activar(autor: String) {
        val formulario = _nueva.value
        if (formulario.enviando || formulario.guardada || formulario.consultandoDireccion || formulario.procesandoFoto) return
        copiaPendiente?.let { copia ->
            _nueva.update { it.copy(enviando = true) }
            viewModelScope.launch { guardarCopia(copia) }
            return
        }
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

        _nueva.update { it.copy(enviando = true, errorEnvio = null) }

        viewModelScope.launch {
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
                        estado = "Activa",
                        fotoBase64 = formulario.fotoBase64
                    )
                )

                guardarCopia(
                    Alerta(
                        idRemoto = respuesta.id,
                        tipo = respuesta.tipo,
                        descripcion = respuesta.descripcion,
                        latitud = formulario.latitud,
                        longitud = formulario.longitud,
                        fotoUri = formulario.fotoUri,
                        autor = autor,
                        fecha = System.currentTimeMillis(),
                        estado = respuesta.estado,
                        fotoBase64 = respuesta.fotoBase64
                    )
                )

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

    private suspend fun guardarCopia(copia: Alerta) {
        copiaPendiente = copia
        try {
            repo.insertarAlerta(copia)
            copiaPendiente = null
            _nueva.update { it.copy(enviando = false, guardada = true, pendienteLocal = false, errorEnvio = null) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _nueva.update { it.copy(enviando = false, pendienteLocal = true) }
        }
    }

    fun reiniciar() {
        if (copiaPendiente != null || _nueva.value.enviando) return
        _nueva.value = NuevaAlertaState()
    }

    private val _errorEstado = MutableStateFlow<String?>(null)
    val errorEstado = _errorEstado.asStateFlow()

    fun cambiarEstado(alerta: Alerta, estado: String) {
        viewModelScope.launch {
            _errorEstado.value = null
            try {
                val id = alerta.idRemoto
                if (id == null) {
                    repo.cambiarEstadoAlerta(alerta.id, estado)
                } else {
                    val a = remoto.buscarAlerta(id)
                    val respuesta = remoto.actualizarAlerta(id,
                        NuevaAlertaRequest(a.titulo, a.descripcion, a.tipo, a.ubicacion, estado))
                    repo.actualizarRemota(id, respuesta.estado)
            actualizar()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _errorEstado.value = "No se pudo sincronizar el estado de la alerta."
            }
        }
    }
}
