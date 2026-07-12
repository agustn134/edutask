package com.pmlp.edutask.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pmlp.edutask.R

class TareaReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val tareaNombre = inputData.getString("TAREA_NOMBRE") ?: "Tarea pendiente"
        val notifId = inputData.getString("TAREA_ID")?.hashCode() ?: System.currentTimeMillis().toInt()

        mostrarNotificacion(tareaNombre, notifId)

        return Result.success()
    }

    private fun mostrarNotificacion(tareaNombre: String, notifId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, "edutask_reminders")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Asegurar que exista
            .setContentTitle("¡Recordatorio de Entrega!")
            .setContentText("Tu tarea '$tareaNombre' vence en menos de 2 horas. ¡No olvides entregarla!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notifId, builder.build())
    }
}
