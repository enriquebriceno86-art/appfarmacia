package com.app.administradorfarmadon.ActivitysPerfilItem

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.app.administradorfarmadon.databinding.BottomsheetCrearTipoPagoBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetCrearTipoPago : BottomSheetDialogFragment() {

    private var _binding: BottomsheetCrearTipoPagoBinding? = null
    private val binding get() = _binding!!

    private var selectedQrUri: Uri? = null
    private var currentQrUrl: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedQrUri = uri
            binding.layoutQrBilletera.isVisible = true
            Glide.with(this).load(uri).into(binding.imgQrBilleteraPreview)
            binding.btnQuitarQrBilletera.isVisible = true
        }
    }

    private val metodosRef by lazy {
        FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("metodosPago")
    }

    private val metodoId: String?
        get() = arguments?.getString(ARG_METODO_ID)?.takeIf { it.isNotBlank() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetCrearTipoPagoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInputs()
        setupActions()
        if (metodoId == null) {
            val defaultCategory = CATEGORY_OPTIONS.first()
            binding.txtTituloBottom.text = "Crear tipo de pago"
            binding.autoCategoria.setText(defaultCategory.id, false)
            binding.txtDescripcionCategoria.text = defaultCategory.description
            updateCategoryViews(defaultCategory.id)
        } else {
            binding.txtTituloBottom.text = "Editar tipo de pago"
            loadMetodo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupInputs() {
        binding.autoCategoria.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                CATEGORY_OPTIONS.map { it.id }
            )
        )
        binding.autoTipoCuenta.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                listOf("Ahorros", "Corriente")
            )
        )

        binding.autoCategoria.setOnItemClickListener { _, _, position, _ ->
            val option = CATEGORY_OPTIONS[position]
            binding.txtDescripcionCategoria.text = option.description
            updateCategoryViews(option.id)
        }

        binding.switchUsaQR.setOnCheckedChangeListener { _, isChecked ->
            val isWallet = binding.cardBilletera.isVisible
            binding.layoutQrBilletera.isVisible = isChecked && isWallet && (selectedQrUri != null || currentQrUrl.isNotBlank())
            binding.btnSeleccionarQrBilletera.isVisible = isChecked && isWallet
        }

        binding.btnSeleccionarQrBilletera.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnQuitarQrBilletera.setOnClickListener {
            selectedQrUri = null
            currentQrUrl = ""
            binding.imgQrBilleteraPreview.setImageDrawable(null)
            binding.layoutQrBilletera.isVisible = false
            binding.btnQuitarQrBilletera.isVisible = false
        }
    }

    private fun setupActions() {
        binding.btnCancelar.setOnClickListener { dismissAllowingStateLoss() }
        binding.btnGuardar.setOnClickListener { saveMetodo() }
    }

    private fun loadMetodo() {
        val id = metodoId ?: return
        showSaving(true, "Cargando configuracion...")
        metodosRef.child(id).get()
            .addOnSuccessListener { snapshot ->
                showSaving(false)
                val metodo = snapshot.getValue(MetodoPagoConfig::class.java)
                if (metodo == null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Metodo no encontrado")
                        .setMessage("No pudimos localizar la configuracion de este metodo de pago.")
                        .setPositiveButton("Cerrar") { _, _ -> dismissAllowingStateLoss() }
                        .setCancelable(false)
                        .show()
                    return@addOnSuccessListener
                }
                bindMetodo(metodo)
            }
            .addOnFailureListener {
                showSaving(false)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error de red")
                    .setMessage("No se pudo conectar con el servidor para cargar la configuracion.")
                    .setPositiveButton("Reintentar") { _, _ -> loadMetodo() }
                    .setNegativeButton("Cerrar") { _, _ -> dismissAllowingStateLoss() }
                    .setCancelable(false)
                    .show()
            }
    }

    private fun bindMetodo(metodo: MetodoPagoConfig) {
        binding.autoCategoria.setText(metodo.categoria, false)
        binding.edtTituloMetodo.setText(metodo.titulo)
        binding.switchActivo.isChecked = metodo.activo
        binding.switchPermiteVuelto.isChecked = metodo.permiteVuelto
        binding.switchSolicitaMontoRecibido.isChecked = metodo.solicitaMontoRecibido
        binding.switchCalculaVuelto.isChecked = metodo.calculaVuelto
        binding.switchPermiteReferencia.isChecked = metodo.permiteReferencia
        binding.switchUsaQR.isChecked = metodo.usaQR
        binding.switchDisponibleMixto.isChecked = metodo.disponibleMixto
        binding.edtBanco.setText(metodo.banco)
        binding.autoTipoCuenta.setText(metodo.tipoCuenta, false)
        binding.edtNumeroCuenta.setText(metodo.numeroCuenta)
        binding.edtTitularBanco.setText(metodo.titularBanco)
        binding.edtDocumentoBanco.setText(metodo.documentoBanco)
        binding.edtTelefonoBilletera.setText(metodo.telefonoBilletera)
        binding.edtTitularBilletera.setText(metodo.titularBilletera)
        binding.edtAliasBilletera.setText(metodo.aliasBilletera)
        binding.edtInstrucciones.setText(metodo.instrucciones)
        binding.edtDescripcion.setText(metodo.descripcion)
        binding.edtOrden.setText(metodo.orden.toString())
        
        currentQrUrl = metodo.qrUrl
        binding.switchUsaQR.isChecked = metodo.usaQR
        if (currentQrUrl.isNotBlank()) {
            binding.layoutQrBilletera.isVisible = metodo.usaQR
            Glide.with(this).load(currentQrUrl).into(binding.imgQrBilleteraPreview)
            binding.btnQuitarQrBilletera.isVisible = true
        }

        val option = CATEGORY_OPTIONS.firstOrNull { it.id == metodo.categoria }
        binding.txtDescripcionCategoria.text =
            option?.description ?: "Selecciona una categoria para configurar su comportamiento."
        updateCategoryViews(metodo.categoria)
    }

    private fun saveMetodo() {
        val categoria = binding.autoCategoria.text?.toString().orEmpty().trim()
        val titulo = binding.edtTituloMetodo.text?.toString().orEmpty().trim()

        binding.layoutCategoria.error = if (categoria.isBlank()) "Selecciona una categoria" else null
        binding.layoutTituloMetodo.error = if (titulo.isBlank()) "Ingresa un nombre" else null

        if (categoria.isBlank() || titulo.isBlank()) return

        val id = metodoId ?: ""

        showSaving(true, "Guardando tipo de pago...")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val qrActivo = binding.switchUsaQR.isChecked
                val metodo = MetodoPagoConfig(
                    id = id,
                    titulo = titulo,
                    categoria = categoria,
                    activo = binding.switchActivo.isChecked,
                    permiteVuelto = binding.switchPermiteVuelto.isChecked,
                    solicitaMontoRecibido = binding.switchSolicitaMontoRecibido.isChecked,
                    calculaVuelto = binding.switchCalculaVuelto.isChecked,
                    permiteReferencia = binding.switchPermiteReferencia.isChecked,
                    usaQR = qrActivo,
                    disponibleMixto = binding.switchDisponibleMixto.isChecked,
                    orden = binding.edtOrden.text?.toString()?.trim()?.toIntOrNull() ?: 0,
                    banco = binding.edtBanco.text?.toString().orEmpty(),
                    tipoCuenta = binding.autoTipoCuenta.text?.toString().orEmpty(),
                    numeroCuenta = binding.edtNumeroCuenta.text?.toString().orEmpty(),
                    titularBanco = binding.edtTitularBanco.text?.toString().orEmpty(),
                    documentoBanco = binding.edtDocumentoBanco.text?.toString().orEmpty(),
                    telefonoBilletera = binding.edtTelefonoBilletera.text?.toString().orEmpty(),
                    titularBilletera = binding.edtTitularBilletera.text?.toString().orEmpty(),
                    aliasBilletera = binding.edtAliasBilletera.text?.toString().orEmpty(),
                    qrUrl = if (qrActivo) currentQrUrl else "",
                    instrucciones = binding.edtInstrucciones.text?.toString().orEmpty(),
                    descripcion = binding.edtDescripcion.text?.toString().orEmpty(),
                    placeholder = titulo,
                    preload = false
                )

                val result = PaymentMethodsRepository.saveMethod(
                    method = metodo,
                    localQrUri = if (qrActivo) selectedQrUri else null
                )

                result.onSuccess {
                    showSaving(false)
                    dismissAllowingStateLoss()
                }.onFailure { e ->
                    showSaving(false)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Revisa la configuracion")
                        .setMessage(e.message)
                        .setPositiveButton("Entendido", null)
                        .show()
                }
            } catch (e: Exception) {
                showSaving(false)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error inesperado")
                    .setMessage(e.message)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }
    }

    private fun updateCategoryViews(category: String) {
        val normalized = category.trim().lowercase()
        val isTransfer = normalized == "transferencia_bancaria"
        val isWallet = normalized == "billetera_digital"

        binding.cardTransferencia.isVisible = isTransfer
        binding.cardBilletera.isVisible = isWallet
        binding.switchPermiteVuelto.isGone = isTransfer || isWallet
        binding.switchSolicitaMontoRecibido.isGone = isTransfer || isWallet
        binding.switchCalculaVuelto.isGone = isTransfer || isWallet
        
        val qrActivo = binding.switchUsaQR.isChecked
        binding.layoutQrBilletera.isVisible = isWallet && qrActivo && (selectedQrUri != null || currentQrUrl.isNotBlank())
        binding.btnSeleccionarQrBilletera.isVisible = isWallet && qrActivo
    }

    private fun showSaving(show: Boolean, message: String = "Guardando tipo de pago...") {
        binding.progressCrearTipoPago.isVisible = show
        binding.textGuardando.isVisible = show
        binding.textGuardando.text = message
        binding.btnGuardar.isEnabled = !show
        binding.btnCancelar.isEnabled = !show
    }

    companion object {
        private const val ARG_METODO_ID = "metodo_id"

        private val CATEGORY_OPTIONS = listOf(
            CategoryOption("efectivo", "Ideal para cobros directos en caja y metodos con vuelto."),
            CategoryOption("transferencia_bancaria", "Para cuentas bancarias y validacion de referencias."),
            CategoryOption("billetera_digital", "Para billeteras digitales como Yape, Plin o similares."),
            CategoryOption("tarjeta", "Para pagos con POS o terminal de tarjeta."),
            CategoryOption("otro", "Configuracion flexible para necesidades especiales.")
        )

        fun newInstance(metodoId: String): BottomSheetCrearTipoPago {
            return BottomSheetCrearTipoPago().apply {
                val args = Bundle()
                args.putString(ARG_METODO_ID, metodoId)
                arguments = args
            }
        }
    }
}

private data class CategoryOption(
    val id: String,
    val description: String
)
