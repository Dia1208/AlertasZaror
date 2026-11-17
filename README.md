# Alertas Zaror

Aplicación Android para apoyar la comunicación de emergencias de una comunidad. Este proyecto académico integra una app Kotlin/Jetpack Compose, almacenamiento local con Room, un microservicio Spring Boot propio y una consulta externa de geocodificación.

## Funcionalidades

- Registro e inicio de sesión local, con perfiles Vecino/a, Encargado/a y Directiva.
- Creación de alertas con tipo, descripción, ubicación aproximada y fotografía opcional.
- Consulta de alertas remotas y operaciones de actualización y eliminación desde la pantalla de alertas.
- Envío de nuevas alertas al microservicio Spring Boot; la app también conserva una copia local en Room.
- Registro de ubicación aproximada mediante GPS y búsqueda inversa de dirección en OpenStreetMap Nominatim. Si la consulta falla, la app usa coordenadas aproximadas.
- Formularios validados, permisos de ubicación/cámara y pruebas unitarias y de interfaz.

Los datos utilizados con fines académicos deben ser ficticios. El inicio de sesión se almacena localmente y no reemplaza un sistema de autenticación de producción.

## Requisitos

- Android Studio y JDK 17.
- Emulador o dispositivo Android con API 26 o superior.
- Para crear y sincronizar alertas remotas: MySQL y el repositorio `microservicioalertas` ejecutándose en el computador.
- Conexión a internet para dependencias Gradle y la geocodificación Nominatim.

## Ejecutar la app

1. Clona este repositorio y abre su carpeta en Android Studio.
2. Espera a que termine la sincronización de Gradle y selecciona un emulador o dispositivo.
3. Inicia primero el microservicio siguiendo su README. El servicio debe escuchar en el puerto `8081`.
4. Ejecuta la configuración `app` desde Android Studio.
5. Para probar GPS en un emulador: **Extended controls → Location**, define una ubicación y vuelve al formulario de alerta.

La configuración de Retrofit usa `http://10.0.2.2:8081/`, la dirección del computador vista desde el emulador Android estándar. En un teléfono físico, configura la dirección IP local del computador y asegúrate de que ambos estén en la misma red. No publiques el servicio en una red no confiable usando HTTP.

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
