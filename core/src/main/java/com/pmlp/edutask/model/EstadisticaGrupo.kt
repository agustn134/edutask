/**
 * Modelos de datos compartidos (EstadisticaGrupo y PromedioMateria) para estructurar
 * las metricas y promedios calculados por grupo y tarea para el Dashboard de TV.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.model

/**
 * Estadísticas de calificaciones para un grupo/clase.
 * Usada por el Dashboard institucional del módulo TV.
 */
data class EstadisticaGrupo(
    val idClase: String = "",
    val nombreClase: String = "",
    /** Promedio general de todos los alumnos del grupo en todas las tareas */
    val promedioGeneral: Double? = null,
    /** Lista de (nombreTarea ? promedio del grupo en esa tarea) */
    val promediosPorTarea: List<PromedioMateria> = emptyList(),
    /** Total de alumnos en el grupo */
    val totalAlumnos: Int = 0,
    /** Total de alumnos que tienen al menos una calificación */
    val alumnosCalificados: Int = 0
)

data class PromedioMateria(
    val idTarea: String = "",
    val nombreTarea: String = "",
    val promedio: Double? = null,
    val totalCalificados: Int = 0
)
