package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Controlador de feedback (haptica + sonido premium) para acciones de la caja.
 *
 * El sonido premium se genera sinteticamente UNA vez la primera ejecucion (tono "ding"
 * tipo Apple Pay: dos notas armonicas con envelope ADSR rapido, ~120ms total). Se cachea
 * en cacheDir como WAV y se carga en SoundPool, que soporta overlap y baja latencia.
 *
 * Toda interaccion con SoundPool/Vibrator/AudioManager va envuelta en runCatching: si algo
 * falla en runtime (servicio no disponible, dispositivo sin vibrador, etc), no propaga.
 */
class FeedbackCajaController(private val appContext: Context) {
    private enum class TipoSonidoUi {
        AGREGAR,
        VENTA_EXITOSA,
        ELIMINAR,
        SWITCH_ON,
        SWITCH_OFF,
        ERROR,
        ERROR_INTERNET,
        INTERNET_RECUPERADO
    }


    // Pool "sonification": para feedback discreto (agregar, venta exitosa). Usa el stream
    // de sistema, volumen normalmente moderado.
    @Volatile private var soundPool: SoundPool? = null
    @Volatile private var soundIdAgregar: Int = 0
    @Volatile private var soundIdVentaExitosa: Int = 0
    @Volatile private var soundIdEliminar: Int = 0
    @Volatile private var soundIdSwitchOn: Int = 0
    @Volatile private var soundIdSwitchOff: Int = 0
    @Volatile private var soundAgregarCargado: Boolean = false
    @Volatile private var soundVentaCargado: Boolean = false
    @Volatile private var soundEliminarCargado: Boolean = false
    @Volatile private var soundSwitchOnCargado: Boolean = false
    @Volatile private var soundSwitchOffCargado: Boolean = false
    private val pendingAgregar = AtomicBoolean(false)
    private val pendingVentaExitosa = AtomicBoolean(false)
    private val pendingEliminar = AtomicBoolean(false)
    private val pendingSwitchOn = AtomicBoolean(false)
    private val pendingSwitchOff = AtomicBoolean(false)
    private val ultimoPlayPorTipoMs = mutableMapOf<TipoSonidoUi, Long>()
    private val ultimoStreamIdPorTipo = mutableMapOf<TipoSonidoUi, Int>()

    // Pool "alertas": SOLO para sonido de error. Usa USAGE_MEDIA para que
    // suba con el volumen maestro (mas alto en farmacias ruidosas).
    @Volatile private var soundPoolAlertas: SoundPool? = null
    @Volatile private var soundIdError: Int = 0
    @Volatile private var soundIdErrorInternet: Int = 0
    @Volatile private var soundIdInternetRecuperado: Int = 0
    @Volatile private var soundErrorCargado: Boolean = false
    @Volatile private var soundErrorInternetCargado: Boolean = false
    @Volatile private var soundInternetRecuperadoCargado: Boolean = false
    private val pendingError = AtomicBoolean(false)
    private val pendingErrorInternet = AtomicBoolean(false)
    private val pendingInternetRecuperado = AtomicBoolean(false)

    init {
        // Precarga eager de ambos SoundPools en background. Sin esto, el primer tap del
        // cajero podria caer antes de que los samples terminen de cargar (~50-100ms).
        Thread {
            runCatching { obtenerSoundPool() }
            runCatching { obtenerSoundPoolAlertas() }
        }.apply {
            isDaemon = true
            name = "FeedbackCaja-SoundPool-init"
            start()
        }
    }

