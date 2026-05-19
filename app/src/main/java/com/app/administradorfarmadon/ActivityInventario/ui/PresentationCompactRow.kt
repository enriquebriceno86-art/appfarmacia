package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.administradorfarmadon.ClasesDatabase.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationCompactRow(
    name: String,
    equivalence: String,
    price: String,
    unitLabel: String,
    isMain: Boolean = false,
    error: String? = null,
    aiSuggestedPrice: String? = null,
    onEquivalenceChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val borderColor = if (!error.isNullOrBlank()) Color(0xFFFDA29B) else Color(0xFFE5EAF0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Al vender descuenta ${equivalence.ifBlank { "0" }} $unitLabel",
                        color = Color(0xFF667085),
                        fontSize = 12.sp
                    )
                }

                if (isMain) {
                    Surface(
                        color = Color(0xFFEAFBF1),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Principal",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = Color(0xFF159455),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(0.85f)) {
                    Text(
                        text = "Cantidad base",
                        color = Color(0xFF667085),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = equivalence,
                        onValueChange = onEquivalenceChange,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        placeholder = { Text(text = "Ej. 12", fontSize = 12.sp, color = Color(0xFF98A2B3)) },
                        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5EAF0),
                            focusedBorderColor = Color(0xFF2E90FA)
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio de venta",
                        color = Color(0xFF667085),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        placeholder = { Text(text = "${SessionManager.monedaSimbolo} 0.00", fontSize = 12.sp, color = Color(0xFF98A2B3)) },
                        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5EAF0),
                            focusedBorderColor = Color(0xFF2E90FA)
                        )
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFF04438),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sugerencia IA sutil debajo del campo si existe
            if (!aiSuggestedPrice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(4.dp),
                        onClick = { onPriceChange(aiSuggestedPrice) }
                    ) {
                        Text(
                            text = "\u2728 Sugerido: ${SessionManager.monedaSimbolo} $aiSuggestedPrice",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF444CE7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(70.dp)) // Alinear bajo el campo precio aprox
                }
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    color = Color(0xFFD92D20),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
