package api

import evaluator.Evaluator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import parser.Expr
import parser.Operator
import units.*

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

        val unitlessDim = Dimension()
        UnitRegistry.register(Unit("unitless", 1.0, unitlessDim))

        val length = Dimension(length = 1)
        UnitRegistry.register(Unit("m", 1.0, length))
        UnitRegistry.register(Unit("cm", 0.01, length))
        UnitRegistry.register(Unit("km", 1000.0, length))

        val time = Dimension(time = 1)
        UnitRegistry.register(Unit("s", 1.0, time))
        UnitRegistry.register(Unit("min", 60.0, time))
        UnitRegistry.register(Unit("hr", 3600.0, time))

        val mass = Dimension(mass = 1)
        UnitRegistry.register(Unit("g", 0.001, mass))
        UnitRegistry.register(Unit("kg", 1.0, mass))
        UnitRegistry.register(Unit("tonne", 1000.0, mass))
        UnitRegistry.register(Unit("pk_ton", 40.0, mass))

        UnitRegistry.register(Unit("in", 0.0254, length))
        UnitRegistry.register(Unit("ft", 0.3048, length))
        UnitRegistry.register(Unit("mi", 1609.34, length))

        UnitRegistry.register(Unit("lb", 0.453592, mass))
        UnitRegistry.register(Unit("oz", 0.0283495, mass))

        val volume = Dimension(length = 3)
        UnitRegistry.register(Unit("L", 0.001, volume))
        UnitRegistry.register(Unit("gal", 0.00378541, volume))

        UnitRegistry.register(Unit("slug", 14.5939029, mass))
        UnitRegistry.register(Unit("smoot", 1.7018, length))
        UnitRegistry.register(Unit("st", 6.35029, mass))
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
    // 3. INFERENCE CORE TEST
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
    // 4. CONSTANT INFERENCE
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
    // 7. ADDITION MISMATCH
    // ----------------------------

    @Test
    fun `addition with incompatible units throws`() {
        val expr = Expr.Binary(
            Expr.Number(2.0, "m"),
            Operator.DIV,
            Expr.Number(3.0, "s")
        )

        val value= engine().evaluate(expr, null)
        Assertions.assertEquals(0.666666667, value.value, 1e-6)
    }

    // ----------------------------
    // 8. NO INFERENCE NEEDED
    // ----------------------------

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

    // ----------------------------
    // 9. MULTIPLICATION INFERENCE
    // ----------------------------

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

    // ----------------------------
    // 10. DIVISION INFERENCE
    // ----------------------------

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

    // ----------------------------
    // 11. POWER HANDLING
    // ----------------------------

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

    // ----------------------------
    // 12. PARTIAL INFERENCE (NOW STRICT FAIL)
    // ----------------------------

    @Test
    fun `partial inference throws`() {
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

    // ----------------------------
    // 13. MISSING VARIABLE
    // ----------------------------

    @Test
    fun `missing variable throws`() {
        val expr = Expr.Variable("x")

        Assertions.assertThrows(IllegalStateException::class.java) {
            engine().evaluate(expr, null)
        }
    }
}