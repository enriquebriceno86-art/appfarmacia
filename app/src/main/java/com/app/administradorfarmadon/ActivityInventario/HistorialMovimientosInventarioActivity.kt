package com.app.administradorfarmadon.ActivityInventario

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoRules
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ui.InventoryMovementHistoryRoute
import com.app.administradorfarmadon.ActivityInventario.ui.InventoryMovementTimelineUi
import com.app.administradorfarmadon.ActivityInventario.ui.MovementDetailScreen
import com.app.administradorfarmadon.ActivityInventario.ui.MovementQuickFilter
import com.app.administradorfarmadon.ActivityInventario.ui.MovementTimelineType
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistorialMovimientosInventarioActivity : AppCompatActivity() {

    private data class LoteMovimientoMeta(
        val numero: String,
        val vencimiento: String
    )

    private data class ProductoMovimientoMeta(
        val nombre: String,
        val presentacionPrincipal: String,
        val unidadBaseCorta: String,
        val lotesPorNumero: Map<String, LoteMovimientoMeta>,
        val loteActual: LoteMovimientoMeta?
    )

    private data class MovimientoRaw(
        val id: String,
        val indiceProducto: String,
        val tipo: String,
        val descripcion: String,
        val hora: String,
        val nombreUsuario: String,
        val referenciaId: String,
        val cantidad: Int?,
        val stockAntes: Int?,
        val stockDespues: Int?,
        val timestamp: Long,
        val nombreProducto: String,
        val presentacion: String,
        val cantidadMovimiento: Int? = null,
        val unidadMovimiento: String = "",
        val motivo: String,
        val detalleLotes: String = "",
        val fechaPath: String = ""
    )

    private var productosRef: DatabaseReference? = null
    private var movimientosRef: DatabaseReference? = null
    private var productosListener: ValueEventListener? = null
    private var movimientosListener: ValueEventListener? = null

    private var productosInicialesListos = false
    private var movimientosInicialesListos = false

    private var productosById by mutableStateOf<Map<String, MoldeProductos>>(emptyMap())
    private var movimientosRaw by mutableStateOf<List<MovimientoRaw>>(emptyList())
    private var movimientosUi by mutableStateOf<List<InventoryMovementTimelineUi>>(emptyList())
    private var cargando by mutableStateOf(true)
    private var reconstruccionMovimientosJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HistorialMovimientosRoot(
                    items = movimientosUi,
                    isLoading = cargando,
                    onBack = { finish() },
                    onOpenProduct = { indiceProducto ->
                        if (indiceProducto.isBlank()) return@HistorialMovimientosRoot
                        startActivity(
                            Intent(this, EditarProductodelInventario::class.java).apply {
                                putExtra("indice", indiceProducto)
                            }
                        )
                    }
                )
            }
        }

        iniciarLecturaProductos()
        iniciarLecturaMovimientos()
    }

    override fun onDestroy() {
        reconstruccionMovimientosJob?.cancel()
        productosListener?.let { productosRef?.removeEventListener(it) }
        movimientosListener?.let { movimientosRef?.removeEventListener(it) }
        super.onDestroy()
    }

    private fun iniciarLecturaProductos() {
        productosRef = FirebaseDatabase.getInstance()
            .getReference("Inventario")
            .child("Productos")

        productosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosById = snapshot.children.mapNotNull { child ->
                    child.toMoldeProductos()?.let { producto ->
                        val indice = producto.indice.ifBlank { child.key.orEmpty() }
                        indice.takeIf { it.isNotBlank() }?.let { safeIndex ->
                            safeIndex to producto.copy(indice = safeIndex)
                        }
                    }
                }.toMap()
                productosInicialesListos = true
                reconstruirMovimientosUi()
                actualizarEstadoCarga()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                productosInicialesListos = true
                actualizarEstadoCarga()
            }
        }
        productosRef?.addValueEventListener(productosListener!!)
    }

    private fun iniciarLecturaMovimientos() {
        movimientosRef = FirebaseDatabase.getInstance()
            .getReference(DbPaths.ROOT_MOVIMIENTOS)
            .child("movimientosInventario")

        movimientosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<MovimientoRaw>()
                snapshot.children.forEach { fechaSnapshot ->
                    val fechaKey = fechaSnapshot.key.orEmpty()
                    fechaSnapshot.children.forEach { movimientoSnapshot ->
                        val descripcion = movimientoSnapshot.child("descripcion")
                            .getValue(String::class.java)
                            .orEmpty()
                        val tipo = movimientoSnapshot.child("tipo")
                            .getValue(String::class.java)
                            .orEmpty()
                        if (descripcion.isBlank() && tipo.isBlank()) return@forEach

                        val timestamp = movimientoSnapshot.child("timestamp").value.toSafeLong()

                        items += MovimientoRaw(
                            id = "${fechaKey}|${movimientoSnapshot.key.orEmpty()}",
                            indiceProducto = movimientoSnapshot.child("indiceProducto")
                                .getValue(String::class.java)
                                .orEmpty(),
                            tipo = tipo,
                            descripcion = descripcion,
                            hora = movimientoSnapshot.child("hora")
                                .getValue(String::class.java)
                                .orEmpty(),
                            nombreUsuario = movimientoSnapshot.child("nombreUsuario")
                                .getValue(String::class.java)
                                .orEmpty(),
                            referenciaId = movimientoSnapshot.child("referenciaId")
                                .getValue(String::class.java)
                                .orEmpty(),
                            cantidad = movimientoSnapshot.child("cantidad").value?.toString()?.toIntOrNull(),
                            stockAntes = movimientoSnapshot.child("stockAntes").value?.toString()?.toIntOrNull(),
                            stockDespues = movimientoSnapshot.child("stockDespues").value?.toString()?.toIntOrNull(),
                            timestamp = timestamp,
                            nombreProducto = movimientoSnapshot.child("metadata")
                                .child("nombreProducto")
                                .getValue(String::class.java)
                                .orEmpty(),
                            presentacion = movimientoSnapshot.child("metadata")
                                .child("presentacion")
                                .getValue(String::class.java)
                                .orEmpty(),
                            cantidadMovimiento = movimientoSnapshot.child("metadata")
                                .child("cantidadIngresada")
                                .value?.toString()?.toIntOrNull()
                                ?: movimientoSnapshot.child("metadata")
                                    .child("cantidadEgresada")
                                    .value?.toString()?.toIntOrNull(),
                            unidadMovimiento = movimientoSnapshot.child("metadata")
                                .child("unidadIngreso")
                                .getValue(String::class.java)
                                .orEmpty()
                                .ifBlank {
                                    movimientoSnapshot.child("metadata")
                                        .child("unidadEgreso")
                                        .getValue(String::class.java)
                                        .orEmpty()
                                }
                                .ifBlank {
                                    movimientoSnapshot.child("metadata")
                                        .child("unidadAjuste")
                                        .getValue(String::class.java)
                                        .orEmpty()
                                },
                            motivo = movimientoSnapshot.child("motivo")
                                .getValue(String::class.java)
                                .orEmpty(),
                            detalleLotes = movimientoSnapshot.obtenerDetalleLotesMovimientoTexto(),
                            fechaPath = fechaKey
                        )
                    }
                }

                movimientosRaw = items.sortedByDescending { it.timestamp }
                movimientosInicialesListos = true
                reconstruirMovimientosUi()
                actualizarEstadoCarga()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                movimientosRaw = emptyList()
                movimientosUi = emptyList()
                movimientosInicialesListos = true
                actualizarEstadoCarga()
            }
        }
        movimientosRef?.addValueEventListener(movimientosListener!!)
    }

    private fun actualizarEstadoCarga() {
        cargando = !(productosInicialesListos && movimientosInicialesListos)
    }

    private fun reconstruirMovimientosUi() {
        reconstruccionMovimientosJob?.cancel()
        val productosSnapshot = productosById
        val movimientosSnapshot = movimientosRaw
        reconstruccionMovimientosJob = lifecycleScope.launch {
            val uiItems = withContext(Dispatchers.Default) {
                val productMetaMap = construirProductoMetaMap(productosSnapshot)
                movimientosSnapshot.mapNotNull { raw ->
                    raw.toTimelineUi(productosSnapshot, productMetaMap)
                }
            }
            movimientosUi = uiItems
        }
    }

    private fun construirProductoMetaMap(
        productos: Map<String, MoldeProductos>
    ): Map<String, ProductoMovimientoMeta> {
        return productos.mapValues { (_, producto) ->
            val unidadBaseCorta = abreviarUnidad(producto.unidadbase)
            val lotesPorNumero = buildMap {
                producto.lotes.forEach { (clave, lote) ->
                    val numeroNormalizado = lote.numero.trim().ifBlank { clave.trim() }
                    if (numeroNormalizado.isNotBlank()) {
                        put(
                            numeroNormalizado.lowercase(Locale.getDefault()),
                            LoteMovimientoMeta(
                                numero = numeroNormalizado,
                                vencimiento = lote.vencimiento.trim()
                            )
                        )
                    }
                }
            }
            val loteActual = LoteConsumoRules.resolver(producto, 1).loteActual?.second?.let { lote ->
                val numero = lote.numero.trim()
                LoteMovimientoMeta(
                    numero = numero,
                    vencimiento = lote.vencimiento.trim()
                )
            }
            ProductoMovimientoMeta(
                nombre = producto.nombre,
                presentacionPrincipal = producto.presentacionprincipal,
                unidadBaseCorta = unidadBaseCorta,
                lotesPorNumero = lotesPorNumero,
                loteActual = loteActual
            )
        }
    }

    private fun MovimientoRaw.toTimelineUi(
        productos: Map<String, MoldeProductos>,
        productMetaMap: Map<String, ProductoMovimientoMeta>
    ): InventoryMovementTimelineUi? {
        val movementType = clasificarMovimiento(tipo, motivo) ?: return null
        val producto = productos[indiceProducto]
        val productMeta = productMetaMap[indiceProducto]
        val productName = nombreProducto.ifBlank {
            productMeta?.nombre
                ?.takeIf { it.isNotBlank() }
                ?: descripcion.extraerNombreProductoFallback()
                ?: indiceProducto.ifBlank { "Producto no identificado" }
        }
        val presentation = presentacion.trim()
            .ifBlank { unidadMovimiento.trim() }
            .ifBlank { productMeta?.presentacionPrincipal.orEmpty() }
            .ifBlank { "Unidad" }
        val unitShort = productMeta?.unidadBaseCorta ?: abreviarUnidad(producto?.unidadbase.orEmpty())
        val amount = cantidadMovimiento ?: cantidad ?: calcularDiferencia(stockAntes, stockDespues) ?: 0

        val quantityLine = when (movementType) {
            MovementTimelineType.ADJUSTMENT -> {
                val antes = stockAntes ?: 0
                val despues = stockDespues ?: 0
                "Sistema: $antes → Físico: $despues"
            }

            MovementTimelineType.ENTRY -> "+${kotlin.math.abs(amount)} $unitShort · $presentation"
            MovementTimelineType.EXIT -> "-${kotlin.math.abs(amount)} $unitShort · $presentation"
            MovementTimelineType.WASTE -> "-${kotlin.math.abs(amount)} $unitShort · $presentation"
        }

        val noteLine: String? = when (movementType) {
            MovementTimelineType.ADJUSTMENT -> {
                val diferencia = calcularDiferencia(stockAntes, stockDespues) ?: 0
                buildString {
                    append("Diferencia: ")
                    append(formatearCantidad(diferencia, unitShort))
                    append("\nMotivo: ")
                    append(motivo.ifBlank { "Conteo físico" })
                }
            }

            MovementTimelineType.WASTE -> "Motivo: ${motivo.ifBlank { "Vencido / Dañado" }}"
            MovementTimelineType.EXIT -> "Motivo: ${motivo.ifBlank { if (tipo.contains("venta", ignoreCase = true)) "Venta" else "Salida" }}"
            MovementTimelineType.ENTRY -> null
        }

        val lotNumber = extraerPrimerNumeroLote(detalleLotes)
        val loteInfo = lotNumber
            ?.trim()
            ?.lowercase(Locale.getDefault())
            ?.let { normalized -> productMeta?.lotesPorNumero?.get(normalized) }
            ?: productMeta?.loteActual?.takeIf { current ->
                !lotNumber.isNullOrBlank() &&
                    current.numero.equals(lotNumber, ignoreCase = true)
            }
        val lotLine = when {
            lotNumber.isNullOrBlank() -> null
            !loteInfo?.vencimiento.isNullOrBlank() -> "Lote $lotNumber · Vence ${loteInfo?.vencimiento}"
            else -> "Lote $lotNumber"
        }

        val quantityDetail = when (movementType) {
            MovementTimelineType.ADJUSTMENT -> formatearCantidad(calcularDiferencia(stockAntes, stockDespues) ?: 0, unitShort)
            MovementTimelineType.ENTRY -> "+${kotlin.math.abs(amount)} $unitShort"
            MovementTimelineType.EXIT -> "-${kotlin.math.abs(amount)} $unitShort"
            MovementTimelineType.WASTE -> "-${kotlin.math.abs(amount)} $unitShort"
        }

        val actor = resolverActor(nombreUsuario, referenciaId, movementType)
        val reason = motivo.ifBlank {
            when (movementType) {
                MovementTimelineType.ENTRY -> "Compra"
                MovementTimelineType.EXIT -> if (tipo.contains("venta", ignoreCase = true)) "Venta" else "Salida"
                MovementTimelineType.ADJUSTMENT -> "Conteo físico"
                MovementTimelineType.WASTE -> "Vencido / Dañado"
            }
        }

        return InventoryMovementTimelineUi(
            id = id,
            productIndex = indiceProducto,
            type = movementType,
            typeTitle = movementType.title,
            productName = productName,
            quantityLine = quantityLine,
            lotLine = lotLine,
            noteLine = noteLine,
            timeLabel = hora.ifBlank { "--:--" },
            actorLabel = actor,
            presentationLabel = presentation,
            quantityDetail = quantityDetail,
            lotNumber = lotNumber,
            expiration = loteInfo?.vencimiento?.takeIf { it.isNotBlank() },
            reason = reason,
            dateTimeLabel = formatearFechaHora(fechaPath, hora),
            originLabel = resolverOrigen(referenciaId, movementType),
            userLabel = nombreUsuario.ifBlank { "Sistema" }
        )
    }

    private fun clasificarMovimiento(tipo: String, motivo: String): MovementTimelineType? {
        val normalizedType = tipo.lowercase(Locale.getDefault())
        val normalizedReason = motivo.lowercase(Locale.getDefault())
        return when {
            normalizedType.contains("merma") ||
                normalizedReason.contains("vencid") ||
                normalizedReason.contains("dañ") ||
                normalizedReason.contains("dan") -> MovementTimelineType.WASTE
            normalizedType.contains("ajuste") -> MovementTimelineType.ADJUSTMENT
            normalizedType.contains("salida") ||
                normalizedType.contains("bloqueado_devolucion") -> MovementTimelineType.EXIT
            normalizedType.contains("ingreso") ||
                normalizedType.contains("entrada") -> MovementTimelineType.ENTRY
            else -> null
        }
    }

    private fun calcularDiferencia(antes: Int?, despues: Int?): Int? {
        if (antes == null || despues == null) return null
        return despues - antes
    }

    private fun abreviarUnidad(unidadBase: String): String {
        return when (unidadBase.trim().lowercase(Locale.getDefault())) {
            "unidad", "unidades" -> "uds"
            "mililitro", "mililitros", "ml" -> "mL"
            "gramo", "gramos", "g" -> "g"
            "litro", "litros", "l" -> "L"
            else -> unidadBase.trim().ifBlank { "uds" }
        }
    }

    private fun formatearCantidad(valor: Int, unidad: String): String {
        val sign = if (valor < 0) "-" else "+"
        return "$sign${kotlin.math.abs(valor)} $unidad"
    }

    private fun extraerPrimerNumeroLote(detalleLotes: String): String? {
        val clean = detalleLotes.trim()
        if (clean.isBlank()) return null
        return clean.substringBefore(",").substringBefore(":").trim().takeIf { it.isNotBlank() }
    }

    private fun resolverActor(
        nombreUsuario: String,
        referenciaId: String,
        movementType: MovementTimelineType
    ): String {
        if (movementType == MovementTimelineType.EXIT &&
            referenciaId.lowercase(Locale.getDefault()).contains("venta")
        ) {
            return "Caja principal"
        }
        return nombreUsuario.ifBlank { "Sistema" }
    }

    private fun resolverOrigen(referenciaId: String, movementType: MovementTimelineType): String {
        val reference = referenciaId.trim().lowercase(Locale.getDefault())
        return when {
            reference.contains("venta") -> "Caja principal"
            reference == "ingreso_stock" -> "Ingreso de stock"
            reference == "ajuste_manual" -> "Ajuste manual"
            reference.isBlank() && movementType == MovementTimelineType.WASTE -> "Inventario"
            reference.isBlank() -> "Sistema"
            else -> referenciaId.humanizarReferencia()
        }
    }

    private fun formatearFechaHora(fechaPath: String, hora: String): String {
        val fecha = runCatching { LocalDate.parse(fechaPath) }.getOrNull()
        val hoy = LocalDate.now()
        val fechaLabel = when (fecha) {
            null -> fechaPath.ifBlank { "Fecha no registrada" }
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        }
        return if (hora.isBlank()) fechaLabel else "$fechaLabel, $hora"
    }

    private fun String.extraerNombreProductoFallback(): String? {
        val patterns = listOf(
            Regex("(?i)para\\s+(.+?)(?:\\.\\s+lotes|$)"),
            Regex("(?i)de\\s+(.+)$")
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(trim())?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private fun String.humanizarReferencia(): String {
        return replace("_", " ")
            .replace("-", " ")
            .trim()
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                word.lowercase(Locale.getDefault()).replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                }
            }
    }

    private fun Any?.toSafeLong(): Long {
        return when (this) {
            is Long -> this
            is Double -> toLong()
            is Int -> toLong()
            is String -> toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun DataSnapshot.obtenerDetalleLotesMovimientoTexto(): String {
        val metadata = child("metadata")
        val flatDetail = metadata.child("detalleLotesTexto")
            .getValue(String::class.java)
            .orEmpty()
            .trim()
        if (flatDetail.isNotBlank()) return flatDetail

        val details = metadata.child("lotesConsumidosDetalle").children.mapNotNull { detalle ->
            val number = detalle.child("numero").getValue(String::class.java).orEmpty().trim()
                .ifBlank { detalle.child("clave").getValue(String::class.java).orEmpty().trim() }
            val amount = detalle.child("cantidad").value?.toString()?.toIntOrNull()
            if (number.isBlank() || amount == null || amount <= 0) {
                null
            } else {
                "$number: $amount"
            }
        }
        if (details.isNotEmpty()) return details.joinToString(", ")

        val lotNumber = metadata.child("loteNumeroConsumido")
            .getValue(String::class.java)
            .orEmpty()
            .trim()
            .ifBlank {
                metadata.child("loteNumero")
                    .getValue(String::class.java)
                    .orEmpty()
                    .trim()
            }
        if (lotNumber.isBlank()) return ""

        val amount = child("cantidad").value?.toString()?.toIntOrNull()
        return if (amount != null && amount > 0) {
            "$lotNumber: $amount"
        } else {
            lotNumber
        }
    }
}

@Composable
private fun HistorialMovimientosRoot(
    items: List<InventoryMovementTimelineUi>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit
) {
    var selectedMovementId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(MovementQuickFilter.ALL) }

    val selectedMovement = items.firstOrNull { it.id == selectedMovementId }

    BackHandler {
        if (selectedMovement != null) {
            selectedMovementId = null
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (selectedMovement != null) {
            MovementDetailScreen(
                movement = selectedMovement,
                onBack = { selectedMovementId = null },
                onOpenProduct = { onOpenProduct(selectedMovement.productIndex) }
            )
        } else {
            InventoryMovementHistoryRoute(
                items = items,
                isLoading = isLoading,
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                onSearchQueryChange = { searchQuery = it },
                onFilterChange = { selectedFilter = it },
                onBack = onBack,
                onMovementClick = { movementId -> selectedMovementId = movementId }
            )
        }
    }
}
