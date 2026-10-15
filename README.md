# Alertas Zaror · App de alarmas comunitarias

Evaluación Parcial 2 · DSY1105 Desarrollo de Aplicaciones Móviles · Duoc UC
Caso: Junta de Vecinos Población Zaror (Cerrillos)

**Integrantes:** 

## Descripción
Aplicación Android (MVP académico) que permite a los vecinos activar una alerta
indicando tipo de emergencia, descripción, ubicación aproximada y foto opcional.
Todos los vecinos ven el historial centralizado de alertas, el/la Encargado/a
registra fallas del sistema de alarmas y la Directiva publica comunicados.
Todos los datos son **ficticios** (protocolo de protección de datos).

## Funcionalidades implementadas
- Registro e inicio de sesión (contraseña guardada como hash SHA-256).
- Roles: Vecino/a, Encargado/a, Directiva.
- Activar alarma (tipo, descripción, ubicación, foto) y ver historial/detalle.
- Marcar alertas como resueltas (Encargado/a y Directiva).
- Reporte y seguimiento de fallas del sistema (Encargado/a).
- Comunicados a la comunidad (Directiva).
- Formularios con validación por campo, íconos y mensajes de error animados.
- Animaciones: pulso del botón de alarma, transiciones de navegación, cambio
  de color por estado, aparición/ocultamiento de formularios y errores.
- Persistencia local con Room (SQLite).
- Recursos nativos: **ubicación (GPS)** y **cámara**, con solicitud de permisos
  en tiempo de ejecución y FileProvider para guardar la foto de forma segura.

## Arquitectura (MVVM)
```
app/src/main/java/cl/zaror/alertas
├── data/          Room: entidades, DAOs, base de datos y Repositorio
├── domain/        Validaciones (lógica pura, testeable) y constantes
├── viewmodel/     AuthViewModel, AlertasViewModel, GestionViewModel + estados
├── recursos/      Acceso a recursos nativos (cámara, ubicación)
└── ui/            Tema, componentes reutilizables, pantallas y navegación
```
- La **UI solo dibuja** el estado que expone el ViewModel (StateFlow).
- Las **validaciones** viven en `domain/Validaciones.kt` y se invocan desde los ViewModel.
- Los ViewModel hablan con el `Repositorio`, nunca directamente con Room.

## Cómo ejecutar
1. Clonar el repositorio y abrir la carpeta en **Android Studio** (Koala o superior, JDK 17).
2. Esperar la sincronización de Gradle.
3. Crear/seleccionar un emulador Android (API 26+) o un teléfono físico.
4. Presionar **Run ▶**.
5. Registrar un usuario (datos ficticios) y probar según el rol elegido.
   - Para probar ubicación en el emulador: *Extended controls → Location → Set location*.
   - Para probar cámara: el emulador usa una cámara virtual.

## Pruebas
`./gradlew test` ejecuta las pruebas unitarias de validaciones.

## Trello
Enlace al tablero: _pegar aquí_

## Próximas etapas (EFT)
Microservicio Spring Boot + Retrofit, notificaciones push y APK firmado en release.
