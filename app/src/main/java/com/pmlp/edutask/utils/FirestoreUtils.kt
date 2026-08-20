/**
 * Funciones de utilidad y extensiones para operaciones con Firebase Firestore
 * (conversion de datos, queries seguras y manejo de colecciones).
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.edutask.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

/**
 * Extrae una fecha de manera segura, soportando:
 * - Timestamp de Firebase
 * - Map (exportado como {_seconds, _nanoseconds})
 * - String (ISO-8601 o numérico)
 * - Number (milisegundos o segundos)
 */
fun DocumentSnapshot.getSafeDate(field: String): Date? {
    val fechaRaw = this.get(field) ?: return null
    return try {
        when (fechaRaw) {
            is Timestamp -> fechaRaw.toDate()
            is Date -> fechaRaw
            is String -> {
                try {
                    Date(java.time.Instant.parse(fechaRaw).toEpochMilli())
                } catch (_: Exception) {
                    fechaRaw.toLongOrNull()?.let { Date(it) }
                }
            }
            is Number -> {
                // Asumimos milisegundos. Si es demasiado pequeño, podría ser segundos.
                val value = fechaRaw.toLong()
                if (value < 100000000000L) Date(value * 1000) else Date(value)
            }
            is Map<*, *> -> {
                val seconds = (fechaRaw["_seconds"] as? Number)?.toLong() ?: 0L
                Date(seconds * 1000)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
