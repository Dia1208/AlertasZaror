package cl.zaror.alertas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.domain.Opciones
import cl.zaror.alertas.ui.components.ChipEstado
import cl.zaror.alertas.ui.components.TarjetaConBorde
import cl.zaror.alertas.ui.components.colorDePrioridad
import cl.zaror.alertas.domain.Roles
import cl.zaror.alertas.ui.comoFecha
import cl.zaror.alertas.ui.components.CampoTexto
import cl.zaror.alertas.ui.components.SelectorChips
import cl.zaror.alertas.viewmodel.AuthViewModel
import cl.zaror.alertas.viewmodel.GestionViewModel

@Composable
fun FallasScreen(gestion: GestionViewModel, auth: AuthViewModel) {
    val fallas by gestion.fallas.collectAsStateWithLifecycle()
    val form by gestion.falla.collectAsStateWithLifecycle()
    val usuario by auth.usuario.collectAsStateWithLifecycle()
    val esEncargado = usuario?.rol == Roles.ENCARGADO
    var formVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Fallas del sistema", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }

        if (esEncargado) {
            item {
                Button(onClick = { formVisible = !formVisible }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Build, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (formVisible) "Cerrar formulario" else "Reportar falla")
                }
            }
            item {
                AnimatedVisibility(visible = formVisible) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectorChips("Componente", Opciones.componentes, form.componente, form.componenteError, gestion::onComponente)
                            SelectorChips("Prioridad", Opciones.prioridades, form.prioridad, form.prioridadError, gestion::onPrioridad)
                            CampoTexto(
                                valor = form.detalle.valor, onCambio = gestion::onDetalleFalla,
                                etiqueta = "Detalle de la falla", icono = Icons.Default.Description,
                                error = form.detalle.error, minLineas = 3
                            )
                            Button(
                                onClick = {
                                    gestion.reportarFalla(usuario?.nombre ?: "Encargado/a") { formVisible = false }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Guardar reporte") }
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    "Solo el/la Encargado/a puede reportar fallas. Aquí puedes ver su estado.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (fallas.isEmpty()) item { Text("No hay fallas registradas.") }

        items(fallas, key = { it.id }) { f ->
            TarjetaConBorde(color = colorDePrioridad(f.prioridad)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${f.componente} · Prioridad ${f.prioridad}", fontWeight = FontWeight.Bold)
                        Text(f.detalle)
                        Text("${f.reportadoPor} · ${f.fecha.comoFecha()}", style = MaterialTheme.typography.bodySmall)
                        ChipEstado(if (f.resuelta) "Resuelta" else "Pendiente", if (f.resuelta) MaterialTheme.colorScheme.tertiary else colorDePrioridad(f.prioridad))
                    }
                    if (esEncargado) {
                        Switch(checked = f.resuelta, onCheckedChange = { gestion.marcarResuelta(f.id, it) })
                    }
                }
            }
        }
    }
}
