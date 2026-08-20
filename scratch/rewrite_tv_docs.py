import os
import re

custom_docs = {
    # MainActivitytv.kt
    "MainActivitytv_onCreate": """/**
 * Se ejecuta al arrancar el modulo de TV (Smart TV o Android TV).
 * Inicializa el reproductor de musica de fondo (ExoPlayer) para ambientar la pantalla
 * y lanza el contenedor principal de Jetpack Compose (`TVHomeScreen`) envuelto en una Surface
 * que ocupa el ancho y alto maximos de la pantalla (10-foot UI).
 *
 * @param savedInstanceState Estado persistido en caso de que el sistema operativo recree la actividad.
 */""",
    "initializePlayer": """/**
 * Configura e inicializa ExoPlayer, el motor multimedia de Android (Media3).
 * Se encarga de cargar el himno institucional desde los recursos (`R.raw.himno`),
 * configurarlo en modo de repeticion infinita (`REPEAT_MODE_ALL`) y darle play automaticamente (`playWhenReady`).
 * Esto permite que el dashboard de TV se sienta vivo y dinamico en pasillos o recepciones.
 */""",
    "onDestroy": """/**
 * Evento del ciclo de vida que se llama cuando se cierra la pantalla de TV.
 * Su funcion principal es liberar recursos criticos, deteniendo la reproduccion de audio,
 * vaciando la cola de MediaItems y destruyendo por completo la instancia de ExoPlayer
 * para evitar fugas de memoria (memory leaks).
 */""",

    # TVHomeScreen.kt
    "TVHomeScreen": """/**
 * El nucleo principal (Dashboard) del modulo de Android TV.
 * Escucha asincronamente dos flujos de datos en tiempo real provenientes de Firestore (a traves del ViewModel):
 * 1. Eventos institucionales (Noticias).
 * 2. Estadisticas academicas globales por grupo.
 * Combina ambos flujos en una sola lista inmutable (CarouselPage) y orquesta la visualizacion
 * mostrando un mensaje de carga, de error, o inyectando la lista combinada al `AutoCarousel`.
 *
 * @param viewModel ViewModel compartido instanciado desde el modulo :core que provee el estado reactivo.
 */""",
    "CenteredMsg": """/**
 * Componente utilitario simple que dibuja un texto completamente centrado en la pantalla del televisor.
 * Se utiliza principalmente como 'Placeholder' (marcador de posicion) durante estados de carga (Loading)
 * o cuando la base de datos devuelve resultados vacios o arroja errores de conexion.
 *
 * @param msg Texto informativo a mostrar en pantalla.
 * @param color Color en el que se dibujara el texto, dictado por el MaterialTheme.
 */""",
    "AutoCarousel": """/**
 * Paginador automatizado de desplazamiento horizontal infinito.
 * Representa el carrusel visual que ira alternando por si solo (sin intervencion humana)
 * cada una de las diapositivas de avisos y grupos. Emplea una corrutina (LaunchedEffect)
 * con un 'delay' constante de 6 segundos, forzando un 'animateScrollToPage' ciclico (modulo)
 * para lograr el efecto de cartelera rotativa.
 *
 * @param pages Lista sellada de objetos CarouselPage que contiene tanto noticias como dashboard de grupos.
 */""",
    "EventoHeroCard": """/**
 * Componente UI que diseña y renderiza una "Hero Card" gigante para mostrar anuncios y noticias de la escuela.
 * Utiliza Coil (AsyncImage) para descargar la fotografia en segundo plano y la dibuja ocupando todo el fondo
 * con un degradado superpuesto oscuro. Esto garantiza que la tipografia blanca
 * de los anuncios sea siempre legible independientemente del contraste de la fotografia original.
 *
 * @param evento Objeto que contiene titulo, descripcion, lugar, fecha y URL de la imagen del aviso institucional.
 */""",
    "GrupoEstadisticaCard": """/**
 * Dashboard analitico de nivel ejecutivo disenado para mostrar las metricas de un grupo.
 * Estructura la informacion en dos columnas grandes (ideal para aspecto 16:9 de TVs):
 * Columna Izquierda: KPIs de alto impacto, incluyendo el promedio general impreso en texto gigante,
 * insignias de rendimiento (Ej. "EXCELENTE") y una barra de progreso que indica el avance de las evaluaciones.
 * Columna Derecha: Un desglose grafico apilado mostrando el rendimiento individual obtenido por los alumnos en cada tarea.
 *
 * @param grupo Objeto EstadisticaGrupo calculado desde el modulo core que contiene los calculos y promedios reales del grupo.
 */""",
    "BarRow": """/**
 * Componente grafico modular que dibuja una unica barra de progreso horizontal.
 * Toma el promedio especifico de una materia/tarea (ej. 8.5) y lo traduce matematicamente
 * en un ancho proporcional usando el modifier `weight`. Pinta el interior de la barra con un gradiente
 * para darle profundidad y estilo moderno al dashboard estadistico.
 *
 * @param index Numero de orden o ranking de la materia en la lista.
 * @param materia Estructura de datos que contiene el titulo de la asignacion y la calificacion numerica.
 */""",
    "StatRow": """/**
 * Fila minimalista para presentar pares de clave-valor.
 * Utilizada extensamente en el panel izquierdo de estadisticas para detallar cantidades
 * exactas como total de alumnos, evaluados y tareas registradas en el periodo en curso.
 *
 * @param label El nombre descriptivo de la metrica.
 * @param value El valor numerico o textual asociado.
 */""",
    "InfoBadge": """/**
 * Etiqueta visual de esquinas redondeadas tipo "Pill" o "Chip".
 * Dibuja un fondo semitransparente (`alpha = 0.1f`) con texto de contraste alto,
 * perfecto para inyectar pequenos metadatos (como la fecha u hora de publicacion)
 * encima de las imagenes de fondo de los eventos sin perder estetica ni estorbar.
 *
 * @param text Cadena de texto corta que aparecera dentro del badge.
 */""",
    "PaginationIndicator": """/**
 * Sistema visual de rastreo de paginacion (los puntos en la parte inferior de la pantalla).
 * Iterara sobre la cantidad total de diapositivas y dibujara una forma diferente dependiendo
 * del tipo de contenido: Dibuja 'Circulos' si la diapositiva es un aviso, y 'Cuadrados o barras'
 * si la diapositiva es un resumen de grupo. Incluye animaciones fluidas de tamano y color que
 * reaccionan cuando cambia la pagina activa.
 *
 * @param count Cantidad total de elementos rotativos en el carrusel.
 * @param currentIndex Indice base cero (0) de la diapositiva que el espectador esta viendo actualmente.
 * @param isGrupo Funcion lambda evaluadora (predicado) que devuelve True si el slide corresponde a estadisticas.
 */""",

    # Theme.kt
    "EdutaskTheme": """/**
 * Gestor del arbol de temas visuales para la plataforma de Android TV.
 * Dado que los televisores se usan comunmente en interiores y se prefieren fondos oscuros
 * (dark UI) para no fatigar la vista, este metodo inyecta de forma incondicional
 * el esquema `darkColorScheme()` compuesto por una paleta de colores violetas y púrpuras.
 * Toda la interfaz del carrusel hereda estos tokens de diseno de manera automatica.
 *
 * @param isInDarkTheme Bandera condicional (ignorada practicamente al forzar DarkMode siempre en TV).
 * @param content El sub-arbol completo de composables (UI) que sera afectado por este tema.
 */"""
}

def update_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = re.compile(r'/\*\*\n(?:\s*\*.*?\n)*\s*\*/\n(?:@[A-Za-z0-9_]+\s*\n)*(?:\s*(?:private\s+|override\s+|suspend\s+)*fun\s+)([A-Za-z0-9_]+)', re.MULTILINE)
    
    def replacer(match):
        func_name = match.group(1)
        if func_name == "onCreate" and "MainActivitytv" in filepath:
            dict_key = "MainActivitytv_onCreate"
        else:
            dict_key = func_name
            
        full_match = match.group(0)
        
        if dict_key in custom_docs:
            new_doc = custom_docs[dict_key]
            doc_end = full_match.find('*/') + 2
            original_doc = full_match[:doc_end]
            signature = full_match[doc_end:]
            return new_doc + signature
            
        return full_match

    new_content = pattern.sub(replacer, content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Updated {filepath}")


def main():
    directory = r"c:\Users\agust\StudioProjects\edutask\tv\src\main\java\com\pmlp\tv"
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                update_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
