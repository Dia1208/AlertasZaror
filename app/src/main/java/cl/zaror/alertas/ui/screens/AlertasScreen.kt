package cl.zaror.alertas.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.zaror.alertas.data.Alerta
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import cl.zaror.alertas.domain.Estados
import cl.zaror.alertas.ui.components.ChipEstado
import cl.zaror.alertas.ui.components.TarjetaConBorde
import cl.zaror.alertas.ui.components.colorDeTipo
import cl.zaror.alertas.ui.comoFecha
import cl.zaror.alertas.viewmodel.AlertasViewModel
import cl.zaror.alertas.viewmodel.AuthViewModel

fun iconoDeTipo(tipo: String): ImageVector = when (tipo) {
    "Incendio" -> Icons.Default.LocalFireDepartment
    "Robo" -> Icons.Default.Warning
    "Persona sospechosa" -> Icons.Default.Visibility
    "Emergencia médica" -> Icons.Default.MedicalServices
    else -> Icons.Default.NotificationsActive
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    vm: AlertasViewModel,
    auth: AuthViewModel,
    onNueva: () -> Unit,
    onDetalle: (Int) -> Unit,
    onSalir: () -> Unit
) {
    val alertas by vm.alertas.collectAsStateWithLifecycle()
    val usuario by auth.usuario.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas del sector", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { auth.cerrarSesion(); onSalir() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = { BotonAlarma(onNueva) }
    ) { relleno ->
        Column(Modifier.fillMaxSize().padding(relleno)) {
            val activas = alertas.count { it.estado == Estados.ACTIVA }
            TarjetaConBorde(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hola, ${usuario?.nombre ?: ""}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (activas == 0) "Todo tranquilo en el sector" else "$activas alerta(s) activa(s) ahora",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ChipEstado(usuario?.rol ?: "", MaterialTheme.colorScheme.secondary)
                }
            }
            if (alertas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin alertas registradas. Todo tranquilo en el sector.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alertas, key = { it.id }) { a -> TarjetaAlerta(a) { onDetalle(a.id) } }
                }
            }
        }
    }
}

/** Botón con animación de "pulso" infinita para llamar la atención. */
@Composable
private fun BotonAlarma(onClick: () -> Unit) {
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "escala"
    )
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.graphicsLayer { scaleX = escala; scaleY = escala },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.NotificationsActive, null)
        Spacer(Modifier.width(8.dp))
        Text("Activar alarma")
    }
}

@Composable
private fun TarjetaAlerta(a: Alerta, onClick: () -> Unit) {
    val color by animateColorAsState(
        if (a.estado == Estados.ACTIVA) Color(0xFFFFF1EE) else Color(0xFFE8F5E9),
        animationSpec = tween(500), label = "colorEstado"
    )
    val colorTipo = colorDeTipo(a.tipo)
    TarjetaConBorde(
        color = colorTipo,
        modifier = Modifier.clickable(onClick = onClick).animateContentSize(),
        containerColor = color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(colorTipo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(iconoDeTipo(a.tipo), null, Modifier.size(28.dp), tint = colorTipo) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.tipo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(a.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${a.autor} · ${a.fecha.comoFecha()}", style = MaterialTheme.typography.bodySmall)
            }
            ChipEstado(
                a.estado,
                if (a.estado == Estados.ACTIVA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
