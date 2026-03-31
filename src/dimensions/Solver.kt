package dimensions

object Solver {

    fun solve(constraints: List<Constraint>): Map<DimVar, DimensionVector> {
        val result = mutableMapOf<DimVar, DimensionVector>()

        for (c in constraints) {
            val sol = solveSingle(c)
            if (sol != null) {
                result.putAll(sol)
            }
        }

        return result
    }

    private fun solveSingle(c: Constraint): Map<DimVar, DimensionVector>? {
        val left = toLinear(c.left)
        val right = toLinear(c.right)

        val diff = merge(left, right, -1) // left - right = 0

        val unknowns = diff.coeffs.keys

        if (unknowns.size > 1) {
            println("⚠️ Multiple unknowns in constraint: $unknowns")
            return null
        }

        if (unknowns.isEmpty()) {
            return null
        }

        val v = unknowns.first()
        val coeff = diff.coeffs[v]!!

        if (coeff == 0) return null

        val solved = when (coeff) {
            1 -> diff.constant * -1
            -1 -> diff.constant
            else -> return null // unsupported for now
        }

        return mapOf(v to solved)
    }

    private fun toLinear(expr: DimExpr): LinearExpr {
        return when (expr) {
            is DimConst -> LinearExpr(mutableMapOf(), expr.dim)

            is DimUnknown -> LinearExpr(mutableMapOf(expr.v to 1), DimensionVector.ZERO)

            is DimAdd -> {
                val l = toLinear(expr.a)
                val r = toLinear(expr.b)
                merge(l, r, +1)
            }

            is DimSub -> {
                val l = toLinear(expr.a)
                val r = toLinear(expr.b)
                merge(l, r, -1)
            }
        }
    }
}