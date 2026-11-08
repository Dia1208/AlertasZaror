package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AlertaRemoteViewModelTest : StringSpec({

    "carga las alertas al crear el ViewModel" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))

            try {
                val repositorio = mockk<AlertaRemoteRepository>()
                val esperadas = listOf(
                    AlertaDTO(
                        id = 1L,
                        titulo = "Incendio",
                        descripcion = "Humo detectado en el sector",
                        tipo = "Incendio",
                        ubicacion = "Sector norte",
                        estado = "Activa"
                    )
                )
                coEvery { repositorio.listarAlertas() } returns esperadas

                val vm = AlertaRemoteViewModel(repositorio)
                advanceUntilIdle()

                vm.alertas.value.shouldContainExactly(esperadas)
                vm.error.value shouldBe null
                coVerify(exactly = 1) { repositorio.listarAlertas() }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "muestra un mensaje cuando falla la carga inicial" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))

            try {
                val repositorio = mockk<AlertaRemoteRepository>()
                coEvery { repositorio.listarAlertas() } throws
                        IOException("Sin conexión")

                val vm = AlertaRemoteViewModel(repositorio)
                advanceUntilIdle()

                vm.alertas.value.shouldBeEmpty()
                vm.error.value shouldBe "No se pudieron cargar las alertas"
                coVerify(exactly = 1) { repositorio.listarAlertas() }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "elimina el error y muestra las alertas cuando el reintento funciona" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))

            try {
                val repositorio = mockk<AlertaRemoteRepository>()
                coEvery { repositorio.listarAlertas() } throws
                        IOException("Sin conexión")

                val vm = AlertaRemoteViewModel(repositorio)
                advanceUntilIdle()

                vm.error.value shouldBe "No se pudieron cargar las alertas"

                val esperadas = listOf(
                    AlertaDTO(
                        id = 2L,
                        titulo = "Robo reportado",
                        descripcion = "Un vecino reporta un robo",
                        tipo = "Robo",
                        ubicacion = "Sector sur",
                        estado = "Activa"
                    )
                )
                coEvery { repositorio.listarAlertas() } returns esperadas

                vm.cargarAlertas()
                advanceUntilIdle()

                vm.alertas.value.shouldContainExactly(esperadas)
                vm.error.value shouldBe null
                coVerify(exactly = 2) { repositorio.listarAlertas() }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})