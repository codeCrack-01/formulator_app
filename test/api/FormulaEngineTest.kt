package api

import evaluator.Evaluator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import parser.Expr
import parser.Operator
import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry

class FormulaEngineTest {

    private val registry = UnitRegistry
    private fun engine(vars: Map<String, Quantity> = emptyMap()) =
        FormulaEngine(
            Evaluator(vars, currency.StaticExchangeRateProvider()),
            registry
        )


    @BeforeEach
    fun setup() {
        UnitRegistry.clear()

        // Unitless
        val unitlessDim = Dimension()
        UnitRegistry.register(Unit("unitless", 1.0, unitlessDim))

        // Length
        val length = Dimension(length = 1)
        UnitRegistry.register(Unit("m", 1.0, length))
        UnitRegistry.register(Unit("cm", 0.01, length))
        UnitRegistry.register(Unit("km", 1000.0, length))

        // Time
        val time = Dimension(time = 1)
        UnitRegistry.register(Unit("s", 1.0, time))
        UnitRegistry.register(Unit("min", 60.0, time))
        UnitRegistry.register(Unit("hr", 3600.0, time))

        // Mass
        val mass = Dimension(mass = 1)
        UnitRegistry.register(Unit("g", 0.001, mass))
        UnitRegistry.register(Unit("kg", 1.0, mass))
        UnitRegistry.register(Unit("tonne", 1000.0, mass))
        UnitRegistry.register(Unit("pk_ton", 40.0, mass))

        // Imperial Length
        UnitRegistry.register(Unit("in", 0.0254, length))
        UnitRegistry.register(Unit("ft", 0.3048, length))
        UnitRegistry.register(Unit("mi", 1609.34, length))

        // Imperial Mass
        UnitRegistry.register(Unit("lb", 0.453592, mass))
        UnitRegistry.register(Unit("oz", 0.0283495, mass))

        // Volume
        val volume = Dimension(length = 3)
        UnitRegistry.register(Unit("L", 0.001, volume))
        UnitRegistry.register(Unit("gal", 0.00378541, volume))

        UnitRegistry.register(Unit("slug", 14.5939029, mass))
        UnitRegistry.register(Unit("smoot", 1.7018, length))
        UnitRegistry.register(Unit("st", 6.35029, mass))

        // CURRENCY DEFINED IN THE DUMMY StaticExchangeRateProvider.kt FILE
    }

    // ----------------------------
    // 1. BASELINE
    // ----------------------------

    @Test
    fun `simple arithmetic works`() {
        val expr = Expr.Binary(
            Expr.Number(2.0, ""),
            Operator.ADD,
            Expr.Number(3.0, "")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(5.0, result.value, 1e-6)
    }

    // ----------------------------
    // 2. UNIT PROPAGATION
    // ----------------------------

    @Test
    fun `multiplication propagates units`() {
        val expr = Expr.Binary(
            Expr.Number(2.0, "m"),
            Operator.MUL,
            Expr.Number(3.0, "m")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals("m*m", result.unit.name)
    }

    // ----------------------------
    // 3. INFERENCE (CORE TEST)
    // ----------------------------

    @Test
    fun `cow formula infers constant dimension`() {

        val G = Expr.Variable("G")
        val L = Expr.Variable("L")

        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Binary(G, Operator.POW, Expr.Number(2.0, "")),
                Operator.MUL,
                L
            ),
            Operator.DIV,
            Expr.Number(300.0, "")
        )

        val vars = mapOf(
            "G" to Quantity(200.0, registry.parse("cm")),
            "L" to Quantity(150.0, registry.parse("cm"))
        )

        val result = engine(vars).evaluate(
            expr,
            registry.parse("kg").dimension
        )

        Assertions.assertEquals(
            registry.parse("kg").dimension,
            result.unit.dimension
        )
    }

    // ----------------------------
    // 4. ATTACH UNITS CHECK
    // ----------------------------

