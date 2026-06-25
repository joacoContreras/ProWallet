package com.undef.prowallet.data.ocr

import org.junit.Test

class TicketParserTest {

    @Test
    fun testParseArgentinaTicketStructure() {
        val rawText = """
            SUPERMERCADOS VEA
            SUCURSAL 140 - PALERMO
            Av. Las Heras 3400, CABA
            C.U.I.T.: 30-58739485-9
            Inicio de Actividades: 01/10/1997
            TICKET FACTURA B   Nro: 0004-00123456
            Fecha: 25/06/2026   Hora: 14:20
            Consumidor Final
            
            7790070411333 gaseosa COCA COLA Zero 1.75 lt PET 1.75 (1x3.100) 3.100
            7791234567890 galletitas criollitas 3 pack (2 x 1.500) 3.000
            Queso Crema Casancrem 290g 2.500
            
            SUBTOTAL             6.600
            TOTAL               8.600
            
            IVA Neto 21%
            Concepto no gravado
            CAE Nro: 73849501847395
            Vto CAE: 05/07/2026
        """.trimIndent()

        val parsed = TicketParser.parse(rawText)
        println("parsed storeName = ${parsed.storeName}")
        println("parsed date = ${parsed.date}")
        println("parsed time = ${parsed.time}")
        println("parsed total = ${parsed.total}")
        println("parsed items size = ${parsed.items.size}")
        parsed.items.forEach {
            println("  item: name='${it.name}', price=${it.price}")
        }
    }
}
