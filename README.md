Listed directory edutask
Viewed Diagrama%20de%20Clases%E2%80%94EduTask.md:1-167
Viewed poblar_base_datos.py:1-503
Listed directory app
Viewed settings.gradle.kts:1-29
Listed directory main
Viewed AndroidManifest.xml:1-43
Listed directory java
Searched for "package "
Searched for "package "
Searched for "package "
Viewed MainActivitytv.kt:1-47
Viewed MainActivityWear.kt:1-524
Viewed MainActivity.kt:1-55
Viewed build.gradle.kts:1-6
Listed directory gradle
Viewed libs.versions.toml:1-64
Viewed build.gradle.kts:1-74

Aquí tienes la información detallada sobre el proyecto **EduTask**:

### 1. Nombre del proyecto
* **EduTask**

### 2. Nombres de los estudiantes
De acuerdo con las iniciales presentes en la configuración de Firebase (`cmpm` y `alp`) y los datos correspondientes en el script de carga de base de datos, los desarrolladores y estudiantes son:
* **Carlos Manuel Palma Muñoz**
* **Agustin Lopez Parra**

---

### 3. Objetivo del proyecto
Desarrollar una plataforma educativa multidispositivo (**Android Móvil/Tablet**, **Wear OS Smartwatch** y **Android TV**) que optimice la gestión escolar, permitiendo a los coordinadores organizar eventos, a los profesores asignar y calificar tareas de forma ágil (incluso desde el reloj inteligente mediante notificaciones táctiles y vibración), y a los alumnos visualizar sus clases, subir evidencias fotográficas o de archivos y ver sus calificaciones en tiempo real.

---

### 4. Descripción de las funcionalidades

* **Módulo Alumno (Dispositivos Móviles)**:
  * Visualización de clases en las que está inscrito.
  * Seguimiento de tareas pendientes y completadas con fechas límite.
  * Envío de evidencias escolares capturando fotos con la cámara o cargando archivos.
  * Consulta de calificaciones finales e historial de comentarios de retroalimentación de los profesores.
  * Agenda de eventos institucionales y escolares.

* **Módulo Profesor (Móvil & Wear OS)**:
  * **Móvil**: Creación de tareas asignadas a clases, control de la lista de alumnos, consulta de analíticas y estadísticas de rendimiento, y evaluación detallada de evidencias con comentarios.
  * **Wear OS (Smartwatch)**: Recepción de alertas de vibración háptica ante nuevas entregas, visualización en pantalla completa de fotos de evidencia y sistema de calificación rápida con un solo toque (notas del 5 al 10).

* **Módulo Coordinador (Móvil)**:
  * Gestión total de usuarios: registro, edición y control de roles (Alumno, Profesor, Coordinador).
  * Agenda Escolar: Creación, actualización y eliminación de eventos institucionales como evaluaciones, entrega de boletas y semanas culturales.

* **Módulo TV (Android TV)**:
  * Interfaz base diseñada para mostrar avisos y pantallas de bienvenida generales a nivel institucional.

---

### 5. Tecnologías utilizadas

* **Lenguajes de programación**: Kotlin (Android, Wear OS, Android TV) y Python (para la automatización e inserción de datos de prueba).
* **Base de datos y Almacenamiento**: Google Firebase (Cloud Firestore para la base de datos relacional orientada a documentos y Firebase Storage para almacenar las evidencias del alumno).
* **Interfaz de usuario**: Jetpack Compose (Compose Material 3, Adaptive Navigation Suite, Jetpack Compose para Wear OS, y Android TV Compose).
* **Componentes de Android**:
  * **WorkManager**: Para programar y procesar notificaciones y recordatorios en segundo plano.
  * **Servicios de Wear OS**: Sincronización en la nube, comunicación mediante `SessionListenerService` y control vibratorio háptico.
  * **FileProvider**: Para la gestión segura del acceso a la cámara y fotos del dispositivo.
* **Herramientas de construcción**: Gradle utilizando Gradle Kotlin DSL y Version Catalogs (`libs.versions.toml`).

---

### 6. Instrucciones para ejecutar el proyecto

#### Requisitos previos
1. Tener instalado **Android Studio** (versión reciente como Koala o Ladybug).
2. Tener configurado **JDK 17+**.
3. Contar con un emulador de Android (Teléfono), un emulador de Wear OS (Reloj) y/o emulador de Android TV, o bien dispositivos físicos configurados en modo depuración.
4. Python 3 instalado con la biblioteca de Firebase.

#### Paso 1: Configurar y poblar la base de datos (Firestore)
Para inicializar Firestore con datos reales de prueba, ejecuta el script de Python provisto:
```bash
pip install firebase-admin
python poblar_base_datos.py
```
*(Este script utiliza el archivo de credenciales de la cuenta de servicio [edutask-cmpm-alp-firebase-adminsdk-fbsvc-c5dd688528.json](file:///c:/Users/agust/StudioProjects/edutask/edutask-cmpm-alp-firebase-adminsdk-fbsvc-c5dd688528.json) ya incluido en el proyecto)*.

#### Paso 2: Importar el proyecto y sincronizar Gradle
1. Abre Android Studio y selecciona **Open** para importar la carpeta raíz del proyecto `c:\Users\agust\StudioProjects\edutask`.
2. Espera a que finalice la sincronización automática de Gradle.
3. Si prefieres compilar desde la terminal, puedes usar el wrapper de Gradle:
   * **Windows**: `.\gradlew.bat assembleDebug`
   * **Linux/macOS**: `./gradlew assembleDebug`

#### Paso 3: Ejecutar los módulos individuales
* **Aplicación Móvil**: Selecciona el módulo `app` en la barra superior de configuraciones de ejecución, elige tu emulador/dispositivo móvil y presiona **Run** (Ejecuta la clase principal [MainActivity](file:///c:/Users/agust/StudioProjects/edutask/app/src/main/java/com/pmlp/edutask/MainActivity.kt)).
* **Aplicación Smartwatch**: Selecciona el módulo `wear`, elige tu emulador/dispositivo Wear OS y presiona **Run** (Inicia la actividad [MainActivityWear](file:///c:/Users/agust/StudioProjects/edutask/wear/src/main/java/com/pmlp/wear/presentation/MainActivityWear.kt#L45)).
* **Aplicación TV**: Selecciona el módulo `tv`, elige tu emulador/dispositivo de Android TV y presiona **Run** (Inicia la actividad [MainActivitytv](file:///c:/Users/agust/StudioProjects/edutask/tv/src/main/java/com/pmlp/tv/MainActivitytv.kt)).
