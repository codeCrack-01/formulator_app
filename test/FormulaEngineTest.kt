import api.FormulaEngine
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry
import units.format.UnitFormatter

class FormulaEngineTest {
    private lateinit var engine: FormulaEngine
    private lateinit var meters: Quantity
    private lateinit var seconds: Quantity

    @BeforeEach
    fun setUp() {
        // Units Setup:
        val unitlessDim = Dimension()
        UnitRegistry.register(units.Unit("unitless", 1.0, unitlessDim))

        val length = Dimension(length = 1)

        UnitRegistry.register(units.Unit("m", 1.0, length))
        UnitRegistry.register(units.Unit("cm", 0.01, length))
        UnitRegistry.register(units.Unit("km", 1000.0, length))

        val time = Dimension(time = 1)

        UnitRegistry.register(units.Unit("s", 1.0, time))
        UnitRegistry.register(units.Unit("min", 60.0, time))
        UnitRegistry.register(units.Unit("hr", 3600.0, time))

        val mass = Dimension(mass = 1)

        UnitRegistry.register(units.Unit("g", 0.001, mass))
        UnitRegistry.register(units.Unit("kg", 1.0, mass))
        UnitRegistry.register(units.Unit("tonne", 1000.0, mass))
        UnitRegistry.register(Unit("ton", 40.0, mass)) // The pk_ton

        // Initialize the engine
        engine = FormulaEngine()

        // Optional: Pre-define some common quantities for cleaner tests
        meters = Quantity(1.0, UnitRegistry.get("m"))
        seconds = Quantity(1.0, UnitRegistry.get("s"))
    }

    @Test
    fun `should handle unary minus`() {
        val result = engine.evaluate("-5 + 10", emptyMap())
        assertEquals(5.0, result.value)
    }

    @Test
    fun `should handle nested unary operators`() {
        // --5 should be 5
        val result = engine.evaluate("--5", emptyMap())
        assertEquals(5.0, result.value)
    }

    @Test
    fun `should handle unary with parentheses`() {
        val result = engine.evaluate("-(2 + 3)", emptyMap())
        assertEquals(-5.0, result.value)
    }

    @Test
    fun `should evaluate expression with variables and units`() {
        val vars = mapOf(
            "L" to Quantity(2.0, UnitRegistry.get("m")),
            "W" to Quantity(3.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("L * W", vars)

        assertEquals(6.0, result.value, 0.0001)
        assertEquals(2, result.unit.dimension.length)
    }

    @Test
    fun `should handle mixed units correctly`() {
        val vars = mapOf(
            "L" to Quantity(200.0, UnitRegistry.get("cm")), // 2 m
            "W" to Quantity(3.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("L + W", vars)

        assertEquals(500.0, result.value, 0.0001) // in cm
    }

    @Test
    fun `should evaluate real physics formula`() {
        val vars = mapOf(
            "d" to Quantity(100.0, UnitRegistry.get("km")),
            "t" to Quantity(2.0, UnitRegistry.get("hr"))
        )

        val result = engine.evaluate("d / t", vars)
        val formattedResult = UnitFormatter.formatStructured(result)

        assertEquals(50.0, formattedResult.value, 0.0001)
        assertEquals(1, result.unit.dimension.length)
        assertEquals(-1, result.unit.dimension.time)
    }

    @Test
    fun `should handle power with units`() {
        val vars = mapOf(
            "r" to Quantity(3.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("r^2", vars)

        assertEquals(9.0, result.value, 0.0001)
        assertEquals(2, result.unit.dimension.length)
    }

    @Test
    fun `should respect precedence with units`() {
        val vars = mapOf(
            "L" to Quantity(2.0, UnitRegistry.get("m")),
            "W" to Quantity(3.0, UnitRegistry.get("m")),
            "t" to Quantity(2.0, UnitRegistry.get("s"))
        )

        val result = engine.evaluate("L * W / t^2", vars)

        assertEquals(1.5, result.value, 0.0001)
    }

    @Test
    fun `should throw on incompatible addition`() {
        val vars = mapOf(
            "L" to Quantity(2.0, UnitRegistry.get("m")),
            "t" to Quantity(2.0, UnitRegistry.get("s"))
        )

        assertThrows(IllegalArgumentException::class.java) {
            engine.evaluate("L + t", vars)
        }
    }

    @Test
    fun `should throw on missing variable`() {
        assertThrows(IllegalStateException::class.java) {
            engine.evaluate("x + 2", emptyMap())
        }
    }

    @Test
    fun `should auto promote unitless scalar in addition`() {
        val vars = mapOf(
            "x" to Quantity(10.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("x + 5", vars)

        assertEquals(15.0, result.value, 0.0001)
        assertEquals(1, result.unit.dimension.length)
    }

    @Test
    fun `should produce dimensionless result`() {
        val vars = mapOf(
            "a" to Quantity(10.0, UnitRegistry.get("m")),
            "b" to Quantity(2.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("a / b", vars)

        assertEquals(5.0, result.value, 0.0001)
        assertTrue(result.unit.dimension.isZero())
    }

    @Test
    fun `power should be right associative`() {
        val result = engine.evaluate("2^3^2", emptyMap())

        // 2^(3^2) = 2^9 = 512
        assertEquals(512.0, result.value)
    }

    @Test
    fun `should handle negative exponents`() {
        val vars = mapOf(
            "x" to Quantity(2.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("x^-1", vars)

        assertEquals(0.5, result.value, 0.0001)
        assertEquals(-1, result.unit.dimension.length)
    }

    @Test
    fun `should handle nested parentheses with units`() {
        val vars = mapOf(
            "a" to Quantity(2.0, UnitRegistry.get("m")),
            "b" to Quantity(3.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("(a + b)^2", vars)

        // (2 + 3)^2 = 25
        assertEquals(25.0, result.value, 0.0001)
        assertEquals(2, result.unit.dimension.length)
    }

    @Test
    fun `formatter should fallback to base dimension string`() {
        val vars = mapOf(
            "x" to Quantity(2.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("x^3", vars)
        val formatted = UnitFormatter.format(result)

        assertTrue(formatted.contains("m^3"))
    }

    @Test
    fun `should multiply quantity with scalar`() {
        val vars = mapOf(
            "x" to Quantity(10.0, UnitRegistry.get("m"))
        )

        val result = engine.evaluate("2 * x", vars)

        assertEquals(20.0, result.value, 0.0001)
        assertEquals(1, result.unit.dimension.length)
    }

    @Test
    fun `formatter should auto convert to human readable units`() {
        val vars = mapOf(
            "d" to Quantity(100.0, UnitRegistry.get("km")),
            "t" to Quantity(2.0, UnitRegistry.get("hr"))
        )

        val result = engine.evaluate("d / t", vars)
        val formatted = UnitFormatter.formatStructured(result)

        println(result.unit.dimension)

        assertEquals(50.0, formatted.value, 0.0001)
        assertEquals("km/hr", formatted.unitName)
    }

    @Test
    fun `should handle complex chained expression`() {
        val vars = mapOf(
            "a" to Quantity(2.0, UnitRegistry.get("m")),
            "b" to Quantity(3.0, UnitRegistry.get("m")),
            "c" to Quantity(4.0, UnitRegistry.get("s"))
        )

        val result = engine.evaluate("(a + b) * a / c", vars)

        // (2 + 3)*2 / 4 = 2.5
        assertEquals(2.5, result.value, 0.0001)
        assertEquals(2, result.unit.dimension.length)
        assertEquals(-1, result.unit.dimension.time)
    }
}