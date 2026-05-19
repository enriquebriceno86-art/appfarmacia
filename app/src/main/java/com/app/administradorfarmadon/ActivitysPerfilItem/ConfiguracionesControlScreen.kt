package com.app.administradorfarmadon.ActivitysPerfilItem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.administradorfarmadon.ClasesDatabase.PreferenciasFeedbackCaja
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionesControlScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var vibracionMaestra by remember { mutableStateOf(PreferenciasFeedbackCaja.estaActivo(context)) }
    var sonidoActivo by remember { mutableStateOf(PreferenciasFeedbackCaja.estaSonidoActivo(context)) }
    var hapticaActiva by remember { mutableStateOf(PreferenciasFeedbackCaja.estaHapticaActiva(context)) }
    var iaInventarioActiva by remember { mutableStateOf(PreferenciasFeedbackCaja.estaIaInventarioActiva(context)) }

    // V15.6: Sincronización inicial con Firebase para reflejar estado administrativo
    LaunchedEffect(Unit) {
        runCatching {
            val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("ConfiguracionTienda/datosGenerales/usaReferenciaComercialIA")
                .get()
                .await()
            val remoteValue = snapshot.getValue(Boolean::class.java) ?: true
            if (remoteValue != iaInventarioActiva) {
                iaInventarioActiva = remoteValue
                PreferenciasFeedbackCaja.setIaInventarioActiva(context, remoteValue)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Configuración de Control", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "RESPUESTA DEL SISTEMA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Tarjeta Maestra
            ConfigItemCard(
                title = "Vibración del POS",
                description = "Habilita o deshabilita toda la retroalimentación táctil de la caja.",
                icon = Icons.Default.Vibration,
                checked = vibracionMaestra,
                onCheckedChange = {
                    vibracionMaestra = it
                    PreferenciasFeedbackCaja.setActivo(context, it)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                "AJUSTES DETALLADOS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            ConfigItemCard(
                title = "Efectos de Sonido",
                description = "Reproducir sonidos breves al confirmar ventas o detectar errores.",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                checked = sonidoActivo,
                onCheckedChange = {
                    sonidoActivo = it
                    PreferenciasFeedbackCaja.setSonidoActivo(context, it)
                },
                enabled = vibracionMaestra
            )

            ConfigItemCard(
                title = "Retroalimentación Háptica",
                description = "Vibraciones sutiles al tocar botones y elementos interactivos.",
                icon = Icons.Default.TouchApp,
                checked = hapticaActiva,
                onCheckedChange = {
                    hapticaActiva = it
                    PreferenciasFeedbackCaja.setHapticaActiva(context, it)
                },
                enabled = vibracionMaestra
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                "INTELIGENCIA ARTIFICIAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            ConfigItemCard(
                title = "IA en el Inventario",
                description = "Habilitar la búsqueda y categorización automática de productos mediante IA.",
                icon = Icons.Default.AutoAwesome,
                checked = iaInventarioActiva,
                onCheckedChange = {
                    iaInventarioActiva = it
                    PreferenciasFeedbackCaja.setIaInventarioActiva(context, it)
                    // V15.5: Sincronizar con Firebase para control administrativo
                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("ConfiguracionTienda/datosGenerales")
                        .child("usaReferenciaComercialIA")
                        .setValue(it)
                }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Estas configuraciones afectan únicamente a este dispositivo y ayudan a confirmar operaciones críticas sin mirar la pantalla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ConfigItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
                        else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
