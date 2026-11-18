package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.Comunicado
import cl.zaror.alertas.data.Falla
import cl.zaror.alertas.data.Repositorio
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

@OptIn(ExperimentalCoroutinesApi::class)
class GestionViewModelTest : StringSpec({
    "valida una falla antes de guardarla y limpia el formulario al terminar" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                val guardada = slot<Falla>()
                coEvery { repo.insertarFalla(capture(guardada)) } returns Unit
                val vm = GestionViewModel(repo)
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
                every { repo.fallas } returns flowOf(emptyList())
                every { repo.comunicados } returns flowOf(emptyList())
                val publicado = slot<Comunicado>()
                coEvery { repo.insertarComunicado(capture(publicado)) } returns Unit
                coEvery { repo.cambiarResuelta(7, true) } returns Unit
                val vm = GestionViewModel(repo)
                var termino = false
                vm.publicarAviso("Directiva") { termino = true }
                vm.aviso.value.titulo.error shouldBe "El título es obligatorio"
                coVerify(exactly = 0) { repo.insertarComunicado(any()) }

                vm.onTituloAviso("Reunión")
                vm.onMensajeAviso("Reunión mañana a las seis")
                vm.publicarAviso("Directiva") { termino = true }
                vm.marcarResuelta(7, true)
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
})
