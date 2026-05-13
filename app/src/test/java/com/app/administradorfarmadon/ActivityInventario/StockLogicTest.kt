package com.app.administradorfarmadon.ActivityInventario

import com.app.administradorfarmadon.ActivityInventario.ui.StockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.calculateValidStockOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class StockLogicTest {

    @Test
    fun testIndivisibleOptions() {
        // 5 frascos
        val options = calculateValidStockOptions(5, StockControlMode.INDIVISIBLE)
        assertEquals(listOf(1, 2, 3, 4, 5), options)

        // 2 frascos
        val options2 = calculateValidStockOptions(2, StockControlMode.INDIVISIBLE)
        assertEquals(listOf(1, 2), options2)
        
        // 1 frasco
        val options1 = calculateValidStockOptions(1, StockControlMode.INDIVISIBLE)
        assertEquals(listOf(1), options1)
    }

    @Test
    fun testDivisibleOptions() {
        // 100 tabletas -> step = 10
        val options100 = calculateValidStockOptions(100, StockControlMode.DIVISIBLE)
        assertEquals(listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100), options100)

        // 19 tabletas -> step = ceil(1.9) = 2
        val options19 = calculateValidStockOptions(19, StockControlMode.DIVISIBLE)
        assertEquals(listOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 19), options19)

        // 7 tabletas -> step = ceil(0.7) = 1
        val options7 = calculateValidStockOptions(7, StockControlMode.DIVISIBLE)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), options7)
        
        // 1 tableta
        val options1 = calculateValidStockOptions(1, StockControlMode.DIVISIBLE)
        assertEquals(listOf(1), options1)
    }
}
