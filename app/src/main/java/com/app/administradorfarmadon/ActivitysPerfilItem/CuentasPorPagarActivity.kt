package com.app.administradorfarmadon.ActivitysPerfilItem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.google.firebase.database.FirebaseDatabase

class CuentasPorPagarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                CuentasPorPagarScreen(onBack = { finish() })
            }
        }
    }
}

data class CuentaPorPagar(
    val id: String = "",
    val nroFactura: String = "",
    val proveedorNombre: String = "",
    val totalAPagar: Double = 0.0,
    val fechaCompra: String = "",
    val fechaVencimientoPago: String = "",
    val productoNombre: String = "",
    val estadoPago: String = "PENDIENTE",
    val condicion: String = "CREDITO",
    val desgloseIngreso: String = "",
    val unidadVisual: String = "",
    val fotoFactura: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentasPorPagarScreen(onBack: () -> Unit) {
    var cuentas by remember { mutableStateOf<List<CuentaPorPagar>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCuenta by remember { mutableStateOf<CuentaPorPagar?>(null) }

    LaunchedEffect(Unit) {
        // V28.8: Cargar desde el historial general para tener toda la info siempre
        FirebaseDatabase.getInstance().getReference(DbPaths.INVENTARIO_PAGOS_PROVEEDORES)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<CuentaPorPagar>()
                snapshot.children.forEach { child ->
                    child.getValue(CuentaPorPagar::class.java)?.let { list.add(it) }
                }
                cuentas = list.reversed() // Más recientes primero
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val pendientes = remember(cuentas) { 
        cuentas.filter { it.estadoPago == "PENDIENTE" && it.condicion == "CREDITO" }
            .sortedBy { it.fechaVencimientoPago }
    }
    
    val historial = remember(cuentas) { 
        cuentas.filter { it.estadoPago == "PAGADO" || it.condicion == "CONTADO" }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Pagos a Proveedores", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF15A05C),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("PENDIENTES (${pendientes.size})", fontSize = 11.sp, fontWeight = FontWeight.Black) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("HISTORIAL", fontSize = 11.sp, fontWeight = FontWeight.Black) }
                    )
                }
            }
        },
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        val currentList = if (selectedTab == 0) pendientes else historial

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF15A05C))
            }
        } else if (currentList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.MoneyOff, null, modifier = Modifier.size(64.dp), tint = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedTab == 0) "No hay deudas pendientes" else "No hay historial de pagos", 
                    color = Color(0xFF6B7280), 
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentList) { cuenta ->
                    CuentaItem(
                        cuenta = cuenta,
                        onClick = { selectedCuenta = cuenta },
                        onMarkAsPaid = {
                            val db = FirebaseDatabase.getInstance()
                            val updates = mapOf(
                                "${DbPaths.INVENTARIO_CUENTAS_POR_PAGAR}/${cuenta.id}" to null, // Eliminar de pendientes
                                "${DbPaths.INVENTARIO_PAGOS_PROVEEDORES}/${cuenta.id}/estadoPago" to "PAGADO"
                            )
                            db.reference.updateChildren(updates).addOnSuccessListener {
                                cuentas = cuentas.map { if (it.id == cuenta.id) it.copy(estadoPago = "PAGADO") else it }
                            }
                        }
                    )
                }
            }
        }
    }

    if (selectedCuenta != null) {
        DetallePagoDialog(
            cuenta = selectedCuenta!!,
            onDismiss = { selectedCuenta = null }
        )
    }
}

@Composable
fun CuentaItem(
    cuenta: CuentaPorPagar, 
    onClick: () -> Unit,
    onMarkAsPaid: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cuenta.proveedorNombre.ifBlank { "Proveedor Desconocido" },
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Factura: ${cuenta.nroFactura}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MonedaHelper.formatear(cuenta.totalAPagar),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (cuenta.estadoPago == "PENDIENTE") Color(0xFFD32F2F) else Color(0xFF15A05C)
                    )
                    Surface(
                        color = if (cuenta.condicion == "CONTADO") Color(0xFFF3F4F6) else Color(0xFFFFF7ED),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = cuenta.condicion,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cuenta.condicion == "CONTADO") Color(0xFF4B5563) else Color(0xFFEA580C)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val accentColor = if (cuenta.estadoPago == "PAGADO") Color(0xFF15A05C) else Color(0xFFEA580C)
                Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (cuenta.estadoPago == "PAGADO") "Pagado el: ${cuenta.fechaCompra}" else "Vence: ${cuenta.fechaVencimientoPago}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.weight(1f))
                
                if (cuenta.estadoPago == "PENDIENTE") {
                    Button(
                        onClick = onMarkAsPaid,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15A05C)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("PAGAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text(
                        text = cuenta.productoNombre,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
fun DetallePagoDialog(cuenta: CuentaPorPagar, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Receipt, null, tint = Color(0xFF15A05C), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Detalle de Factura", fontWeight = FontWeight.Black, fontSize = 20.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Proveedor", cuenta.proveedorNombre)
                    DetailRow("Producto", cuenta.productoNombre)
                    
                    if (cuenta.fotoFactura != null) {
                        val bitmap = remember(cuenta.fotoFactura) {
                            val decodedString = android.util.Base64.decode(cuenta.fotoFactura, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        }
                        
                        if (bitmap != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                color = Color(0xFFF3F4F6)
                            ) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto de factura",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF7F9FC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5EAF0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("COMO SE RECIBIÓ", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cuenta.desgloseIngreso.ifBlank { "Sin desglose detallado" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DetailBlock("FECHA COMPRA", cuenta.fechaCompra)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DetailBlock("CONDICIÓN", cuenta.condicion)
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("COSTO TOTAL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15A05C))
                            Text(
                                text = MonedaHelper.formatear(cuenta.totalAPagar),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color(0xFF15A05C)
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
    }
}

@Composable
fun DetailBlock(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF9CA3AF), letterSpacing = 0.5.sp)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}
