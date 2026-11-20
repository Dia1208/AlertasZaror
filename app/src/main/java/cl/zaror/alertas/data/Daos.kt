package cl.zaror.alertas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
    @Update suspend fun actualizar(usuario: Usuario)
}

@Dao
interface AlertaDao {
    @Query("SELECT * FROM alertas ORDER BY fecha DESC")
    fun todas(): Flow<List<Alerta>>

    @Query("UPDATE alertas SET estado = :estado WHERE idRemoto = :id")
    suspend fun actualizarRemota(id: Long, estado: String)

    @Query("DELETE FROM alertas WHERE idRemoto = :id")
    suspend fun eliminarRemota(id: Long)

    @Query("SELECT * FROM alertas WHERE id = :id")
    fun porId(id: Int): Flow<Alerta?>

    @Insert
    suspend fun insertar(alerta: Alerta): Long

    @Query("UPDATE alertas SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String)
    @Query("SELECT * FROM alertas WHERE idRemoto IS NOT NULL")
    suspend fun remotas(): List<Alerta>
    @Update suspend fun actualizar(alerta: Alerta)
    @Query("DELETE FROM alertas WHERE idRemoto IS NOT NULL")
    suspend fun borrarRemotas()
    @Query("DELETE FROM alertas WHERE idRemoto IS NOT NULL AND idRemoto NOT IN (:ids)")
    suspend fun borrarRemotasAusentes(ids: List<Long>)
}

@Dao
interface FallaDao {
    @Query("SELECT * FROM fallas ORDER BY fecha DESC")
    fun todas(): Flow<List<Falla>>

    @Insert
    suspend fun insertar(falla: Falla): Long

    @Query("UPDATE fallas SET resuelta = :resuelta WHERE id = :id")
    suspend fun cambiarResuelta(id: Int, resuelta: Boolean)
    @Query("SELECT * FROM fallas WHERE idRemoto IS NOT NULL")
    suspend fun remotas(): List<Falla>
    @Update suspend fun actualizar(falla: Falla)
    @Query("DELETE FROM fallas WHERE idRemoto IS NOT NULL")
    suspend fun borrarRemotas()
    @Query("DELETE FROM fallas WHERE idRemoto IS NOT NULL AND idRemoto NOT IN (:ids)")
    suspend fun borrarRemotasAusentes(ids: List<Long>)
}

@Dao
interface ComunicadoDao {
    @Query("SELECT * FROM comunicados ORDER BY fecha DESC")
    fun todos(): Flow<List<Comunicado>>

    @Insert
    suspend fun insertar(comunicado: Comunicado): Long
    @Query("SELECT * FROM comunicados WHERE idRemoto IS NOT NULL")
    suspend fun remotos(): List<Comunicado>
    @Update suspend fun actualizar(comunicado: Comunicado)
    @Query("DELETE FROM comunicados WHERE idRemoto IS NOT NULL")
    suspend fun borrarRemotos()
    @Query("DELETE FROM comunicados WHERE idRemoto IS NOT NULL AND idRemoto NOT IN (:ids)")
    suspend fun borrarRemotosAusentes(ids: List<Long>)
}
