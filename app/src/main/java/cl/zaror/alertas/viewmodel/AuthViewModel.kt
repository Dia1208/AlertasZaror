package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.Usuario
import cl.zaror.alertas.data.remote.ApiSession
import cl.zaror.alertas.data.remote.AuthRemoteRepository
import cl.zaror.alertas.domain.Roles
import cl.zaror.alertas.domain.Validaciones
import cl.zaror.alertas.domain.sha256
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: Repositorio,
    private val remoto: AuthRemoteRepository = AuthRemoteRepository()
) : ViewModel() {

    private val _login = MutableStateFlow(LoginState())
    val login: StateFlow<LoginState> = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroState())
    val registro: StateFlow<RegistroState> = _registro.asStateFlow()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    // ---------- Login ----------
    fun onLoginCorreo(v: String) =
        _login.update {
            it.copy(correo = Campo(v, Validaciones.correo(v)), mensaje = null)
        }

    fun onLoginClave(v: String) =
        _login.update {
            it.copy(clave = Campo(v, Validaciones.claveLogin(v)), mensaje = null)
        }

    fun iniciarSesion() {
        val s = _login.value
        val errCorreo = Validaciones.correo(s.correo.valor)
        val errClave = Validaciones.claveLogin(s.clave.valor)

        if (errCorreo != null || errClave != null) {
            _login.update {
                it.copy(
                    correo = it.correo.copy(error = errCorreo),
                    clave = it.clave.copy(error = errClave)
                )
            }
            return
        }

        viewModelScope.launch {
            _login.update { it.copy(cargando = true) }
            val correo = s.correo.valor.trim().lowercase()
            try {
                    val local = repo.usuarioPorCorreo(correo)
                    val perfil = remoto.autenticar(correo, s.clave.valor, local?.rol)
                    val rolServidor = when (perfil.rol) {
                        "VECINO" -> Roles.VECINO
                        "ENCARGADO" -> Roles.ENCARGADO
                        "DIRECTIVA" -> Roles.DIRECTIVA
                        else -> null
                    }
                    if (rolServidor == null) {
                        _login.update { it.copy(cargando = false, mensaje = "Rol de servidor desconocido") }
                    } else {
                        val usuario = if (local == null) {
                            val nuevo = Usuario(
                                nombre = perfil.nombre?.takeIf { it.isNotBlank() }
                                    ?: correo.substringBefore('@'),
                                correo = correo,
                                telefono = perfil.telefono.orEmpty(),
                                direccion = perfil.direccion.orEmpty(),
                                claveHash = sha256(s.clave.valor), rol = rolServidor
                            )
                            repo.registrar(nuevo)
                            repo.usuarioPorCorreo(correo) ?: error("No se pudo guardar la cuenta local")
                        } else {
                            val actualizado = local.copy(
                                claveHash = sha256(s.clave.valor), rol = rolServidor,
                                nombre = perfil.nombre?.takeIf { it.isNotBlank() } ?: local.nombre,
                                telefono = perfil.telefono ?: local.telefono,
                                direccion = perfil.direccion ?: local.direccion
                            )
                            repo.actualizarUsuario(actualizado)
                            actualizado
                        }
                        ApiSession.establecer(correo, s.clave.valor)
                        _usuario.value = usuario
                        _login.value = LoginState()
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _login.update {
                    it.copy(cargando = false, mensaje = "No se pudo iniciar sesión. Revisa la cuenta y la conexión")
                }
            }
        }
    }

    fun cerrarSesion() {
        ApiSession.limpiar()
        _usuario.value = null
    }

    // ---------- Registro ----------
    fun onNombre(v: String) =
        _registro.update {
            it.copy(nombre = Campo(v, Validaciones.nombre(v)))
        }

    fun onCorreo(v: String) =
        _registro.update {
            it.copy(
                correo = Campo(v, Validaciones.correo(v)),
                mensaje = null
            )
        }

    fun onTelefono(v: String) =
        _registro.update {
            it.copy(telefono = Campo(v, Validaciones.telefono(v)))
        }

    fun onDireccion(v: String) =
        _registro.update {
            it.copy(direccion = Campo(v, Validaciones.direccion(v)))
        }

    fun onClave(v: String) =
        _registro.update {
            it.copy(clave = Campo(v, Validaciones.clave(v)))
        }

    /**
     * Se conserva este método para no romper posibles llamadas existentes,
     * pero el rol elegido se ignora: los registros son siempre Vecino/a.
     */
    @Suppress("UNUSED_PARAMETER")
    fun onRol(v: String) = Unit

    fun registrar() {
        val s = _registro.value
        val errores = listOf(
            Validaciones.nombre(s.nombre.valor),
            Validaciones.correo(s.correo.valor),
            Validaciones.telefono(s.telefono.valor),
            Validaciones.direccion(s.direccion.valor),
            Validaciones.clave(s.clave.valor)
        )

        if (errores.any { it != null }) {
            _registro.update {
                it.copy(
                    nombre = it.nombre.copy(error = errores[0]),
                    correo = it.correo.copy(error = errores[1]),
                    telefono = it.telefono.copy(error = errores[2]),
                    direccion = it.direccion.copy(error = errores[3]),
                    clave = it.clave.copy(error = errores[4])
                )
            }
            return
        }

        viewModelScope.launch {
            _registro.update { it.copy(cargando = true) }
            try {
                val correo = s.correo.valor.trim().lowercase()
                if (repo.correoRegistrado(correo)) {
                    _registro.update { it.copy(cargando = false, mensaje = "Ese correo ya está registrado") }
                    return@launch
                }
                remoto.registrarVecino(correo, s.clave.valor,
                    s.nombre.valor.trim(), s.telefono.valor.trim(), s.direccion.valor.trim())
                val correcto = repo.registrar(
                    Usuario(
                        nombre = s.nombre.valor.trim(),
                        correo = correo,
                        telefono = s.telefono.valor,
                        direccion = s.direccion.valor.trim(),
                        claveHash = sha256(s.clave.valor),
                        rol = Roles.VECINO
                    )
                )
                _registro.update {
                    if (correcto) it.copy(cargando = false, exito = true)
                    else it.copy(cargando = false, mensaje = "Ese correo ya está registrado")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _registro.update {
                    it.copy(cargando = false, mensaje = "No se pudo registrar en el servidor")
                }
            }
        }
    }

    fun limpiarRegistro() {
        _registro.value = RegistroState()
    }
}
