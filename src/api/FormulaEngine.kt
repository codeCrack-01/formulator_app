package api

import dimensions.*
import evaluator.Evaluator
import parser.Expr
import parser.Operator
import units.*

class FormulaEngine(
    private val evaluator: Evaluator,
    private val unitRegistry: UnitRegistry
) {

    fun evaluate(
        expr: Expr,
        expected: Dimension?,
        overrides: Map<String, String> = emptyMap()
    ): Quantity {

        // ✅ SINGLE PASS BUILD (FIXED)
        val build = DimBuilder.build(expr, evaluator.variables)
        val constraints = build.constraints
        val exprDim = build.expr

        // Add expected constraint
        if (expected != null) {
            constraints += Constraint(
                exprDim,
                DimConst(expected.toVector())
            )
        }

        // Solve
        val inferred = Solver.solve(constraints).toMutableMap()

//        if (inferred.isEmpty()) {
//            println("⚠️ No dimension inference possible for this expression")
//        }

        // Apply overrides
        applyOverrides(inferred, overrides)

        // Attach inferred units
        val enriched = attachUnits(expr, inferred)

        // Evaluate numerically
        val result = evaluator.eval(enriched)

        if (expected != null && result.unit.dimension != expected) {
            throw IllegalStateException()
        }

        return result
    }

    private fun applyOverrides(
        inferred: MutableMap<DimVar, DimensionVector>,
        overrides: Map<String, String>
    ) {
        for ((id, unitStr) in overrides) {
            try {
                val dim = unitRegistry.parse(unitStr).dimension.toVector()
                inferred[DimVar(id)] = dim
            } catch (e: Exception) {
                println("⚠️ Invalid override unit: $unitStr for constant $id")
            }
        }
    }

    private fun attachUnits(
        expr: Expr,
        inferred: Map<DimVar, DimensionVector>
    ): Expr {
        return when (expr) {

            is Expr.Number -> {
                if (expr.unit.isNotEmpty()) return expr

                val key = DimVar(expr.id)
                val dim = inferred[key]
                if (dim == null) {
                    println("⚠️ No unit inferred for constant ${expr.id}")
                    return expr
                }

                val unitStr = dimToUnitString(dim)

                expr.copy(unit = unitStr)
            }

            is Expr.Variable -> expr

            is Expr.Binary -> {
                val leftAttached = attachUnits(expr.left, inferred)
                val rightAttached = attachUnits(expr.right, inferred)

                // ❗ DO NOT collapse POW into unit strings
                if (expr.op == Operator.POW) {
                    return Expr.Binary(leftAttached, expr.op, rightAttached)
                }

                expr.copy(
                    left = leftAttached,
                    right = rightAttached
                )
            }
        }
    }

    private fun dimToUnitString(dim: DimensionVector): String {
        val baseUnits = listOf("m", "kg", "s", "CUR")

        val numerator = mutableListOf<String>()
        val denominator = mutableListOf<String>()

        dim.exponents.forEachIndexed { i, exp ->
            val base = baseUnits[i]
            when {
                exp > 0 -> {
                    numerator += if (exp == 1) base else "$base^$exp"
                }
                exp < 0 -> {
                    val e = -exp
                    denominator += if (e == 1) base else "$base^$e"
                }
            }
        }

        return when {
            numerator.isEmpty() && denominator.isEmpty() -> "unitless"
            denominator.isEmpty() -> numerator.joinToString("*")
            numerator.isEmpty() -> "unitless/${denominator.joinToString("*")}"
            else -> numerator.joinToString("*") + "/" + denominator.joinToString("*")
        }
    }
}