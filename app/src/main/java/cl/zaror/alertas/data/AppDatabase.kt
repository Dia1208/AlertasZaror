package cl.zaror.alertas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, Alerta::class, Falla::class, Comunicado::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun fallaDao(): FallaDao
    abstract fun comunicadoDao(): ComunicadoDao

    companion object {
        fun crear(contexto: Context): AppDatabase =
            Room.databaseBuilder(contexto, AppDatabase::class.java, "zaror.db").build()
    }
}
