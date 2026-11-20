# Alertas Zaror

Aplicación Android para apoyar la comunicación de emergencias de una comunidad. Este proyecto académico integra una app Kotlin/Jetpack Compose, almacenamiento local con Room, un microservicio Spring Boot propio y una consulta externa de geocodificación.

## Funcionalidades

- Registro de Vecino/a en el servidor e inicio de sesión verificado con el microservicio, incluso desde un dispositivo nuevo. Los roles son Vecino/a, Encargado/a y Directiva.
- Creación de alertas con tipo, descripción, ubicación aproximada y fotografía opcional.
- Alertas, fallas y comunicados compartidos mediante el microservicio. Las pantallas tienen un botón para actualizar los datos desde otro dispositivo.
- Envío de alertas y fotografías comprimidas al microservicio; la app conserva copias locales en Room.
- Registro de ubicación aproximada mediante GPS y búsqueda inversa de dirección en OpenStreetMap Nominatim. Si la consulta falla, la app usa coordenadas aproximadas.
- Formularios validados, permisos de ubicación/cámara y pruebas unitarias y de interfaz.

Los datos utilizados con fines académicos deben ser ficticios. Las dos cuentas de demostración usan una contraseña pública y no deben utilizarse con datos reales.

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Encargado/a | `alejandro@encargado.cl` | `Duoc.2026@` |
| Directiva | `alejandro@directiva.cl` | `Duoc.2026@` |

Room conserva una copia local de los datos consultados. MySQL en el microservicio es la fuente compartida: las nuevas alertas, fallas y comunicados se envían allí antes de guardarse en Room. Los registros nuevos reciben siempre el rol Vecino/a. Encargado/a puede reportar y resolver fallas; Directiva puede publicar comunicados. Ambos pueden gestionar alertas. Los registros locales anteriores a esta versión que no tienen identificador remoto permanecen solo en el dispositivo.

## Requisitos

- Android Studio y JDK 17.
- Emulador o dispositivo Android con API 26 o superior.
- Para iniciar sesión y sincronizar los datos: el [microservicio](https://github.com/Dia1208/microservicioalertas) actualizado debe estar funcionando en EC2.
- Conexión a internet para dependencias Gradle y la geocodificación Nominatim.

## Ejecutar la app

1. Clona este repositorio y abre su carpeta en Android Studio.
2. Espera a que termine la sincronización de Gradle y selecciona un emulador o dispositivo.
3. Comprueba que el microservicio responda en `https://52-90-150-61.sslip.io/`.
4. Ejecuta la configuración `app` desde Android Studio.
5. Para probar GPS en un emulador: **Extended controls → Location**, define una ubicación y vuelve al formulario de alerta.
6. En otro dispositivo, inicia sesión y pulsa **Actualizar alertas**, **Actualizar fallas** o **Actualizar comunicados** para recibir los cambios. La actualización es manual al pulsar el botón y también se intenta al entrar en cada pantalla; no hay notificaciones en tiempo real.

Retrofit usa `https://52-90-150-61.sslip.io/`, por lo que un teléfono puede usar la app desde otra red mientras la instancia EC2 siga funcionando. Si cambia la IP pública de EC2, hay que actualizar el dominio/certificado y generar una APK nueva. Las credenciales de MySQL y el archivo de firma de la APK no se incluyen en Git.

## API externa de geocodificación

La app consulta el endpoint de geocodificación inversa de OpenStreetMap Nominatim para convertir las coordenadas elegidas en una referencia legible. La ubicación se redondea a tres decimales antes de consultarla y se etiqueta como aproximada. La interfaz acredita a OpenStreetMap. La geocodificación depende de disponibilidad de internet y de los datos cartográficos, por lo que puede devolver un lugar cercano y no una dirección exacta.

## Pruebas

Desde PowerShell en la raíz del proyecto, o desde la terminal de Android Studio:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

La primera tarea ejecuta pruebas locales de lógica, repositorios y ViewModels. La segunda requiere un emulador activo o un dispositivo conectado.

## APK firmado (release)

1. En Android Studio, selecciona **Build → Generate Signed Bundle / APK → APK**.
2. Crea o selecciona un archivo de claves `.jks`, ingresa su contraseña y alias, y selecciona la variante `release`.
3. Sigue la ubicación de salida que muestra el asistente. `assembleRelease` por sí sola genera `app/build/outputs/apk/release/app-release-unsigned.apk`, que no está firmado.
4. Conserva el `.jks` y sus contraseñas en un respaldo privado. No los subas a Git ni los compartas: se necesitan para firmar futuras actualizaciones con la misma identidad.

## Arquitectura

El código Android se organiza bajo `app/src/main/java/cl/zaror/alertas`:

- `data`: entidades, Room, repositorios y clientes Retrofit.
- `domain`: reglas de validación.
- `viewmodel`: estado y lógica de presentación.
- `recursos`: acceso a recursos nativos como cámara y ubicación.
- `ui`: pantallas Compose, componentes, tema y navegación.
