package cl.zaror.alertas.data.remote

import cl.zaror.alertas.domain.Roles
import okhttp3.Credentials
import retrofit2.HttpException

class AuthRemoteRepository(private val api: AuthApi = RetrofitInstance.authApi) {
    suspend fun registrarVecino(correo: String, clave: String): PerfilRemoto =
        api.registrar(RegistroRemotoRequest(correo, clave))

    suspend fun autenticar(correo: String, clave: String, rolLocal: String): PerfilRemoto {
        val autorizacion = Credentials.basic(correo, clave)
        return try {
            api.perfil(autorizacion)
        } catch (e: HttpException) {
            if (e.code() != 401 || rolLocal != Roles.VECINO) throw e
            registrarVecino(correo, clave)
            api.perfil(autorizacion)
        }
    }
}
