package com.app.administradorfarmadon.ActivitysUsuarios

object UsuarioUiRules {

    fun construirEstadoBuscador(mostrar: Boolean): BuscadorUiState {
        return BuscadorUiState(
            mostrarBuscador = mostrar,
            mostrarIconoBusqueda = !mostrar
        )
    }

    fun construirEstadoPantallaSolicitudes(
        mostrarSolicitudes: Boolean,
        haySolicitudes: Boolean
    ): PantallaUsuariosUiState {
        return PantallaUsuariosUiState(
            mostrarSolicitudes = mostrarSolicitudes,
            mostrarListaUsuarios = !mostrarSolicitudes,
            mostrarBotonSolicitudes = !mostrarSolicitudes && haySolicitudes
        )
    }

    fun debeCerrarActividad(activityPage: Int): Boolean {
        return activityPage == 1
    }
}
