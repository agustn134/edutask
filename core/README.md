# Módulo Core — EduTask Core (Modelos y Lógica Compartida)

El módulo `:core` es una biblioteca compartida de Android que centraliza los **modelos de datos comunes**, **DTOs de agregación** y **ViewModels compartidos** utilizados tanto por la aplicación móvil (`:app`) como por el módulo de Android TV (`:tv`).

---

## 1. Librerías y Dependencias

Definidas en `core/build.gradle.kts`:
- `com.google.firebase:firebase-firestore`: Conexión directa a base de datos NoSQL para queries compartidas.
- `androidx.lifecycle:lifecycle-viewmodel-ktx`: Soporte para ViewModels y `viewModelScope`.
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`: Manejo de concurrencia y StateFlows.

---

## 2. Documentación de Modelos y Clases

### Modelos de Datos (`com.pmlp.edutask.model`)

#### `Evento.kt`
Modelo que representa una noticia o evento institucional publicado por la coordinación:
```kotlin
data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaPublicacion: Long = 0L,
    val lugar: String = "",
    val imagenUrl: String? = null
)
```

#### `Calificacion.kt`
Modelo que representa una calificación asignada a una entrega de tarea:
```kotlin
data class Calificacion(
    val id: String = "",
    val idEvidencia: String = "",
    val idUsuario: String = "",
    val valor: Int = 0,
    val comentario: String = "",
    val esBorrador: Boolean = false,
    val fechaCalificacion: Date? = null
)
```

#### `EstadisticaGrupo.kt`
Modelos de agregación utilizados por el Dashboard de TV y pantallas de monitoreo:
```kotlin
data class PromedioMateria(
    val idTarea: String = "",
    val nombreTarea: String = "",
    val promedio: Double? = null,
    val totalCalificados: Int = 0
)

data class EstadisticaGrupo(
    val idClase: String = "",
    val nombreClase: String = "",
    val promedioGeneral: Double? = null,
    val totalAlumnos: Int = 0,
    val alumnosCalificados: Int = 0,
    val promediosPorTarea: List<PromedioMateria> = emptyList()
)
```

---

### ViewModels Compartidos (`com.pmlp.edutask.ui`)

#### `EventosSharedViewModel.kt`
- **`sealed class EventosUiState`**: `Loading`, `Success(eventos)`, `Error(message)`.
- **`sealed class EstadisticasUiState`**: `Loading`, `Success(grupos)`, `Error(message)`.
- **Funciones Principales:**
  - `fetchEventos()`: Escucha en tiempo real la colección `eventos` de Firestore.
  - `fetchEstadisticasInstitucionales()`: Establece un `addSnapshotListener` sobre `evidencias_tarea` que recalcula automáticamente las estadísticas de todos los grupos del sistema cada vez que se registra una nueva calificación.
  - `calcularEstadisticas()`: Realiza el cruce relacional entre `clases`, `clase_alumno`, `tareas`, `asignaciones_tarea` y `evidencias_tarea` para computar promedios precisos.
