package cl.zaror.alertas.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow

/** Único punto de acceso a los datos. Los ViewModel nunca tocan Room directamente. */
class Repositorio(db: AppDatabase) {
    private val usuarioDao = db.usuarioDao()
    private val alertaDao = db.alertaDao()
    private val fallaDao = db.fallaDao()
    private val comunicadoDao = db.comunicadoDao()

    val alertas: Flow<List<Alerta>> = alertaDao.todas()
    val fallas: Flow<List<Falla>> = fallaDao.todas()
    val comunicados: Flow<List<Comunicado>> = comunicadoDao.todos()

    fun alerta(id: Int): Flow<Alerta?> = alertaDao.porId(id)

    suspend fun registrar(usuario: Usuario): Boolean =
        try { usuarioDao.insertar(usuario); true } catch (e: SQLiteConstraintException) { false }

    suspend fun iniciarSesion(correo: String, claveHash: String): Usuario? =
        usuarioDao.buscarPorCorreo(correo)?.takeIf { it.claveHash == claveHash }

    suspend fun insertarAlerta(a: Alerta) { alertaDao.insertar(a) }
    suspend fun cambiarEstadoAlerta(id: Int, estado: String) = alertaDao.cambiarEstado(id, estado)
    suspend fun insertarFalla(f: Falla) { fallaDao.insertar(f) }
    suspend fun cambiarResuelta(id: Int, resuelta: Boolean) = fallaDao.cambiarResuelta(id, resuelta)
    suspend fun insertarComunicado(c: Comunicado) { comunicadoDao.insertar(c) }
}
