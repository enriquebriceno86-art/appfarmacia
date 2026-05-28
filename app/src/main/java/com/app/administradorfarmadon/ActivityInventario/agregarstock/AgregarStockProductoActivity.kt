package com.app.administradorfarmadon.ActivityInventario.agregarstock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect


class AgregarStockProductoActivity : ComponentActivity() {

    private val viewModel: AgregarStockProductoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val indice = intent.getStringExtra("indice")
        if (indice.isNullOrBlank()) {
            Toast.makeText(this, "No se recibió un índice válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        viewModel.cargarProducto(indice)

        setContent {
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.saveSuccess) {
                if (state.saveSuccess) {
                    Toast.makeText(this@AgregarStockProductoActivity, "Stock ingresado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            // Omitimos el tema de FluidBackground si no está disponible o usamos Surface normal,
            // asumiendo que lo usas en el proyecto
            AgregarStockProductoScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBack = { finish() },
                onSupplierSelectClick = {
                    viewModel.onEvent(AgregarStockEvent.OnSupplierClick)
                }
            )
        }
    }
}