package cl.zaror.alertas.data.remote

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

class AlertaRemoteRepositoryTest : StringSpec({

    "devuelve las alertas recibidas desde la API" {
        runTest {
            val api = mockk<AlertaApi>()
            val esperadas = listOf(
                AlertaDTO(
                    id = 1L,
                    titulo = "Alerta de incendio",
                    descripcion = "Humo detectado en el sector",
                    tipo = "Incendio",
                    ubicacion = "Sector norte",
                    estado = "Activa"
                ),
                AlertaDTO(
                    id = 2L,
                    titulo = "Alerta de robo",
                    descripcion = "Robo reportado por un vecino",
                    tipo = "Robo",
                    ubicacion = "Sector sur",
                    estado = "Resuelta"
                )
            )

            coEvery { api.listarAlertas() } returns esperadas
            val repositorio = AlertaRemoteRepository(api)

            val resultado = repositorio.listarAlertas()

            resultado.shouldContainExactly(esperadas)
            coVerify(exactly = 1) { api.listarAlertas() }
        }
    }

    "devuelve una lista vacía cuando no existen alertas" {
        runTest {
            val api = mockk<AlertaApi>()
            coEvery { api.listarAlertas() } returns emptyList()
            val repositorio = AlertaRemoteRepository(api)

            val resultado = repositorio.listarAlertas()

            resultado.shouldBeEmpty()
            coVerify(exactly = 1) { api.listarAlertas() }
        }
    }

    "propaga el error de conexión para que el ViewModel lo gestione" {
        runTest {
            val api = mockk<AlertaApi>()
            coEvery { api.listarAlertas() } throws IOException(
                "Sin conexión con el servidor"
            )
            val repositorio = AlertaRemoteRepository(api)

            shouldThrow<IOException> {
                repositorio.listarAlertas()
            }

            coVerify(exactly = 1) { api.listarAlertas() }
        }
    }
})