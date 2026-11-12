package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.Usuario
import cl.zaror.alertas.domain.Roles
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : StringSpec({

    "registra como Vecino aunque se intente elegir otro rol" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))

            try {
                val repositorio = mockk<Repositorio>()
                val usuarioRegistrado = slot<Usuario>()

                coEvery {
                    repositorio.registrar(capture(usuarioRegistrado))
                } returns true

                val vm = AuthViewModel(repositorio)

                vm.onNombre("Ana Vecina")
                vm.onCorreo("ana@correo.cl")
                vm.onTelefono("912345678")
                vm.onDireccion("Población Zaror")
                vm.onClave("Zaror2026")

                // Simula un intento de cambiar el rol desde otra parte del código.
                vm.onRol(Roles.DIRECTIVA)

                vm.registrar()
                advanceUntilIdle()

                usuarioRegistrado.captured.rol shouldBe Roles.VECINO
                vm.registro.value.exito shouldBe true
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})