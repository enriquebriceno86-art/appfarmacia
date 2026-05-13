package com.app.administradorfarmadon

import android.app.Application
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.google.firebase.database.FirebaseDatabase

class AppFarmadon : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializamos el gestor de sesión con el contexto global
        SessionManager.init(this)

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Firebase solo permite activar persistencia una vez por proceso.
        }
    }
}
