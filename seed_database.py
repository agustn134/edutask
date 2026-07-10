"""
seed_database.py
=================
Script de siembra (seed) para la base de datos Firestore del proyecto EduTask.

Estructura basada en el Diagrama de Clases EduTask:
    Usuario, Clase, ClaseAlumno, Tarea, AsignacionTarea, EvidenciaTarea,
    NotificacionReloj, Calificacion, EventoEscolar

Enumeraciones:
    RolUsuario      -> "Alumno" | "Profesor" | "Coordinador"
    EstadoEvidencia -> "Pendiente" | "Aprobada" | "Rechazada"

Requisitos:
    pip install firebase-admin

Uso:
    python seed_database.py
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timedelta

# --------------------------------------------------------------------------
# 1. CONFIGURACIÓN E INICIALIZACIÓN
# --------------------------------------------------------------------------

# Nombre del archivo de credenciales descargado desde Firebase Console.
# Ajusta este nombre si tu archivo .json se llama distinto.
CREDENTIALS_FILE = "edutask-cmpm-alp-firebase-adminsdk-fbsvc-c5dd688528.json"

cred = credentials.Certificate(CREDENTIALS_FILE)
firebase_admin.initialize_app(cred)
db = firestore.client()

# --------------------------------------------------------------------------
# 2. ENUMERACIONES (como constantes de Python)
# --------------------------------------------------------------------------

class RolUsuario:
    ALUMNO = "Alumno"
    PROFESOR = "Profesor"
    COORDINADOR = "Coordinador"


class EstadoEvidencia:
    PENDIENTE = "Pendiente"
    APROBADA = "Aprobada"
    RECHAZADA = "Rechazada"


# --------------------------------------------------------------------------
# 3. UTILIDADES
# --------------------------------------------------------------------------

def ahora():
    """Devuelve el timestamp actual (usado como fechas de referencia)."""
    return datetime.now()


def limpiar_coleccion(nombre_coleccion):
    """
    Elimina todos los documentos de una colección antes de sembrar.
    Útil para poder correr el script varias veces sin duplicar datos.
    """
    docs = db.collection(nombre_coleccion).stream()
    borrados = 0
    for doc in docs:
        doc.reference.delete()
        borrados += 1
    if borrados:
        print(f"   🧹 {nombre_coleccion}: {borrados} documento(s) eliminado(s) antes de sembrar.")


# --------------------------------------------------------------------------
# 4. FUNCIONES DE SIEMBRA POR ENTIDAD
# --------------------------------------------------------------------------

def sembrar_usuarios():
    """
    Colección: usuarios
    Atributos: idUsuario, nombre, matricula, correo, contrasena, rol

    Nota: 'matricula' es el identificador con el que el usuario inicia sesión
    (ver Usuario.iniciarSesion(matricula, pass) en el diagrama).

    ⚠️ DESARROLLO: las contraseñas se guardan en texto plano únicamente para
    facilitar las pruebas. Antes de pasar a producción hay que hashearlas
    (bcrypt u otro algoritmo) y jamás dejarlas legibles en la base de datos.
    """
    print("👤 Sembrando 'usuarios'...")
    limpiar_coleccion("usuarios")

    usuarios = {
        "alumno_001": {
            "nombre": "Alumno de prueba",
            "matricula": "A12345",
            "correo": "alumno.prueba@edutask.mx",
            "contrasena": "alumno123",
            "rol": RolUsuario.ALUMNO,
        },
        "profesor_001": {
            "nombre": "Profesor de prueba",
            "matricula": "P98765",
            "correo": "profesor.prueba@edutask.mx",
            "contrasena": "profesor123",
            "rol": RolUsuario.PROFESOR,
        },
        "coordinador_001": {
            "nombre": "Coordinador de prueba",
            "matricula": "C00001",
            "correo": "coordinador.prueba@edutask.mx",
            "contrasena": "coord123",
            "rol": RolUsuario.COORDINADOR,
        },
    }

    for id_usuario, datos in usuarios.items():
        db.collection("usuarios").document(id_usuario).set(datos)

    print(f"   ✅ {len(usuarios)} usuarios creados.")
    return usuarios


def sembrar_clases(id_profesor):
    """
    Colección: clases
    Atributos: idClase, nombre, descripcion, enlace, idProfesor (relación "Crea")
    """
    print("🏫 Sembrando 'clases'...")
    limpiar_coleccion("clases")

    clases = {
        "clase_001": {
            "nombre": "Matemáticas III",
            "descripcion": "Álgebra y geometría analítica",
            "enlace": "https://meet.edutask.mx/mate3",
            "idUsuario": id_profesor,  # profesor que crea la clase
        },
        "clase_002": {
            "nombre": "Programación Básica",
            "descripcion": "Introducción a la lógica de programación",
            "enlace": "https://meet.edutask.mx/prog1",
            "idUsuario": id_profesor,
        },
    }

    for id_clase, datos in clases.items():
        db.collection("clases").document(id_clase).set(datos)

    print(f"   ✅ {len(clases)} clases creadas.")
    return clases


def sembrar_clase_alumno(clases_ids, alumnos_ids):
    """
    Colección: clase_alumno
    Atributos: idClaseAlumno, idClase, idUsuario (inscripción)
    """
    print("📋 Sembrando 'clase_alumno' (inscripciones)...")
    limpiar_coleccion("clase_alumno")

    inscripciones = {}
    contador = 1
    for id_clase in clases_ids:
        for id_alumno in alumnos_ids:
            id_insc = f"insc_{contador:03d}"
            inscripciones[id_insc] = {
                "idClase": id_clase,
                "idUsuario": id_alumno,
            }
            contador += 1

    for id_insc, datos in inscripciones.items():
        db.collection("clase_alumno").document(id_insc).set(datos)

    print(f"   ✅ {len(inscripciones)} inscripciones creadas.")
    return inscripciones


def sembrar_tareas(id_clase):
    """
    Colección: tareas
    Atributos: idTarea, titulo, descripcion, fechaLimite, idClase (relación "Pertenece a")
    """
    print("📝 Sembrando 'tareas'...")
    limpiar_coleccion("tareas")

    tareas = {
        "tarea_001": {
            "titulo": "Ejercicios de factorización",
            "descripcion": "Resolver los ejercicios 1 al 20 de la página 45",
            "fechaLimite": ahora() + timedelta(days=5),
            "idClase": id_clase,
        },
        "tarea_002": {
            "titulo": "Proyecto de geometría analítica",
            "descripcion": "Entregar el análisis de las cónicas asignadas",
            "fechaLimite": ahora() + timedelta(days=10),
            "idClase": id_clase,
        },
    }

    for id_tarea, datos in tareas.items():
        db.collection("tareas").document(id_tarea).set(datos)

    print(f"   ✅ {len(tareas)} tareas creadas.")
    return tareas


def sembrar_asignaciones_tarea(tareas_ids, alumnos_ids):
    """
    Colección: asignaciones_tarea
    Atributos: idAsignacion, idUsuario, idTarea, fechaAsignacion
    """
    print("📌 Sembrando 'asignaciones_tarea'...")
    limpiar_coleccion("asignaciones_tarea")

    asignaciones = {}
    contador = 1
    for id_tarea in tareas_ids:
        for id_alumno in alumnos_ids:
            id_asig = f"asig_{contador:03d}"
            asignaciones[id_asig] = {
                "idUsuario": id_alumno,
                "idTarea": id_tarea,
                "fechaAsignacion": ahora(),
            }
            contador += 1

    for id_asig, datos in asignaciones.items():
        db.collection("asignaciones_tarea").document(id_asig).set(datos)

    print(f"   ✅ {len(asignaciones)} asignaciones creadas.")
    return asignaciones


def sembrar_evidencias_tarea(asignaciones_ids):
    """
    Colección: evidencias_tarea
    Atributos: idEvidencia, tituloTarea, fotoUrl, fechaEnvio, estado, idAsignacion
    """
    print("📸 Sembrando 'evidencias_tarea'...")
    limpiar_coleccion("evidencias_tarea")

    evidencias = {}
    estados = [EstadoEvidencia.PENDIENTE, EstadoEvidencia.APROBADA, EstadoEvidencia.RECHAZADA]

    for i, id_asignacion in enumerate(asignaciones_ids):
        id_evidencia = f"evid_{i + 1:03d}"
        evidencias[id_evidencia] = {
            "tituloTarea": "Evidencia de entrega",
            "fotoUrl": f"https://storage.edutask.mx/evidencias/evid_{i + 1:03d}.jpg",
            "fechaEnvio": ahora(),
            "estado": estados[i % len(estados)],
            "idAsignacion": id_asignacion,
        }

    for id_evidencia, datos in evidencias.items():
        db.collection("evidencias_tarea").document(id_evidencia).set(datos)

    print(f"   ✅ {len(evidencias)} evidencias creadas.")
    return evidencias


def sembrar_notificaciones_reloj(evidencias_ids):
    """
    Colección: notificaciones_reloj
    Atributos: idNotificacion, mensajeAlerta, fechaHoraRecepcion, fueLeida, idEvidencia
    Relación 1→1 con EvidenciaTarea ("Genera").
    """
    print("⌚ Sembrando 'notificaciones_reloj'...")
    limpiar_coleccion("notificaciones_reloj")

    notificaciones = {}
    for i, id_evidencia in enumerate(evidencias_ids):
        id_notif = f"notif_{i + 1:03d}"
        notificaciones[id_notif] = {
            "mensajeAlerta": "Nueva evidencia enviada, revisa tu smartwatch",
            "fechaHoraRecepcion": ahora(),
            "fueLeida": False,
            "idEvidencia": id_evidencia,
        }

    for id_notif, datos in notificaciones.items():
        db.collection("notificaciones_reloj").document(id_notif).set(datos)

    print(f"   ✅ {len(notificaciones)} notificaciones creadas.")
    return notificaciones


def sembrar_calificaciones(evidencias_ids, id_profesor):
    """
    Colección: calificaciones
    Atributos: idCalificacion, idEvidencia, idUsuario, valor, comentario, fechaCalificacion
    """
    print("🏆 Sembrando 'calificaciones'...")
    limpiar_coleccion("calificaciones")

    calificaciones = {}
    for i, id_evidencia in enumerate(evidencias_ids):
        id_calif = f"calif_{i + 1:03d}"
        calificaciones[id_calif] = {
            "idEvidencia": id_evidencia,
            "idUsuario": id_profesor,  # profesor que califica
            "valor": 8 + (i % 3),  # valores de ejemplo entre 8 y 10
            "comentario": "Buen trabajo, revisa la ortografía.",
            "fechaCalificacion": ahora(),
        }

    for id_calif, datos in calificaciones.items():
        db.collection("calificaciones").document(id_calif).set(datos)

    print(f"   ✅ {len(calificaciones)} calificaciones creadas.")
    return calificaciones


def sembrar_eventos_escolares(id_coordinador):
    """
    Colección: eventos_escolares
    Atributos: idEvento, idUsuario, titulo, descripcion, fechaEvento, lugar
    """
    print("📅 Sembrando 'eventos_escolares'...")
    limpiar_coleccion("eventos_escolares")

    eventos = {
        "evento_001": {
            "idUsuario": id_coordinador,
            "titulo": "Junta de padres de familia",
            "descripcion": "Entrega de boletas del primer parcial",
            "fechaEvento": ahora() + timedelta(days=15),
            "lugar": "Auditorio principal",
        },
        "evento_002": {
            "idUsuario": id_coordinador,
            "titulo": "Semana cultural",
            "descripcion": "Actividades artísticas y deportivas",
            "fechaEvento": ahora() + timedelta(days=30),
            "lugar": "Patio central",
        },
    }

    for id_evento, datos in eventos.items():
        db.collection("eventos_escolares").document(id_evento).set(datos)

    print(f"   ✅ {len(eventos)} eventos creados.")
    return eventos


# --------------------------------------------------------------------------
# 5. ORQUESTADOR PRINCIPAL
# --------------------------------------------------------------------------

def main():
    print("=" * 60)
    print("  INICIANDO SIEMBRA DE BASE DE DATOS — EduTask (Firestore)")
    print("=" * 60)

    usuarios = sembrar_usuarios()
    id_profesor = "profesor_001"
    id_coordinador = "coordinador_001"
    alumnos_ids = [uid for uid, datos in usuarios.items() if datos["rol"] == RolUsuario.ALUMNO]

    clases = sembrar_clases(id_profesor)
    clases_ids = list(clases.keys())

    sembrar_clase_alumno(clases_ids, alumnos_ids)

    tareas = sembrar_tareas(clases_ids[0])
    tareas_ids = list(tareas.keys())

    asignaciones = sembrar_asignaciones_tarea(tareas_ids, alumnos_ids)
    asignaciones_ids = list(asignaciones.keys())

    evidencias = sembrar_evidencias_tarea(asignaciones_ids)
    evidencias_ids = list(evidencias.keys())

    sembrar_notificaciones_reloj(evidencias_ids)
    sembrar_calificaciones(evidencias_ids, id_profesor)
    sembrar_eventos_escolares(id_coordinador)

    print("=" * 60)
    print("  ✅ SIEMBRA COMPLETADA CON ÉXITO")
    print("=" * 60)


if __name__ == "__main__":
    main()