package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.Usuario
import cl.zaror.alertas.domain.Validaciones
import cl.zaror.alertas.domain.sha256
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: Repositorio) : ViewModel() {

    private val _login = MutableStateFlow(LoginState())
    val login: StateFlow<LoginState> = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroState())
    val registro: StateFlow<RegistroState> = _registro.asStateFlow()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    // ---------- Login ----------
    fun onLoginCorreo(v: String) =
        _login.update { it.copy(correo = Campo(v, Validaciones.correo(v)), mensaje = null) }

    fun onLoginClave(v: String) =
        _login.update { it.copy(clave = Campo(v, Validaciones.claveLogin(v)), mensaje = null) }

    fun iniciarSesion() {
        val s = _login.value
        val errCorreo = Validaciones.correo(s.correo.valor)
        val errClave = Validaciones.claveLogin(s.clave.valor)
        if (errCorreo != null || errClave != null) {
            _login.update {
                it.copy(correo = it.correo.copy(error = errCorreo), clave = it.clave.copy(error = errClave))
            }
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(cargando = true) }
            val u = repo.iniciarSesion(s.correo.valor.trim().lowercase(), sha256(s.clave.valor))
            if (u == null) {
                _login.update { it.copy(cargando = false, mensaje = "Correo o contraseña incorrectos") }
            } else {
                _usuario.value = u
                _login.value = LoginState()
            }
        }
    }

    fun cerrarSesion() { _usuario.value = null }

    // ---------- Registro ----------
    fun onNombre(v: String) = _registro.update { it.copy(nombre = Campo(v, Validaciones.nombre(v))) }
    fun onCorreo(v: String) = _registro.update { it.copy(correo = Campo(v, Validaciones.correo(v)), mensaje = null) }
    fun onTelefono(v: String) = _registro.update { it.copy(telefono = Campo(v, Validaciones.telefono(v))) }
    fun onDireccion(v: String) = _registro.update { it.copy(direccion = Campo(v, Validaciones.direccion(v))) }
    fun onClave(v: String) = _registro.update { it.copy(clave = Campo(v, Validaciones.clave(v))) }
    fun onRol(v: String) = _registro.update { it.copy(rol = v) }

    fun registrar() {
        val s = _registro.value
        val e = listOf(
            Validaciones.nombre(s.nombre.valor),
            Validaciones.correo(s.correo.valor),
            Validaciones.telefono(s.telefono.valor),
            Validaciones.direccion(s.direccion.valor),
            Validaciones.clave(s.clave.valor)
        )
        if (e.any { it != null }) {
            _registro.update {
                it.copy(
                    nombre = it.nombre.copy(error = e[0]),
                    correo = it.correo.copy(error = e[1]),
                    telefono = it.telefono.copy(error = e[2]),
                    direccion = it.direccion.copy(error = e[3]),
                    clave = it.clave.copy(error = e[4])
                )
            }
            return
        }
        viewModelScope.launch {
            _registro.update { it.copy(cargando = true) }
            val ok = repo.registrar(
                Usuario(
                    nombre = s.nombre.valor.trim(),
                    correo = s.correo.valor.trim().lowercase(),
                    telefono = s.telefono.valor,
                    direccion = s.direccion.valor.trim(),
                    claveHash = sha256(s.clave.valor),
                    rol = s.rol
                )
            )
            _registro.update {
                if (ok) it.copy(cargando = false, exito = true)
                else it.copy(cargando = false, mensaje = "Ese correo ya está registrado")
            }
        }
    }

    fun limpiarRegistro() { _registro.value = RegistroState() }
}
