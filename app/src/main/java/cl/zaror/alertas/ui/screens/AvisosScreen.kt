package cl.zaror.alertas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.domain.Roles
import cl.zaror.alertas.ui.components.TarjetaConBorde
import cl.zaror.alertas.ui.comoFecha
import cl.zaror.alertas.ui.components.CampoTexto
import cl.zaror.alertas.viewmodel.AuthViewModel
import cl.zaror.alertas.viewmodel.GestionViewModel

@Composable
fun AvisosScreen(gestion: GestionViewModel, auth: AuthViewModel) {
    val avisos by gestion.comunicados.collectAsStateWithLifecycle()
    val form by gestion.aviso.collectAsStateWithLifecycle()
    val usuario by auth.usuario.collectAsStateWithLifecycle()
    val esDirectiva = usuario?.rol == Roles.DIRECTIVA
    var formVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Comunicados de la directiva", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }

        if (esDirectiva) {
            item {
                Button(onClick = { formVisible = !formVisible }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Campaign, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (formVisible) "Cerrar formulario" else "Nuevo comunicado")
                }
            }
            item {
                AnimatedVisibility(visible = formVisible) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoTexto(
                                valor = form.titulo.valor, onCambio = gestion::onTituloAviso,
                                etiqueta = "Título", icono = Icons.Default.Title, error = form.titulo.error
                            )
                            CampoTexto(
                                valor = form.mensaje.valor, onCambio = gestion::onMensajeAviso,
                                etiqueta = "Mensaje", icono = Icons.Default.Description,
                                error = form.mensaje.error, minLineas = 3
                            )
                            Button(
                                onClick = {
                                    gestion.publicarAviso(usuario?.nombre ?: "Directiva") { formVisible = false }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Publicar") }
                        }
                    }
                }
            }
        } else {
            item { Text("Solo la Directiva puede publicar comunicados.", style = MaterialTheme.typography.bodyMedium) }
        }

        if (avisos.isEmpty()) item { Text("No hay comunicados publicados.") }

        items(avisos, key = { it.id }) { c ->
            TarjetaConBorde(color = MaterialTheme.colorScheme.secondary) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(c.mensaje)
                    Text("${c.autor} · ${c.fecha.comoFecha()}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
