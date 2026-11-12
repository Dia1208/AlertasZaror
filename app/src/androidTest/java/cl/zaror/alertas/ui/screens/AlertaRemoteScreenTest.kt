package cl.zaror.alertas.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.zaror.alertas.data.remote.AlertaApi
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import cl.zaror.alertas.viewmodel.AlertaRemoteViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AlertaRemoteScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun muestraLaAlertaRecibida() {
        val apiSimulada = object : AlertaApi {
            override suspend fun listarAlertas(): List<AlertaDTO> {
                return listOf(
                    AlertaDTO(
                        id = 1L,
                        titulo = "Incendio en el sector",
                        descripcion = "Humo detectado por un vecino",
                        tipo = "Incendio",
                        ubicacion = "Sector norte",
                        estado = "Activa"
                    )
                )
            }
        }

        mostrarPantalla(apiSimulada)

        composeRule
            .onNodeWithText("Incendio en el sector")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Estado: Activa")
            .assertIsDisplayed()
    }

    @Test
    fun muestraElErrorCuandoFallaLaConexion() {
        val apiSimulada = object : AlertaApi {
            override suspend fun listarAlertas(): List<AlertaDTO> {
                throw IOException("Sin conexión")
            }
        }

        mostrarPantalla(apiSimulada)

        composeRule
            .onNodeWithText("No se pudieron cargar las alertas")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Actualizar alertas")
            .assertIsDisplayed()
    }

    private fun mostrarPantalla(api: AlertaApi) {
        val repositorio = AlertaRemoteRepository(api)

        val fabrica = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass != AlertaRemoteViewModel::class.java) {
                    throw IllegalArgumentException("ViewModel desconocido")
                }

                return AlertaRemoteViewModel(repositorio) as T
            }
        }

        composeRule.setContent {
            val vm: AlertaRemoteViewModel = viewModel(
                factory = fabrica
            )

            MaterialTheme {
                AlertaRemoteScreen(vm = vm)
            }
        }
    }
}