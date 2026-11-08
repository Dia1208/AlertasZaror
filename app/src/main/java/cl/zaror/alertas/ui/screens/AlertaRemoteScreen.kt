
package cl.zaror.alertas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.zaror.alertas.viewmodel.AlertaRemoteViewModel

@Composable
fun AlertaRemoteScreen(
    vm: AlertaRemoteViewModel = viewModel()
) {
    val alertas by vm.alertas.collectAsState()
    val error by vm.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Alertas desde MySQL",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { vm.cargarAlertas() }) {
            Text("Actualizar alertas")
        }

        if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn {
            items(alertas) { alerta ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = alerta.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Descripción: ${alerta.descripcion}")
                        Text("Tipo: ${alerta.tipo}")
                        Text("Ubicación: ${alerta.ubicacion}")
                        Text("Estado: ${alerta.estado}")
                    }
                }
            }
        }
    }
}
