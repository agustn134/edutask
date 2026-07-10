# Diagrama de Clases — EduTask

## 1. Entidades del Sistema

| # | Clase | Atributos | Métodos |
|---|-------|-----------|---------|
| 1 | **Usuario** | idUsuario: Integer · nombre: String · Correo: String · contrasena: String · rol: RolUsuario | + iniciarSesion(matricula: String, pass: String): Boolean |
| 2 | **Tarea** | idTarea: Integer · titulo: String · descripcion: String · fechaLimite: DateTime · idClase: Integer | + crearTarea(): void |
| 3 | **AsignacionTarea** | idAsignacion: Integer · idUsuario: Integer · idTarea: Integer · fechaAsignacion: DateTime | + asignarTarea(): void |
| 4 | **EvidenciaTarea** | idEvidencia: Integer · tituloTarea: String · fotoUrl: String · fechaEnvio: DateTime · estado: EstadoEvidencia · idAsignacion: Integer | + capturarFoto(): String<br>+ enviarEvidencia(): void |
| 5 | **NotificacionReloj** | idNotificacion: Integer · mensajeAlerta: String · fechaHoraRecepcion: DateTime · fueLeida: Boolean | + mostrarAlertaVibracion(): void<br>+ calificarEvidencia(valor: Integer): void<br>+ procesarTeclaDPad(codigoTecla: Integer): void<br>+ seleccionarElementoOK(): void |
| 6 | **Calificacion** | idCalificacion: Integer · idEvidencia: Integer · idUsuario: Integer · valor: Integer · comentario: String · fechaCalificacion: DateTime | + calificar(): void<br>+ actualizarCalificacion(): void |
| 7 | **Clase** | idClase: Integer · nombre: String · descripcion: String · enlace: String | + crearClase(): void<br>+ eliminarClase(): void |
| 8 | **ClaseAlumno** | idClaseAlumno: Integer · idClase: Integer · idUsuario: Integer | + inscribir(): void |
| 9 | **EventoEscolar** | idEvento: Integer · idUsuario: Integer · titulo: String · descripcion: String · fechaEvento: DateTime · lugar: String | + crearEvento(): void<br>+ actualizarEvento(): void<br>+ eliminarEvento(): void |

---

## 2. Enumeraciones referenciadas (no definidas explícitamente en el diagrama)

| Enum | Usada en | Valores sugeridos (a definir) |
|------|----------|-------------------------------|
| `RolUsuario` | Usuario.rol | Alumno, Profesor, Coordinador |
| `EstadoEvidencia` | EvidenciaTarea.estado | Pendiente, Aprobada, Rechazada |

> **Observación de revisión:** el diagrama usa los tipos `RolUsuario` y `EstadoEvidencia` como tipos de dato en los atributos, pero ninguna de las dos enumeraciones aparece modelada como clase/enum independiente. Se recomienda agregarlas explícitamente para que el diagrama esté completo.

---

## 3. Relaciones entre clases

| Origen | Destino | Cardinalidad | Descripción / Etiqueta |
|--------|---------|--------------|--------------------------|
| Usuario | AsignacionTarea | 1 → 0..n | "Crea" (el Profesor genera asignaciones) |
| AsignacionTarea | Tarea | 0..n → 1 | Una asignación referencia una tarea |
| AsignacionTarea | EvidenciaTarea | 1 → 0..n | Una asignación puede tener evidencias entregadas |
| EvidenciaTarea | NotificacionReloj | 1 → 1 | "Genera" (al enviarse la evidencia se dispara la notificación) |
| EvidenciaTarea | Usuario | 1 → 1 | "Recibida por el rol Profesor" |
| Usuario | Clase | 1 → 0..n | "Crea" (el Profesor crea clases) |
| Clase | ClaseAlumno | 1 → 0..n | Una clase tiene múltiples inscripciones |
| Usuario | ClaseAlumno | 1 → 0..n | Un alumno se inscribe en varias clases |
| Usuario | Calificacion | 1 → 0..n | "Califica" (el Profesor califica evidencias) |
| Calificacion | EvidenciaTarea | 0..n → 1 | Cada calificación corresponde a una evidencia |
| Tarea | Clase | 0..n → 1 | "Pertenece" (una tarea pertenece a una clase) |
| Usuario | EventoEscolar | 1 → 0..n | El usuario (Coordinador) crea eventos escolares |