    fun productoAgregado(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoAgregar()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaSuave(view)
        }
    }

    fun productoRestado(view: View?) {
        if (!PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) return
        reproducirHapticaSuave(view)
    }

    fun accionDestructiva(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoEliminar()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaDestructiva(view)
        }
    }

    fun ventaExitosa(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoVentaExitosa()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaExito(view)
        }
    }

    /**
     * V21.5: Sonido de éxito largo (5s) generado sintéticamente para animaciones premium.
     */
    fun ventaExitosaLong(duracionMs: Long) {
        val sp = obtenerSoundPool()
        Thread {
            runCatching {
                val file = obtenerOCrearWav("venta_exitosa_long_$duracionMs") {
                    generarPcmVentaExitosaLong(duracionMs)
                }
                val soundId = sp.load(file.absolutePath, 1)
                // Esperar carga sutil
                var loadedId = -1
                sp.setOnLoadCompleteListener { _, sid, status -> if (status == 0) loadedId = sid }
                val start = System.currentTimeMillis()
                while (loadedId != soundId && System.currentTimeMillis() - start < 2000) { Thread.sleep(10) }
                sp.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }.start()
    }

    fun error(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoError()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaError(view)
        }
    }

    fun falloInternet(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoErrorInternet()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaError(view)
        }
    }

    fun internetRecuperado(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoInternetRecuperado()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaExito(view)
        }
    }

    fun switchComprobanteActivado(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoSwitchOn()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaSwitchOn(view)
        }
    }

    fun switchComprobanteDesactivado(view: View?) {
        if (PreferenciasFeedbackCaja.estaSonidoActivo(appContext)) {
            reproducirSonidoSwitchOff()
        }
        if (PreferenciasFeedbackCaja.estaHapticaActiva(appContext)) {
            reproducirHapticaSwitchOff(view)
        }
    }

    // ================== SONIDO PREMIUM ==================

    private fun reproducirSonidoAgregar() {
        runCatching {
            val pool = obtenerSoundPool()
            if (!soundAgregarCargado) {
                pendingAgregar.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.AGREGAR,
                pool = pool,
                sampleId = soundIdAgregar,
                leftVolume = 1f,
                rightVolume = 1f,
                priority = 1,
                minIntervalMs = 135L
            )
        }
    }

    private fun reproducirSonidoVentaExitosa() {
        runCatching {
            val pool = obtenerSoundPool()
            if (!soundVentaCargado) {
                pendingVentaExitosa.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.VENTA_EXITOSA,
                pool = pool,
                sampleId = soundIdVentaExitosa,
                leftVolume = 1f,
                rightVolume = 1f,
                priority = 2,
                minIntervalMs = 300L
            )
        }
    }

    private fun reproducirSonidoEliminar() {
        runCatching {
            val pool = obtenerSoundPool()
            if (!soundEliminarCargado) {
                pendingEliminar.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.ELIMINAR,
                pool = pool,
                sampleId = soundIdEliminar,
                leftVolume = 0.84f,
                rightVolume = 0.84f,
                priority = 2,
                minIntervalMs = 160L
            )
        }
    }

    private fun reproducirSonidoSwitchOn() {
        runCatching {
            val pool = obtenerSoundPool()
            if (!soundSwitchOnCargado) {
                pendingSwitchOn.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.SWITCH_ON,
                pool = pool,
                sampleId = soundIdSwitchOn,
                leftVolume = 0.82f,
                rightVolume = 0.82f,
                priority = 2,
                minIntervalMs = 110L
            )
        }
    }

    private fun reproducirSonidoSwitchOff() {
        runCatching {
            val pool = obtenerSoundPool()
            if (!soundSwitchOffCargado) {
                pendingSwitchOff.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.SWITCH_OFF,
                pool = pool,
                sampleId = soundIdSwitchOff,
                leftVolume = 0.76f,
                rightVolume = 0.76f,
                priority = 2,
                minIntervalMs = 110L
            )
        }
    }

    private fun reproducirSonidoError() {
        runCatching {
            val pool = obtenerSoundPoolAlertas()
            if (!soundErrorCargado) {
                pendingError.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.ERROR,
                pool = pool,
                sampleId = soundIdError,
                leftVolume = 1f,
                rightVolume = 1f,
                priority = 3,
                minIntervalMs = 250L
            )
        }
    }

    private fun reproducirSonidoErrorInternet() {
        runCatching {
            val pool = obtenerSoundPoolAlertas()
            if (!soundErrorInternetCargado) {
                pendingErrorInternet.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.ERROR_INTERNET,
                pool = pool,
                sampleId = soundIdErrorInternet,
                leftVolume = 1f,
                rightVolume = 1f,
                priority = 4,
                minIntervalMs = 1000L
            )
        }
    }

    private fun reproducirSonidoInternetRecuperado() {
        runCatching {
            val pool = obtenerSoundPoolAlertas()
            if (!soundInternetRecuperadoCargado) {
                pendingInternetRecuperado.set(true)
                return@runCatching
            }
            reproducirSampleControlado(
                tipo = TipoSonidoUi.INTERNET_RECUPERADO,
                pool = pool,
                sampleId = soundIdInternetRecuperado,
                leftVolume = 1f,
                rightVolume = 1f,
                priority = 4,
                minIntervalMs = 2000L
            )
        }
    }

    @Synchronized
    private fun obtenerSoundPool(): SoundPool {
        soundPool?.let { return it }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            when (sampleId) {
                soundIdAgregar -> {
                    soundAgregarCargado = true
                    reproducirPendiente(pool, pendingAgregar) {
                        reproducirSampleControlado(
                            tipo = TipoSonidoUi.AGREGAR,
                            pool = pool,
                            sampleId = soundIdAgregar,
                            leftVolume = 1f,
                            rightVolume = 1f,
                            priority = 1,
                            minIntervalMs = 135L
                        )
                    }
                }
                soundIdVentaExitosa -> {
                    soundVentaCargado = true
                    reproducirPendiente(pool, pendingVentaExitosa) {
                        reproducirSampleControlado(
                            tipo = TipoSonidoUi.VENTA_EXITOSA,
                            pool = pool,
                            sampleId = soundIdVentaExitosa,
                            leftVolume = 1f,
                            rightVolume = 1f,
                            priority = 2,
                            minIntervalMs = 300L
                        )
                    }
                }
                soundIdEliminar -> {
                    soundEliminarCargado = true
                    reproducirPendiente(pool, pendingEliminar) {
                        reproducirSampleControlado(
                            tipo = TipoSonidoUi.ELIMINAR,
                            pool = pool,
                            sampleId = soundIdEliminar,
                            leftVolume = 0.84f,
                            rightVolume = 0.84f,
                            priority = 2,
                            minIntervalMs = 160L
                        )
                    }
                }
                soundIdSwitchOn -> {
                    soundSwitchOnCargado = true
                    reproducirPendiente(pool, pendingSwitchOn) {
                        reproducirSampleControlado(
                            tipo = TipoSonidoUi.SWITCH_ON,
                            pool = pool,
                            sampleId = soundIdSwitchOn,
                            leftVolume = 0.82f,
                            rightVolume = 0.82f,
                            priority = 2,
                            minIntervalMs = 110L
                        )
                    }
                }
                soundIdSwitchOff -> {
                    soundSwitchOffCargado = true
                    reproducirPendiente(pool, pendingSwitchOff) {
                        reproducirSampleControlado(
                            tipo = TipoSonidoUi.SWITCH_OFF,
                            pool = pool,
                            sampleId = soundIdSwitchOff,
                            leftVolume = 0.76f,
                            rightVolume = 0.76f,
                            priority = 2,
                            minIntervalMs = 110L
                        )
                    }
                }
            }
        }
        val wavAgregar = obtenerOCrearWav("feedback_agregar_v1.wav") { generarPcmAgregar() }
        soundIdAgregar = pool.load(wavAgregar.absolutePath, 1)
        // Cambia "v1" por "v2" para resetear la caché del archivo en el teléfono
        val wavVenta = obtenerOCrearWav("feedback_venta_exitosa_v2.wav") { generarPcmVentaExitosa() }
        soundIdVentaExitosa = pool.load(wavVenta.absolutePath, 1)
        val wavEliminar = obtenerOCrearWav("feedback_eliminar_suave_v1.wav") { generarPcmEliminar() }
        soundIdEliminar = pool.load(wavEliminar.absolutePath, 1)
        val wavSwitchOn = obtenerOCrearWav("feedback_switch_on_v2.wav") { generarPcmSwitchOn() }
        soundIdSwitchOn = pool.load(wavSwitchOn.absolutePath, 1)
        val wavSwitchOff = obtenerOCrearWav("feedback_switch_off_v2.wav") { generarPcmSwitchOff() }
        soundIdSwitchOff = pool.load(wavSwitchOff.absolutePath, 1)
        soundPool = pool
        return pool
    }

    /**
     * SoundPool dedicado a alertas (sonido de error). Usa USAGE_NOTIFICATION_EVENT para que
     * tome el volumen de notificaciones del SO, normalmente mas alto que el de sistema.
     * Critico para entornos ruidosos (farmacias con clientes, ventiladores, musica).
     */
    @Synchronized
    private fun obtenerSoundPoolAlertas(): SoundPool {
        soundPoolAlertas?.let { return it }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            if (sampleId == soundIdError) {
                soundErrorCargado = true
                reproducirPendiente(pool, pendingError) {
                    reproducirSampleControlado(
                        tipo = TipoSonidoUi.ERROR,
                        pool = pool,
                        sampleId = soundIdError,
                        leftVolume = 1f,
                        rightVolume = 1f,
                        priority = 3,
                        minIntervalMs = 250L
                    )
                }
            } else if (sampleId == soundIdErrorInternet) {
                soundErrorInternetCargado = true
                reproducirPendiente(pool, pendingErrorInternet) {
                    reproducirSampleControlado(
                        tipo = TipoSonidoUi.ERROR_INTERNET,
                        pool = pool,
                        sampleId = soundIdErrorInternet,
                        leftVolume = 1f,
                        rightVolume = 1f,
                        priority = 4,
                        minIntervalMs = 1000L
                    )
                }
            } else if (sampleId == soundIdInternetRecuperado) {
                soundInternetRecuperadoCargado = true
                reproducirPendiente(pool, pendingInternetRecuperado) {
                    reproducirSampleControlado(
                        tipo = TipoSonidoUi.INTERNET_RECUPERADO,
                        pool = pool,
                        sampleId = soundIdInternetRecuperado,
                        leftVolume = 1f,
                        rightVolume = 1f,
                        priority = 4,
                        minIntervalMs = 2000L
                    )
                }
            }
        }
        // v4: sonido de error "Buzzer" industrial, mas fuerte y original.
        val wavError = obtenerOCrearWav("feedback_error_v4.wav") { generarPcmError() }
        soundIdError = pool.load(wavError.absolutePath, 1)

        val wavErrorInternet = obtenerOCrearWav("feedback_error_internet_v1.wav") { generarPcmErrorInternet() }
        soundIdErrorInternet = pool.load(wavErrorInternet.absolutePath, 1)

        val wavInternetRecuperado = obtenerOCrearWav("feedback_internet_recuperado_v1.wav") { generarPcmInternetRecuperado() }
        soundIdInternetRecuperado = pool.load(wavInternetRecuperado.absolutePath, 1)

        soundPoolAlertas = pool
        return pool
    }

    /**
     * Devuelve el archivo WAV. Si no existe, lo genera con el bloque que se le pasa.
     * Vive en cacheDir, asi que el SO puede borrarlo si necesita espacio (se regenera).
     */
    private fun obtenerOCrearWav(nombre: String, generador: () -> ShortArray): File {
        val archivo = File(appContext.cacheDir, nombre)
        if (archivo.exists() && archivo.length() > 44) return archivo
        val pcm = generador()
        escribirWavMono16Bit(archivo, pcm, SAMPLE_RATE_HZ)
        return archivo
    }

    /**
     * "Ding" premium para agregar al carrito: dos notas (A5 880Hz + E6 1318Hz) con envelope
     * exponencial decay. ~120ms total. Estilo confirmacion Apple Pay.
     */
    private fun generarPcmAgregar(): ShortArray {
        val notas = listOf(
            NotaSintetica(frecuenciaHz = 880.0, duracionMs = 80, attackMs = 4, decayCoef = 22.0, amplitud = 0.55, retrasoMs = 0),
            NotaSintetica(frecuenciaHz = 1318.5, duracionMs = 80, attackMs = 4, decayCoef = 24.0, amplitud = 0.45, retrasoMs = 80)
        )
        return mezclarNotas(notas, totalDuracionMs = 160)
    }

    /**
     * Arpegio ascendente C-mayor para venta exitosa: C5, E5, G5, C6 con overlap. La ultima
     * nota (C6) tiene sustain mas largo para dar sensacion de "logro completado". ~480ms total.
     * Estilo registradora moderna / "ka-ching" digital premium.
     */
    private fun generarPcmVentaExitosa(): ShortArray {
        val baseNotas = listOf(
            // Frecuencia (Hz) | Retraso (ms) para el efecto arpegio en cascada
            Pair(587.33, 0),    // D5  - Base armónica cálida
            Pair(739.99, 50),   // F#5 - Tercera mayor brillante
            Pair(880.00, 100),  // A5  - Quinta justa de soporte
            Pair(1174.66, 150), // D6  - Octava de claridad cristalina
            Pair(1318.51, 200), // E6  - Novena mágica (Aporta el toque moderno/premium)
            Pair(1760.00, 250)  // A6  - Destello final que flota en el aire
        )

        val notasSinteticas = mutableListOf<NotaSintetica>()

        for ((freq, delay) in baseNotas) {
            // La última nota se queda flotando más tiempo (Sustain largo), las primeras decaen rápido
            val esNotaFinal = freq == 1760.00
            val duracion = if (esNotaFinal) 1200 else 220
            val decay = if (esNotaFinal) 2.5 else 18.0

            // Layer 1: Frecuencia Fundamental (Cuerpo principal del sonido)
            notasSinteticas.add(NotaSintetica(freq, duracion, if (esNotaFinal) 15 else 5, decay, 0.35, delay))

            // Layer 2: Armónicos sutiles para textura
            notasSinteticas.add(NotaSintetica(freq * 2, duracion - 20, 10, decay * 1.5, 0.10, delay))
        }

        return mezclarNotas(notasSinteticas, totalDuracionMs = 1500)
    }

    private fun generarPcmVentaExitosaLong(duracionMs: Long): ShortArray {
        val notas = mutableListOf<NotaSintetica>()
        val baseFreq = 440.0 // La (A4)
        val escala = listOf(1.0, 1.125, 1.25, 1.333, 1.5, 1.666, 1.875, 2.0)
        
        for (i in 0 until 14) {
            val freq = baseFreq * escala[i % escala.size] * (1 + (i / escala.size) * 0.5)
            notas.add(NotaSintetica(
                frecuenciaHz = freq,
                duracionMs = 1500,
                attackMs = 150,
                decayCoef = 2.0,
                amplitud = 0.12,
                retrasoMs = i * 320
            ))
        }
        
        return mezclarNotas(notas, totalDuracionMs = duracionMs.toInt())
    }
    /**
     * Eliminacion: gesto corto en descenso, suave y limpio. Busca comunicar "se fue" sin
     * sonar como error; por eso cae de una nota media a otra mas grave con una cola breve.
     */
    private fun generarPcmEliminar(): ShortArray {
        val notas = listOf(
            NotaSintetica(frecuenciaHz = 659.25, duracionMs = 80, attackMs = 4, decayCoef = 18.0, amplitud = 0.36, retrasoMs = 0),
            NotaSintetica(frecuenciaHz = 523.25, duracionMs = 120, attackMs = 4, decayCoef = 13.5, amplitud = 0.34, retrasoMs = 45),
            NotaSintetica(frecuenciaHz = 392.00, duracionMs = 100, attackMs = 5, decayCoef = 15.0, amplitud = 0.20, retrasoMs = 92)
        )
        return mezclarNotas(notas, totalDuracionMs = 230)
    }

    /**
     * Variante de error: "Fallo de Internet / Glitch"
     * Tres pulsos digitales secos con caida de frecuencia.
     * Simula la sensacion de una señal que se corta o un proceso interrumpido.
     * Total ~400ms.
     */
    private fun generarPcmErrorInternet(): ShortArray {
        val notas = listOf(
            // Primer pulso: Alerta digital corta
            NotaSintetica(frecuenciaHz = 440.0, duracionMs = 60, attackMs = 2, decayCoef = 35.0, amplitud = 0.90, retrasoMs = 0),
            // Segundo pulso: Caida de señal
            NotaSintetica(frecuenciaHz = 330.0, duracionMs = 60, attackMs = 2, decayCoef = 40.0, amplitud = 0.90, retrasoMs = 120),
            // Tercer pulso: Grave y seco (el "corte" final)
            NotaSintetica(frecuenciaHz = 110.0, duracionMs = 150, attackMs = 2, decayCoef = 20.0, amplitud = 0.95, retrasoMs = 240)
        )
        return mezclarNotas(notas, totalDuracionMs = 450)
    }

    /**
     * Tono de error "Buzzer" industrial, FUERTE y con carácter.
     * Utiliza dos frecuencias disonantes bajas para crear un efecto de interferencia
     * que se siente "pesado" y claramente como un fallo.
     * Dos pulsos de 180ms cada uno.
     */
    private fun generarPcmError(): ShortArray {
        val notas = listOf(
            // Primer pulso (disonante): 220Hz + 225Hz (Efecto batido/rugoso)
            NotaSintetica(frecuenciaHz = 220.0, duracionMs = 180, attackMs = 10, decayCoef = 4.0, amplitud = 0.98, retrasoMs = 0),
            NotaSintetica(frecuenciaHz = 225.0, duracionMs = 180, attackMs = 10, decayCoef = 4.0, amplitud = 0.98, retrasoMs = 0),
            
            // Segundo pulso igual después de un silencio corto
            NotaSintetica(frecuenciaHz = 220.0, duracionMs = 180, attackMs = 10, decayCoef = 4.0, amplitud = 0.98, retrasoMs = 280),
            NotaSintetica(frecuenciaHz = 225.0, duracionMs = 180, attackMs = 10, decayCoef = 4.0, amplitud = 0.98, retrasoMs = 280)
        )
        return mezclarNotas(notas, totalDuracionMs = 500)
    }

    /**
     * Encendido: click mecanico con cola brillante, corto pero claramente "on".
     */
    private fun generarPcmSwitchOn(): ShortArray {
        return generarPcmSwitchMecanico(
            totalDuracionMs = 118,
            frecuenciaInicioHz = 980.0,
            frecuenciaFinHz = 1620.0,
            frecuenciaResonanciaHz = 720.0,
            nivelRuido = 0.44,
            nivelTono = 0.32,
            nivelResonancia = 0.18,
            ruidoDecay = 78.0,
            tonoDecay = 24.0,
            resonanciaDelayMs = 10,
            resonanciaDecay = 16.0
        )
    }

    /**
     * Apagado: click mas seco y mas grave, con una caida corta que se siente como "off".
     */
    private fun generarPcmSwitchOff(): ShortArray {
        return generarPcmSwitchMecanico(
            totalDuracionMs = 112,
            frecuenciaInicioHz = 1220.0,
            frecuenciaFinHz = 610.0,
            frecuenciaResonanciaHz = 290.0,
            nivelRuido = 0.46,
            nivelTono = 0.24,
            nivelResonancia = 0.21,
            ruidoDecay = 86.0,
            tonoDecay = 26.0,
            resonanciaDelayMs = 12,
            resonanciaDecay = 14.0
        )
    }

    /**
     * Genera un "click" sintetico mas natural para switches:
     * una rafaga de ruido corta + un cuerpo tonal + una resonancia muy breve.
     */
    private fun generarPcmSwitchMecanico(
        totalDuracionMs: Int,
        frecuenciaInicioHz: Double,
        frecuenciaFinHz: Double,
        frecuenciaResonanciaHz: Double,
        nivelRuido: Double,
        nivelTono: Double,
        nivelResonancia: Double,
        ruidoDecay: Double,
        tonoDecay: Double,
        resonanciaDelayMs: Int,
        resonanciaDecay: Double
    ): ShortArray {
        val totalSamples = (totalDuracionMs * SAMPLE_RATE_HZ) / 1000
        val buffer = ShortArray(totalSamples)
        val resonanciaOffset = (resonanciaDelayMs * SAMPLE_RATE_HZ) / 1000
        var fasePrincipal = 0.0
        var ruidoPrevio = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progreso = i.toDouble() / maxOf(totalSamples - 1, 1)
            val frecuenciaActual = frecuenciaInicioHz + ((frecuenciaFinHz - frecuenciaInicioHz) * progreso)
            fasePrincipal += (2.0 * PI * frecuenciaActual) / SAMPLE_RATE_HZ

            val ruidoBase = ruidoDeterminista((i + 1) * 37 + totalSamples * 13)
            val ruidoClick = (ruidoBase - (ruidoPrevio * 0.58))
            ruidoPrevio = ruidoBase

            val envelopeRuido = exp(-ruidoDecay * t)
            val envelopeTono = exp(-tonoDecay * t)

            val resonancia = if (i >= resonanciaOffset) {
                val tiempoResonancia = (i - resonanciaOffset).toDouble() / SAMPLE_RATE_HZ
                sin((2.0 * PI * frecuenciaResonanciaHz * tiempoResonancia) + (PI / 6.0)) *
                    exp(-resonanciaDecay * tiempoResonancia)
            } else {
                0.0
            }

            val muestra =
                (ruidoClick * nivelRuido * envelopeRuido) +
                (sin(fasePrincipal) * nivelTono * envelopeTono) +
                (resonancia * nivelResonancia)

            buffer[i] = (muestra.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }

        return buffer
    }

    private fun ruidoDeterminista(seed: Int): Double {
        var valor = seed.toLong()
        valor = (valor xor (valor shl 13))
        valor = (valor xor (valor ushr 17))
        valor = (valor xor (valor shl 5))
        val normalizado = (valor and 0x7FFFFFFF).toDouble() / 0x7FFFFFFF.toDouble()
        return (normalizado * 2.0) - 1.0
    }

    /**
     * Mezcla varias notas en un solo buffer respetando el retraso de cada una. Permite
     * que las notas se solapen para sonar como un acorde, no notas secuenciales secas.
     */
    private fun mezclarNotas(notas: List<NotaSintetica>, totalDuracionMs: Int): ShortArray {
        val sr = SAMPLE_RATE_HZ
        val totalSamples = (totalDuracionMs * sr) / 1000
        val mix = DoubleArray(totalSamples)
        for (nota in notas) {
            val samples = (nota.duracionMs * sr) / 1000
            val attackSamples = (nota.attackMs * sr) / 1000
            val offset = (nota.retrasoMs * sr) / 1000
            val omega = 2.0 * PI * nota.frecuenciaHz / sr
            for (i in 0 until samples) {
                val pos = offset + i
                if (pos >= totalSamples) break
                val t = i.toDouble() / sr
                val attack = if (i < attackSamples && attackSamples > 0) i.toDouble() / attackSamples else 1.0
                val decay = exp(-nota.decayCoef * t)
                val envelope = attack * decay
                mix[pos] += sin(omega * i) * envelope * nota.amplitud
            }
        }
        // Limitador suave para evitar clipping cuando las notas se solapan.
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val v = mix[i].coerceIn(-0.95, 0.95)
            buffer[i] = (v * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun escribirWavMono16Bit(archivo: File, pcm: ShortArray, sampleRate: Int) {
        val byteRate = sampleRate * 2
        val pcmBytes = ByteArray(pcm.size * 2)
        for (i in pcm.indices) {
            val s = pcm[i].toInt()
            pcmBytes[i * 2] = (s and 0xff).toByte()
            pcmBytes[i * 2 + 1] = ((s shr 8) and 0xff).toByte()
        }
        val totalDataLen = pcmBytes.size + 36
        FileOutputStream(archivo).use { out ->
            // Header WAV PCM mono 16-bit
            out.write("RIFF".toByteArray())
            out.write(intLe(totalDataLen))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intLe(16))                  // chunk size
            out.write(shortLe(1))                  // PCM
            out.write(shortLe(1))                  // mono
            out.write(intLe(sampleRate))
            out.write(intLe(byteRate))
            out.write(shortLe(2))                  // block align (2 bytes/sample)
            out.write(shortLe(16))                 // bits per sample
            out.write("data".toByteArray())
            out.write(intLe(pcmBytes.size))
            out.write(pcmBytes)
        }
    }

    private fun intLe(v: Int) = byteArrayOf(
        (v and 0xff).toByte(),
        ((v shr 8) and 0xff).toByte(),
        ((v shr 16) and 0xff).toByte(),
        ((v shr 24) and 0xff).toByte()
    )

    private fun shortLe(v: Int) = byteArrayOf(
        (v and 0xff).toByte(),
        ((v shr 8) and 0xff).toByte()
    )

    private fun reproducirPendiente(
        pool: SoundPool,
        pending: AtomicBoolean,
        play: () -> Unit
    ) {
        if (!pending.compareAndSet(true, false)) return
        runCatching { play() }
    }

    @Synchronized
    private fun reproducirSampleControlado(
        tipo: TipoSonidoUi,
        pool: SoundPool,
        sampleId: Int,
        leftVolume: Float,
        rightVolume: Float,
        priority: Int,
        minIntervalMs: Long
    ) {
        val ahora = SystemClock.elapsedRealtime()
        val ultimoPlay = ultimoPlayPorTipoMs[tipo] ?: 0L
        if (ahora - ultimoPlay < minIntervalMs) return

        // HUMANO: Quitamos pool.stop(streamAnterior). 
        // Permitimos que SoundPool maneje el overlap natural (harmonic decay)
        // en lugar de cortar el sonido abruptamente, lo que causa ruidos molestos.

        val streamNuevo = pool.play(sampleId, leftVolume, rightVolume, priority, 0, 1f)
        if (streamNuevo != 0) {
            ultimoPlayPorTipoMs[tipo] = ahora
            ultimoStreamIdPorTipo[tipo] = streamNuevo
        }
    }

    // ================== HAPTICA ==================

    private fun reproducirHapticaSuave(view: View?) {
        runCatching {
            val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
            view?.performHapticFeedback(feedback)
        }
    }

    private fun reproducirHapticaDestructiva(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrarPredefinido(VibrationEffect.EFFECT_HEAVY_CLICK, 50L)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            vibrarUnaVez(50L)
        }
    }

    private fun reproducirHapticaExito(view: View?) {
        // Patron expresivo "tic-tic-TAC" celebrando: dos pulsos cortos y un final mas largo.
        // Sincroniza visualmente con el arpegio del sonido (~250ms).
        vibrarPatron(longArrayOf(0L, 35L, 55L, 35L, 70L, 90L))
    }

    private fun reproducirHapticaError(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }
        // Patrón más agresivo: Pulso largo, pausa, pulso largo, pulso largo
        vibrarPatron(longArrayOf(0L, 100L, 80L, 100L, 80L, 150L))
    }

    private fun reproducirHapticaSwitchOn(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrarPredefinido(VibrationEffect.EFFECT_CLICK, 22L)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            vibrarUnaVez(22L)
        }
    }

    private fun reproducirHapticaSwitchOff(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrarPredefinido(VibrationEffect.EFFECT_TICK, 16L)
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            vibrarUnaVez(16L)
        }
    }

    private fun vibrarPredefinido(effectId: Int, fallbackDuracionMs: Long) {
        runCatching {
            val vibrator = obtenerVibrador() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(effectId))
            } else {
                vibrarUnaVez(fallbackDuracionMs)
            }
        }
    }

    private fun vibrarUnaVez(duracionMs: Long) {
        runCatching {
            val vibrator = obtenerVibrador() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duracionMs)
            }
        }
    }

    private fun vibrarPatron(pattern: LongArray) {
        runCatching {
            val vibrator = obtenerVibrador() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    private fun obtenerVibrador(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Arpegio ascendente brillante para internet recuperado.
     * Cuatro notas armonicas (E5, G#5, B5, E6) con un ataque rapido y decay suave.
     * Suena "lindo", limpio y comunica éxito. Total ~600ms.
     */
    private fun generarPcmInternetRecuperado(): ShortArray {
        val notas = listOf(
            NotaSintetica(frecuenciaHz = 659.25, duracionMs = 250, attackMs = 5, decayCoef = 12.0, amplitud = 0.35, retrasoMs = 0),
            NotaSintetica(frecuenciaHz = 830.61, duracionMs = 250, attackMs = 5, decayCoef = 12.0, amplitud = 0.35, retrasoMs = 120),
            NotaSintetica(frecuenciaHz = 987.77, duracionMs = 250, attackMs = 5, decayCoef = 12.0, amplitud = 0.35, retrasoMs = 240),
            NotaSintetica(frecuenciaHz = 1318.51, duracionMs = 350, attackMs = 6, decayCoef = 8.0, amplitud = 0.45, retrasoMs = 360)
        )
        return mezclarNotas(notas, totalDuracionMs = 750)
    }

    private data class NotaSintetica(
        val frecuenciaHz: Double,
        val duracionMs: Int,
        val attackMs: Int,
        val decayCoef: Double,
        val amplitud: Double,
        val retrasoMs: Int = 0
    )

    companion object {
        private const val SAMPLE_RATE_HZ = 44100
    }
}
