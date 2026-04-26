package api

import currency.StaticExchangeRateProvider
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

class FormulaServiceTest {

    private val registry = UnitRegistry
    private val exchangeProvider = StaticExchangeRateProvider()

    private fun service(vars: Map<String, Quantity> = emptyMap()) =
        FormulaService(
            FormulaEngine(
                Evaluator(vars, exchangeProvider),
                registry
            ),
            exchangeProvider
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

        // Volume
        val volume = Dimension(length = 3)
        UnitRegistry.register(Unit("L", 0.001, volume))

        // Currency handled by StaticExchangeRateProvider
        UnitRegistry.register(
            Unit(
                "USD",
                1.0,
                Dimension(currency = mutableMapOf("USD" to 1))
            )
        )

        UnitRegistry.register(
            Unit(
                "PKR",
                1.0,
                Dimension(currency = mutableMapOf("PKR" to 1))
            )
        )

        UnitRegistry.register(
            Unit(
                "SAR",
                1.0,
                Dimension(currency = mutableMapOf("SAR" to 1))
            )
        )
    }

    @Test
    fun `service evaluates simple arithmetic`() {
        val expr = Expr.Binary(
            Expr.Number(2.0, ""),
            Operator.ADD,
            Expr.Number(3.0, "")
        )

        when (val result = service().evaluate(expr)) {
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
    fun `service validates expected dimension`() {
        val expr = Expr.Binary(
            Expr.Number(10.0, "m"),
            Operator.DIV,
            Expr.Number(2.0, "s")
        )

        when (
            val result = service().evaluateWithExpected(
                expr,
                registry.parse("m/s").dimension
            )
        ) {
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
    fun `service converts currency`() {
        val quantity = Quantity(
            100.0,
            registry.parse("USD")
        )

        when (
            val result = service().convert(
                quantity,
                "PKR"
            )
        ) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    mapOf("PKR" to 1),
                    result.quantity.unit.dimension.currency
                )
                Assertions.assertEquals(
                    28000.0,
                    result.quantity.value
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }

    @Test
    fun `service returns error for incompatible addition`() {
        val expr = Expr.Binary(
            Expr.Number(2.0, "m"),
            Operator.ADD,
            Expr.Number(3.0, "s")
        )

        val result = service().evaluate(expr)

        Assertions.assertTrue(result is FormulaResult.Error)
    }

    @Test
    fun `service handles variables`() {
        val expr = Expr.Binary(
            Expr.Variable("x"),
            Operator.MUL,
            Expr.Number(2.0, "")
        )

        val vars = mapOf(
            "x" to Quantity(5.0, registry.parse("m"))
        )

        when (val result = service(vars).evaluate(expr)) {
            is FormulaResult.Success -> {
                Assertions.assertEquals(
                    10.0,
                    result.quantity.value,
                    1e-6
                )

                Assertions.assertEquals(
                    registry.parse("m").dimension,
                    result.quantity.unit.dimension
                )
            }

            is FormulaResult.Error -> {
                Assertions.fail("Expected success but got: ${result.error}")
            }
        }
    }
}
