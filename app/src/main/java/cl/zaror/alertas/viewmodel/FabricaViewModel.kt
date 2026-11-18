package cl.zaror.alertas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.zaror.alertas.data.Repositorio
import cl.zaror.alertas.data.remote.AlertaRemoteRepository
import cl.zaror.alertas.data.remote.GeocodificacionRepository

class FabricaViewModel(
    private val repo: Repositorio
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repo)

            modelClass.isAssignableFrom(AlertasViewModel::class.java) ->
                AlertasViewModel(
                    repo = repo,
                    remoto = AlertaRemoteRepository(),
                    geocodificacion = GeocodificacionRepository()
                )

            modelClass.isAssignableFrom(GestionViewModel::class.java) ->
                GestionViewModel(repo)

            modelClass.isAssignableFrom(AlertaRemoteViewModel::class.java) ->
                AlertaRemoteViewModel(local = repo)

            else ->
                throw IllegalArgumentException(
                    "ViewModel desconocido: " + modelClass.name
                )
        } as T
    }
}