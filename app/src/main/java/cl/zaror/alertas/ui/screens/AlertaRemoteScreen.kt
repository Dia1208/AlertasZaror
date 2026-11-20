package cl.zaror.alertas.ui.screens

import android.util.Base64
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.viewmodel.AlertaRemoteViewModel

@Composable
fun AlertaRemoteScreen(
    vm: AlertaRemoteViewModel = viewModel(),
    puedeGestionar: Boolean = false
) {
    val alertas by vm.alertas.collectAsState()
    val error by vm.error.collectAsState()
    val procesando by vm.procesando.collectAsState()
    var alertaParaEliminar by remember { mutableStateOf<AlertaDTO?>(null) }

    alertaParaEliminar?.takeIf { puedeGestionar }?.let { alerta ->
        AlertDialog(
            onDismissRequest = { alertaParaEliminar = null },
            title = { Text("Eliminar alerta") },
            text = { Text("¿Quieres eliminar “" + alerta.titulo + "” del servidor?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.eliminarAlerta(alerta.id)
                        alertaParaEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { alertaParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Alertas del microservicio",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { vm.cargarAlertas() },
            enabled = !procesando
        ) {
            Text("Actualizar alertas")
        }

        Text(
            "Direcciones aproximadas: © OpenStreetMap contributors",
            style = MaterialTheme.typography.bodySmall
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        if (procesando) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        if (alertas.isEmpty() && error == null && !procesando) {
            Spacer(Modifier.height(12.dp))
            Text("No hay alertas en el servidor.")
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alertas, key = { it.id }) { alerta ->
                TarjetaAlertaRemota(
                    alerta = alerta,
                    habilitado = !procesando,
                    puedeGestionar = puedeGestionar,
                    onCambiarEstado = { vm.cambiarEstado(alerta) },
                    onEliminar = { alertaParaEliminar = alerta }
                )
            }
        }
    }
}

@Composable
private fun TarjetaAlertaRemota(
    alerta: AlertaDTO,
    habilitado: Boolean,
    puedeGestionar: Boolean,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(alerta.titulo, style = MaterialTheme.typography.titleMedium)
            Text("Descripción: " + alerta.descripcion)
            Text("Tipo: " + alerta.tipo)
            Text("Ubicación: " + alerta.ubicacion)
            Text("Estado: " + alerta.estado)
            alerta.fotoBase64?.let { codificada ->
                val imagen = remember(codificada) {
                    runCatching { Base64.decode(codificada, Base64.DEFAULT) }.getOrNull()
                }
                imagen?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Fotografía de la alerta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            }

            if (puedeGestionar) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCambiarEstado,
                    enabled = habilitado
                ) {
                    Text(
                        if (alerta.estado.equals("Activa", ignoreCase = true)) {
                            "Marcar resuelta"
                        } else {
                            "Reactivar"
                        }
                    )
                }

                TextButton(onClick = onEliminar, enabled = habilitado) {
                    Text("Eliminar")
                }
            }
        }
    }
}
