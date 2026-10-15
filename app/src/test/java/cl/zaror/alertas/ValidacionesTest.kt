package cl.zaror.alertas

import cl.zaror.alertas.domain.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {
    @Test fun correoValido() = assertNull(Validaciones.correo("vecino@correo.cl"))
    @Test fun correoInvalido() = assertNotNull(Validaciones.correo("vecino@"))
    @Test fun telefonoValido() = assertNull(Validaciones.telefono("912345678"))
    @Test fun telefonoInvalido() = assertNotNull(Validaciones.telefono("812345678"))
    @Test fun claveDebil() = assertNotNull(Validaciones.clave("abc"))
    @Test fun claveSinMayuscula() = assertEquals("Debe incluir al menos una mayúscula", Validaciones.clave("abcdefg1"))
    @Test fun claveFuerte() = assertNull(Validaciones.clave("Zaror2026"))
    @Test fun descripcionCorta() = assertNotNull(Validaciones.texto("hola", "La descripción", 10, 200))
}
