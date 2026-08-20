import os
import re

# Dictionary mapping function names to custom detailed Spanish docstrings
custom_docs = {
    "EdutaskTheme": """/**
 * Aplica el tema visual principal de EduTask para el modulo de Wear OS.
 * Esta funcion se encarga de inyectar los colores, tipografias y formas
 * proporcionados por MaterialTheme de Compose for Wear OS, asegurando
 * que toda la interfaz sea coherente, de alto contraste y facil de leer
 * en pantallas pequenas (circulares o cuadradas).
 *
 * @param content El contenido composable (pantallas, botones, etc.) que estara envuelto por este tema.
 */""",

    "onDataChanged": """/**
 * Escucha en segundo plano cualquier cambio en la capa de datos de Wearable Data Layer API.
 * Su funcion especifica es detectar cuando la app movil (del telefono) envia o actualiza
 * un evento a traves del path "/edutask/session". Si detecta un cambio, extrae el
 * `idUsuario` (el ID del profesor logueado) y lo guarda localmente en SharedPreferences.
 * Esto permite que el reloj "sepa" que profesor esta usando la app sin necesidad de un login manual en el reloj.
 *
 * @param dataEvents Buffer de eventos sincronizados desde el dispositivo movil.
 */""",

    "onBind": """/**
 * Vincula el servicio al componente que lo invoca.
 * En este caso, como es un servicio en segundo plano basado en Firebase/Background,
 * no requerimos que un cliente se enlace directamente a el mediante IPC, por lo que retorna null.
 *
 * @param intent La intencion (Intent) con la que se solicito el enlace.
 * @return Siempre null, indicando que no soporta enlaces (binding) directos.
 */""",

    "onCreate": """/**
 * Se invoca una unica vez cuando el sistema operativo Android Wear crea este servicio.
 * Su proposito es inicializar los componentes esenciales en segundo plano:
 * 1. Crea el canal de notificaciones necesario para Android Oreo (API 26) o superior.
 * 2. Inicia la escucha de base de datos (`startListening()`) para monitorear nuevas entregas.
 */""",

    "createNotificationChannel": """/**
 * Registra un canal de notificaciones ("edutask_wear_channel") en el sistema operativo Android.
 * Esto es un requisito obligatorio de Android a partir de la version 8.0 (Oreo).
 * Configura la importancia de las alertas como 'HIGH' (alta) para asegurar que el reloj
 * vibre y despierte la pantalla cuando el profesor reciba una notificacion de tarea.
 */""",

    "startListening": """/**
 * Establece una conexion activa (listener) con Firebase Cloud Firestore.
 * Su proposito principal es monitorear en tiempo real la coleccion "Evidencias",
 * buscando documentos que coincidan con el `idUsuario` del profesor y cuyo estado sea "Pendiente".
 * Cuando detecta una nueva evidencia (y salta la carga inicial), extrae los datos del alumno y la tarea,
 * y dispara la funcion `showNotification()` para alertar al maestro.
 */""",

    "showNotification": """/**
 * Construye y emite una notificacion nativa en el smartwatch del profesor.
 * Genera un Intent que abrira la clase `MainActivity` (la app de Wear OS) al tocar la notificacion.
 * Ademas, aplica el icono de la aplicacion y un texto dinamico informando que un alumno
 * ha entregado una tarea especifica.
 *
 * @param alumno Nombre del alumno que realizo la entrega.
 * @param tarea Titulo de la tarea entregada.
 * @param tieneArchivosNoImagen Booleano que indica si la entrega tiene adjuntos incompatibles con imagenes simples.
 */""",

    "onDestroy": """/**
 * Se invoca cuando el sistema operativo destruye este servicio para liberar memoria.
 * Su tarea es hacer limpieza (cleanup) eliminando el listener de Firestore (`listener?.remove()`)
 * para evitar fugas de memoria y consumo innecesario de bateria en el reloj.
 */""",

    "MainActivityWear": """/**
 * Punto de entrada principal (Activity) de la aplicacion en el smartwatch.
 * Extiende ComponentActivity para inicializar Jetpack Compose y lanzar
 * el contenedor principal de navegacion de la app.
 */""",

    # Re-mapped for the override fun onCreate
    "MainActivityWear_onCreate": """/**
 * Se ejecuta al iniciar la aplicacion en la pantalla del smartwatch.
 * Establece el tema visual base llamando a `EdutaskTheme` e infla el
 * composable raiz `EduTaskWearApp`, el cual gestiona la navegacion general de las pantallas.
 *
 * @param savedInstanceState Estado de la instancia previo, en caso de que la Activity haya sido recreada por el SO.
 */""",

    "EduTaskWearApp": """/**
 * Contenedor principal y motor de navegacion para la app de Wear OS.
 * Implementa SwipeDismissableNavHost para permitir que el usuario retroceda
 * deslizando el dedo desde el borde izquierdo (comportamiento estandar en Wear OS).
 * Declara las rutas disponibles: listado de pendientes, detalle/calificacion de evidencia, y visor de imagenes.
 */""",

    "NuevaEntregaScreen": """/**
 * Pantalla (Composable) que notifica o muestra el resumen detallado de una entrega
 * a traves de informacion estatica. En esta version de la app, actua como una vista
 * rapida para entender de que se trata la tarea y quien la envia, antes de entrar a evaluarla.
 *
 * @param nombreAlumno Nombre del alumno que realizo la entrega.
 * @param tituloTarea Nombre o titulo asignado a la tarea original.
 */""",

    "VerFotoScreen": """/**
 * Pantalla interactiva disenada exclusivamente para visualizar las imagenes enviadas
 * por el alumno, adaptadas al tamano limitado del smartwatch.
 * Decodifica las cadenas en Base64, convierte la imagen a un Bitmap de Android, y
 * gestiona un estado de paginacion (anterior/siguiente) para navegar por multiples fotos
 * usando botones grandes y comodos en la interfaz de Wear OS.
 *
 * @param fotos Lista de cadenas de texto en formato Base64 que representan las imagenes adjuntas.
 * @param onVolver Callback o accion disparada cuando el usuario decide salir del visor de fotos.
 */""",

    "decodeBase64ToBitmap": """/**
 * Transforma un string de imagen en codificacion Base64 hacia un objeto Bitmap nativo de Android.
 * Esto es necesario para poder renderizar las fotos descargadas de Firestore directamente en
 * un componente visual como `Image` dentro del contexto de Compose for Wear OS.
 *
 * @param base64Str Cadena de texto que contiene la informacion de la imagen codificada en Base64.
 * @return El objeto Bitmap listo para dibujarse en pantalla, o null si el string es invalido o corrupto.
 */""",

    "PendientesScreen": """/**
 * Pantalla principal del smartwatch para el profesor que renderiza una lista desplazable (ScalingLazyColumn).
 * Presenta un listado de todas las evidencias (tareas) que estan en estado "Pendiente" y requieren evaluacion.
 * Los items de la lista tienen un efecto de escalado tipo carrusel (los extremos se ven mas pequenos) tipico
 * del ecosistema Wear OS para mejorar el enfoque central.
 *
 * @param nombreProfesor Nombre del usuario activo (Profesor) para mostrar una bienvenida personalizada.
 * @param items Coleccion (List) de objetos EvidenciaPendiente que se iteraran para generar la UI.
 * @param onSeleccionarEvidencia Accion disparada al hacer clic sobre una de las tareas pendientes.
 */""",

    "PreviewPendientes": """/**
 * Funcion auxiliar para uso del desarrollador (Tooling).
 * Provee datos ficticios para poder previsualizar (renderizar) la pantalla de "PendientesScreen"
 * directamente en el editor (Android Studio) sin tener que compilar ni ejecutar el emulador del reloj.
 */""",

    "CalificarScreen": """/**
 * Pantalla de interaccion critica (Core Feature) del reloj.
 * Le muestra al profesor los datos del estudiante, botones de acceso para abrir fotos,
 * y lo mas importante: los botones de evaluacion numerica (10, 9, 8 o 0) para evaluar rapidamente la tarea
 * con pocos toques. Almacena en memoria (confirmando = x) la calificacion seleccionada y lanza
 * un dialogo de confirmacion para evitar toques accidentales o evaluaciones erroneas.
 *
 * @param evidencia El objeto EvidenciaPendiente con la informacion a evaluar.
 * @param esCargando Bandera que activa un indicador circular de progreso y deshabilita botones si se esta subiendo la nota a Firebase.
 * @param onCalificar Callback disparado cuando el profesor confirma finalmente la calificacion numerica.
 * @param onVolver Callback ejecutado para retroceder a la lista de pendientes.
 */""",

    "BotonNota": """/**
 * Componente UI modular y reutilizable para pintar los botones de calificacion (10, 9, 8, 0).
 * Facilita establecer un diseno uniforme (alturas fijas, tipografia gruesa) y permite
 * la inyeccion dinamica de esquemas de colores (tertiary, primary, secondary, error) para una
 * comunicacion visual mas clara segun la nota (verde para 10, rojo para 0).
 *
 * @param label El texto numerico del boton (ej. "10").
 * @param colors Objeto de Compose que define el color de fondo y de texto del boton.
 * @param onClick Accion ejecutada cuando el profesor presiona el boton en el smartwatch.
 * @param modifier Modificador opcional para inyectar estilos extra de layout (padding, pesos, etc.).
 */""",

    "ConfirmacionCalificacion": """/**
 * Vista de paso intermedio de seguridad.
 * Cuando el profesor toca un boton numerico, se superpone esta vista preguntandole "Guardar nota?".
 * Previene el fat-finger (toques accidentales muy comunes en relojes pequenos) y obliga
 * a elegir 'Si' o 'No' (onConfirmar u onCancelar) antes de lanzar el update a la base de datos de Firebase.
 *
 * @param nota La calificacion numerica elegida que se somete a verificacion visual.
 * @param onConfirmar Callback que lanza la actualizacion asincrona hacia Firestore.
 * @param onCancelar Callback que limpia el estado y regresa la UI a la matriz de botones de calificacion.
 */""",

    "PreviewCalificar": """/**
 * Herramienta de visualizacion para Android Studio (Tooling).
 * Renderiza la pantalla 'CalificarScreen' inyectando un modelo de evidencia falso.
 * Permite a los desarrolladores ajustar el padding, tipografia y colores de los botones
 * sin tener que lanzar el emulador cada vez que hacen un cambio estético.
 */""",

    "cargarPendientes": """/**
 * Funcion asincrona (Coroutine) que se conecta con la coleccion 'Evidencias' de Firestore.
 * Realiza una consulta (Query) buscando aquellas evidencias que correspondan a las clases de este profesor
 * y cuyo campo 'estado' sea equivalente a 'Pendiente'. Al recibirlas, muta la variable StateFlow _uiState
 * a CalificarUiState.Exito, forzando a Compose a redibujar la pantalla de lista (PendientesScreen).
 */""",

    "calificar": """/**
 * Metodo central de logica de negocio (Business Logic) responsable de procesar la evaluacion del profesor.
 * Ejecuta una transaccion o actualizacion asincrona hacia Firestore apuntando al ID de la evidencia elegida.
 * Modifica dos campos en la nube: 'estado' (lo pasa de "Pendiente" a "Calificado") y 'calificacion' (asigna la nota numerica).
 * Una vez finalizado el proceso de subida con exito, dispara la devolucion de llamada (onDone).
 *
 * @param idEvidencia String que contiene el identificador unico del documento en la base de datos (Firestore).
 * @param nota Numero entero (int) otorgado por el profesor.
 * @param onDone Evento (Unit) que avisa a la UI que la transaccion ha culminado, util para retroceder la navegacion.
 */""",

    "resetEstado": """/**
 * Regresa el manejador de estados internos (_uiState) del ViewModel a su valor inactivo original (Idle).
 * Utilizado por lo general para prevenir que un estado anterior persistente (por ejemplo, Exito o Cargando)
 * interfiera de forma anomala al volver a abrir una pantalla o cancelar un proceso.
 */"""
}

