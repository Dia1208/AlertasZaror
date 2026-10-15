package cl.zaror.alertas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.zaror.alertas.ui.navegacion.AppNavegacion
import cl.zaror.alertas.ui.theme.ZarorTheme
import cl.zaror.alertas.viewmodel.AlertasViewModel
import cl.zaror.alertas.viewmodel.AuthViewModel
import cl.zaror.alertas.viewmodel.FabricaViewModel
import cl.zaror.alertas.viewmodel.GestionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fabrica = FabricaViewModel((application as ZarorApp).repositorio)
        setContent {
            ZarorTheme {
                val auth: AuthViewModel = viewModel(factory = fabrica)
                val alertas: AlertasViewModel = viewModel(factory = fabrica)
                val gestion: GestionViewModel = viewModel(factory = fabrica)
                AppNavegacion(auth, alertas, gestion)
            }
        }
    }
}
