package evaluator

import units.Quantity
import parser.Expr
import parser.Operator
import units.UnitRegistry

class Evaluator(
    private val variables: Map<String, Quantity>
) {

    fun eval(expr: Expr): Quantity {
        return when (expr) {

            is Expr.Number ->
                Quantity(expr.value, UnitRegistry.get("unitless"))

            is Expr.Variable ->
                variables[expr.name]
                    ?: error("Variable ${expr.name} not found")

            is Expr.Binary -> {
                var left = eval(expr.left)
                var right = eval(expr.right)

                // 🚨 DO NOT promote for POW and multiply/divide
                if (expr.op == Operator.ADD || expr.op == Operator.SUB) {
                    if (left.unit.dimension.isZero() && !right.unit.dimension.isZero()) {
                        left = Quantity(left.value, right.unit)
                    } else if (right.unit.dimension.isZero() && !left.unit.dimension.isZero()) {
                        right = Quantity(right.value, left.unit)
                    }
                }

                when (expr.op) {
                    Operator.ADD -> left + right
                    Operator.SUB -> left - right
                    Operator.MUL -> left * right
                    Operator.DIV -> left / right

                    Operator.POW -> {
                        require(right.unit.dimension.isZero()) {
                            "Exponent must be unitless"
                        }

                        val exp = right.value
                        require(exp % 1.0 == 0.0) {
                            "Exponent must be an integer"
                        }

                        left.pow(exp.toInt())
                    }
                }
            }
        }
    }
}