package evaluator

import units.Quantity
import parser.Expr
import parser.Operator
import units.UnitRegistry
import kotlin.math.roundToInt
import currency.ExchangeRateProvider

class Evaluator(
    val variables: Map<String, Quantity>,
    val exchange: ExchangeRateProvider? = null
) {
    private fun convertCurrency(
        q: Quantity,
        targetCode: String
    ): Quantity {

        val sourceCode = q.unit.dimension.currency.keys.first()

        val rate: Double = exchange!!.rate(sourceCode, targetCode)

        val newValue = q.value * rate
        val newUnit = UnitRegistry.get(targetCode)

        return Quantity(newValue, newUnit)
    }

    fun eval(expr: Expr): Quantity {
        return when (expr) {

            is Expr.Number -> {
                val unit = if (expr.unit.isNotEmpty()) {
                    UnitRegistry.parse(expr.unit)
                } else {
                    UnitRegistry.get("unitless")
                }
                Quantity(expr.value, unit)
            }

            is Expr.Variable ->
                variables[expr.name]
                    ?: error("Variable ${expr.name} not found")

            is Expr.Binary -> {
                var left = eval(expr.left)
                var right = eval(expr.right)

                if (expr.op == Operator.ADD || expr.op == Operator.SUB) {
                    if (left.unit.dimension.isZero() && !right.unit.dimension.isZero()) {
                        left = Quantity(left.value, right.unit)
                    } else if (right.unit.dimension.isZero() && !left.unit.dimension.isZero()) {
                        right = Quantity(right.value, left.unit)
                    }
                }

                when (expr.op) {

                    Operator.ADD, Operator.SUB -> {

                        val leftDim = left.unit.dimension
                        val rightDim = right.unit.dimension

                        if (leftDim == rightDim) {
                            return if (expr.op == Operator.ADD) left + right else left - right
                        }

                        if (leftDim.isPureCurrency() && rightDim.isPureCurrency()) {

                            val leftCurrency = leftDim.currency.keys.first()
                            val rightCurrency = rightDim.currency.keys.first()

                            val convertedRight = convertCurrency(
                                right.value,
                                rightCurrency,
                                leftCurrency
                            )

                            val resultValue = if (expr.op == Operator.ADD) {
                                left.value + convertedRight
                            } else {
                                left.value - convertedRight
                            }

                            return Quantity(resultValue, left.unit)
                        }

                        throw IllegalArgumentException(
                            "Cannot ${expr.op} quantities with different dimensions: " +
                                    "$leftDim and $rightDim"
                        )
                    }

                    Operator.MUL -> {
                        val result = left * right
                        result.unit.dimension.validate()
                        result
                    }

                    Operator.DIV -> {
                        val result = left / right
                        result.unit.dimension.validate()
                        result
                    }

                    Operator.POW -> {
                        require(right.unit.dimension.isZero())
                            "Exponent must be unitless"

                        val exp = right.value
                        require(kotlin.math.abs(exp - exp.roundToInt()) < 1e-9)
                            "Exponent must be an integer"

                        // ❗ THIS MUST BE EXACT
                        if (left.unit.dimension.currency.isNotEmpty())
                            throw IllegalArgumentException("Cannot apply power to currency quantities")

                        val result = left.pow(exp.toInt())
                        result
                    }
                }
            }
        }
    }

    private fun convertCurrency(
        amount: Double,
        from: String,
        to: String
    ): Double {
        val provider = exchange ?: error("ExchangeRateProvider not set")
        if (from == to)
            return amount

        val rate = provider.rate(from, to)
        return amount * rate
    }
}