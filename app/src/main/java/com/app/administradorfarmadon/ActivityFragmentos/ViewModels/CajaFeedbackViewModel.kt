package com.app.administradorfarmadon.ActivityFragmentos.ViewModels

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.app.administradorfarmadon.ClasesDatabase.FeedbackCajaController

class CajaFeedbackViewModel(application: Application) : AndroidViewModel(application) {
    private val controller = FeedbackCajaController(application.applicationContext)

    fun precalentar() {
        // Acceder temprano al ViewModel fuerza la creación del controller y su precarga eager,
        // evitando que el primer gesto del cajero tenga que esperar la carga del audio.
    }

    fun notificarProductoAgregado(view: View?) {
        controller.productoAgregado(view)
    }

    fun notificarProductoRestado(view: View?) {
        controller.productoRestado(view)
    }

    fun notificarAccionDestructiva(view: View?) {
        controller.accionDestructiva(view)
    }

    fun notificarVentaExitosa(view: View?) {
        controller.ventaExitosa(view)
    }

    fun notificarError(view: View?) {
        controller.error(view)
    }

    fun notificarFalloInternet(view: View?) {
        controller.falloInternet(view)
    }

    fun notificarInternetRecuperado(view: View?) {
        controller.internetRecuperado(view)
    }

    fun notificarSwitchComprobante(view: View?, activado: Boolean) {
        if (activado) {
            controller.switchComprobanteActivado(view)
        } else {
            controller.switchComprobanteDesactivado(view)
        }
    }
}
