package dimensions

import parser.Expr
import parser.Operator
import units.Quantity
import units.UnitParser

// Result wrapper to avoid double-building bug
data class DimBuildResult(
    val expr: DimExpr,
    val constraints: MutableList<Constraint>
)

object DimBuilder {

    fun build(
        expr: Expr,
        variables: Map<String, Quantity>
    ): DimBuildResult {
        val constraints = mutableListOf<Constraint>()
        val dimExpr = buildExpr(expr, constraints, variables)
        return DimBuildResult(dimExpr, constraints)
    }

    fun buildExpr(
        expr: Expr,
        constraints: MutableList<Constraint>,
        variables: Map<String, Quantity>
    ): DimExpr {

        return when (expr) {

            is Expr.Number -> {
                if (expr.unit.isNotEmpty()) {
                    DimConst(unitToDim(expr.unit))
                } else {
                    DimUnknown(DimVar(expr.id))
                }
            }

            is Expr.Variable -> {
                val quantity = variables[expr.name]
                    ?: try {
                        Quantity(
                            1.0,
                            units.UnitRegistry.get(expr.name)
                        )
                    } catch (e: Exception) {
                        error("Variable ${expr.name} not found")
                    }

                DimConst(
                    quantity.unit.dimension.toVector()
                )
            }

            is Expr.Binary -> {
                val left = buildExpr(expr.left, constraints, variables)
                val right = buildExpr(expr.right, constraints, variables)

                when (expr.op) {
                    Operator.MUL -> DimAdd(left, right)
                    Operator.DIV -> DimSub(left, right)

                    Operator.ADD, Operator.SUB -> {
                        constraints += Constraint(left, right)
                        println("ADD/SUB constraint added: $left == $right")
                        left
                    }

                    Operator.POW -> {
                        require(expr.right is Expr.Number) {
                            "Exponent must be a number"
                        }

                        val exp = expr.right.value.toInt()
                        if (exp == 0) {
                            return DimConst(DimensionVector.ZERO)
                        }
                        repeatAdd(left, exp)
                    }
                }
            }
        }
    }

    private fun repeatAdd(base: DimExpr, k: Int): DimExpr {
        if (k == 0) {
            return DimConst(DimensionVector.ZERO)
        }

        val absK = kotlin.math.abs(k)

        var result = base
        repeat(absK - 1) {
            result = DimAdd(result, base)
        }

        return if (k > 0) result else DimSub(DimConst(DimensionVector.ZERO), result)
    }

    // 🔧 Bridge: Unit → DimensionVector
    private fun unitToDim(unit: String): DimensionVector {
        return UnitParser.parse(unit)
    }
}