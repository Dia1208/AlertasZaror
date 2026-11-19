package cl.zaror.alertas.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
fun NuevaAlertaScreen(
    vm: AlertasViewModel,
    auth: AuthViewModel,
    onVolver: () -> Unit
) {
    val estado by vm.nueva.collectAsStateWithLifecycle()
    val usuario by auth.usuario.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var uriTemporal by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        vm.reiniciar()
    }

    LaunchedEffect(estado.guardada) {
        if (estado.guardada) {
            Toast.makeText(contexto, "Alerta enviada al servidor", Toast.LENGTH_SHORT).show()
            vm.reiniciar()
            onVolver()
        }
    }

    val lanzarCamara =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok) vm.onFoto(uriTemporal?.toString())
        }

    val pedirCamara =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) {
                val uri = crearUriFoto(contexto)
                uriTemporal = uri
                lanzarCamara.launch(uri)
            } else {
                Toast.makeText(contexto, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }

    val pedirUbicacion =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { resultado ->
            if (resultado.values.any { it }) {
                obtenerUbicacion(contexto, vm::onUbicacion, vm::onErrorUbicacion)
            } else {
                vm.onErrorUbicacion("Permiso de ubicación denegado")
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activar alarma") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.pendienteLocal) {
                Text("La alerta ya llegó al servidor. Falta guardar la copia en este dispositivo.")
                Button(onClick = { vm.activar(usuario?.nombre ?: "Vecino/a") }, enabled = !estado.enviando) {
                    Text("Reintentar guardado local")
                }
                return@Column
            }
            SelectorChips(
                titulo = "Tipo de emergencia",
                opciones = Opciones.tiposAlerta,
                seleccionado = estado.tipo,
                error = estado.tipoError,
                onSeleccion = vm::onTipo
            )

            CampoTexto(
                valor = estado.descripcion.valor,
                onCambio = vm::onDescripcion,
                etiqueta = "¿Qué está pasando?",
                icono = Icons.Default.Description,
                error = estado.descripcion.error,
                minLineas = 3
            )

            OutlinedButton(
                onClick = {
                    if (
                        tienePermiso(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        tienePermiso(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
                    ) {
                        obtenerUbicacion(contexto, vm::onUbicacion, vm::onErrorUbicacion)
                    } else {
                        pedirUbicacion.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar ubicación aproximada")
            }

            AnimatedVisibility(estado.latitud != null && estado.longitud != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (estado.consultandoDireccion) {
                        Text(
                            "Buscando dirección aproximada en OpenStreetMap…",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        CircularProgressIndicator()
                    } else {
                        Text(
                            estado.direccionAproximada
                                ?: "Coordenadas aproximadas disponibles",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Text(
                        "Dirección sugerida por OpenStreetMap; puede ser aproximada. © OpenStreetMap contributors.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            AnimatedVisibility(estado.mensajeUbicacion != null) {
                Text(
                    estado.mensajeUbicacion.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedButton(
                onClick = {
                    if (tienePermiso(contexto, Manifest.permission.CAMERA)) {
                        val uri = crearUriFoto(contexto)
                        uriTemporal = uri
                        lanzarCamara.launch(uri)
                    } else {
                        pedirCamara.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar foto (opcional)")
            }

            AnimatedVisibility(estado.fotoUri != null) {
                Column {
                    AsyncImage(
                        model = estado.fotoUri,
                        contentDescription = "Foto de la alerta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    TextButton(onClick = { vm.onFoto(null) }) {
                        Text("Quitar foto")
                    }
                }
            }

            AnimatedVisibility(estado.errorEnvio != null) {
                Text(
                    text = estado.errorEnvio.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { vm.activar(usuario?.nombre ?: "Vecino/a") },
                enabled = !estado.enviando && !estado.consultandoDireccion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        estado.enviando -> "Enviando alerta…"
                        estado.consultandoDireccion -> "Buscando dirección…"
                        else -> "Enviar alerta"
                    }
                )
            }

            Text(
                "La ubicación aproximada se consulta con OpenStreetMap y se envía junto con la alerta.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
