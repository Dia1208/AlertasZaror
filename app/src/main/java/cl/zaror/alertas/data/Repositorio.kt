package cl.zaror.alertas.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import cl.zaror.alertas.data.remote.AlertaDTO
import cl.zaror.alertas.data.remote.FallaDTO
import cl.zaror.alertas.data.remote.ComunicadoDTO
import kotlinx.coroutines.flow.Flow

/** Único punto de acceso a los datos. Los ViewModel nunca tocan Room directamente. */
class Repositorio(db: AppDatabase) {
    private val base = db
    private val usuarioDao = db.usuarioDao()
    private val alertaDao = db.alertaDao()
    private val fallaDao = db.fallaDao()
    private val comunicadoDao = db.comunicadoDao()

    val alertas: Flow<List<Alerta>> = alertaDao.todas()
    val fallas: Flow<List<Falla>> = fallaDao.todas()
    val comunicados: Flow<List<Comunicado>> = comunicadoDao.todos()

    suspend fun actualizarRemota(id: Long, estado: String) = alertaDao.actualizarRemota(id, estado)
    suspend fun eliminarRemota(id: Long) = alertaDao.eliminarRemota(id)

    fun alerta(id: Int): Flow<Alerta?> = alertaDao.porId(id)

    suspend fun registrar(usuario: Usuario): Boolean =
        try { usuarioDao.insertar(usuario); true } catch (e: SQLiteConstraintException) { false }

    suspend fun correoRegistrado(correo: String): Boolean =
        usuarioDao.buscarPorCorreo(correo) != null

    suspend fun iniciarSesion(correo: String, claveHash: String): Usuario? =
        usuarioDao.buscarPorCorreo(correo)?.takeIf { it.claveHash == claveHash }

    suspend fun usuarioPorCorreo(correo: String): Usuario? = usuarioDao.buscarPorCorreo(correo)
    suspend fun actualizarUsuario(usuario: Usuario) = usuarioDao.actualizar(usuario)

    suspend fun sincronizarAlertas(datos: List<AlertaDTO>) = base.withTransaction {
        val existentes = alertaDao.remotas().associateBy { it.idRemoto }
        datos.forEach { remoto ->
            val anterior = existentes[remoto.id]
            val alerta = Alerta(
                id = anterior?.id ?: 0,
                tipo = remoto.tipo,
                descripcion = remoto.descripcion,
                latitud = anterior?.latitud,
                longitud = anterior?.longitud,
                fotoUri = anterior?.fotoUri,
                autor = remoto.autor ?: anterior?.autor ?: "Servidor",
                fecha = remoto.fecha ?: anterior?.fecha ?: System.currentTimeMillis(),
                estado = remoto.estado,
                idRemoto = remoto.id,
                fotoBase64 = remoto.fotoBase64
            )
            if (anterior == null) alertaDao.insertar(alerta) else alertaDao.actualizar(alerta)
        }
        if (datos.isEmpty()) alertaDao.borrarRemotas()
        else alertaDao.borrarRemotasAusentes(datos.map { it.id })
    }

    suspend fun sincronizarFallas(datos: List<FallaDTO>) = base.withTransaction {
        val existentes = fallaDao.remotas().associateBy { it.idRemoto }
        datos.forEach { remoto ->
            val anterior = existentes[remoto.id]
            val falla = Falla(
                id = anterior?.id ?: 0,
                componente = remoto.componente,
                detalle = remoto.detalle,
                prioridad = remoto.prioridad,
                reportadoPor = remoto.reportadoPor ?: "Encargado/a",
                fecha = remoto.fecha ?: System.currentTimeMillis(),
                resuelta = remoto.resuelta,
                idRemoto = remoto.id
            )
            if (anterior == null) fallaDao.insertar(falla) else fallaDao.actualizar(falla)
        }
        if (datos.isEmpty()) fallaDao.borrarRemotas()
        else fallaDao.borrarRemotasAusentes(datos.map { it.id })
    }

    suspend fun sincronizarComunicados(datos: List<ComunicadoDTO>) = base.withTransaction {
        val existentes = comunicadoDao.remotos().associateBy { it.idRemoto }
        datos.forEach { remoto ->
            val anterior = existentes[remoto.id]
            val comunicado = Comunicado(
                id = anterior?.id ?: 0,
                titulo = remoto.titulo,
                mensaje = remoto.mensaje,
                autor = remoto.autor ?: "Directiva",
                fecha = remoto.fecha ?: System.currentTimeMillis(),
                idRemoto = remoto.id
            )
            if (anterior == null) comunicadoDao.insertar(comunicado)
            else comunicadoDao.actualizar(comunicado)
        }
        if (datos.isEmpty()) comunicadoDao.borrarRemotos()
        else comunicadoDao.borrarRemotosAusentes(datos.map { it.id })
    }

    suspend fun insertarAlerta(a: Alerta) { alertaDao.insertar(a) }
    suspend fun cambiarEstadoAlerta(id: Int, estado: String) = alertaDao.cambiarEstado(id, estado)
    suspend fun insertarFalla(f: Falla) { fallaDao.insertar(f) }
    suspend fun cambiarResuelta(id: Int, resuelta: Boolean) = fallaDao.cambiarResuelta(id, resuelta)
    suspend fun insertarComunicado(c: Comunicado) { comunicadoDao.insertar(c) }
}
