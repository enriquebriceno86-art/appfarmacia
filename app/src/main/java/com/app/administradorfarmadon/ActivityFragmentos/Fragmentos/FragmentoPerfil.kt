package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.app.administradorfarmadon.ActivitysPerfilItem.*
import com.app.administradorfarmadon.ActivitysUsuarios.UserActivity
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.SesionUnicaManager
import com.app.administradorfarmadon.R

class FragmentoPerfil : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ProfileScreen(
                        onNavigate = { activityClass -> abrirActivity(activityClass) },
                        onLogout = { salir() }
                    )
                }
            }
        }
    }

    private fun abrirActivity(activity: Class<*>) {
        startActivity(Intent(requireContext(), activity))
    }

    private fun salir() {
        val ctx = requireContext()
        val idUsuario = SessionManager.idCajera.trim()

        SesionUnicaManager.detenerHeartbeat()
        if (idUsuario.isNotBlank()) {
            SesionUnicaManager.cerrarSesionActual(ctx, idUsuario)
        }

        SessionManager.limpiarSesion(ctx)

        val intent = Intent(ctx, com.app.administradorfarmadon.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}

@Composable
fun ProfileScreen(
    onNavigate: (Class<*>) -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val nombre = SessionManager.nombreCajera.ifBlank { "Administrador" }
    val rol = SessionManager.rol.ifBlank { "Gestión Total" }
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "AD" }

    val premiumGreen = Color(0xFF15A05C)
    val premiumHeaderGradient = Brush.verticalGradient(
        colors = listOf(premiumGreen, premiumGreen.copy(alpha = 0.8f), Color.Transparent),
        startY = 0f,
        endY = 400f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // FONDO DECORATIVO SUPERIOR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(premiumHeaderGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // CABECERA PERFIL PREMIUM
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AVATAR PREMIUM CON EFECTO VIDRIO Y DOBLE BORDE
                Box(
                    modifier = Modifier
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Sombra exterior suave
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {}

                    // Círculo principal con gradiente
                    Surface(
                        modifier = Modifier
                            .size(94.dp)
                            .graphicsLayer {
                                shadowElevation = 16f
                                shape = CircleShape
                                clip = true
                            },
                        color = Color.White,
                        border = BorderStroke(3.dp, Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iniciales,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = rol.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CONTENIDO PRINCIPAL (TARJETAS)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // SECCIÓN ESTABLECIMIENTO
                ProfileMenuSection(
                    title = "ESTABLECIMIENTO",
                    items = listOf(
                        ProfileMenuData("Información de la tienda", Icons.Default.Storefront, { onNavigate(DatosdelaTienda::class.java) }),
                        ProfileMenuData("Horarios de atención", Icons.Default.Schedule, { onNavigate(HorariosActivity::class.java) }),
                        ProfileMenuData("Tipos de pago", Icons.Default.Payments, { onNavigate(TiposPagoActivity::class.java) })
                    )
                )

                // SECCIÓN ADMINISTRACIÓN
                ProfileMenuSection(
                    title = "ADMINISTRACIÓN",
                    items = listOf(
                        ProfileMenuData("Gestión de usuarios", Icons.Default.Group, { onNavigate(UserActivity::class.java) }),
                        ProfileMenuData("Historial y reportes", Icons.Default.Assessment, { onNavigate(ListaReportesCaja::class.java) }),
                        ProfileMenuData("Cuentas por pagar", Icons.Default.AccountBalanceWallet, { onNavigate(CuentasPorPagarActivity::class.java) })
                    )
                )

                // SECCIÓN SISTEMA
                ProfileMenuSection(
                    title = "SISTEMA Y APP",
                    items = listOf(
                        ProfileMenuData("Configuraciones", Icons.Default.Settings, { onNavigate(ConfiguracionesControlActivity::class.java) })
                    )
                )

                // BOTÓN SALIR PREMIUM
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onLogout() },
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CERRAR SESIÓN SEGURA",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Farmadon Administrador • v1.0.0\nProducción Cloud",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

data class ProfileMenuData(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileMenuSection(
    title: String,
    items: List<ProfileMenuData>
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF64748B),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuItem(
                        title = item.title,
                        icon = item.icon,
                        onClick = item.onClick
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 68.dp),
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CONTENEDOR DE ICONO TIPO AVATAR MINI
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF059669)
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFCBD5E1)
        )
    }
}
