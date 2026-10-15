package cl.zaror.alertas.domain

object Roles {
    const val VECINO = "Vecino/a"
    const val ENCARGADO = "Encargado/a"
    const val DIRECTIVA = "Directiva"
    val todos = listOf(VECINO, ENCARGADO, DIRECTIVA)
}

object Estados {
    const val ACTIVA = "Activa"
    const val RESUELTA = "Resuelta"
}

object Opciones {
    val tiposAlerta = listOf("Robo", "Incendio", "Persona sospechosa", "Emergencia médica", "Otro")
    val componentes = listOf("Sirena", "Botón de pánico", "Batería", "Cableado", "Otro")
    val prioridades = listOf("Baja", "Media", "Alta")
}
