package api

import currency.StaticExchangeRateProvider
import evaluator.Evaluator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry

class FormulaApiTest {

    private val registry = UnitRegistry

    private fun api(vars: Map<String, Quantity> = emptyMap()) =
        FormulaApi(
            FormulaService(
                FormulaEngine(
                    Evaluator(vars, StaticExchangeRateProvider()),
                    registry
                ),
                StaticExchangeRateProvider()
            )
        )

    @BeforeEach
    fun setup() {
        UnitRegistry.clear()

        val length = Dimension(length = 1)
        val time = Dimension(time = 1)
        val mass = Dimension(mass = 1)

        UnitRegistry.register(Unit("m", 1.0, length))
        UnitRegistry.register(Unit("cm", 0.01, length))
        UnitRegistry.register(Unit("s", 1.0, time))
        UnitRegistry.register(Unit("kg", 1.0, mass))

        val force = mass + length - Dimension(time = 2)
        UnitRegistry.register(Unit("N", 1.0, force))

        val unitless = Dimension()
        UnitRegistry.register(Unit("unitless", 1.0, unitless))
    }

    @Test
    fun `api evaluates simple arithmetic string`() {
        val result = api().evaluate("2 + 3")

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    5.0,
                    result.quantity.value,
                    1e-6
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `api evaluates unit expression`() {
        val result = api().evaluate("10 m / 2 s")

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    registry.parse("m/s").dimension,
                    result.quantity.unit.dimension
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `api handles invalid expression gracefully`() {
        val result = api().evaluate("10 +")

        Assertions.assertTrue(result is FormulaResult.Error)
    }

    @Test
    fun `api evaluates with expected dimension`() {
        val result = api().evaluate(
            "10 m / 2 s",
            registry.parse("m/s").dimension
        )

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    registry.parse("m/s").dimension,
                    result.quantity.unit.dimension
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `api supports explicit unit multiplication syntax`() {
        val result = api().evaluate("10 kg.m / s^2")

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    registry.parse("N").dimension,
                    result.quantity.unit.dimension
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `api rejects implicit unit multiplication syntax`() {
        val result = api().evaluate("10 kgm / s^2")

        Assertions.assertTrue(
            result is FormulaResult.Error
        )
    }

    @Test
    fun `api evaluates expression with variables`() {
        val vars = mapOf(
            "x" to Quantity(
                5.0,
                registry.get("m")
            )
        )

        val result = api(vars).evaluate("x + 5 m")

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    10.0,
                    result.quantity.value,
                    1e-6
                )

                Assertions.assertEquals(
                    registry.get("m").dimension,
                    result.quantity.unit.dimension
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `api rejects dimension mismatch`() {
        val result = api().evaluate("10 m + 5 s")

        Assertions.assertTrue(
            result is FormulaResult.Error
        )
    }

    @Test
    fun `api supports unary negative values`() {
        val result = api().evaluate("-5 + 10")

        when (result) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    5.0,
                    result.quantity.value,
                    1e-6
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }
}