package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

import android.os.Parcelable
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException

/**
 * Deserializa un DataSnapshot a MoldeProductos tolerando tipos mixtos (Long/String).
 * Firebase almacena números como Long; si algún campo String recibe un Long,
 * getValue(Class) lanza DatabaseException. Esta función cae en parseo manual en ese caso.
 */
fun DataSnapshot.toMoldeProductos(): MoldeProductos? {
    if (!exists()) return null
    return try {
        getValue(MoldeProductos::class.java)?.let { producto ->
            if (producto.tipoBaseInventario.isBlank()) {
                producto.copy(
                    tipoBaseInventario = PresentacionRules.normalizarTipoBaseInventario(
                        producto.tipoBaseInventario,
                        producto.unidadbase
                    )
                )
            } else {
                producto
            }
        }
    } catch (e: DatabaseException) {
        // Parseo defensivo: convierte cualquier tipo numérico a String
        MoldeProductos(
            nombre                    = child("nombre").value?.toString().orEmpty(),
            codigo                    = child("codigo").value?.toString().orEmpty(),
            vencimiento               = child("vencimiento").value?.toString().orEmpty(),
            categoria                 = child("categoria").value?.toString().orEmpty(),
            preciodecompra            = child("preciodecompra").value?.toString().orEmpty(),
            cantidadinicial           = child("cantidadinicial").value?.toString().orEmpty(),
            stockminimo               = child("stockminimo").value?.toString().orEmpty(),
            stockMinimoContenedores   = child("stockMinimoContenedores").value?.toString()?.toIntOrNull() ?: 0,
            unidadStockMinimo         = child("unidadStockMinimo").value?.toString().orEmpty(),
            unidadbase                = child("unidadbase").value?.toString().orEmpty(),
            unidadVisualInventario    = child("unidadVisualInventario").value?.toString().orEmpty(),
            tipoBaseInventario        = PresentacionRules.normalizarTipoBaseInventario(
                child("tipoBaseInventario").value?.toString(),
                child("unidadbase").value?.toString().orEmpty()
            ),
            presentacionprincipal     = child("presentacionprincipal").value?.toString().orEmpty(),
            requierereceta            = child("requierereceta").getValue(Boolean::class.java) ?: false,
            estadodelproducto         = child("estadodelproducto").getValue(Boolean::class.java) ?: true,
            tieneCodigoBarra          = child("tieneCodigoBarra").getValue(Boolean::class.java)
                ?: child("codigo").value?.toString().orEmpty().trim().isNotBlank(),
            indice                    = child("indice").value?.toString().orEmpty().ifBlank { key.orEmpty() },
            tienePresentaciones       = child("tienePresentaciones").getValue(Boolean::class.java) ?: false,
            unidadesPorPresentacionCompra = child("unidadesPorPresentacionCompra").value?.toString()?.toIntOrNull() ?: 0,
            basePorUnidad             = child("basePorUnidad").value?.toString()?.toDoubleOrNull() ?: 0.0,
            imagenUrl                 = child("imagenUrl").value?.toString().orEmpty(),
            loteConsumoSeleccionado   = child("loteConsumoSeleccionado").value?.toString().orEmpty(),
            loteConsumoSeleccionManual = child("loteConsumoSeleccionManual").getValue(Boolean::class.java) ?: false,
            referenceCommonUse        = child("referenceCommonUse").value?.toString().orEmpty(),
            referenceUseCases         = child("referenceUseCases").children.mapNotNull { it.value?.toString() },
            referenceHowToUse         = child("referenceHowToUse").value?.toString().orEmpty(),
            referenceNotRecommendedFor = child("referenceNotRecommendedFor").children.mapNotNull { it.value?.toString() },
            referenceKeywords         = child("referenceKeywords").children.mapNotNull { it.value?.toString() },
            referenceWarnings         = child("referenceWarnings").children.mapNotNull { it.value?.toString() },
            referenceSourceName       = child("referenceSourceName").value?.toString().orEmpty(),
            referenceSourceUrl        = child("referenceSourceUrl").value?.toString().orEmpty(),
            referenceRxcui            = child("referenceRxcui").value?.toString().orEmpty(),
            referenceNdc              = child("referenceNdc").value?.toString().orEmpty(),
            referenceMatchedName      = child("referenceMatchedName").value?.toString().orEmpty(),
            referenceConfidence       = child("referenceConfidence").value?.toString()?.toDoubleOrNull() ?: 0.0,
            referenceLanguage         = child("referenceLanguage").value?.toString().orEmpty().ifBlank { "es" },
            ubicacion                 = child("ubicacion").value?.toString().orEmpty(),
            proveedorId               = child("proveedorId").value?.toString().orEmpty(),
            proveedorNombre           = child("proveedorNombre").value?.toString().orEmpty(),
            lotes                     = child("lotes").children.mapNotNull { loteSnapshot ->
                val lote = loteSnapshot.getValue(LoteProducto::class.java)
                val clave = loteSnapshot.key.orEmpty()
                if (lote != null && clave.isNotBlank()) clave to lote else null
            }.toMap()
        )
    }
}

