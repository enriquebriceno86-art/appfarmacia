package com.app.administradorfarmadon.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.app.administradorfarmadon.ActivitysPerfilItem.CuentaPorPagar
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class PaymentReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "payment_reminders"
        private const val TAG = "PaymentReminderWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        try {
            val db = FirebaseDatabase.getInstance().getReference(DbPaths.INVENTARIO_CUENTAS_POR_PAGAR)
            val snapshot = db.get().await()

            if (!snapshot.exists()) return Result.success()

            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val facturasHoy = mutableListOf<CuentaPorPagar>()
            val facturasManana = mutableListOf<CuentaPorPagar>()

            snapshot.children.forEach { child ->
                val cuenta = child.getValue(CuentaPorPagar::class.java) ?: return@forEach
                if (cuenta.estadoPago == "PAGADO") return@forEach

                try {
                    val dueDate = LocalDate.parse(cuenta.fechaVencimientoPago, formatter)
                    when {
                        dueDate.isEqual(today) -> facturasHoy.add(cuenta)
                        dueDate.isEqual(tomorrow) -> facturasManana.add(cuenta)
                    }
                } catch (e: Exception) {
                    // Formato de fecha inválido o nulo
                }
            }

            // 1. Notificación de facturas que vencen HOY
            if (facturasHoy.isNotEmpty()) {
                val mensaje = if (facturasHoy.size == 1) {
                    "Factura ${facturasHoy[0].nroFactura} (${facturasHoy[0].proveedorNombre}) debe pagarse hoy."
                } else {
                    "Tienes ${facturasHoy.size} pagos pendientes para hoy."
                }
                showNotification(title = "¡Vence HOY!", message = mensaje, notificationId = 1001)
            }

            // 2. Notificación de facturas que vencen MAÑANA
            if (facturasManana.isNotEmpty()) {
                val mensaje = if (facturasManana.size == 1) {
                    "Factura ${facturasManana[0].nroFactura} (${facturasManana[0].proveedorNombre}) vence mañana."
                } else {
                    "Tienes ${facturasManana.size} pagos que vencen mañana."
                }
                // Pequeña espera para no solapar sonidos si hay de ambos tipos
                if (facturasHoy.isNotEmpty()) kotlinx.coroutines.delay(2000)
                showNotification(title = "Vence mañana", message = mensaje, notificationId = 1002)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Pago",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica sobre facturas por pagar que vencen pronto"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_receipt)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
