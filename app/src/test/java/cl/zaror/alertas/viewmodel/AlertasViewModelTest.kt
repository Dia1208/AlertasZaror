package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.Alerta
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import cl.zaror.alertas.data.remote.GeocodificacionRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class AlertasViewModelTest : StringSpec({
    "si falla la copia local reintenta sin publicar otra alerta" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                val respuesta = AlertaDTO(42L, "Robo", "Robo en el pasaje", "Robo", "No especificada", "Activa")
                coEvery { remoto.crearAlerta(any()) } returns respuesta
                var intentos = 0
                coEvery { repo.insertarAlerta(any()) } coAnswers {
                    intentos++
                    if (intentos == 1) throw IOException("Room no disponible")
                }
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                vm.onTipo("Robo")
                vm.onDescripcion("Robo en el pasaje")
                vm.activar("Ana")
                advanceUntilIdle()

                vm.nueva.value.pendienteLocal shouldBe true
                vm.nueva.value.guardada shouldBe false
                vm.activar("Ana")
                advanceUntilIdle()

                vm.nueva.value.guardada shouldBe true
                vm.nueva.value.pendienteLocal shouldBe false
                coVerify(exactly = 1) { remoto.crearAlerta(any()) }
                coVerify(exactly = 2) { repo.insertarAlerta(match { it.idRemoto == 42L }) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "no publica una alerta con formulario inválido" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                vm.activar("Ana")
                advanceUntilIdle()
                vm.nueva.value.tipoError shouldBe "Selecciona un tipo de alerta"
                coVerify(exactly = 0) { remoto.crearAlerta(any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "actualiza el estado remoto antes de cambiar la copia local" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                val dto = AlertaDTO(11L, "Robo", "Robo en el pasaje", "Robo", "Centro", "Activa")
                coEvery { remoto.buscarAlerta(11L) } returns dto
                coEvery { remoto.actualizarAlerta(11L, any()) } returns dto.copy(estado = "Resuelta")
                coEvery { repo.actualizarRemota(11L, "Resuelta") } returns Unit
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                val alerta = Alerta(tipo = "Robo", descripcion = dto.descripcion, latitud = null,
                    longitud = null, fotoUri = null, autor = "Ana", fecha = 1L, idRemoto = 11L)
                vm.cambiarEstado(alerta, "Resuelta")
                advanceUntilIdle()
                vm.errorEstado.value shouldBe null
                coVerify(exactly = 1) { remoto.actualizarAlerta(11L, match { it.estado == "Resuelta" }) }
                coVerify(exactly = 1) { repo.actualizarRemota(11L, "Resuelta") }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "si el servidor falla no cambia el estado local" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                coEvery { remoto.buscarAlerta(11L) } throws IOException("Sin conexión")
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                val alerta = Alerta(tipo = "Robo", descripcion = "Robo en el pasaje", latitud = null,
                    longitud = null, fotoUri = null, autor = "Ana", fecha = 1L, idRemoto = 11L)
                vm.cambiarEstado(alerta, "Resuelta")
                advanceUntilIdle()
                vm.errorEstado.value shouldBe "No se pudo sincronizar el estado de la alerta."
                coVerify(exactly = 0) { repo.actualizarRemota(any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "si falla el envío muestra error y no guarda copia local" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                coEvery { remoto.crearAlerta(any()) } throws IOException("Sin conexión")
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                vm.onTipo("Robo")
                vm.onDescripcion("Robo en el pasaje")
                vm.activar("Ana")
                advanceUntilIdle()
                vm.nueva.value.guardada shouldBe false
                vm.nueva.value.enviando shouldBe false
                vm.nueva.value.errorEnvio shouldBe
                    "No se pudo enviar la alerta. Revisa la conexión y vuelve a intentarlo."
                coVerify(exactly = 0) { repo.insertarAlerta(any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "redondea ubicación y usa coordenadas si falla la geocodificación" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val geo = mockk<GeocodificacionRepository>()
                coEvery { geo.buscarDireccionAproximada(any(), any()) } throws IOException("Sin conexión")
                val vm = AlertasViewModel(repo, mockk<AlertaRemoteRepository>(), geo)
                vm.onUbicacion(-33.45678, -70.65432)
                advanceUntilIdle()
                vm.nueva.value.latitud shouldBe -33.457
                vm.nueva.value.longitud shouldBe -70.654
                vm.nueva.value.direccionAproximada shouldBe
                    "Coordenadas aproximadas: -33.457, -70.654"
                vm.nueva.value.consultandoDireccion shouldBe false
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "sincroniza las alertas del servidor y muestra fallos de actualización" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                val datos = listOf(AlertaDTO(15L, "Robo", "Robo en el pasaje", "Robo", "Centro", "Activa"))
                coEvery { remoto.listarAlertas() } returns datos
                coEvery { repo.sincronizarAlertas(datos) } returns Unit
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())

                vm.actualizar()
                advanceUntilIdle()
                vm.errorSincronizacion.value shouldBe null
                coVerify(exactly = 1) { repo.sincronizarAlertas(datos) }

                coEvery { remoto.listarAlertas() } throws IOException("Sin conexión")
                vm.actualizar()
                advanceUntilIdle()
                vm.errorSincronizacion.value shouldBe "No se pudieron actualizar las alertas del servidor."
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "guarda una alerta local y reinicia el formulario" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                every { repo.alertas } returns flowOf(emptyList())
                val remoto = mockk<AlertaRemoteRepository>()
                coEvery { remoto.crearAlerta(any()) } returns
                    AlertaDTO(42L, "Robo", "Robo en el pasaje", "Robo", "Centro", "Activa")
                coEvery { repo.insertarAlerta(any()) } returns Unit
                val vm = AlertasViewModel(repo, remoto, mockk<GeocodificacionRepository>())
                vm.onTipo("Robo")
                vm.onDescripcion("Robo en el pasaje")
                vm.activar("Ana")
                advanceUntilIdle()
                vm.nueva.value.guardada shouldBe true
                vm.nueva.value.errorEnvio shouldBe null
                coVerify(exactly = 1) { repo.insertarAlerta(match { it.idRemoto == 42L && it.autor == "Ana" }) }
                vm.reiniciar()
                vm.nueva.value.tipo shouldBe ""
                vm.nueva.value.guardada shouldBe false
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
