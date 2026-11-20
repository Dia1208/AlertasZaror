package cl.zaror.alertas.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val claveHash: String,
    val rol: String
)

@Entity(tableName = "alertas")
data class Alerta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val descripcion: String,
    val latitud: Double?,
    val longitud: Double?,
    val fotoUri: String?,
    val autor: String,
    val fecha: Long,
    val estado: String = "Activa",
    val idRemoto: Long? = null,
    val fotoBase64: String? = null
)

@Entity(tableName = "fallas")
data class Falla(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val componente: String,
    val detalle: String,
    val prioridad: String,
    val reportadoPor: String,
    val fecha: Long,
    val resuelta: Boolean = false,
    val idRemoto: Long? = null
)

@Entity(tableName = "comunicados")
data class Comunicado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val mensaje: String,
    val autor: String,
    val fecha: Long,
    val idRemoto: Long? = null
)
