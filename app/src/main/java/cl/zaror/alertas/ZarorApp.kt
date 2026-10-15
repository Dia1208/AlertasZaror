package cl.zaror.alertas

import android.app.Application
import cl.zaror.alertas.data.AppDatabase
import cl.zaror.alertas.data.Repositorio

class ZarorApp : Application() {
    val repositorio: Repositorio by lazy { Repositorio(AppDatabase.crear(this)) }
}