---

## 4. Representación visual (Mermaid)

classDiagram
    %% Enumeraciones
    class RolUsuario {
        <<enumeration>>
        Alumno
        Profesor
        Coordinador
    }
    class EstadoEvidencia {
        <<enumeration>>
        Pendiente
        Aprobada
        Rechazada
    }

    %% Clases Principales
    class Usuario {
        -Integer idUsuario
        -String nombre
        -String correo
        -String contrasena
        -RolUsuario rol
        +Usuario(nombre, correo, contrasena, rol)
        +iniciarSesion(matricula, pass) Boolean
    }

    class Tarea {
        -Integer idTarea
        -String titulo
        -String descripcion
        -DateTime fechaLimite
        +Tarea(titulo, descripcion, fechaLimite)
        +crearTarea() void
    }

    class AsignacionTarea {
        -Integer idAsignacion
        -DateTime fechaAsignacion
        +AsignacionTarea(fechaAsignacion)
        +asignarTarea() void
    }

    class EvidenciaTarea {
        -Integer idEvidencia
        -String tituloTarea
        -String fotoUrl
        -DateTime fechaEnvio
        -EstadoEvidencia estado
        +EvidenciaTarea(titulo, fotoUrl)
        +capturarFoto() String
        +enviarEvidencia() void
    }

    class NotificacionReloj {
        -Integer idNotificacion
        -String mensajeAlerta
        -DateTime fechaHoraRecepcion
        -Boolean fueLeida
        +mostrarAlertaVibracion() void
        +procesarTeclaDPad(codigoTecla) void
        +seleccionarElementoOK() void
    }

    class Calificacion {
        -Integer idCalificacion
        -Integer valor
        -String comentario
        -DateTime fechaCalificacion
        +Calificacion(valor, comentario)
        +calificar() void
        +actualizarCalificacion() void
    }

    class Clase {
        -Integer idClase
        -String nombre
        -String descripcion
        -String enlace
        +Clase(nombre, descripcion, enlace)
        +crearClase() void
        +eliminarClase() void
    }

    class ClaseAlumno {
        -Integer idClaseAlumno
        +inscribir() void
    }

    class EventoEscolar {
        -Integer idEvento
        -String titulo
        -String descripcion
        -DateTime fechaEvento
        -String lugar
        +crearEvento() void
        +actualizarEvento() void
        +eliminarEvento() void
    }

    %% Relaciones
    Usuario "1" --> "0..n" AsignacionTarea : Crea (Profesor)
    Tarea "1" --> "0..n" AsignacionTarea : Es asignada en
    AsignacionTarea "1" --> "0..n" EvidenciaTarea : Genera
    EvidenciaTarea "1" --> "1" NotificacionReloj : Dispara
    EvidenciaTarea "1" ..> "1" Usuario : Notifica a (Profesor)
    Usuario "1" --> "0..n" Clase : Crea (Profesor)
    Clase "1" --> "0..n" ClaseAlumno : Tiene
    Usuario "1" --> "0..n" ClaseAlumno : Se inscribe (Alumno)
    Usuario "1" --> "0..n" Calificacion : Emite (Profesor)
    Calificacion "0..n" --> "1" EvidenciaTarea : Evalua
    Tarea "0..n" --> "1" Clase : Pertenece a
    Usuario "1" --> "0..n" EventoEscolar : Organiza (Coordinador)
    
    %% Uso de Enums
    Usuario --> RolUsuario
    EvidenciaTarea --> EstadoEvidencia
