package com.pmlp.wear.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class SessionListenerService : WearableListenerService() {
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
