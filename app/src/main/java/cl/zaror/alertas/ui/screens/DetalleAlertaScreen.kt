package cl.zaror.alertas.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import cl.zaror.alertas.ui.components.ChipEstado
import cl.zaror.alertas.ui.components.colorDeTipo
import cl.zaror.alertas.domain.Estados
import cl.zaror.alertas.domain.Roles
import cl.zaror.alertas.ui.comoFecha
import cl.zaror.alertas.viewmodel.AlertasViewModel
import cl.zaror.alertas.viewmodel.AuthViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAlertaScreen(id: Int, vm: AlertasViewModel, auth: AuthViewModel, onVolver: () -> Unit) {
    val alerta by remember(id) { vm.alerta(id) }.collectAsStateWithLifecycle(initialValue = null)
    val usuario by auth.usuario.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Detalle de la alerta") },
            navigationIcon = {
                IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            }
        )
    }) { relleno ->
        val a = alerta
        if (a == null) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(32.dp))
            }
        } else {
            val color by animateColorAsState(
                if (a.estado == Estados.ACTIVA) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer,
                animationSpec = tween(600), label = "colorDetalle"
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val colorTipo = colorDeTipo(a.tipo)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(colorTipo, colorTipo.copy(alpha = 0.7f))))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(iconoDeTipo(a.tipo), null, Modifier.size(48.dp), tint = Color.White)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(a.tipo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(a.fecha.comoFecha(), color = Color.White.copy(alpha = 0.9f))
                    }
                    ChipEstado(a.estado, Color.White)
                }
                Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(a.tipo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Estado: ${a.estado}", style = MaterialTheme.typography.titleMedium)
                        Text(a.descripcion)
                        Text("Activada por: ${a.autor}")
                        Text("Fecha: ${a.fecha.comoFecha()}")
                        Text(
                            if (a.latitud != null) "Ubicación aprox.: ${a.latitud}, ${a.longitud}"
                            else "Ubicación: no adjuntada"
                        )
                    }
                }
                if (a.fotoUri != null) {
                    AsyncImage(
                        model = a.fotoUri,
                        contentDescription = "Evidencia fotográfica",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
                val puedeResolver = usuario?.rol == Roles.ENCARGADO || usuario?.rol == Roles.DIRECTIVA
                if (puedeResolver && a.estado == Estados.ACTIVA) {
                    Button(
                        onClick = { vm.cambiarEstado(a.id, Estados.RESUELTA) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Marcar como resuelta") }
                }
            }
        }
    }
}
