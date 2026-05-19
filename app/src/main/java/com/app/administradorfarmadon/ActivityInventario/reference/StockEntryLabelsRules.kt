package com.app.administradorfarmadon.ActivityInventario.reference

import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStockEntryMode
import java.util.Locale

object StockEntryLabelsRules {

    enum class VisibleUnitKind(
        val singular: String,
        val plural: String
    ) {
        FRASCO("frasco", "frascos"),
        CAPSULA("c\u00e1psula", "c\u00e1psulas"),
        TABLETA("tableta", "tabletas"),
        AMPOLLA("ampolla", "ampollas"),
        BLISTER("bl\u00edster", "bl\u00edsters"),
        SOBRE("sobre", "sobres"),
        GENERIC("unidad", "unidades")
    }

    /**
     * Infiere el tipo de unidad visible seg\u00fan el nombre y el tipo de control.
     */
    fun inferUnitKind(name: String, controlType: CreateProductControlType?): VisibleUnitKind {
        val n = name.lowercase(Locale.getDefault())
        val parsed = PharmaceuticalParser.parse(name)
        val form = parsed.form.lowercase(Locale.getDefault())
        
        return when {
            form.contains("jarabe") || form.contains("susp") || form.contains("gotas") || n.contains("jarabe") || n.contains("suspension") -> VisibleUnitKind.FRASCO
            form.contains("amp") || form.contains("vial") || n.contains("ampolla") -> VisibleUnitKind.AMPOLLA
            form.contains("caps") || n.contains("capsula") -> VisibleUnitKind.CAPSULA
            form.contains("tab") || form.contains("past") || form.contains("comp") || n.contains("tableta") || n.contains("comprimido") -> VisibleUnitKind.TABLETA
            form.contains("blister") || n.contains("blister") -> VisibleUnitKind.BLISTER
            form.contains("sachet") || form.contains("sobre") || n.contains("sachet") || n.contains("sobre") -> VisibleUnitKind.SOBRE
            controlType == CreateProductControlType.LIQUIDO -> VisibleUnitKind.FRASCO
            else -> VisibleUnitKind.GENERIC
        }
    }

    /**
     * Devuelve el t\u00edtulo y subt\u00edtulo para cada modo de ingreso seg\u00fan el contexto.
     */
    fun getModeTexts(mode: CreateProductStockEntryMode, kind: VisibleUnitKind, controlType: CreateProductControlType?): Pair<String, String> {
        val plural = kind.plural.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val unitLabel = controlType?.baseUnitLabel ?: "unidades"
        
        return when (mode) {
            CreateProductStockEntryMode.UNIDAD -> {
                // P1 FIX: No decir "mL sueltos". Preferir "Frascos sueltos" o el tipo inferido.
                // Solo usamos la unidad base literal (mL/g) si el tipo inferido es GENERIC y es LIQUIDO/PESO.
                val title = if (kind == VisibleUnitKind.GENERIC && (controlType == CreateProductControlType.LIQUIDO || controlType == CreateProductControlType.PESO)) {
                    "${unitLabel.replaceFirstChar { it.uppercase() }} sueltos"
                } else {
                    "$plural sueltas"
                }
                
                val helper = if (kind == VisibleUnitKind.GENERIC && (controlType == CreateProductControlType.LIQUIDO || controlType == CreateProductControlType.PESO)) {
                    "Recibiste $unitLabel individuales (sin caja ni bultos)"
                } else {
                    "Recibiste ${kind.plural} individuales (sin caja)"
                }
                title to helper
            }
            CreateProductStockEntryMode.CAJA -> {
                val title = "Caja con ${kind.plural}"
                val helper = "Cada caja contiene una cantidad en $unitLabel (ej. caja de 60 $unitLabel)"
                title to helper
            }
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                val containerPlural = getContainerPlural(kind)
                val title = "Caja con $containerPlural"
                val helper = "Cada caja contiene varios $containerPlural, y cada uno tiene su cantidad en $unitLabel"
                title to helper
            }
        }
    }

    /**
     * Devuelve los labels para los campos del Dialog de configuraci\u00f3n.
     */
    data class DialogFieldLabels(
        val boxesLabel: String = "Cajas recibidas *",
        val boxesTrailing: String = "cajas",
        val boxesPlaceholder: String = "Ej. 10",
        
        val unitsPerBoxLabel: String = "Unidades por caja",
        val unitsPerBoxPlaceholder: String = "Ej. 60",
        
        val packagesPerBoxLabel: String = "Paquetes por caja",
        val packagesPerBoxTrailing: String = "paquetes",
        val packagesPerBoxPlaceholder: String = "Ej. 5",
        
        val unitsPerPackageLabel: String = "Unidades por paquete",
        val unitsPerPackagePlaceholder: String = "Ej. 10",
        
        val totalLabel: String = "Unidades totales",
        val totalPlaceholder: String = "Ej. 100",
        val totalTrailing: String = "unidades"
    )

    fun getDialogFieldLabels(mode: CreateProductStockEntryMode, kind: VisibleUnitKind, controlType: CreateProductControlType?): DialogFieldLabels {
        val unitLabel = controlType?.baseUnitLabel ?: "unidades"
        val singular = kind.singular
        val plural = kind.plural
        val containerPlural = getContainerPlural(kind)
        val containerSingular = getContainerSingular(kind)
        
        val isPhysicalUnit = controlType == CreateProductControlType.UNIDAD || controlType == null

        return when (mode) {
            CreateProductStockEntryMode.UNIDAD -> DialogFieldLabels(
                totalLabel = if (isPhysicalUnit || kind != VisibleUnitKind.GENERIC) "${plural.replaceFirstChar { it.uppercase() }} totales" else "${unitLabel.replaceFirstChar { it.uppercase() }} totales",
                totalPlaceholder = "Ej. 24 $plural",
                totalTrailing = if (isPhysicalUnit || kind != VisibleUnitKind.GENERIC) plural else unitLabel
            )
            CreateProductStockEntryMode.CAJA -> DialogFieldLabels(
                boxesLabel = "Cajas recibidas *",
                boxesTrailing = "cajas",
                boxesPlaceholder = "Ej. 10",
                unitsPerBoxLabel = if (!isPhysicalUnit) "$unitLabel por caja" else "$plural por caja",
                unitsPerBoxPlaceholder = if (!isPhysicalUnit) "Ej. 120" else "Ej. 60"
            )
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> DialogFieldLabels(
                boxesLabel = "Cajas recibidas *",
                boxesTrailing = "cajas",
                boxesPlaceholder = "Ej. 10",
                packagesPerBoxLabel = "$containerPlural por caja",
                packagesPerBoxTrailing = containerPlural,
                packagesPerBoxPlaceholder = "Ej. 5",
                unitsPerPackageLabel = "$unitLabel por $containerSingular",
                unitsPerPackagePlaceholder = "Ej. 10"
            )
        }
    }

    private fun getContainerPlural(kind: VisibleUnitKind): String {
        return when (kind) {
            VisibleUnitKind.FRASCO -> "frascos"
            VisibleUnitKind.AMPOLLA -> "ampollas"
            VisibleUnitKind.SOBRE -> "sobres"
            else -> "bl\u00edsters"
        }
    }

    private fun getContainerSingular(kind: VisibleUnitKind): String {
        return when (kind) {
            VisibleUnitKind.FRASCO -> "frasco"
            VisibleUnitKind.AMPOLLA -> "ampolla"
            VisibleUnitKind.SOBRE -> "sobre"
            else -> "bl\u00edster"
        }
    }
}
