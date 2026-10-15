package cl.zaror.alertas.ui.navegacion

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.zaror.alertas.ui.screens.AlertasScreen
import cl.zaror.alertas.ui.screens.AvisosScreen
import cl.zaror.alertas.ui.screens.DetalleAlertaScreen
import cl.zaror.alertas.ui.screens.FallasScreen
import cl.zaror.alertas.ui.screens.LoginScreen
import cl.zaror.alertas.ui.screens.NuevaAlertaScreen
import cl.zaror.alertas.ui.screens.RegistroScreen
import cl.zaror.alertas.viewmodel.AlertasViewModel
import cl.zaror.alertas.viewmodel.AuthViewModel
import cl.zaror.alertas.viewmodel.GestionViewModel

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val ALERTAS = "alertas"
    const val FALLAS = "fallas"
    const val AVISOS = "avisos"
    const val NUEVA = "nueva"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Int) = "detalle/$id"
}

@Composable
fun AppNavegacion(auth: AuthViewModel, alertasVm: AlertasViewModel, gestionVm: GestionViewModel) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    val pestanas = listOf(
        Triple(Rutas.ALERTAS, "Alertas", Icons.Default.Notifications),
        Triple(Rutas.FALLAS, "Fallas", Icons.Default.Build),
        Triple(Rutas.AVISOS, "Avisos", Icons.Default.Campaign)
    )

    Scaffold(bottomBar = {
        if (pestanas.any { it.first == rutaActual }) {
            NavigationBar {
                pestanas.forEach { (ruta, etiqueta, icono) ->
                    NavigationBarItem(
                        selected = rutaActual == ruta,
                        onClick = {
                            nav.navigate(ruta) {
                                popUpTo(Rutas.ALERTAS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icono, contentDescription = etiqueta) },
                        label = { Text(etiqueta) }
                    )
                }
            }
        }
    }) { relleno ->
        NavHost(
            navController = nav,
            startDestination = Rutas.LOGIN,
            modifier = Modifier.padding(relleno),
            enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it / 8 }) },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    vm = auth,
                    onRegistro = { nav.navigate(Rutas.REGISTRO) },
                    onExito = { nav.navigate(Rutas.ALERTAS) { popUpTo(Rutas.LOGIN) { inclusive = true } } }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(vm = auth, onVolver = { nav.popBackStack() })
            }
            composable(Rutas.ALERTAS) {
                AlertasScreen(
                    vm = alertasVm, auth = auth,
                    onNueva = { nav.navigate(Rutas.NUEVA) },
                    onDetalle = { id -> nav.navigate(Rutas.detalle(id)) },
                    onSalir = { nav.navigate(Rutas.LOGIN) { popUpTo(Rutas.ALERTAS) { inclusive = true } } }
                )
            }
            composable(Rutas.FALLAS) { FallasScreen(gestion = gestionVm, auth = auth) }
            composable(Rutas.AVISOS) { AvisosScreen(gestion = gestionVm, auth = auth) }
            composable(Rutas.NUEVA) {
                NuevaAlertaScreen(vm = alertasVm, auth = auth, onVolver = { nav.popBackStack() })
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetalleAlertaScreen(id = id, vm = alertasVm, auth = auth, onVolver = { nav.popBackStack() })
            }
        }
    }
}
