# EduTask

**Proyecto Final: Desarrollo para Dispositivos Inteligentes 2026**

---

### Datos del Equipo
* **Materia:** Desarrollo para Dispositivos Inteligentes
* **Grupo:** GIDS6093
* **Estudiantes:**
  * Carlos Manuel Palma Muñoz
  * Agustin Lopez Parra

---

### Objetivo del Proyecto
El objetivo de **EduTask** es crear una plataforma educativa completa que funcione en diferentes dispositivos (celulares, relojes inteligentes y, próximamente, pantallas de TV). Buscamos hacer más fácil la vida escolar: los alumnos pueden ver sus tareas y subir fotos de sus trabajos, los profesores pueden calificar rápido (incluso usando su smartwatch), y los coordinadores pueden gestionar los eventos de la escuela de manera organizada. *(Nota: La aplicación para TV aún no está realizada, ya que está contemplada para la Unidad de aprendizaje III).*

---

### Funcionalidades Principales

Nuestra plataforma se divide en las siguientes grandes áreas, dependiendo del usuario y el dispositivo:

**1. App Móvil (Para Alumnos)**
* Ver las clases en las que están inscritos.
* Revisar las tareas pendientes y las que ya entregaron.
* Subir fotos o archivos para entregar evidencias de sus tareas usando la cámara.
* Ver las calificaciones y los comentarios que les dejó el profesor.
* Checar el calendario de eventos de la escuela.

**2. App Móvil (Para Profesores)**
* Crear tareas nuevas para sus clases y ponerles fecha límite.
* Ver la lista de alumnos inscritos en cada clase.
* Revisar las fotos/archivos que mandan los alumnos y evaluarlas.
* Ver gráficas y estadísticas sencillas de cómo va la clase.

**3. Wear OS (Reloj Inteligente para el Profesor)**
* Recibir notificaciones (con vibración) al instante en que un alumno entrega una tarea.
* Ver la foto de la tarea directamente en la pantalla del reloj.
* Calificar rápidamente la entrega usando botones directamente en el smartwatch.

**4. Módulo para el Coordinador (App Móvil)**
* Dar de alta, editar o eliminar usuarios (alumnos, profesores y otros coordinadores).
* Administrar los eventos de la escuela (fechas de exámenes, entrega de calificaciones, etc.).

**5. Android TV (Pendiente)**
* Esta aplicación aún no está realizada ya que queda pendiente para la **Unidad de aprendizaje III. Aplicaciones para pantallas inteligentes**. Por el momento solo se cuenta con la estructura base del proyecto.

---

### Tecnologías que usamos

* **Android & Wear OS:** Kotlin.
* **Diseño e Interfaz:** Jetpack Compose (Material 3, Adaptive Navigation, Wear Compose).
* **Base de Datos en la nube:** Firebase Cloud Firestore (para guardar usuarios, tareas y clases).
* **Almacenamiento en la nube:** Firebase Storage (para guardar las fotos y archivos de las tareas).
* **Componentes extra:** WorkManager y servicios en segundo plano para manejar las notificaciones del reloj.
* **Automatización:** Un script en Python (`poblar_base_datos.py`) que hicimos para llenar nuestra base de datos con datos de prueba fácilmente.

---

### Cómo ejecutar el proyecto

Para probar el proyecto en tu computadora, estos son los pasos a seguir:

**Requisitos Previos:**
* Android Studio instalado.
* Tener configurados emuladores (un teléfono, un reloj con Wear OS y una Android TV) o tener dispositivos físicos a la mano.
* (Opcional) Python 3 instalado si quieres correr el script de prueba de la base de datos.

**Paso 1: Preparar la base de datos**
1. Abre una terminal dentro de la carpeta del proyecto.
2. Instala la librería de Firebase: `pip install firebase-admin`
3. Corre el script: `python poblar_base_datos.py` (esto automáticamente insertará alumnos y clases ficticias para poder probar la app).

**Paso 2: Correr la aplicación**
1. Abre la carpeta del proyecto en Android Studio.
2. En el menú superior de ejecución, selecciona el módulo que deseas probar:
   * Elige el módulo **`app`** para correr la app en un celular o tablet.
   * Elige el módulo **`wear`** para correr la app en el reloj inteligente.
   * Elige el módulo **`tv`** para correr la pantalla de televisión.
3. Da clic en el botón de **Run** (el ícono de 'play' verde).

*(Nota: Dentro de la carpeta `APK/` que viene en el proyecto ya están los archivos `.apk` generados y listos para instalarse en un dispositivo físico si se requiere).*

---

### Evidencias y Capturas de Pantalla

A continuación demostramos el funcionamiento de las distintas partes del sistema:

**1. Módulo del Alumno (App Móvil)**

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/50e3f05c-ca6d-425b-9614-43cfeadce754" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/1daa8231-faa9-4112-aa3d-75e39826c815" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/4a8b7735-b963-4a15-bf39-ab2f03637072" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/bb6ba882-8194-45ae-8658-fffca4fa65a8" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/8fd93c65-d2ab-48dd-a281-6f7a7388d2b2" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/72306a37-18ba-494c-9ef4-4bb023ba2b3f" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/57b0bb28-0165-4b15-a3f4-dbc975c8c2e2" />

**2. Módulo del Profesor (App Móvil)**


**3. Módulo del Reloj (Wear OS)**


**4. Módulo del Coordinador**


**5. Pantalla Institucional (Android TV)**



**6. Base de Datos (Firebase)**
<img width="1920" height="1080" alt="{FCFEF5E6-69CB-4292-8C93-3FA6B453C99A}" src="https://github.com/user-attachments/assets/395bf70a-5647-4b7a-ad19-73a30fd1cf06" />

<img width="1920" height="1080" alt="{55631EB2-2F2E-4DF3-9F86-01498EA9A92A}" src="https://github.com/user-attachments/assets/fc061273-b1c6-45d2-89a9-9e67e141a72a" />
