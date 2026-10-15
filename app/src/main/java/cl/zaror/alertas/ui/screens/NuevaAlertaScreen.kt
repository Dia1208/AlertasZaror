package cl.zaror.alertas.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.domain.Opciones
import cl.zaror.alertas.recursos.crearUriFoto
import cl.zaror.alertas.recursos.obtenerUbicacion
import cl.zaror.alertas.recursos.tienePermiso
import cl.zaror.alertas.ui.components.CampoTexto
import cl.zaror.alertas.ui.components.SelectorChips
import cl.zaror.alertas.viewmodel.AlertasViewModel
import cl.zaror.alertas.viewmodel.AuthViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaAlertaScreen(vm: AlertasViewModel, auth: AuthViewModel, onVolver: () -> Unit) {
    val e by vm.nueva.collectAsStateWithLifecycle()
    val usuario by auth.usuario.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var uriTemporal by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) { vm.reiniciar() }
    LaunchedEffect(e.guardada) {
        if (e.guardada) {
            Toast.makeText(contexto, "Alerta enviada a los vecinos", Toast.LENGTH_SHORT).show()
            vm.reiniciar()
            onVolver()
        }
    }

    // ----- Cámara -----
    val lanzarCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.onFoto(uriTemporal?.toString())
    }
    val pedirCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            val uri = crearUriFoto(contexto)
            uriTemporal = uri
            lanzarCamara.launch(uri)
        } else {
            Toast.makeText(contexto, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // ----- Ubicación -----
    val pedirUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) obtenerUbicacion(contexto, vm::onUbicacion, vm::onErrorUbicacion)
        else vm.onErrorUbicacion("Permiso de ubicación denegado")
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Activar alarma") },
            navigationIcon = {
                IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            }
        )
    }) { relleno ->
        Column(
            modifier = Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectorChips("Tipo de emergencia", Opciones.tiposAlerta, e.tipo, e.tipoError, vm::onTipo)

            CampoTexto(
                valor = e.descripcion.valor, onCambio = vm::onDescripcion,
                etiqueta = "¿Qué está pasando?", icono = Icons.Default.Description,
                error = e.descripcion.error, minLineas = 3
            )

            // Ubicación
            OutlinedButton(
                onClick = {
                    if (tienePermiso(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        tienePermiso(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
                    ) obtenerUbicacion(contexto, vm::onUbicacion, vm::onErrorUbicacion)
                    else pedirUbicacion.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, null)
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar ubicación aproximada")
            }
            AnimatedVisibility(visible = e.latitud != null && e.longitud != null) {
                Text("Ubicación: ${e.latitud}, ${e.longitud}", color = MaterialTheme.colorScheme.tertiary)
            }
            AnimatedVisibility(visible = e.mensajeUbicacion != null) {
                Text(e.mensajeUbicacion ?: "", color = MaterialTheme.colorScheme.error)
            }

            // Cámara (opcional)
            OutlinedButton(
                onClick = {
                    if (tienePermiso(contexto, Manifest.permission.CAMERA)) {
                        val uri = crearUriFoto(contexto)
                        uriTemporal = uri
                        lanzarCamara.launch(uri)
                    } else pedirCamara.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar foto (opcional)")
            }
            AnimatedVisibility(visible = e.fotoUri != null) {
                Column {
                    AsyncImage(
                        model = e.fotoUri,
                        contentDescription = "Foto de la alerta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                    )
                    TextButton(onClick = { vm.onFoto(null) }) { Text("Quitar foto") }
                }
            }

            Button(
                onClick = { vm.activar(usuario?.nombre ?: "Vecino/a") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.NotificationsActive, null)
                Spacer(Modifier.width(8.dp))
                Text("Enviar alerta")
            }
            Text(
                "Datos ficticios: no se usan datos reales de personas (protocolo de protección de datos).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
