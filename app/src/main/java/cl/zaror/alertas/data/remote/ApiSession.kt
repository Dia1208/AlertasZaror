package cl.zaror.alertas.data.remote

object ApiSession {
    @Volatile private var correo: String? = null
    @Volatile private var clave: String? = null

    fun establecer(nuevoCorreo: String, nuevaClave: String) {
        correo = nuevoCorreo
        clave = nuevaClave
    }

    fun limpiar() {
        correo = null
        clave = null
    }

    fun credenciales(): Pair<String, String>? {
        val actualCorreo = correo ?: return null
        val actualClave = clave ?: return null
        return actualCorreo to actualClave
    }
}
