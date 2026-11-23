package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.Comunicado
import cl.zaror.alertas.data.Falla
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.remote.GestionRemoteRepository
import cl.zaror.alertas.data.remote.FallaDTO
import cl.zaror.alertas.data.remote.ComunicadoDTO
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GestionViewModelTest : StringSpec({
    "valida una falla antes de guardarla y limpia el formulario al terminar" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<GestionRemoteRepository>()
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                val guardada = slot<Falla>()
                coEvery { repo.insertarFalla(capture(guardada)) } returns Unit
                coEvery { remoto.crearFalla(any()) } returns FallaDTO(11L, "Alumbrado",
                    "Luminaria apagada", "Alta", "Ana", 1000L, false)
                val vm = GestionViewModel(repo, remoto)
                var termino = false
                vm.reportarFalla("Ana") { termino = true }
                vm.falla.value.componenteError shouldBe "Selecciona un componente"
                coVerify(exactly = 0) { repo.insertarFalla(any()) }

                vm.onComponente("Alumbrado")
                vm.onPrioridad("Alta")
                vm.onDetalleFalla("Luminaria apagada")
                vm.reportarFalla("Ana") { termino = true }
                advanceUntilIdle()
                guardada.captured.reportadoPor shouldBe "Ana"
                guardada.captured.detalle shouldBe "Luminaria apagada"
                vm.falla.value shouldBe FallaForm()
                termino shouldBe true
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "publica un comunicado válido y permite marcar una falla resuelta" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<GestionRemoteRepository>()
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                val publicado = slot<Comunicado>()
                coEvery { repo.insertarComunicado(capture(publicado)) } returns Unit
                coEvery { repo.cambiarResuelta(7, true) } returns Unit
                coEvery { remoto.crearComunicado(any()) } returns ComunicadoDTO(12L,
                    "Reunión", "Reunión mañana a las seis", "Directiva", 1000L)
                coEvery { remoto.resolverFalla(13L, true) } returns FallaDTO(13L,
                    "Alumbrado", "Luminaria apagada", "Alta", "Ana", 1000L, true)
                val vm = GestionViewModel(repo, remoto)
                var termino = false
                vm.publicarAviso("Directiva") { termino = true }
                vm.aviso.value.titulo.error shouldBe "El título es obligatorio"
                coVerify(exactly = 0) { repo.insertarComunicado(any()) }

                vm.onTituloAviso("Reunión")
                vm.onMensajeAviso("Reunión mañana a las seis")
                vm.publicarAviso("Directiva") { termino = true }
                vm.marcarResuelta(Falla(id = 7, componente = "Alumbrado",
                    detalle = "Luminaria apagada", prioridad = "Alta", reportadoPor = "Ana",
                    fecha = 1000L, idRemoto = 13L), true)
                advanceUntilIdle()
                publicado.captured.autor shouldBe "Directiva"
                vm.aviso.value shouldBe AvisoForm()
                termino shouldBe true
                coVerify(exactly = 1) { repo.cambiarResuelta(7, true) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "actualiza fallas y comunicados y muestra un error cuando el servidor falla" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<GestionRemoteRepository>()
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                val fallas = listOf(FallaDTO(21L, "Agua", "Corte de agua", "Alta", "Ana", 1L, false))
                val comunicados = listOf(ComunicadoDTO(31L, "Aviso", "Habrá reunión mañana", "Directiva", 2L))
                coEvery { remoto.fallas() } returns fallas
                coEvery { remoto.comunicados() } returns comunicados
                coEvery { repo.sincronizarFallas(fallas) } returns Unit
                coEvery { repo.sincronizarComunicados(comunicados) } returns Unit
                val vm = GestionViewModel(repo, remoto)

                vm.actualizarFallas()
                vm.actualizarComunicados()
                advanceUntilIdle()
                vm.error.value shouldBe null
                coVerify(exactly = 1) { repo.sincronizarFallas(fallas) }
                coVerify(exactly = 1) { repo.sincronizarComunicados(comunicados) }

                coEvery { remoto.fallas() } throws IOException("Sin conexión")
                vm.actualizarFallas()
                advanceUntilIdle()
                vm.error.value shouldBe "No se pudieron actualizar las fallas."
                coEvery { remoto.comunicados() } throws IOException("Sin conexión")
                vm.actualizarComunicados()
                advanceUntilIdle()
                vm.error.value shouldBe "No se pudieron actualizar los comunicados."
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "no marca la falla local si el servidor rechaza el cambio" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<GestionRemoteRepository>()
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                coEvery { remoto.resolverFalla(13L, true) } throws IOException("Sin conexión")
                val vm = GestionViewModel(repo, remoto)
                vm.marcarResuelta(Falla(id = 7, componente = "Alumbrado",
                    detalle = "Luminaria apagada", prioridad = "Alta", reportadoPor = "Ana",
                    fecha = 1000L, idRemoto = 13L), true)
                advanceUntilIdle()
                vm.error.value shouldBe "No se pudo actualizar la falla."
                coVerify(exactly = 0) { repo.cambiarResuelta(any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
