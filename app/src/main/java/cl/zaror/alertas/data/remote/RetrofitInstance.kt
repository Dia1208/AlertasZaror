
package cl.zaror.alertas.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Credentials
import okhttp3.OkHttpClient

object RetrofitInstance {

    private const val BASE_URL = "https://52-90-150-61.sslip.io/"

    private val cliente = OkHttpClient.Builder().addInterceptor { cadena ->
        val solicitud = cadena.request().newBuilder()
        ApiSession.credenciales()?.let { (correo, clave) ->
            solicitud.header("Authorization", Credentials.basic(correo, clave))
        }
        cadena.proceed(solicitud.build())
    }.build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AlertaApi by lazy {
        retrofit.create(AlertaApi::class.java)
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val gestionApi: GestionApi by lazy { retrofit.create(GestionApi::class.java) }
}