data class MoldeProductos(
    var nombre: String = "",
    var codigo: String = "",
    var vencimiento: String = "",
    var categoria: String = "",
    var preciodecompra: String = "",
    var cantidadinicial: String = "",
    var stockminimo: String = "",
    var stockMinimoContenedores: Int = 0,
    var unidadStockMinimo: String = "",
    var unidadbase: String = "",
    var unidadVisualInventario: String = "",
    var tipoBaseInventario: String = "",
    var presentacionprincipal: String = "",
    var requierereceta: Boolean = false,
    var estadodelproducto: Boolean = true,
    /** false = producto registrado sin codigo de barras (no aparece en CodigosProductos). */
    var tieneCodigoBarra: Boolean = false,
    var indice: String = "",
    var tienePresentaciones: Boolean = false,
    var presentaciones: MutableList<PresentacionProducto> = mutableListOf(),
    /**
     * Si el stock inicial se registró "por paquetes" (cajas, etc.), este valor guarda cuántas
     * unidades base trae 1 paquete de compra. Se usa como referencia para evitar presentaciones
     * ilógicas (ej: registrar "Caja" con 30 unidades si la caja de compra era de 20).
     *
     * 0 significa "no aplica / desconocido".
     */
    var unidadesPorPresentacionCompra: Int = 0,
    var basePorUnidad: Double = 0.0,  // ← mL o g por unidad física (ej: 300 mL/unidad)
    var imagenUrl: String = "",
    var loteConsumoSeleccionado: String = "",
    var loteConsumoSeleccionManual: Boolean = false,
    var referenceCommonUse: String = "",
    var referenceUseCases: List<String> = emptyList(),
    var referenceHowToUse: String = "",
    var referenceNotRecommendedFor: List<String> = emptyList(),
    var referenceKeywords: List<String> = emptyList(),
    var referenceWarnings: List<String> = emptyList(),
    var referenceSourceName: String = "",
    var referenceSourceUrl: String = "",
    var referenceRxcui: String = "",
    var referenceNdc: String = "",
    var referenceMatchedName: String = "",
    var referenceConfidence: Double = 0.0,
    var referenceLanguage: String = "es",
    var ubicacion: String = "",
    var proveedorId: String = "",
    var proveedorNombre: String = "",
    var lotes: Map<String, LoteProducto> = emptyMap()
)

val MoldeProductos.precioDeCompraDouble: Double
    get() = preciodecompra.toDoubleOrNull() ?: 0.0

val MoldeProductos.stockMinimoDouble: Double
    get() = stockminimo.toDoubleOrNull() ?: 0.0

fun MoldeProductos.stockFisicoBase(): Double {
    if (lotes.isEmpty()) {
        return cantidadinicial.toDoubleOrNull() ?: 0.0
    }
    return lotes.values.sumOf { it.cantidad + it.cantidadBloqueada }
}

fun MoldeProductos.stockMinimoBase(): Double {
    // V22.0: Prioridad absoluta al valor en unidad mínima para evitar errores de escala
    val visual = stockminimo.toDoubleOrNull() ?: 0.0
    
    // Si la unidad visual es kg/L, el valor base es x1000
    val factor = if (unidadVisualInventario == "kg" || unidadVisualInventario == "L") 1000.0 else 1.0
    return visual * factor
}

fun MoldeProductos.tieneCodigoBarraActivo(): Boolean =
    tieneCodigoBarra || codigo.trim().isNotBlank()

fun MoldeProductos.esProductoSinCodigoBarra(): Boolean = !tieneCodigoBarraActivo()

/**
 * V17.46: Registro simplificado para el índice plano de lotes.
 * Permite saber a qué producto pertenece un lote sin descargar todo el inventario.
 */
data class LoteIndexado(
    val numero: String = "",
    val productoId: String = "",
    val productoNombre: String = "",
    val loteId: String = "",
    val lotePath: String = "",
    val vencimiento: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