def update_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern to find generic docstrings to replace
    # We look for: /**\n * Metodo principal ...\n ... */ OR /**\n * Componente de interfaz ...\n ... */ OR /**\n * Manejador de evento ... \n ... */ 
    # followed by the function signature
    
    # We can match:
    # /**
    #  * (anything up to */)
    #  */
    # fun <name>
    
    pattern = re.compile(r'/\*\*\n(?:\s*\*.*?\n)*\s*\*/\n(?:@[A-Za-z0-9_]+\s*\n)*(?:\s*(?:private\s+|override\s+|suspend\s+)*fun\s+)([A-Za-z0-9_]+)', re.MULTILINE)
    
    def replacer(match):
        func_name = match.group(1)
        # Check if it's the specific override in MainActivity Wear
        if func_name == "onCreate" and "MainActivity" in filepath:
            dict_key = "MainActivityWear_onCreate"
        else:
            dict_key = func_name
            
        full_match = match.group(0)
        
        if dict_key in custom_docs:
            new_doc = custom_docs[dict_key]
            # Replace the old docstring with the new one, keeping the signature
            # We reconstruct the signature by replacing the matched docstring with new_doc
            # Since full_match includes the docstring and the signature, we split it.
            
            # Find where the docstring ends
            doc_end = full_match.find('*/') + 2
            original_doc = full_match[:doc_end]
            signature = full_match[doc_end:]
            
            # Check if this original doc is actually the generic one we want to replace
            # To avoid overwriting top file headers, the regex already requires `fun ` after it.
            return new_doc + signature
            
        return full_match

    new_content = pattern.sub(replacer, content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Updated {filepath}")


def main():
    directory = r"c:\Users\agust\StudioProjects\edutask\wear\src\main\java\com\pmlp\wear\presentation"
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                update_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
