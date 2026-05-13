package com.app.administradorfarmadon.ActivityFragmentos

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

/**
 * Extensiones para manejar la visibilidad de overlays con animaciones suaves y consistentes.
 * HUMANO: Centralizamos esto para que todos los overlays (apertura, cierre, bloqueos)
 * se comporten igual y no haya saltos visuales bruscos.
 */

fun View.showElegantemente(duration: Long = 280L) {
    // Si ya es visible y no está en proceso de ocultarse, no hacemos nada
    if (this.visibility == View.VISIBLE && this.alpha > 0.9f) return
    
    this.animate().cancel()
    this.visibility = View.VISIBLE
    // Si ya tiene algo de alpha, partimos de ahí para que sea más fluido
    if (this.alpha == 1f) this.alpha = 0f
    
    this.animate()
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

fun View.hideElegantemente(duration: Long = 250L, onFinish: (() -> Unit)? = null) {
    if (this.visibility == View.GONE) return
    
    this.animate().cancel()
    this.animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(AccelerateInterpolator())
        .withEndAction {
            this.visibility = View.GONE
            this.alpha = 1f // Reset para la próxima vez
            onFinish?.invoke()
        }
        .start()
}
