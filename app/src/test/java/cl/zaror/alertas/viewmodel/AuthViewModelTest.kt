package cl.zaror.alertas.viewmodel

import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.Usuario
import cl.zaror.alertas.data.remote.AuthRemoteRepository
import cl.zaror.alertas.data.remote.PerfilRemoto
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
                val remoto = mockk<AuthRemoteRepository>()
                val usuarioRegistrado = slot<Usuario>()

                coEvery { repositorio.correoRegistrado(any()) } returns false
                coEvery { remoto.registrarVecino(any(), any()) } returns PerfilRemoto("ana@correo.cl", "VECINO")

                coEvery {
                    repositorio.registrar(capture(usuarioRegistrado))
                } returns true

                val vm = AuthViewModel(repositorio, remoto)

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

    "inicia sesión con correo normalizado y permite cerrarla" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<AuthRemoteRepository>()
                val usuario = Usuario(nombre = "Ana", correo = "ana@correo.cl", telefono = "912345678",
                    direccion = "Pasaje Norte", claveHash = "hash", rol = Roles.VECINO)
                coEvery { repo.iniciarSesion("ana@correo.cl", any()) } returns usuario
                coEvery { remoto.autenticar(any(), any(), any()) } returns PerfilRemoto("ana@correo.cl", "VECINO")
                val vm = AuthViewModel(repo, remoto)
                vm.onLoginCorreo(" ANA@CORREO.CL ")
                vm.onLoginClave("Zaror2026")
                vm.iniciarSesion()
                advanceUntilIdle()
                vm.usuario.value shouldBe usuario
                vm.login.value shouldBe LoginState()
                vm.cerrarSesion()
                vm.usuario.value shouldBe null
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "muestra error de credenciales sin iniciar sesión" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<AuthRemoteRepository>()
                coEvery { repo.iniciarSesion(any(), any()) } returns null
                val vm = AuthViewModel(repo, remoto)
                vm.iniciarSesion()
                vm.login.value.correo.error shouldBe "El correo es obligatorio"
                vm.onLoginCorreo("ana@correo.cl")
                vm.onLoginClave("Incorrecta1")
                vm.iniciarSesion()
                advanceUntilIdle()
                vm.usuario.value shouldBe null
                vm.login.value.mensaje shouldBe "Correo o contraseña incorrectos"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "rechaza correo duplicado y permite limpiar el formulario" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<Repositorio>()
                val remoto = mockk<AuthRemoteRepository>()
                coEvery { repo.correoRegistrado(any()) } returns true
                coEvery { repo.registrar(any()) } returns false
                val vm = AuthViewModel(repo, remoto)
                vm.registrar()
                vm.registro.value.nombre.error shouldBe "El nombre es obligatorio"
                vm.onNombre("Ana Vecina")
                vm.onCorreo("ana@correo.cl")
                vm.onTelefono("912345678")
                vm.onDireccion("Pasaje Norte")
                vm.onClave("Zaror2026")
                vm.registrar()
                advanceUntilIdle()
                vm.registro.value.mensaje shouldBe "Ese correo ya está registrado"
                vm.registro.value.exito shouldBe false
                vm.limpiarRegistro()
                vm.registro.value shouldBe RegistroState()
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
