package cl.zaror.alertas.viewmodel

/** Un campo de formulario: su texto y su error (null = válido). */
data class Campo(val valor: String = "", val error: String? = null)

data class LoginState(
    val correo: Campo = Campo(),
    val clave: Campo = Campo(),
    val mensaje: String? = null,
    val cargando: Boolean = false
)

data class RegistroState(
    val nombre: Campo = Campo(),
    val correo: Campo = Campo(),
    val telefono: Campo = Campo(),
    val direccion: Campo = Campo(),
    val clave: Campo = Campo(),
    val rol: String = "Vecino/a",
    val mensaje: String? = null,
    val cargando: Boolean = false,
    val exito: Boolean = false
)

data class NuevaAlertaState(
    val tipo: String = "",
    val tipoError: String? = null,
    val descripcion: Campo = Campo(),
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val mensajeUbicacion: String? = null,
    val guardada: Boolean = false
)

data class FallaForm(
    val componente: String = "",
    val componenteError: String? = null,
    val detalle: Campo = Campo(),
    val prioridad: String = "",
    val prioridadError: String? = null
)

data class AvisoForm(
    val titulo: Campo = Campo(),
    val mensaje: Campo = Campo()
)
