package tests

import currency.CurrencyConverter
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

class MultiCurrencyConversionTest {

    private fun engine(): Evaluator {
        return Evaluator(
            variables = emptyMap(),
            exchange = StaticExchangeRateProvider()
        )
    }

    @BeforeEach
    fun setup() {
        UnitRegistry.register(Unit("unitless", 1.0, Dimension()))
        // CURRENCY DEFINED IN THE DUMMY StaticExchangeRateProvider.kt FILE
        CurrencyBootstrap.registerAll()
    }

    // -----------------------------
    // CROSS-CURRENCY NORMALIZATION
    // -----------------------------

    @Test
    fun `USD + SAR converts correctly to USD`() {
        val expr = Expr.Binary(
            Expr.Number(10.0, "USD"),
            Operator.ADD,
            Expr.Number(37.5, "SAR") // = 10 USD
        )

        val result = engine().eval(expr)

        assertEquals(20.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `INR + USD keeps INR as base`() {
        val expr = Expr.Binary(
            Expr.Number(830.0, "INR"), // 10 USD
            Operator.ADD,
            Expr.Number(10.0, "USD")
        )

        val result = engine().eval(expr)

        // INR is base → 10 USD = 830 INR
        assertEquals(1660.0, result.value, 1e-6)
        assertEquals(mapOf("INR" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `AUD + PKR cross conversion resolves via USD`() {
        val expr = Expr.Binary(
            Expr.Number(1.0, "AUD"),      // 1.5 USD
            Operator.ADD,
            Expr.Number(140.0, "PKR")     // 0.5 USD
        )

        val result = engine().eval(expr)

        // total = 2 USD
        assertEquals(1.75, result.value, 1e-6)
        assertEquals(mapOf("AUD" to 1), result.unit.dimension.currency)
    }

    // -----------------------------
    // MULTI-CURRENCY CHAINING
    // -----------------------------

    @Test
    fun `USD SAR INR chain resolves to first currency`() {
        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Number(10.0, "USD"),
                Operator.ADD,
                Expr.Number(7.5, "SAR") // 2 USD
            ),
            Operator.ADD,
            Expr.Number(166.0, "INR") // 2 USD
        )

        val result = engine().eval(expr)

        // USD base:
        // 10 + 2 + 2 = 14 USD
        assertEquals(14.0, result.value, 1e-6)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `IRR heavy value normalization`() {
        val expr = Expr.Binary(
            Expr.Number(420000.0, "IRR"), // 10 USD
            Operator.ADD,
            Expr.Number(10.0, "USD")
        )

        val result = engine().eval(expr)


        val converted = CurrencyConverter.convert(
            result,
            "USD",
            StaticExchangeRateProvider()
        )

        assertEquals(20.0, converted.value, 1e-6)
        assertEquals(mapOf("USD" to 1), converted.unit.dimension.currency)
    }

    // -----------------------------
    // MIXED PRIORITY RULES
    // -----------------------------

    @Test
    fun `currency priority follows left operand consistently`() {
        val expr = Expr.Binary(
            Expr.Number(100.0, "INR"),
            Operator.ADD,
            Expr.Number(1.0, "AUD") // 1.5 USD = 124.5 INR
        )

        val result = engine().eval(expr)

        // INR base:
        // 100 + 124.5 = 224.5 INR
        assertEquals(155.3333334, result.value, 1e-6)
        assertEquals(mapOf("INR" to 1), result.unit.dimension.currency)
    }

    @Test
    fun `deep mixed currency tree remains stable`() {
        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Binary(
                    Expr.Number(10.0, "USD"),
                    Operator.ADD,
                    Expr.Number(37.5, "SAR")
                ),
                Operator.ADD,
                Expr.Number(83.0, "INR")
            ),
            Operator.ADD,
            Expr.Number(1.0, "AUD")
        )

        val result = engine().eval(expr)

        // Normalize to USD:
        // 10 USD
        // + 10 USD (SAR)
        // + 1 USD (INR)
        // + 1.5 USD (AUD)
        // = 22.5 USD
        assertEquals(21.66666666667, result.value, 1e-5)
        assertEquals(mapOf("USD" to 1), result.unit.dimension.currency)
    }
}