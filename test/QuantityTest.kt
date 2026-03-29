import api.FormulaEngine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry
import units.format.UnitFormatter

class QuantityTest {

    val engine = FormulaEngine()

    @BeforeEach
    fun setup() {
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
    }

    @Test
    fun `adding two quantities of same dimension`() {
        val q1 = Quantity(10.0, UnitRegistry.get("cm"))
        val q2 = Quantity(5.0, UnitRegistry.get("m"))

        val result = q1 + q2
        assertEquals(510.0, result.value, 0.0001)  // still in q1's unit (cm)
        assertEquals("cm", result.unit.name)
    }

    @Test
    fun `adding quantities of different dimensions throws`() {
        val q1 = Quantity(10.0, UnitRegistry.get("kg"))
        val q2 = Quantity(5.0, UnitRegistry.get("cm"))

        assertThrows<IllegalArgumentException> {
            q1 + q2
        }
    }

    @Test
    fun `subtraction of same dimension`() {
        val q1 = Quantity(1.0, UnitRegistry.get("m"))
        val q2 = Quantity(50.0, UnitRegistry.get("cm"))

        val result = q1 - q2
        assertEquals(0.5, result.value, 0.0001)
        assertEquals("m", result.unit.name)
    }

    @Test
    fun `multiplication of quantities`() {
        val q1 = Quantity(10.0, UnitRegistry.get("kg"))
        val q2 = Quantity(5.6, UnitRegistry.get("pk_ton"))
        val q3 = Quantity(1.2, UnitRegistry.get("tonne"))

        val result = q1 * q2 * q3

        // Value is raw multiplication
        assertEquals(2688000.0, result.value, 0.0001)
        assertEquals("kg*pk_ton*tonne", result.unit.name)
        assertEquals(3, result.unit.dimension.mass)
    }

    @Test
    fun `division of same dimensions should be unitless`() {
        val distance = Quantity(10.0, UnitRegistry.get("km"))
        val length = Quantity(100.0, UnitRegistry.get("m"))

        val result = distance / length
        assertEquals(100.0, result.value, 0.0001) // 10 km / 100 m
        assertTrue(result.unit.dimension.isZero())
    }

    @Test
    fun `division of different dimensions produces rate`() {
        val dist = Quantity(100.0, UnitRegistry.get("km"))
        val time = Quantity(2.0, UnitRegistry.get("hr"))

        val speed = dist / time
        val speedKmHr = speed.to("km/hr")  // convert for human-readable test

        assertEquals(50.0, speedKmHr.value, 0.0001)
        assertEquals(1, speed.unit.dimension.length)
        assertEquals(-1, speed.unit.dimension.time)
    }

    @Test
    fun `power scales correctly`() {
        val q = Quantity(2.0, UnitRegistry.get("m"))
        val result = q.pow(3)

        assertEquals(8.0, result.value, 0.0001)  // 2^3 = 8
        assertEquals(3, result.unit.dimension.length)
    }

    @Test
    fun `conversion works`() {
        val q = Quantity(100.0, UnitRegistry.get("cm"))
        val result = q.to("m")
        assertEquals(1.0, result.value, 0.0001)
    }

    @Test
    fun `nested conversion works`() {
        val start = Quantity(1.0, UnitRegistry.get("hr"))
        val result = start.to("min").to("s")
        assertEquals(3600.0, result.value, 0.0001)
    }

    @Test
    fun `unitless multiplication preserves dimensions`() {
        val q = Quantity(5.0, UnitRegistry.get("m"))
        val one = Quantity(1.0, UnitRegistry.get("unitless"))
        val result = q * one
        assertEquals(5.0, result.value)
        assertEquals(q.unit.dimension, result.unit.dimension)
    }

    @Test
    fun `division by zero throws`() {
        val q = Quantity(5.0, UnitRegistry.get("m"))
        val zero = Quantity(0.0, UnitRegistry.get("m"))

        assertThrows<ArithmeticException> { q / zero }
    }

    @Test
    fun `formatter integration example`() {
        val q = Quantity(1500.0, UnitRegistry.get("m"))
        val formatted = UnitFormatter.format(q, "km")
        assertEquals("1.5 km", formatted)
    }

    @Test
    fun `multiplying quantity with scalar preserves dimension`() {
        val q = Quantity(10.0, UnitRegistry.get("m"))
        val result = Quantity(2.0, UnitRegistry.get("unitless")) * q
        assertEquals(20.0, result.value, 0.0001)
        assertEquals(1, result.unit.dimension.length)
    }
}