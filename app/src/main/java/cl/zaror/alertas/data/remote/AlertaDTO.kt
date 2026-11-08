
package cl.zaror.alertas.data.remote

data class AlertaDTO(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val ubicacion: String,
    val estado: String
)
