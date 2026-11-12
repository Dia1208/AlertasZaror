package cl.zaror.alertas.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.ui.components.CampoTexto
import cl.zaror.alertas.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(vm: AuthViewModel, onVolver: () -> Unit) {
    val e by vm.registro.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(e.exito) {
        if (e.exito) {
            Toast.makeText(contexto, "Cuenta creada. Ya puedes ingresar", Toast.LENGTH_SHORT).show()
            vm.limpiarRegistro()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = { vm.limpiarRegistro(); onVolver() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                valor = e.nombre.valor,
                onCambio = vm::onNombre,
                etiqueta = "Nombre completo",
                icono = Icons.Default.Person,
                error = e.nombre.error
            )
            CampoTexto(
                valor = e.correo.valor,
                onCambio = vm::onCorreo,
                etiqueta = "Correo",
                icono = Icons.Default.Email,
                error = e.correo.error,
                teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = e.telefono.valor,
                onCambio = vm::onTelefono,
                etiqueta = "Teléfono (9XXXXXXXX)",
                icono = Icons.Default.Phone,
                error = e.telefono.error,
                teclado = KeyboardType.Phone
            )
            CampoTexto(
                valor = e.direccion.valor,
                onCambio = vm::onDireccion,
                etiqueta = "Dirección en la población",
                icono = Icons.Default.Home,
                error = e.direccion.error
            )
            CampoTexto(
                valor = e.clave.valor,
                onCambio = vm::onClave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                error = e.clave.error,
                esClave = true
            )

            Text(
                "Las cuentas nuevas se registran como Vecino/a.",
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(visible = e.mensaje != null) {
                Text(e.mensaje ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = vm::registrar,
                enabled = !e.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (e.cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrarme")
                }
            }

            Text(
                "Usa datos ficticios: esta es una aplicación académica (MVP).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}