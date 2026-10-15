package cl.zaror.alertas.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.comoFecha(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(this))
