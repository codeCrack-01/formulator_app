package tests

import currency.StaticExchangeRateProvider
import evaluator.Evaluator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import parser.Expr
import parser.Operator
import units.Dimension
import units.Unit
import units.UnitRegistry

class CurrencyTest {

    private fun engine(): Evaluator {
        return Evaluator(
            variables = emptyMap(),
            exchange = StaticExchangeRateProvider()
        )
    }

    @BeforeEach
    fun setup() {
        // Unitless
        val unitlessDim = Dimension()
        UnitRegistry.register(Unit("unitless", 1.0, unitlessDim))
        // CURRENCY DEFINED IN THE DUMMY StaticExchangeRateProvider.kt FILE
        val length = Dimension(length = 1)
        UnitRegistry.register(Unit("m", 1.0, length))
    }

    @Test
    fun `same currency addition`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(50.0, "USD")
        )

        val result = engine().eval(expr)

        assertEquals(150.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `currency conversion addition`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(28000.0, "PKR")
        )

        val result = engine().eval(expr)

        assertEquals(200.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `currency subtraction`() {
        val expr = Expr.Binary(
            Expr.Number(200.0, "USD"),
            Operator.SUB,
            Expr.Number(28000.0, "PKR")
        )

        val result = engine().eval(expr)

        assertEquals(100.0, result.value, 1e-6)
    }

    @Test
    fun `currency and physical unit should fail`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(5.0, "m")
        )

        assertThrows(IllegalArgumentException::class.java) {
            engine().eval(expr)
        }
    }

    @Test
    fun `currency with unit multiplication`() {
        val expr = Expr.Binary(
            Expr.Number(3.0, "USD/m"),
            Operator.MUL,
            Expr.Number(50.0, "m")
        )

        val result = engine().eval(expr)

        assertEquals(150.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `currency division becomes dimensionless`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.DIV,
            Expr.Number(50.0, "USD")
        )

        val result = engine().eval(expr)

        assertTrue(result.unit.dimension.isDimensionless)
    }

    @Test
    fun `currency power should fail`() {
        val expr = Expr.Binary(
            Expr.Number(10.0, "USD"),
            Operator.POW,
            Expr.Number(2.0, "")
        )

        assertThrows(IllegalArgumentException::class.java) {
            engine().eval(expr)
        }
    }

    @Test
    fun `debug currency dimension`() {
        val expr = Expr.Number(100.0, "USD")
        val result = engine().eval(expr)

        println("DEBUG DIMENSION: ${result.unit.dimension}")

        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }
}