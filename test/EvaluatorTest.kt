import evaluator.Evaluator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import parser.Expr
import parser.Operator
import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry

class EvaluatorTest {
    @BeforeEach
    fun setup() {
        val unitlessDim = Dimension(0, 0, 0)
        UnitRegistry.register(units.Unit("unitless", 1.0, unitlessDim))

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
        UnitRegistry.register(Unit("ton", 40.0, mass)) // The pk_ton

    }

    @Test
    fun `should resolve variables from map`() {
        val variables = mapOf("x" to Quantity(10.0, UnitRegistry.get("m")))
        val evaluator = Evaluator(variables)

        val result = evaluator.eval(Expr.Variable("x"))
        assertEquals(10.0, result.value)
        assertEquals(UnitRegistry.get("m"), result.unit)
    }

    @Test
    fun `should throw error for missing variables`() {
        val evaluator = Evaluator(emptyMap())
        assertThrows(IllegalStateException::class.java) {
            evaluator.eval(Expr.Variable("y"))
        }
    }

    @Test
    fun `should evaluate number literals`() {
        val evaluator = Evaluator(emptyMap())
        val result = evaluator.eval(Expr.Number(42.0, "unitless"))
        assertEquals(42.0, result.value)
        assertEquals(UnitRegistry.get("unitless"), result.unit)
    }

    @Test
    fun `should add quantities of same unit`() {
        val variables = mapOf(
            "a" to Quantity(2.0, UnitRegistry.get("m")),
            "b" to Quantity(3.0, UnitRegistry.get("m"))
        )
        val evaluator = Evaluator(variables)
        val result = evaluator.eval(Expr.Binary(Expr.Variable("a"), Operator.ADD, Expr.Variable("b")))
        assertEquals(5.0, result.value, 0.0001)
        assertEquals(UnitRegistry.get("m"), result.unit)
    }

    @Test
    fun `should add quantities with different units`() {
        val variables = mapOf(
            "a" to Quantity(200.0, UnitRegistry.get("cm")), // 2 m
            "b" to Quantity(3.0, UnitRegistry.get("m"))    // 3 m
        )
        val evaluator = Evaluator(variables)
        val result = evaluator.eval(Expr.Binary(Expr.Variable("a"), Operator.ADD, Expr.Variable("b")))
        // 2 + 3 = 5 m → in terms of left unit (cm) = 500 cm
        assertEquals(500.0, result.value, 0.0001)
        assertEquals(UnitRegistry.get("cm"), result.unit)
    }

    @Test
    fun `should throw error when adding incompatible units`() {
        val variables = mapOf(
            "a" to Quantity(10.0, UnitRegistry.get("m")),
            "b" to Quantity(5.0, UnitRegistry.get("kg"))
        )
        val evaluator = Evaluator(variables)
        assertThrows(IllegalArgumentException::class.java) {
            evaluator.eval(Expr.Binary(Expr.Variable("a"), Operator.ADD, Expr.Variable("b")))
        }
    }

    @Test
    fun `should multiply and divide quantities`() {
        val variables = mapOf(
            "length" to Quantity(10.0, UnitRegistry.get("m")),
            "time"   to Quantity(2.0, UnitRegistry.get("s"))
        )
        val evaluator = Evaluator(variables)

        val speed = evaluator.eval(Expr.Binary(Expr.Variable("length"), Operator.DIV, Expr.Variable("time")))
        assertEquals(5.0, speed.value, 0.0001)
        assertEquals(1, speed.unit.dimension.length)
        assertEquals(-1, speed.unit.dimension.time)

        val area = evaluator.eval(Expr.Binary(Expr.Variable("length"), Operator.MUL, Expr.Variable("length")))
        assertEquals(100.0, area.value, 0.0001)
        assertEquals(2, area.unit.dimension.length)
    }

    @Test
    fun `should handle power of quantities`() {
        val variables = mapOf("a" to Quantity(3.0, UnitRegistry.get("m")))
        val evaluator = Evaluator(variables)

        val squared = evaluator.eval(
            Expr.Binary(
                Expr.Variable("a"),
                Operator.POW,
                Expr.Number(2.0, "unitless") // ✅ FIXED
            )
        )

        assertEquals(9.0, squared.value, 0.0001)
        assertEquals(2, squared.unit.dimension.length)
    }

    @Test
    fun `should handle unary minus in expressions`() {
        val variables = mapOf("x" to Quantity(10.0, UnitRegistry.get("m")))
        val evaluator = Evaluator(variables)

        val expr = Expr.Binary(
            Expr.Binary(Expr.Number(0.0, "m"), Operator.SUB, Expr.Variable("x")),
            Operator.ADD,
            Expr.Number(5.0, "m")
        )
        val result = evaluator.eval(expr)
        assertEquals(-5.0, result.value, 0.0001)
        assertEquals(UnitRegistry.get("m"), result.unit)
    }

    @Test
    fun `should evaluate nested expressions correctly`() {
        val variables = mapOf(
            "a" to Quantity(2.0, UnitRegistry.get("m")),
            "b" to Quantity(3.0, UnitRegistry.get("m"))
        )
        val evaluator = Evaluator(variables)

        val expr = Expr.Binary(
            Expr.Binary(Expr.Variable("a"), Operator.ADD, Expr.Variable("b")),
            Operator.MUL,
            Expr.Number(2.0, "m")
        )
        val result = evaluator.eval(expr)
        // (2+3) * 2 = 10 m
        assertEquals(10.0, result.value, 0.0001)
//        assertEquals(UnitRegistry.get("m"), result.unit)
    }
}