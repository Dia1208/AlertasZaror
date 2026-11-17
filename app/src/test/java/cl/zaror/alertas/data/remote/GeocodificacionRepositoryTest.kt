package cl.zaror.alertas.data.remote

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

class GeocodificacionRepositoryTest : StringSpec({
    "consulta una dirección aproximada y reutiliza el resultado en caché" {
        runTest {
            val api = mockk<NominatimApi>()
            val respuesta = DireccionDTO("Cerrillos, Santiago, Chile")
            coEvery {
                api.buscarDireccion("jsonv2", -33.512, -70.712, 14, 1)
            } returns respuesta
            val repositorio = GeocodificacionRepository(api)

            repositorio.buscarDireccionAproximada(-33.512, -70.712) shouldBe
                "Cerrillos, Santiago, Chile"
            repositorio.buscarDireccionAproximada(-33.512, -70.712) shouldBe
                "Cerrillos, Santiago, Chile"

            coVerify(exactly = 1) {
                api.buscarDireccion("jsonv2", -33.512, -70.712, 14, 1)
            }
        }
    }

    "propaga errores de red para que el formulario use coordenadas aproximadas" {
        runTest {
            val api = mockk<NominatimApi>()
            coEvery {
                api.buscarDireccion(any(), any(), any(), any(), any())
            } throws IOException("Sin conexión")
            val repositorio = GeocodificacionRepository(api)

            val error = runCatching {
                repositorio.buscarDireccionAproximada(-33.512, -70.712)
            }.exceptionOrNull()

            error.shouldBeInstanceOf<IOException>()
            coVerify(exactly = 1) {
                api.buscarDireccion("jsonv2", -33.512, -70.712, 14, 1)
            }
        }
    }
})
