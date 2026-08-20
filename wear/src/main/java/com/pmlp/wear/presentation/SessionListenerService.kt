/**
 * Servicio en segundo plano para Wear OS que escucha y sincroniza la sesion del profesor con la app movil.
 
 * @author Agustin Parra, Carlos Palma
 * @date Agosto 2026
 */
package com.pmlp.wear.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class SessionListenerService : WearableListenerService() {
    /**
 * Escucha en segundo plano cualquier cambio en la capa de datos de Wearable Data Layer API.
 * Su funcion especifica es detectar cuando la app movil (del telefono) envia o actualiza
 * un evento a traves del path "/edutask/session". Si detecta un cambio, extrae el
 * `idUsuario` (el ID del profesor logueado) y lo guarda localmente en SharedPreferences.
 * Esto permite que el reloj "sepa" que profesor esta usando la app sin necesidad de un login manual en el reloj.
 *
 * @param dataEvents Buffer de eventos sincronizados desde el dispositivo movil.
 */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("SessionListenerService", "Data changed event received on wear")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/usuario_logueado") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val idUsuario = dataMap.getString("idUsuario") ?: ""
                val nombre = dataMap.getString("nombre") ?: ""

                Log.d("SessionListenerService", "Synchronized session on Wear OS: idUsuario=$idUsuario, nombre=$nombre")

                // Save locally on the watch SharedPreferences
                val prefs = getSharedPreferences("edutask_wear_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("idUsuario", idUsuario)
                    .putString("nombre", nombre)
                    .apply()
            }
        }
    }
}
