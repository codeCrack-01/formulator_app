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

        UnitRegistry.register(Unit("m", 1.0, length))
        UnitRegistry.register(Unit("s", 1.0, time))

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
}