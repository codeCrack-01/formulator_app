package evaluator

import units.Quantity
import parser.Expr
import parser.Operator
import units.UnitRegistry
import kotlin.math.roundToInt

class Evaluator(
    val variables: Map<String, Quantity>
) {

    fun eval(expr: Expr): Quantity {
        return when (expr) {

            is Expr.Number -> {
                val unit = if (expr.unit.isNotEmpty()) {
                    UnitRegistry.parse(expr.unit)
                } else {
                    println("⚠️ Failed to parse unit: ${expr.unit}")
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

                // Promote unitless for ADD/SUB only
                if (expr.op == Operator.ADD || expr.op == Operator.SUB) {
                    if (left.unit.dimension.isZero() && !right.unit.dimension.isZero()) {
                        left = Quantity(left.value, right.unit)
                    } else if (right.unit.dimension.isZero() && !left.unit.dimension.isZero()) {
                        right = Quantity(right.value, left.unit)
                    }
                }

                when (expr.op) {
                    Operator.ADD, Operator.SUB -> {

                        if (left.unit.dimension != right.unit.dimension) {
                            throw IllegalArgumentException(
                                "Cannot ${expr.op} quantities with different dimensions: " +
                                        "${left.unit.dimension} and ${right.unit.dimension}"
                            )
                        }

                        if (expr.op == Operator.ADD) left + right else left - right
                    }
                    Operator.MUL -> left * right
                    Operator.DIV -> left / right

                    Operator.POW -> {
                        require(right.unit.dimension.isZero()) {
                            "Exponent must be unitless"
                        }

                        val exp = right.value
                        require(kotlin.math.abs(exp - exp.roundToInt()) < 1e-9) {
                            "Exponent must be an integer"
                        }

                        left.pow(exp.toInt())
                    }
                }
            }
        }
    }
}