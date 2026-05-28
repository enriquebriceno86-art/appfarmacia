package com.app.administradorfarmadon.ActivityInventario.agregarstock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteIndexado
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockFisicoBase
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ClasesDatabase.MovimientoInventarioLogger
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AgregarStockProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AgregarStockProductoState())
    val state: StateFlow<AgregarStockProductoState> = _state.asStateFlow()

    private var productoIndice: String = ""
    private var isFirstLoad = true
    private var lotValidationJob: kotlinx.coroutines.Job? = null

    fun cargarProducto(indice: String) {
        if (!isFirstLoad) return
        isFirstLoad = false
        productoIndice = indice

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val snapshot = FirebaseDatabase.getInstance()
                    .getReference("Inventario/Productos/$indice")
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    _state.update { it.copy(isLoading = false, error = "El producto no existe.") }
                    return@launch
                }

                val producto = snapshot.toMoldeProductos()
                if (producto == null) {
                    _state.update { it.copy(isLoading = false, error = "Error al leer los datos del producto.") }
                    return@launch
                }

                val uBase = producto.unidadbase.ifBlank { "unidades" }
                val tipo = normalizarTipoControl(
                    tipoRaw = producto.tipoBaseInventario.ifBlank { "UNIDAD" },
                    unidadRaw = uBase
                )

                val esDivisible = tipo == "PESO" || tipo == "LIQUIDO"
                val contenidoBase = producto.basePorUnidad
                val necesitaConfig = esDivisible && contenidoBase <= 0.0
                val unidadesPorCaja = producto.unidadesPorPresentacionCompra

                _state.update {
                    it.copy(
                        isLoading = false,
                        productoOriginal = producto,
                        tipoControl = tipo,
                        unidadBase = uBase,
                        contenidoPorEnvase = if (contenidoBase > 0) contenidoBase else 1.0,
                        necesitaConfigurarIdentidadFisica = necesitaConfig,
                        pendingTipoControl = tipo,
                        pendingUnidadBase = uBase,
                        pendingContenidoPorEnvaseTexto = if (contenidoBase > 0) contenidoBase.toString() else "",
                        envasesPorCajaText = if (unidadesPorCaja > 0) unidadesPorCaja.toString() else "",
                        unidadesPorCajaAncladas = unidadesPorCaja > 0,
                        modoIngreso = if (unidadesPorCaja > 0) ModoIngresoStock.CAJA_ENVASES else ModoIngresoStock.UNIDADES_SUELTAS
                    )
                }
                recalcularTotales()
                cargarProveedores()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun cargarProveedores() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = FirebaseDatabase.getInstance().getReference("Proveedores")
                val snapshot = db.get().await()
                val lista = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor>()
                snapshot.children.forEach { child ->
                    child.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor::class.java)?.let { lista.add(it) }
                }
                _state.update { it.copy(suppliers = lista) }
            } catch (_: Exception) {
                // Silent load error
            }
        }
    }

    fun onEvent(event: AgregarStockEvent) {
        when (event) {
            is AgregarStockEvent.OnModoIngresoChanged -> {
                if (
                    event.modo == ModoIngresoStock.CAJA_PAQUETES &&
                    _state.value.unidadesPorCajaAncladas
                ) {
                    return
                }

                _state.update {
                    clearMismatchState(
                        it.copy(modoIngreso = event.modo)
                    )
                }
                recalcularTotales()
            }
            is AgregarStockEvent.OnUnidadesSueltasChanged -> {
                _state.update { clearMismatchState(it.copy(unidadesSueltasText = event.text)) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnCajasRecibidasChanged -> {
                _state.update { clearMismatchState(it.copy(cajasRecibidasText = event.text)) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnEnvasesPorCajaChanged -> {
                if (_state.value.unidadesPorCajaAncladas) return
                _state.update { clearMismatchState(it.copy(envasesPorCajaText = event.text)) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnPaquetesPorCajaChanged -> {
                if (_state.value.unidadesPorCajaAncladas) return
                _state.update { clearMismatchState(it.copy(paquetesPorCajaText = event.text)) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnEnvasesPorPaqueteChanged -> {
                if (_state.value.unidadesPorCajaAncladas) return
                _state.update { clearMismatchState(it.copy(envasesPorPaqueteText = event.text)) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnLoteChanged -> {
                _state.update {
                    it.copy(
                        loteNumero = event.text,
                        lotValidatedFor = "",
                        lotConflictMessage = null,
                        lotConflictSeverity = 0,
                        lotWillMerge = false,
                        isFormValid = false
                    )
                }
                validarLoteIngreso(event.text)
            }
            is AgregarStockEvent.OnVencimientoChanged -> _state.update { it.copy(loteVencimiento = event.text) }
            is AgregarStockEvent.OnCostoChanged -> {
                _state.update { it.copy(costoCompra = event.text) }
                recalcularTotales()
            }
            is AgregarStockEvent.OnFacturaChanged -> _state.update { it.copy(numeroFactura = event.text) }
            is AgregarStockEvent.OnProveedorSelected -> {
                _state.update {
                    it.copy(
                        proveedorId = event.id,
                        proveedorNombre = event.nombre,
                        showSupplierDialog = false
                    )
                }
                recalcularTotales()
            }
            AgregarStockEvent.OnSupplierClick -> _state.update { it.copy(showSupplierDialog = true) }
            AgregarStockEvent.OnSupplierDialogDismiss -> _state.update { it.copy(showSupplierDialog = false) }
            is AgregarStockEvent.OnIdentidadFisicaChanged -> {
                _state.update {
                    it.copy(
                        pendingTipoControl = event.tipoControl,
                        pendingUnidadBase = event.unidadBase,
                        pendingContenidoPorEnvaseTexto = event.contenidoText
                    )
                }
            }
            AgregarStockEvent.ConfirmarIdentidadFisica -> {
                val contenido = parseDecimal(_state.value.pendingContenidoPorEnvaseTexto)
                if (contenido <= 0.0) {
                    _state.update { it.copy(error = "Ingresa un contenido por envase válido.") }
                    return
                }
                _state.update {
                    it.copy(
                        tipoControl = it.pendingTipoControl,
                        unidadBase = it.pendingUnidadBase,
                        contenidoPorEnvase = contenido,
                        necesitaConfigurarIdentidadFisica = false,
                        error = null
                    )
                }
                recalcularTotales()
            }
            is AgregarStockEvent.OnReconciliationReasonSelected -> {
                _state.update { it.copy(mismatchJustified = true, reconciliationReason = event.reason) }
                recalcularTotales()
            }
            AgregarStockEvent.GuardarIngreso -> guardarIngreso()
        }
    }

    private fun validarLoteIngreso(numero: String) {
        lotValidationJob?.cancel()
        val lote = numero.trim().uppercase(Locale.getDefault())
        if (lote.isBlank()) {
            _state.update {
                it.copy(
                    isCheckingLot = false,
                    lotValidatedFor = "",
                    lotConflictMessage = null,
                    lotConflictSeverity = 0,
                    lotWillMerge = false
                )
            }
            recalcularTotales()
            return
        }
        lotValidationJob = viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            val current = _state.value
            val loteActual = current.loteNumero.trim().uppercase(Locale.getDefault())
            if (loteActual != lote) return@launch
            _state.update { it.copy(isCheckingLot = true) }
            try {
                val root = FirebaseDatabase.getInstance().reference
                val snapshot = root
                    .child("Inventario/LotesPorNumero")
                    .child(ProductUtils.encodeLotKey(lote))
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    _state.update {
                        it.copy(
                            isCheckingLot = false,
                            lotValidatedFor = lote,
                            lotConflictMessage = "Lote disponible para registrar",
                            lotConflictSeverity = 0,
                            lotWillMerge = false
                        )
                    }
                    recalcularTotales()
                    return@launch
                }

                val loteIndexado = snapshot.getValue(LoteIndexado::class.java)
                val productoIdDelLote = loteIndexado?.productoId.orEmpty()

                if (productoIdDelLote == productoIndice) {
                    _state.update {
                        it.copy(
                            isCheckingLot = false,
                            lotValidatedFor = lote,
                            lotConflictMessage = "Este lote ya existe. El stock se sumará al lote existente.",
                            lotConflictSeverity = 1,
                            lotWillMerge = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isCheckingLot = false,
                            lotValidatedFor = lote,
                            lotConflictMessage = "Lote usado en otro producto: ${loteIndexado?.productoNombre.orEmpty()}",
                            lotConflictSeverity = 2,
                            lotWillMerge = false
                        )
                    }
                }
                recalcularTotales()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isCheckingLot = false,
                        lotValidatedFor = "",
                        lotConflictMessage = "No se pudo validar el lote. Revisa tu conexión.",
                        lotConflictSeverity = 2,
                        lotWillMerge = false
                    )
                }
                recalcularTotales()
            }
        }
    }

    private fun parseDecimal(text: String): Double {
        return text.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private fun clearMismatchState(state: AgregarStockProductoState): AgregarStockProductoState {
        return state.copy(mismatchJustified = false, reconciliationReason = "")
    }

    private fun recalcularTotales() {
        val s = _state.value
        if (s.productoOriginal == null) return

        val esDivisible = s.tipoControl == "PESO" || s.tipoControl == "LIQUIDO"
        val contenido = if (esDivisible) s.contenidoPorEnvase else 1.0

        val totalEnContenedoresFisicos = when (s.modoIngreso) {
            ModoIngresoStock.UNIDADES_SUELTAS -> parseDecimal(s.unidadesSueltasText)
            ModoIngresoStock.CAJA_ENVASES -> parseDecimal(s.cajasRecibidasText) * parseDecimal(s.envasesPorCajaText)
            ModoIngresoStock.CAJA_PAQUETES -> parseDecimal(s.cajasRecibidasText) * parseDecimal(s.paquetesPorCajaText) * parseDecimal(s.envasesPorPaqueteText)
        }

        val totalBase = totalEnContenedoresFisicos * contenido

        val hasMismatch = when (s.modoIngreso) {
            ModoIngresoStock.CAJA_ENVASES -> {
                val envasesIngresados = parseDecimal(s.envasesPorCajaText)
                val envasesEsperados = s.productoOriginal.unidadesPorPresentacionCompra.toDouble()
                envasesEsperados > 0 && envasesIngresados > 0 && envasesIngresados != envasesEsperados
            }
            ModoIngresoStock.CAJA_PAQUETES -> {
                val envasesIngresados = parseDecimal(s.paquetesPorCajaText) * parseDecimal(s.envasesPorPaqueteText)
                val envasesEsperados = s.productoOriginal.unidadesPorPresentacionCompra.toDouble()
                envasesEsperados > 0 && envasesIngresados > 0 && envasesIngresados != envasesEsperados
            }
            else -> false
        }
        val isCostValid = parseDecimal(s.costoCompra) > 0
        val isProviderValid = s.proveedorId.isNotBlank() || s.proveedorNombre.isNotBlank()
        val isMismatchSatisfied = !hasMismatch || s.mismatchJustified
        val loteClean = s.loteNumero.trim().uppercase(Locale.getDefault())
        val isLotValid = loteClean.isNotBlank() && !s.isCheckingLot && s.lotValidatedFor == loteClean && s.lotConflictSeverity != 2

        _state.update {
            it.copy(
                totalBaseCalculado = totalBase,
                reconciliationHasMismatch = hasMismatch,
                isFormValid = totalBase > 0 && isCostValid && isProviderValid && isMismatchSatisfied && isLotValid
            )
        }
    }

    private fun guardarIngreso() {
        val s = _state.value
        val prod = s.productoOriginal ?: return
        if (!s.isFormValid) {
            _state.update { it.copy(error = "Completa todos los campos correctamente.") }
            return
        }
        if (s.necesitaConfigurarIdentidadFisica) {
            _state.update { it.copy(error = "Debes confirmar la identidad del producto primero.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            var loteIdParaIndexar: String? = null
            val nuevoLoteIdSeguro = UUID.randomUUID().toString()
            val loteInicialLegacyId = "stock_inicial_legacy"

            try {
                val dbRef = FirebaseDatabase.getInstance().getReference("Inventario/Productos/$productoIndice")

                dbRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                    override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                        val productData = currentData.getValue(MoldeProductos::class.java) ?: return com.google.firebase.database.Transaction.abort()
                        val totalUnidades = s.totalBaseCalculado
                        
                        if (productData.lotes.isEmpty() && productData.stockFisicoBase() > 0.0) {
                            val loteLegacy = LoteProducto(
                                numero = "STOCK_INICIAL_SIN_LOTE",
                                vencimiento = productData.vencimiento,
                                cantidad = productData.stockFisicoBase(),
                                fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                reconciliationSource = "LEGACY_STOCK_BACKFILL"
                            )
                            currentData.child("lotes").child(loteInicialLegacyId).value = loteLegacy
                            val lotesActualizados = productData.lotes.toMutableMap()
                            lotesActualizados[loteInicialLegacyId] = loteLegacy
                            productData.lotes = lotesActualizados
                        }

                        val stockDespues = productData.stockFisicoBase() + totalUnidades
                        val stockDespuesStr = if (stockDespues % 1.0 == 0.0) stockDespues.toInt().toString() else String.format(Locale.US, "%.2f", stockDespues)
                        currentData.child("cantidadinicial").value = stockDespuesStr

                        if (s.proveedorId.isNotBlank()) {
                            currentData.child("proveedorId").value = s.proveedorId
                            currentData.child("proveedorNombre").value = s.proveedorNombre
                        }
                        if (productData.vencimiento.isBlank() && s.loteVencimiento.isNotBlank()) {
                            currentData.child("vencimiento").value = s.loteVencimiento
                        }
                        if (productData.basePorUnidad <= 0.0 && s.contenidoPorEnvase > 0.0) {
                            currentData.child("tipoBaseInventario").value = s.tipoControl
                            currentData.child("unidadbase").value = s.unidadBase
                            currentData.child("basePorUnidad").value = s.contenidoPorEnvase
                        }

                        val costoTotal = parseDecimal(s.costoCompra)
                        val costoUnitario = if (totalUnidades > 0) costoTotal / totalUnidades else 0.0
                        val numeroLoteNorm = s.loteNumero.ifBlank { "SIN_LOTE" }.trim().uppercase(Locale.getDefault())

                        val loteExistenteEntry = productData.lotes.entries.find {
                            it.value.numero.trim().uppercase(Locale.getDefault()) == numeroLoteNorm
                        }

                        if (loteExistenteEntry != null) {
                            val loteId = loteExistenteEntry.key
                            loteIdParaIndexar = loteId
                            val loteData = loteExistenteEntry.value
                            val nuevaCantidad = loteData.cantidad + totalUnidades
                            currentData.child("lotes/$loteId/cantidad").value = nuevaCantidad
                            currentData.child("lotes/$loteId/costoUltimoIngreso").value = costoTotal
                            currentData.child("lotes/$loteId/costoUltimoIngresoUnitario").value = costoUnitario
                            if (loteData.vencimiento.isBlank() && s.loteVencimiento.isNotBlank()) {
                                currentData.child("lotes/$loteId/vencimiento").value = s.loteVencimiento
                            }
                        } else {
                            val nuevoLoteId = nuevoLoteIdSeguro
                            loteIdParaIndexar = nuevoLoteId
                            val nuevoLote = crearNuevoLote(
                                numero = numeroLoteNorm,
                                cantidad = totalUnidades,
                                costoTotal = costoTotal,
                                costoUnitario = costoUnitario,
                                vencimientoProducto = productData.vencimiento
                            )
                            currentData.child("lotes").child(nuevoLoteId).value = nuevoLote
                        }

                        return com.google.firebase.database.Transaction.success(currentData)
                    }

                    override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {
                        if (error != null) {
                            _state.update { it.copy(isSaving = false, error = "Error en transacción: ${error.message}") }
                            return
                        }
                        if (!committed) {
                            _state.update { it.copy(isSaving = false, error = "No se pudo guardar. Intenta nuevamente.") }
                            return
                        }

                        val productDataActualizado = currentData?.toMoldeProductos()
                        val stockDespues = productDataActualizado?.stockFisicoBase() ?: 0.0
                        val totalUnidades = s.totalBaseCalculado
                        val stockAntes = stockDespues - totalUnidades

                        registrarLogs(prod, stockAntes, stockDespues, totalUnidades)

                        viewModelScope.launch {
                            try {
                                loteIdParaIndexar?.let { loteId ->
                                    actualizarIndiceDeLotes(loteId, productDataActualizado)
                                }
                                _state.update { it.copy(isSaving = false, saveSuccess = true) }
                            } catch (e: Exception) {
                                _state.update {
                                    it.copy(
                                        isSaving = false,
                                        isFormValid = false,
                                        error = "Stock guardado, pero índice del lote falló. No guardes de nuevo."
                                    )
                                }
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun crearNuevoLote(numero: String, cantidad: Double, costoTotal: Double, costoUnitario: Double, vencimientoProducto: String): LoteProducto {
        val s = _state.value
        return LoteProducto(
            numero = numero,
            vencimiento = s.loteVencimiento.ifBlank { vencimientoProducto },
            cantidad = cantidad,
            cantidadBloqueada = 0.0,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            costoCompraUnitario = costoUnitario,
            costoUltimoIngreso = costoTotal,
            costoUltimoIngresoUnitario = costoUnitario,
            nroFactura = s.numeroFactura,
            condicionPago = "CONTADO",
            estadoPago = "PAGADO",
            reconciliationSource = "ADD_STOCK_EXISTING_PRODUCT",
            reconciliationEnteredTotal = s.totalBaseCalculado,
            reconciliationHasMismatch = s.reconciliationHasMismatch,
            reconciliationReason = if (s.mismatchJustified) s.reconciliationReason else null
        )
    }

    private fun registrarLogs(prod: MoldeProductos, stockAntes: Double, stockDespues: Double, totalUnidades: Double) {
        val s = _state.value
        val app = getApplication<Application>()
        MovimientoInventarioLogger.registrarConSesion(
            context = app,
            indiceProducto = productoIndice,
            tipo = "stock_entrada",
            titulo = "Ingreso de stock",
            descripcion = "Entró mercadería para ${prod.nombre}",
            cantidad = totalUnidades.toInt(),
            stockAntes = stockAntes.toInt(),
            stockDespues = stockDespues.toInt(),
            motivo = "Ingreso de stock manual",
            referenciaId = s.numeroFactura.ifBlank { "ingreso_stock" },
            extra = mapOf(
                "costoIngresoTotal" to parseDecimal(s.costoCompra),
                "loteNumero" to s.loteNumero,
                "modoIngreso" to s.modoIngreso.name,
                "cantidadExacta" to totalUnidades,
                "stockAntesExacto" to stockAntes,
                "stockDespuesExacto" to stockDespues
            )
        )
        MovimientoLogger.registrarConSesion(
            context = app,
            tipo = "ingreso_stock",
            modulo = "inventario",
            titulo = "Ingreso de stock",
            descripcion = "Entró stock para ${prod.nombre}",
            referenciaId = productoIndice,
            extra = mapOf("stockAntes" to stockAntes, "stockDespues" to stockDespues, "loteNumero" to s.loteNumero)
        )
    }

    private suspend fun actualizarIndiceDeLotes(loteId: String, producto: MoldeProductos?) {
        val prod = producto ?: return
        val lote = prod.lotes[loteId] ?: return

        val loteKey = ProductUtils.encodeLotKey(lote.numero)
        val indexado = LoteIndexado(
            numero = lote.numero,
            productoId = productoIndice,
            productoNombre = prod.nombre,
            loteId = loteId,
            lotePath = "Inventario/Productos/$productoIndice/lotes/$loteId",
            vencimiento = lote.vencimiento,
            timestamp = System.currentTimeMillis()
        )
        FirebaseDatabase.getInstance().getReference("Inventario/LotesPorNumero")
            .child(loteKey)
            .setValue(indexado)
            .await()
    }

    private fun normalizarTipoControl(tipoRaw: String, unidadRaw: String): String {
        val tipo = tipoRaw.trim().uppercase(Locale.getDefault()).replace("Í", "I")
        val unidad = unidadRaw.trim().lowercase(Locale.getDefault()).replace(".", "")
        return when {
            (tipo == "PESO" || unidad in setOf("g", "gr", "gramo", "gramos", "kg", "kilo", "kilos", "kilogramo", "kilogramos")) -> "PESO"
            (tipo == "LIQUIDO" || unidad in setOf("ml", "mililitro", "mililitros", "l", "lt", "lts", "litro", "litros")) -> "LIQUIDO"
            else -> "UNIDAD"
        }
    }
}

sealed class AgregarStockEvent {

    data class OnModoIngresoChanged(val modo: ModoIngresoStock) : AgregarStockEvent()
    data class OnUnidadesSueltasChanged(val text: String) : AgregarStockEvent()
    data class OnCajasRecibidasChanged(val text: String) : AgregarStockEvent()
    data class OnEnvasesPorCajaChanged(val text: String) : AgregarStockEvent()
    data class OnPaquetesPorCajaChanged(val text: String) : AgregarStockEvent()
    data class OnEnvasesPorPaqueteChanged(val text: String) : AgregarStockEvent()
    data class OnLoteChanged(val text: String) : AgregarStockEvent()
    data class OnVencimientoChanged(val text: String) : AgregarStockEvent()
    data class OnCostoChanged(val text: String) : AgregarStockEvent()
    data class OnFacturaChanged(val text: String) : AgregarStockEvent()
    data class OnProveedorSelected(val id: String, val nombre: String) : AgregarStockEvent()
    object OnSupplierClick : AgregarStockEvent()
    object OnSupplierDialogDismiss : AgregarStockEvent()
    data class OnIdentidadFisicaChanged(val tipoControl: String, val unidadBase: String, val contenidoText: String) : AgregarStockEvent()
    object ConfirmarIdentidadFisica : AgregarStockEvent()
    data class OnReconciliationReasonSelected(val reason: String) : AgregarStockEvent()
    object GuardarIngreso : AgregarStockEvent()
}
