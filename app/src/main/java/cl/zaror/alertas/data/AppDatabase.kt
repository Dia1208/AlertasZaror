package cl.zaror.alertas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, Alerta::class, Falla::class, Comunicado::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun fallaDao(): FallaDao
    abstract fun comunicadoDao(): ComunicadoDao

    companion object {
        fun crear(contexto: Context): AppDatabase =
            Room.databaseBuilder(contexto, AppDatabase::class.java, "zaror.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        sembrarCuentas(db)
                    }
                })
                .addMigrations(object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE alertas ADD COLUMN idRemoto INTEGER")
                }
            }, object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    sembrarCuentas(db)
                    db.execSQL("""DELETE FROM usuarios WHERE correo IN
                        ('aleencargado@duocuc.cl', 'aledirectiva@duocuc.cl')""".trimIndent())
                }
            }, object : androidx.room.migration.Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE alertas ADD COLUMN fotoBase64 TEXT")
                    db.execSQL("ALTER TABLE fallas ADD COLUMN idRemoto INTEGER")
                    db.execSQL("ALTER TABLE comunicados ADD COLUMN idRemoto INTEGER")
                }
            }).build()

        private fun sembrarCuentas(db: SupportSQLiteDatabase) {
            val claveHash = "0654517c1d42ed5f5fddd77f19e47ec80cff8acd0b51dda40c7faf9ab4cbd9d7"
            val insertar = """INSERT OR IGNORE INTO usuarios
                (nombre, correo, telefono, direccion, claveHash, rol)
                VALUES (?, ?, ?, ?, ?, ?)""".trimIndent()
            val cuentas = listOf(
                arrayOf<Any>("Alejandro Encargado", "alejandro@encargado.cl", "912345678",
                    "Dirección ficticia", claveHash, "Encargado/a"),
                arrayOf<Any>("Alejandro Directiva", "alejandro@directiva.cl", "912345679",
                    "Dirección ficticia", claveHash, "Directiva")
            )
            cuentas.forEach { db.execSQL(insertar, it) }
            db.execSQL("""UPDATE usuarios SET claveHash = ?, rol = 'Encargado/a'
                WHERE correo = 'alejandro@encargado.cl'""".trimIndent(), arrayOf(claveHash))
            db.execSQL("""UPDATE usuarios SET claveHash = ?, rol = 'Directiva'
                WHERE correo = 'alejandro@directiva.cl'""".trimIndent(), arrayOf(claveHash))
        }
    }
}