    @Test
    fun `constant gets inferred when required`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m/s").dimension
        )

        // Now constant MUST become seconds
        Assertions.assertEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    // ----------------------------
    // 5. OVERRIDE TEST
    // ----------------------------

    @Test
    fun `override replaces inferred unit`() {

        val constant = Expr.Number(5.0, "")

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            constant
        )

        val overrides = mapOf(
            constant.id to "s"
        )

        val result = engine().evaluate(expr, null, overrides)

        Assertions.assertEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    // ----------------------------
    // 6. AMBIGUOUS CASE
    // ----------------------------

    @Test
    fun `multiple unknowns does not crash`() {

        val expr = Expr.Binary(
            Expr.Number(2.0, ""),
            Operator.MUL,
            Expr.Number(3.0, "")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(6.0, result.value, 1e-6)
    }

    // ----------------------------
    // 7. ADDITION MISMATCH
    // ----------------------------

    @Test
    fun `addition with incompatible units throws`() {

        val expr = Expr.Binary(
            Expr.Number(2.0, "m"),
            Operator.ADD,
            Expr.Number(3.0, "s")
        )

        Assertions.assertThrows(Exception::class.java) {
            engine().evaluate(expr, null)
        }
    }

    @Test
    fun `no inference needed`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m").dimension
        )

        Assertions.assertEquals(
            registry.parse("m").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `infers constant for division`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m/s").dimension
        )

        Assertions.assertEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `infers constant for multiplication`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.MUL,
            Expr.Number(2.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m*s").dimension
        )

        Assertions.assertEquals(
            registry.parse("m*s").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `handles power in inference`() {

        val expr = Expr.Binary(
            Expr.Binary(
                Expr.Number(2.0, "m"),
                Operator.POW,
                Expr.Number(2.0, "")
            ),
            Operator.DIV,
            Expr.Number(4.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m^2").dimension
        )

        Assertions.assertEquals(
            registry.parse("m^2").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `multiple unknowns not solved`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Binary(
                Expr.Number(2.0, ""),
                Operator.MUL,
                Expr.Number(3.0, "")
            )
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m/s").dimension
        )

        // Should not magically succeed
        Assertions.assertNotEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `partial inference leaves unknowns`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Binary(
                Expr.Number(2.0, ""),
                Operator.MUL,
                Expr.Variable("x")
            )
        )

        val vars = mapOf(
            "x" to Quantity(5.0, registry.parse("s"))
        )

        val result = engine(vars).evaluate(
            expr,
            registry.parse("m/s").dimension
        )

        Assertions.assertEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `invalid addition dimensions`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.ADD,
            Expr.Number(5.0, "s")
        )

        // Should NOT unify
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            engine().evaluate(expr, null)
        }
    }

    @Test
    fun `negative exponent`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.POW,
            Expr.Number(-1.0, "")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(
            registry.parse("unitless/m").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `expected mismatch detected`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "s")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("kg").dimension
        )

        Assertions.assertNotEquals(
            registry.parse("kg").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `missing variable throws`() {

        val expr = Expr.Variable("x")

        Assertions.assertThrows(IllegalStateException::class.java) {
            engine().evaluate(expr, null)
        }
    }

    @Test
    fun `override forces unit`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "")
        )

        val result = engine().evaluate(
            expr,
            registry.parse("m/s").dimension,
            overrides = mapOf("CONST_2" to "s")
        )

        Assertions.assertEquals(
            registry.parse("m/s").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `invalid override ignored`() {

        val expr = Expr.Number(10.0, "")

        val result = engine().evaluate(
            expr,
            null,
            overrides = mapOf(expr.id to "invalid_unit")
        )

        Assertions.assertEquals(
            registry.parse("unitless").dimension,
            result.unit.dimension
        )
    }

    @Test
    fun `zero exponent becomes unitless`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.POW,
            Expr.Number(0.0, "")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertTrue(result.unit.dimension.isZero())
    }

    // ---------- Currency -------------- //
    @Test
    fun `currency addition with conversion`() {

        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(28000.0, "PKR")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(200.0, result.value, 1e-6)
        Assertions.assertEquals(
            mapOf("USD" to 1),
            result.unit.dimension.currency
        )
    }

    @Test
    fun `same currency addition`() {

        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(50.0, "USD")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(150.0, result.value, 1e-6)
    }

    @Test
    fun `currency and unit mismatch`() {

        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.ADD,
            Expr.Number(5.0, "m")
        )

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            engine().evaluate(expr, null)
        }
    }

    @Test
    fun `currency with unit multiplication`() {

        val expr = Expr.Binary(
            Expr.Number(3.0, "USD/L"),
            Operator.MUL,
            Expr.Number(50.0, "L")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertEquals(150.0, result.value, 1e-6)
        Assertions.assertEquals(
            mapOf("USD" to 1),
            result.unit.dimension.currency
        )
    }

    @Test
    fun `currency division cancels`() {

        val expr = Expr.Binary(
            Expr.Number(100.0, "USD"),
            Operator.DIV,
            Expr.Number(50.0, "USD")
        )

        val result = engine().evaluate(expr, null)

        Assertions.assertTrue(result.unit.dimension.isZero())
    }

    @Test
    fun `currency power invalid`() {

        val expr = Expr.Binary(
            Expr.Number(10.0, "USD"),
            Operator.POW,
            Expr.Number(2.0, "")
        )
//        println(engine().evaluate(expr, null))
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            engine().evaluate(expr, null)
        }
    }
}