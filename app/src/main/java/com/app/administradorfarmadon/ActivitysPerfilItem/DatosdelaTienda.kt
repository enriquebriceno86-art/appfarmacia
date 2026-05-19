package com.app.administradorfarmadon.ActivitysPerfilItem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

class DatosdelaTienda : ComponentActivity() {
    private val viewModel: StoreConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreConfigScreen(viewModel = viewModel)
        }
    }
}
