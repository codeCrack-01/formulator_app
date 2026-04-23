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

class CurrencyConversionTest {

    private fun engine(): Evaluator {
        return Evaluator(
            variables = emptyMap(),
            exchange = StaticExchangeRateProvider()
        )
    }

    @BeforeEach
    fun setup() {
        val unitlessDim = Dimension()
        UnitRegistry.register(Unit("unitless", 1.0, unitlessDim))
        // CURRENCY DEFINED IN THE DUMMY StaticExchangeRateProvider.kt FILE
    }

    // -------------------------------
    // BASIC CONVERSION PRIORITY RULE
    // -------------------------------

    @Test
    fun `USD + PKR keeps USD as priority currency`() {
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
    fun `PKR + USD keeps PKR as priority currency`() {
        val expr = Expr.Binary(
            Expr.Number(28000.0, "PKR"),
            Operator.ADD,
            Expr.Number(100.0, "USD")
        )

        val result = engine().eval(expr)

        // PKR is base: 100 USD -> 28000 PKR, so total = 56000 PKR
        assertEquals(56000.0, result.value, 1e-6)
        assertEquals(mapOf("PKR" to 1), result.unit.dimension.currency)
    }

    // -------------------------------
    // CHAINED CONVERSIONS
    // -------------------------------

    @Test
    fun `chained USD PKR USD conversion maintains consistency`() {
        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Number(100.0, "USD"),
                Operator.ADD,
                Expr.Number(28000.0, "PKR")
            ),
            Operator.ADD,
            Expr.Number(50.0, "USD")
        )

        val result = engine().eval(expr)

        // First: 100 USD + 28000 PKR = 200 USD
        // Then: 200 USD + 50 USD = 250 USD
        assertEquals(250.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `chained PKR USD PKR conversion maintains PKR dominance`() {
        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Number(28000.0, "PKR"),
                Operator.ADD,
                Expr.Number(100.0, "USD")
            ),
            Operator.ADD,
            Expr.Number(14000.0, "PKR")
        )

        val result = engine().eval(expr)

        // Step 1: PKR + USD => PKR
        // 100 USD = 28000 PKR → total = 56000 PKR
        // Step 2: + 14000 PKR = 70000 PKR
        assertEquals(70000.0, result.value, 1e-6)
        assertEquals(mapOf("PKR" to 1), result.unit.dimension.currency)
    }

    // -------------------------------
    // SUBTRACTION WITH CONVERSION
    // -------------------------------

    @Test
    fun `USD minus PKR with conversion`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.SUB,
            Expr.Number(14000.0, "PKR")
        )

        val result = engine().eval(expr)

        // 14000 PKR = 50 USD
        assertEquals(50.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `PKR minus USD with conversion priority PKR`() {
        val expr = Expr.Binary(
            Expr.Number(28000.0, "PKR"),
            Operator.SUB,
            Expr.Number(50.0, "USD")
        )

        val result = engine().eval(expr)

        // 50 USD = 14000 PKR
        // 28000 - 14000 = 14000 PKR
        assertEquals(14000.0, result.value, 1e-6)
        assertEquals(mapOf("PKR" to 1), result.unit.dimension.currency)
    }

    // -------------------------------
    // MIXED MULTI-STEP MIXING
    // -------------------------------

    @Test
    fun `USD PKR USD PKR deep chain resolves consistently`() {
        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Binary(
                    Expr.Number(100.0, "USD"),
                    Operator.ADD,
                    Expr.Number(28000.0, "PKR")
                ),
                Operator.ADD,
                Expr.Number(50.0, "USD")
            ),
            Operator.ADD,
            Expr.Number(14000.0, "PKR")
        )

        val result = engine().eval(expr)

        // USD base:
        // 100 USD + 28000 PKR = 200 USD
        // + 50 USD = 250 USD
        // + 14000 PKR = +50 USD => 300 USD
        assertEquals(300.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    // -------------------------------
    // IDENTITY CONVERSION STABILITY
    // -------------------------------

    @Test
    fun `repeated same-currency conversions are stable`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(50.0, "USD")
        )

        val result = engine().eval(expr)

        assertEquals(150.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }
}