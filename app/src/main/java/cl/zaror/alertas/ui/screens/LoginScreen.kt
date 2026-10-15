package cl.zaror.alertas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.ui.components.CampoTexto
import cl.zaror.alertas.ui.components.EncabezadoDegradado
import cl.zaror.alertas.viewmodel.AuthViewModel

@Composable
fun LoginScreen(vm: AuthViewModel, onRegistro: () -> Unit, onExito: () -> Unit) {
    val estado by vm.login.collectAsStateWithLifecycle()
    val usuario by vm.usuario.collectAsStateWithLifecycle()

    LaunchedEffect(usuario) { if (usuario != null) onExito() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EncabezadoDegradado(
            titulo = "Alertas Población Zaror",
            subtitulo = "Junta de Vecinos · Cerrillos",
            icono = Icons.Default.NotificationsActive
        )
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Inicia sesión", style = MaterialTheme.typography.titleLarge)
            CampoTexto(
                valor = estado.correo.valor, onCambio = vm::onLoginCorreo, etiqueta = "Correo",
                icono = Icons.Default.Email, error = estado.correo.error, teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = estado.clave.valor, onCambio = vm::onLoginClave, etiqueta = "Contraseña",
                icono = Icons.Default.Lock, error = estado.clave.error, esClave = true
            )

            AnimatedVisibility(visible = estado.mensaje != null) {
                Text(estado.mensaje ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = vm::iniciarSesion,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (estado.cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Ingresar")
            }
            TextButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
