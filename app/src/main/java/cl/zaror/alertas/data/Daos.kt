package cl.zaror.alertas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
}

@Dao
interface AlertaDao {
    @Query("SELECT * FROM alertas ORDER BY fecha DESC")
    fun todas(): Flow<List<Alerta>>

    @Query("SELECT * FROM alertas WHERE id = :id")
    fun porId(id: Int): Flow<Alerta?>

    @Insert
    suspend fun insertar(alerta: Alerta): Long

    @Query("UPDATE alertas SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String)
}

@Dao
interface FallaDao {
    @Query("SELECT * FROM fallas ORDER BY fecha DESC")
    fun todas(): Flow<List<Falla>>

    @Insert
    suspend fun insertar(falla: Falla): Long

    @Query("UPDATE fallas SET resuelta = :resuelta WHERE id = :id")
    suspend fun cambiarResuelta(id: Int, resuelta: Boolean)
}

@Dao
interface ComunicadoDao {
    @Query("SELECT * FROM comunicados ORDER BY fecha DESC")
    fun todos(): Flow<List<Comunicado>>

    @Insert
    suspend fun insertar(comunicado: Comunicado): Long
}
