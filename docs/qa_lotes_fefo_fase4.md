# Fase 4 - Pruebas guiadas de lotes y FEFO

## Objetivo

Validar que la app mantenga consistencia entre:

- lote recomendado por FEFO
- lote elegido manualmente
- lote realmente descontado
- lote realmente repuesto en reversión

## Datos base sugeridos

Usar un producto de prueba con estas condiciones:

- Producto: `Paracetamol QA`
- Unidad base: `unidades`
- Presentación principal: `Caja`
- 1 caja = `10 unidades`

Crear 3 lotes:

1. `LOTE-001`
   - fecha de registro: la más antigua
   - vencimiento: el más próximo
   - stock: `30`
2. `LOTE-002`
   - fecha de registro: intermedia
   - vencimiento: posterior
   - stock: `40`
3. `LOTE-003`
   - fecha de registro: la más reciente
   - vencimiento: el más lejano
   - stock: `50`

## Caso 1 - FEFO automático sin selección manual

### Pasos

1. Abrir `Gestionar lotes` del producto.
2. No seleccionar ningún lote manual.
3. Ir a `Caja` o `Agregar producto`.
4. Agregar una cantidad que pueda cubrir `LOTE-001`.
5. Completar la venta.

### Resultado esperado

- La UI debe indicar que FEFO recomienda `LOTE-001`.
- El carrito no debe marcar lote manual.
- La venta debe descontar de `LOTE-001`.
- En el movimiento debe verse:
  - `modoConsumoLote = fefo_automatico`
  - `loteNumeroConsumido = LOTE-001`
  - `loteNumeroFefo = LOTE-001`
  - `loteCoincideConFefo = true`

## Caso 2 - Selección manual válida distinta de FEFO

### Pasos

1. Abrir `Gestionar lotes`.
2. Seleccionar manualmente `LOTE-003`.
3. Ir a `Caja` o `Agregar producto`.
4. Agregar una cantidad menor o igual al stock de `LOTE-003`.
5. Completar la venta.

### Resultado esperado

- La UI debe mostrar:
  - lote actual manual: `LOTE-003`
  - FEFO recomendado: `LOTE-001`
- La venta debe descontar de `LOTE-003`.
- En el movimiento debe verse:
  - `modoConsumoLote = manual`
  - `loteNumeroConsumido = LOTE-003`
  - `loteNumeroFefo = LOTE-001`
  - `loteCoincideConFefo = false`

## Caso 3 - Lote manual insuficiente antes de vender

### Pasos

1. Dejar `LOTE-003` como lote manual.
2. Intentar agregar una cantidad que supere el stock de `LOTE-003`.
3. No cambiar el lote.

### Resultado esperado

- Debe bloquearse el flujo.
- No debe caer silenciosamente a FEFO.
- El mensaje debe indicar:
  - que el lote manual no alcanza
  - y, si aplica, cuál es el FEFO recomendado

## Caso 4 - Lote manual eliminado o agotado antes de vender

### Pasos

1. Dejar un lote manual seleccionado.
2. Agotar o eliminar ese lote desde inventario.
3. Volver al flujo de caja e intentar vender.

### Resultado esperado

- Debe bloquearse el flujo.
- No debe cambiar automáticamente a otro lote sin avisar.
- El mensaje debe indicar:
  - que la selección manual ya no está disponible
  - y, si existe, qué lote recomienda FEFO

## Caso 5 - Reversión con lote exacto disponible

### Pasos

1. Realizar una venta válida.
2. Forzar la reversión/cancelación del flujo.
3. Revisar inventario y lotes.

### Resultado esperado

- Debe reponerse el stock global.
- Debe reponerse el mismo lote consumido.
- En el movimiento de reversión debe verse:
  - `loteRepuestoExacto = true`
  - `loteNumeroConsumido` igual al lote vendido

## Caso 6 - Reversión cuando el lote consumido ya no existe

### Pasos

1. Realizar una venta con lote identificado.
2. Antes de revertir, eliminar manualmente ese lote o dejar un escenario donde ya no exista.
3. Ejecutar la reversión.

### Resultado esperado

- Debe reponerse al menos el stock global.
- No debe fallar toda la reversión por ausencia del lote.
- El movimiento de reversión debe dejar trazabilidad:
  - `loteRepuestoExacto = false`
  - `detalleReversionLote` con explicación
  - `requiereRevisionLote = true`

## Caso 7 - Orden de lotes en Gestionar lotes

### Pasos

1. Abrir `Gestionar lotes`.
2. Verificar el orden visual.

### Resultado esperado

- Los lotes deben verse por fecha de creación/registro.
- Arriba debe mostrarse:
  - lote en consumo actual
  - lote recomendado FEFO

## Caso 8 - Persistencia de selección manual

### Pasos

1. Elegir un lote manual.
2. Salir del flujo.
3. Volver a abrir el producto en inventario/caja.

### Resultado esperado

- Debe seguir mostrándose el lote manual mientras siga siendo válido.
- Si dejó de ser válido, debe verse el aviso y no asumirse FEFO en silencio.

## Evidencia mínima a revisar

- `Gestionar lotes`
- `Caja`
- `Agregar producto`
- movimiento de venta
- movimiento de reversión
- cantidades finales de:
  - stock global
  - lote consumido
  - lote repuesto

## Criterio de cierre

La fase se considera aprobada si:

- no hay reemplazo silencioso de lote manual por FEFO
- FEFO automático funciona cuando no existe selección manual
- el lote realmente descontado coincide con la auditoría
- la reversión deja el inventario coherente y trazable
