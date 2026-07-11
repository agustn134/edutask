#!/usr/bin/env python3
"""
poblar_base_datos.py  ·  EduTask
──────────────────────────────────────────────────────────────
Limpia TODAS las colecciones de Firestore e inserta datos
realistas: usuarios, clases, tareas, asignaciones,
evidencias, calificaciones y eventos escolares.

Uso:
    python poblar_base_datos.py
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timezone, timedelta

# ╔══════════════════════════════════════════╗
# ║  INICIALIZAR FIREBASE                   ║
# ╚══════════════════════════════════════════╝
cred = credentials.Certificate("edutask-cmpm-alp-firebase-adminsdk-fbsvc-c5dd688528.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

COLECCIONES = [
    "usuarios",
    "clases",
    "clase_alumno",
    "tareas",
    "asignaciones_tarea",
    "evidencias_tarea",
    "notificaciones_reloj",
    "calificaciones",
    "eventos_escolares",
]

# Imagen placeholder 1×1 px (base64) para evidencias de prueba
_FOTO = (
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwC"
    "AAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
)


def ts(delta_days: int = 0) -> datetime:
    """Objeto datetime desplazado N días desde hoy."""
    return datetime.now(timezone.utc) + timedelta(days=delta_days)


def save(col: str, doc_id: str, data: dict):
    db.collection(col).document(doc_id).set(data)


# ╔══════════════════════════════════════════╗
# ║  PASO 1 — BORRAR TODO                   ║
# ╚══════════════════════════════════════════╝
def borrar_todo():
    print("\n" + "=" * 62)
    print("  LIMPIANDO BASE DE DATOS — EduTask (Firestore)")
    print("=" * 62)
    for col in COLECCIONES:
        docs = list(db.collection(col).stream())
        for doc in docs:
            doc.reference.delete()
        print(f"  🗑️  '{col}' → {len(docs)} doc(s) eliminado(s).")
    print("  ✅ Base de datos limpia.\n")


# ╔══════════════════════════════════════════╗
# ║  PASO 2 — POBLAR CON DATOS REALISTAS    ║
# ╚══════════════════════════════════════════╝
def poblar():
    print("=" * 62)
    print("  INSERTANDO DATOS — EduTask (Firestore)")
    print("=" * 62)

    # ── USUARIOS ────────────────────────────────────────────────
    #  Convención de clave:
    #    Coordinador → C-<INICIALES>
    #    Profesor    → P-<INICIALES>
    #    Alumno      → A-<INICIALES>
    #  (iniciales = primera letra de cada palabra del nombre completo)
    usuarios = {

        # ── Coordinador ─────────────────────────────────────────
        # BARRIENTOS AVALOS JOSE DE JESUS EDUARDO → B,A,J,J,D,E → BAJJDE
        "C-BAJJDE": {
            "nombre":     "Barrientos Avalos Jose De Jesus Eduardo",
            "matricula":  "C-BAJJDE",
            "correo":     "eduardo.barrientos@edutask.edu.mx",
            "rol":        "Coordinador",
            "contrasena": "linux",
        },

        # ── Profesores ──────────────────────────────────────────
        # GARCIA SANDOVAL MARTHA ELENA → G,S,M,E → GSME
        "P-GSME": {
            "nombre":     "Garcia Sandoval Martha Elena",
            "matricula":  "P-GSME",
            "correo":     "martha.garcia@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },
        # MARTÍNEZ MEJÍA ELSA VERÓNICA → M,M,E,V → MMEV
        "P-MMEV": {
            "nombre":     "Martinez Mejia Elsa Veronica",
            "matricula":  "P-MMEV",
            "correo":     "elsa.martinez@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },
        # TORRES YAÑEZ JAVIER JESUS → T,Y,J,J → TYJJ
        "P-TYJJ": {
            "nombre":     "Torres Yañez Javier Jesus",
            "matricula":  "P-TYJJ",
            "correo":     "javier.torres@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },
        # BARRON RODRIGUEZ GABRIEL → B,R,G → BRG
        "P-BRG": {
            "nombre":     "Barron Rodriguez Gabriel",
            "matricula":  "P-BRG",
            "correo":     "gabriel.barron@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },
        # RODRIGUEZ GARCIA ANASTACIO → R,G,A → RGA
        "P-RGA": {
            "nombre":     "Rodriguez Garcia Anastacio",
            "matricula":  "P-RGA",
            "correo":     "anastacio.rodriguez@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },
        # SALAZAR DELGADO CÉSAR ALEJANDRO → S,D,C,A → SDCA
        "P-SDCA": {
            "nombre":     "Salazar Delgado César Alejandro",
            "matricula":  "P-SDCA",
            "correo":     "cesar.salazar@edutask.edu.mx",
            "rol":        "Profesor",
            "contrasena": "linux",
        },

        # ── Alumnos ─────────────────────────────────────────────
        # AGUSTIN LOPEZ PARRA → A,L,P → ALP
        "A-ALP": {
            "nombre":     "Agustin Lopez Parra",
            "matricula":  "A-ALP",
            "correo":     "agustinlopezparra13@gmail.com",
            "rol":        "Alumno",
            "contrasena": "linux",
        },
        # CARLOS MANUEL PALMA MUÑOZ → C,M,P,M → CMPM
        "A-CMPM": {
            "nombre":     "Carlos Manuel Palma Muñoz",
            "matricula":  "A-CMPM",
            "correo":     "carlospalmaa74@gmail.com",
            "rol":        "Alumno",
            "contrasena": "linux",
        },
        # Alumnos adicionales para que se vea más real
        # KARLA RAMIREZ IBARRA → K,R,I → KRI
        "A-KRI": {
            "nombre":     "Karla Ramirez Ibarra",
            "matricula":  "A-KRI",
            "correo":     "karla.ramirez@edutask.edu.mx",
            "rol":        "Alumno",
            "contrasena": "linux",
        },
        # SOFIA LUCIA MENDOZA TORRES → S,L,M,T → SLMT
        "A-SLMT": {
            "nombre":     "Sofia Lucia Mendoza Torres",
            "matricula":  "A-SLMT",
            "correo":     "sofia.mendoza@edutask.edu.mx",
            "rol":        "Alumno",
            "contrasena": "linux",
        },
        # DIEGO GABRIEL ROMO SALINAS → D,G,R,S → DGRS
        "A-DGRS": {
            "nombre":     "Diego Gabriel Romo Salinas",
            "matricula":  "A-DGRS",
            "correo":     "diego.romo@edutask.edu.mx",
            "rol":        "Alumno",
            "contrasena": "linux",
        },
    }
    for uid, d in usuarios.items():
        save("usuarios", uid, d)
    print(f"\n  👤 usuarios        → {len(usuarios)}")

    # ── CLASES (1 por profesor, 6 en total) ─────────────────────
    clases = {
        "clase_001": {
            "idUsuario":   "P-GSME",
            "nombre":      "Dirección de Equipos de Alto Rendimiento",
            "enlace":      "https://meet.google.com/dar-ear-gsme",
            "descripcion": "Estrategias de liderazgo, gestión del talento y técnicas para potenciar equipos de trabajo de alto desempeño.",
        },
        "clase_002": {
            "idUsuario":   "P-MMEV",
            "nombre":      "Administración de Proyectos de TI",
            "enlace":      "https://meet.google.com/apt-iti-mmev",
            "descripcion": "Metodologías PMI y ágiles, gestión de alcance, tiempo, costo y riesgo en proyectos de tecnología.",
        },
        "clase_003": {
            "idUsuario":   "P-TYJJ",
            "nombre":      "Extracción de Conocimiento en Base de Datos",
            "enlace":      "https://meet.google.com/ecb-dat-tyjj",
            "descripcion": "Técnicas de minería de datos, preprocesamiento, clasificación, regresión y visualización de resultados.",
        },
        "clase_004": {
            "idUsuario":   "P-BRG",
            "nombre":      "Desarrollo Web Integral",
            "enlace":      "https://meet.google.com/dwi-web-brgx",
            "descripcion": "Desarrollo full-stack con HTML5, CSS3, JavaScript, frameworks modernos y consumo de APIs REST.",
        },
        "clase_005": {
            "idUsuario":   "P-RGA",
            "nombre":      "Desarrollo para Dispositivos Inteligentes",
            "enlace":      "https://meet.google.com/ddi-int-rgax",
            "descripcion": "Creación de aplicaciones nativas para Android usando Kotlin, Jetpack Compose y servicios en la nube.",
        },
        "clase_006": {
            "idUsuario":   "P-SDCA",
            "nombre":      "Inglés VIII",
            "enlace":      "https://meet.google.com/ing-eng-sdca",
            "descripcion": "Nivel avanzado: comprensión lectora técnica, redacción académica y presentaciones orales en inglés.",
        },
    }
    for cid, d in clases.items():
        save("clases", cid, d)
    print(f"  📚 clases          → {len(clases)}")

    # ── CLASE_ALUMNO (todos los alumnos en todas las clases) ─────
    alumnos_ids = [k for k in usuarios if k.startswith("A-")]
    inscripciones: dict = {}
    idx = 1
    for alumno in alumnos_ids:
        for clase in clases:
            key = f"insc_{idx:03d}"
            inscripciones[key] = {"idUsuario": alumno, "idClase": clase}
            idx += 1
    for iid, d in inscripciones.items():
        save("clase_alumno", iid, d)
    print(f"  🔗 clase_alumno    → {len(inscripciones)}")

    # ── TAREAS (2 por clase, 12 en total) ───────────────────────
    tareas = {
        # — clase_001: Dirección de Equipos de Alto Rendimiento —
        "tarea_001": {
            "idClase":     "clase_001",
            "titulo":      "Plan de Desarrollo de Equipo",
            "descripcion": "Elaborar un plan semestral de desarrollo para un equipo ficticio de 5 personas, incluyendo objetivos SMART, roles y métricas de desempeño.",
            "fechaLimite": ts(+5),
        },
        "tarea_002": {
            "idClase":     "clase_001",
            "titulo":      "Análisis de Liderazgo Situacional",
            "descripcion": "Estudiar el caso 'Equipo en crisis' y proponer un estilo de liderazgo adecuado según el modelo de Hersey y Blanchard.",
            "fechaLimite": ts(+12),
        },
        # — clase_002: Administración de Proyectos de TI —
        "tarea_003": {
            "idClase":     "clase_002",
            "titulo":      "Estructura de Desglose de Trabajo (EDT)",
            "descripcion": "Construir la EDT de un sistema de gestión escolar con al menos 3 niveles de descomposición y entregables definidos.",
            "fechaLimite": ts(+4),
        },
        "tarea_004": {
            "idClase":     "clase_002",
            "titulo":      "Cronograma con MS Project",
            "descripcion": "Capturar el EDT en MS Project, asignar recursos, definir dependencias y generar la ruta crítica del proyecto.",
            "fechaLimite": ts(+9),
        },
        # — clase_003: Extracción de Conocimiento en BD —
        "tarea_005": {
            "idClase":     "clase_003",
            "titulo":      "Preprocesamiento de Datos con Pandas",
            "descripcion": "Limpiar el dataset 'ventas_retail.csv': tratar valores nulos, eliminar duplicados, normalizar columnas y generar estadísticas descriptivas.",
            "fechaLimite": ts(+6),
        },
        "tarea_006": {
            "idClase":     "clase_003",
            "titulo":      "Clasificación con Árbol de Decisión",
            "descripcion": "Implementar un árbol de decisión con scikit-learn sobre el dataset de churn bancario y reportar precisión, recall y F1-score.",
            "fechaLimite": ts(+11),
        },
        # — clase_004: Desarrollo Web Integral —
        "tarea_007": {
            "idClase":     "clase_004",
            "titulo":      "Landing Page Responsiva",
            "descripcion": "Diseñar una landing page de producto con HTML5, CSS3 y media queries; debe verse correctamente en móvil, tablet y escritorio.",
            "fechaLimite": ts(+3),
        },
        "tarea_008": {
            "idClase":     "clase_004",
            "titulo":      "API REST con Node.js y Express",
            "descripcion": "Desarrollar una API con al menos 4 endpoints CRUD para el recurso 'productos', con validación y respuestas en JSON.",
            "fechaLimite": ts(+10),
        },
        # — clase_005: Desarrollo para Dispositivos Inteligentes —
        "tarea_009": {
            "idClase":     "clase_005",
            "titulo":      "App Lista de Tareas en Android Studio",
            "descripcion": "Crear una app en Kotlin con RecyclerView, Room Database y navegación entre pantallas para gestionar tareas personales.",
            "fechaLimite": ts(+7),
        },
        "tarea_010": {
            "idClase":     "clase_005",
            "titulo":      "Consumo de API REST con Retrofit",
            "descripcion": "Integrar la API pública de Rick and Morty en la app usando Retrofit2, mostrar personajes en lista y detalle en pantalla.",
            "fechaLimite": ts(+14),
        },
        # — clase_006: Inglés VIII —
        "tarea_011": {
            "idClase":     "clase_006",
            "titulo":      "Technical Writing: IT Project Summary",
            "descripcion": "Write a 300-word executive summary in English describing your final semester IT project, including objectives, scope and expected results.",
            "fechaLimite": ts(+5),
        },
        "tarea_012": {
            "idClase":     "clase_006",
            "titulo":      "Oral Presentation: Technology Trends",
            "descripcion": "Prepare and deliver a 5-minute oral presentation in English about an emerging technology trend (AI, IoT, blockchain or cloud computing).",
            "fechaLimite": ts(+13),
        },
    }
    for tid, d in tareas.items():
        save("tareas", tid, d)
    print(f"  📝 tareas          → {len(tareas)}")

    # ── ASIGNACIONES (cada alumno → cada tarea de su clase) ──────
    clase_alumnos: dict[str, list[str]] = {}
    for insc in inscripciones.values():
        clase_alumnos.setdefault(insc["idClase"], []).append(insc["idUsuario"])

    asignaciones: dict = {}
    idx = 1
    for tid, td in tareas.items():
        for alumno in clase_alumnos.get(td["idClase"], []):
            key = f"asig_{idx:03d}"
            asignaciones[key] = {
                "idUsuario":       alumno,
                "idTarea":         tid,
                "fechaAsignacion": ts(-5),
            }
            idx += 1
    for aid, d in asignaciones.items():
        save("asignaciones_tarea", aid, d)
    print(f"  📋 asignaciones    → {len(asignaciones)}")

    # Índices de apoyo
    asig_map: dict[tuple, str] = {
        (d["idUsuario"], d["idTarea"]): k
        for k, d in asignaciones.items()
    }
    clase_por_tarea = {tid: td["idClase"]   for tid, td in tareas.items()}
    prof_por_clase  = {cid: cd["idUsuario"] for cid, cd in clases.items()}

    # ── EVIDENCIAS + CALIFICACIONES ──────────────────────────────
    # (alumno, tarea, calificacion|None, estado, comentario|None, dias_envío)
    #   días < 0 → entregó hace N días (ya revisada)
    #   días = 0 → entregó hoy         (pendiente)
    submissions = [
        # ── Agustín López Parra (A-ALP) — el más adelantado ─────
        ("A-ALP",  "tarea_001", 10, "Aprobada",  "Plan muy completo con métricas claras y objetivos SMART bien definidos. ¡Excelente trabajo!",      -3),
        ("A-ALP",  "tarea_003",  9, "Aprobada",  "EDT bien estructurada, todos los entregables identificados. Cuida los niveles de indentación.",     -2),
        ("A-ALP",  "tarea_005",  8, "Aprobada",  "Buen preprocesamiento, pero faltó documentar el criterio para el manejo de outliers.",               -1),
        ("A-ALP",  "tarea_007", 10, "Aprobada",  "Landing page impecable, responsiva en todos los dispositivos probados. ¡Felicidades!",               -2),
        ("A-ALP",  "tarea_009",  9, "Aprobada",  "App funcional con Room bien configurado. Agrega validaciones en el formulario de nueva tarea.",       -1),
        ("A-ALP",  "tarea_011",  9, "Aprobada",  "Well-written summary with clear objectives. Minor grammar issues in the third paragraph.",            -2),
        ("A-ALP",  "tarea_002", None, "Pendiente", None,                                                                                                 0),
        ("A-ALP",  "tarea_008", None, "Pendiente", None,                                                                                                 0),

        # ── Carlos Manuel Palma Muñoz (A-CMPM) ──────────────────
        ("A-CMPM", "tarea_001",  9, "Aprobada",  "Muy buen análisis de roles y métricas. Faltó profundizar en el plan de contingencias del equipo.",   -2),
        ("A-CMPM", "tarea_005",  5, "Rechazada", "El dataset entregado no coincide con el solicitado. Revisar instrucciones y reenviar.",               -3),
        ("A-CMPM", "tarea_007",  8, "Aprobada",  "Diseño atractivo, pero la versión móvil presenta desbordamiento en el menú de navegación.",           -1),
        ("A-CMPM", "tarea_009",  7, "Aprobada",  "La app corre bien, pero Room no persiste datos al cerrar la aplicación. Revisar configuración de BD.", -1),
        ("A-CMPM", "tarea_011",  8, "Aprobada",  "Good content and structure. Work on sentence connectors to improve the flow of your writing.",         -2),
        ("A-CMPM", "tarea_003", None, "Pendiente", None,                                                                                                  0),
        ("A-CMPM", "tarea_006", None, "Pendiente", None,                                                                                                  0),

        # ── Karla Ramírez Ibarra (A-KRI) ─────────────────────────
        ("A-KRI",  "tarea_001",  8, "Aprobada",  "Plan bien redactado. Los indicadores de desempeño son medibles aunque algunos plazos son muy amplios.", -2),
        ("A-KRI",  "tarea_007",  9, "Aprobada",  "Landing muy profesional. El contraste de colores y la tipografía están muy bien seleccionados.",        -1),
        ("A-KRI",  "tarea_011",  7, "Aprobada",  "Decent structure, but the vocabulary needs to be more technical for an IT summary.",                     -1),
        ("A-KRI",  "tarea_003", None, "Pendiente", None,                                                                                                    0),
        ("A-KRI",  "tarea_009", None, "Pendiente", None,                                                                                                    0),

        # ── Sofía Mendoza Torres (A-SLMT) ────────────────────────
        ("A-SLMT", "tarea_001",  7, "Aprobada",  "Los objetivos están definidos, pero las métricas de seguimiento son muy generales.",                    -1),
        ("A-SLMT", "tarea_007",  6, "Rechazada", "La página no es responsiva; en móvil los elementos se superponen. Corregir y reenviar.",                -2),
        ("A-SLMT", "tarea_003", None, "Pendiente", None,                                                                                                    0),
        ("A-SLMT", "tarea_011", None, "Pendiente", None,                                                                                                    0),

        # ── Diego Romo Salinas (A-DGRS) ──────────────────────────
        ("A-DGRS", "tarea_005",  6, "Aprobada",  "Los nulos fueron tratados, pero la normalización aplicada no es la adecuada para variables categóricas.", -1),
        ("A-DGRS", "tarea_009",  8, "Aprobada",  "App funcional y con buena UX. Agrega manejo de errores cuando la BD esté vacía.",                         -1),
        ("A-DGRS", "tarea_001", None, "Pendiente", None,                                                                                                      0),
        ("A-DGRS", "tarea_007", None, "Pendiente", None,                                                                                                      0),
    ]

    evid_idx  = 1
    calif_idx = 1
    for alumno, tarea, calif, estado, comentario, dias in submissions:
        asig_key = asig_map.get((alumno, tarea))
        if not asig_key:
            continue

        evid_key = f"evid_{evid_idx:03d}"
        clase    = clase_por_tarea[tarea]
        profe    = prof_por_clase[clase]

        evid = {
            "idEvidencia":  evid_key,
            "idAsignacion": asig_key,
            "tituloTarea":  tareas[tarea]["titulo"],
            "nombreAlumno": usuarios[alumno]["nombre"],
            "fechaEnvio":   ts(dias),
            "estado":       estado,
            "fotoBase64":   _FOTO,
        }
        if calif is not None:
            evid["calificacion"] = calif
        save("evidencias_tarea", evid_key, evid)
        evid_idx += 1

        # Calificación solo si ya fue revisada (Aprobada o Rechazada)
        if estado in ("Aprobada", "Rechazada"):
            calif_key = f"calif_{calif_idx:03d}"
            save("calificaciones", calif_key, {
                "idEvidencia":       evid_key,
                "idUsuario":         profe,
                "valor":             calif,
                "comentario":        comentario,
                "esBorrador":        False,
                "fechaCalificacion": ts(dias + 1),
            })
            calif_idx += 1

    print(f"  📸 evidencias      → {evid_idx - 1}")
    print(f"  ⭐ calificaciones  → {calif_idx - 1}")

    # ── EVENTOS ESCOLARES ────────────────────────────────────────
    eventos = {
        "evento_001": {
            "idUsuario":   "C-BAJJDE",
            "titulo":      "Entrega de Boletas — Primer Parcial",
            "descripcion": "Distribución de calificaciones del primer parcial a alumnos y padres de familia.",
            "lugar":       "Auditorio principal",
            "fechaEvento": ts(+18),
        },
        "evento_002": {
            "idUsuario":   "C-BAJJDE",
            "titulo":      "Semana Cultural Institucional",
            "descripcion": "Exposiciones artísticas, conferencias de egresados y competencias deportivas interfacultades.",
            "lugar":       "Patio central de la universidad",
            "fechaEvento": ts(+32),
        },
        "evento_003": {
            "idUsuario":   "C-BAJJDE",
            "titulo":      "Examen Departamental — Primer Parcial",
            "descripcion": "Evaluación simultánea de todas las materias del cuatrimestre en curso.",
            "lugar":       "Aulas 101 al 106, Edificio A",
            "fechaEvento": ts(+22),
        },
        "evento_004": {
            "idUsuario":   "C-BAJJDE",
            "titulo":      "Taller de Titulación y Egreso",
            "descripcion": "Orientación sobre modalidades de titulación, trámites administrativos y bolsa de trabajo institucional.",
            "lugar":       "Sala de juntas, Edificio B",
            "fechaEvento": ts(+11),
        },
    }
    for eid, d in eventos.items():
        save("eventos_escolares", eid, d)
    print(f"  📅 eventos         → {len(eventos)}")

    # ── RESUMEN FINAL ────────────────────────────────────────────
    n_coord = sum(1 for k in usuarios if k.startswith("C-"))
    n_prof  = sum(1 for k in usuarios if k.startswith("P-"))
    n_alum  = sum(1 for k in usuarios if k.startswith("A-"))

    print("\n" + "=" * 62)
    print("  ✅  ¡Base de datos poblada exitosamente!")
    print("=" * 62)
    print(f"\n  Coordinadores  : {n_coord}")
    print(f"  Profesores     : {n_prof}  (P-GSME, P-MMEV, P-TYJJ, P-BRG, P-RGA, P-SDCA)")
    print(f"  Alumnos        : {n_alum}  (A-ALP, A-CMPM, A-KRI, A-SLMT, A-DGRS)")
    print(f"  Clases         : {len(clases)}")
    print(f"  Inscripciones  : {len(inscripciones)}  ({n_alum} alumnos × {len(clases)} clases)")
    print(f"  Tareas         : {len(tareas)}  (2 por clase)")
    print(f"  Asignaciones   : {len(asignaciones)}")
    print(f"  Evidencias     : {evid_idx - 1}")
    print(f"  Calificaciones : {calif_idx - 1}")
    print(f"  Eventos        : {len(eventos)}")
    print()


# ── MAIN ─────────────────────────────────────────────────────────
if __name__ == "__main__":
    borrar_todo()
    poblar()