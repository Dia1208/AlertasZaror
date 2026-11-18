package cl.zaror.alertas.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class RepositorioTest : StringSpec({
    fun repositorio(usuarioDao: UsuarioDao, alertaDao: AlertaDao): Repositorio {
        val db = mockk<AppDatabase>()
        every { db.usuarioDao() } returns usuarioDao
        every { db.alertaDao() } returns alertaDao
        every { db.fallaDao() } returns mockk<FallaDao> {
            every { todas() } returns flowOf(emptyList())
        }
        every { db.comunicadoDao() } returns mockk<ComunicadoDao> {
            every { todos() } returns flowOf(emptyList())
        }
        return Repositorio(db)
    }

    "solo inicia sesión si coincide el hash" {
        runTest {
            val usuario = Usuario(nombre = "Ana", correo = "ana@correo.cl", telefono = "912345678",
                direccion = "Pasaje Norte", claveHash = "hash-correcto", rol = "Vecino/a")
            val usuarios = mockk<UsuarioDao>()
            coEvery { usuarios.buscarPorCorreo("ana@correo.cl") } returns usuario
            val alertas = mockk<AlertaDao>()
            every { alertas.todas() } returns flowOf(emptyList())
            val repo = repositorio(usuarios, alertas)

            repo.iniciarSesion("ana@correo.cl", "incorrecto") shouldBe null
            repo.iniciarSesion("ana@correo.cl", "hash-correcto") shouldBe usuario
        }
    }

    "delega guardado y actualización de una alerta remota" {
        runTest {
            val usuarios = mockk<UsuarioDao>()
            val alertas = mockk<AlertaDao>()
            every { alertas.todas() } returns flowOf(emptyList())
            coEvery { alertas.insertar(any()) } returns 1L
            coEvery { alertas.actualizarRemota(4L, "Resuelta") } returns Unit
            val repo = repositorio(usuarios, alertas)
            val alerta = Alerta(tipo = "Robo", descripcion = "Robo en el pasaje", latitud = null,
                longitud = null, fotoUri = null, autor = "Ana", fecha = 1L, idRemoto = 4L)

            repo.insertarAlerta(alerta)
            repo.actualizarRemota(4L, "Resuelta")
            coVerify(exactly = 1) { alertas.insertar(alerta) }
            coVerify(exactly = 1) { alertas.actualizarRemota(4L, "Resuelta") }
        }
    }
})
