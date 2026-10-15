package cl.zaror.alertas.recursos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

fun tienePermiso(contexto: Context, permiso: String): Boolean =
    ContextCompat.checkSelfPermission(contexto, permiso) == PackageManager.PERMISSION_GRANTED

/** RECURSO NATIVO 1: Cámara. Crea el archivo donde se guardará la foto y entrega un Uri seguro. */
fun crearUriFoto(contexto: Context): Uri {
    val carpeta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File.createTempFile("alerta_", ".jpg", carpeta)
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)
}

/** RECURSO NATIVO 2: Ubicación (GPS). Pide un punto actual y avisa éxito o error. */
@SuppressLint("MissingPermission") // el permiso se verifica justo antes con tienePermiso()
fun obtenerUbicacion(
    contexto: Context,
    onResultado: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val fina = tienePermiso(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
    val aproximada = tienePermiso(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
    if (!fina && !aproximada) {
        onError("Sin permiso de ubicación")
        return
    }
    LocationServices.getFusedLocationProviderClient(contexto)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { loc ->
            if (loc != null) onResultado(loc.latitude, loc.longitude)
            else onError("No se pudo obtener la ubicación. Activa el GPS.")
        }
        .addOnFailureListener { onError("Error al obtener la ubicación") }
}
