package cl.zaror.alertas.domain

import java.security.MessageDigest

/**
 * Toda la lógica de validación vive aquí (y en los ViewModel), NO en la interfaz.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 */
object Validaciones {
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_TELEFONO = Regex("^9\\d{8}$")

    fun nombre(v: String): String? = when {
        v.isBlank() -> "El nombre es obligatorio"
        v.trim().length < 3 -> "Mínimo 3 caracteres"
        !v.all { it.isLetter() || it == ' ' } -> "Solo letras y espacios"
        else -> null
    }

    fun correo(v: String): String? = when {
        v.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(v.trim()) -> "Formato de correo inválido"
        else -> null
    }

    fun telefono(v: String): String? = when {
        v.isBlank() -> "El teléfono es obligatorio"
        !REGEX_TELEFONO.matches(v) -> "Debe tener 9 dígitos y comenzar con 9"
        else -> null
    }

    fun direccion(v: String): String? = when {
        v.isBlank() -> "La dirección es obligatoria"
        v.trim().length < 5 -> "Mínimo 5 caracteres"
        else -> null
    }

    fun clave(v: String): String? = when {
        v.isBlank() -> "La contraseña es obligatoria"
        v.length < 8 -> "Mínimo 8 caracteres"
        !v.any { it.isDigit() } -> "Debe incluir al menos un número"
        !v.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
        else -> null
    }

    fun claveLogin(v: String): String? =
        if (v.isBlank()) "La contraseña es obligatoria" else null

    fun texto(v: String, etiqueta: String, min: Int, max: Int): String? = when {
        v.isBlank() -> "$etiqueta es obligatorio"
        v.trim().length < min -> "Mínimo $min caracteres"
        v.trim().length > max -> "Máximo $max caracteres"
        else -> null
    }

    fun seleccion(v: String, queCosa: String): String? =
        if (v.isBlank()) "Selecciona $queCosa" else null
}

/** Las contraseñas nunca se guardan en texto plano. */
fun sha256(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }
