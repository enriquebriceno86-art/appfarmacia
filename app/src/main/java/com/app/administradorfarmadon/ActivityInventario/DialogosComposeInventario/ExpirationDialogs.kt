package com.app.administradorfarmadon.ActivityInventario.DialogosComposeInventario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.administradorfarmadon.ActivityInventario.ui.CreateBackground
import com.app.administradorfarmadon.ActivityInventario.ui.CreateBorder
import com.app.administradorfarmadon.ActivityInventario.ui.CreateGreen
import com.app.administradorfarmadon.ActivityInventario.ui.CreateTextPrimary
import com.app.administradorfarmadon.ActivityInventario.ui.CreateTextSecondary
import com.app.administradorfarmadon.ActivityInventario.ui.PremiumGradient
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpirationMonthYearDialog(
    value: String, 
    onDismiss: () -> Unit, 
    onConfirm: (String) -> Unit
) {
    val today = LocalDate.now()
    val currentMonth = today.monthValue
    val currentYear = today.year
    val initialParts = value.split("/")
    var selectedYear by remember(value) {
        mutableStateOf(
            (initialParts.getOrNull(1)?.toIntOrNull()?.plus(2000) ?: currentYear).coerceIn(
                currentYear, currentYear + 10
            )
        )
    }
    var selectedMonth by remember(value, selectedYear) {
        val initialMonth =
            initialParts.getOrNull(0)?.toIntOrNull()?.takeIf { it in 1..12 } ?: currentMonth
        mutableStateOf(
            if (selectedYear == currentYear) initialMonth.coerceAtLeast(currentMonth) else initialMonth
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumGradient)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Vencimiento del lote",
                        color = CreateTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Selecciona mes y año. No se muestran meses vencidos.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedYear = (selectedYear - 1).coerceAtLeast(currentYear)
                                if (selectedYear == currentYear) selectedMonth =
                                    selectedMonth.coerceAtLeast(currentMonth)
                            },
                            enabled = selectedYear > currentYear,
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = selectedYear.toString(),
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = CreateGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                selectedYear = (selectedYear + 1).coerceAtMost(currentYear + 10)
                            },
                            enabled = selectedYear < currentYear + 10,
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..12).forEach { month ->
                            val enabled = selectedYear > currentYear || month >= currentMonth
                            val isSelected = selectedMonth == month

                            Surface(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = enabled) { selectedMonth = month },
                                color = if (isSelected) CreateGreen else if (enabled) Color.White.copy(
                                    alpha = 0.4f
                                ) else CreateBackground.copy(alpha = 0.5f),
                                border = if (isSelected) null else BorderStroke(
                                    1.dp, Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = month.toString().padStart(2, '0'),
                                        color = if (isSelected) Color.White else if (enabled) CreateTextPrimary else CreateTextSecondary.copy(
                                            alpha = 0.4f
                                        ),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text(
                                "Cancelar",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                val mm = selectedMonth.toString().padStart(2, '0')
                                val aa = (selectedYear % 100).toString().padStart(2, '0')
                                onConfirm("$mm/$aa")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                        ) {
                            Text("Aplicar", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}