package dimensions

data class LinearExpr(
    val coeffs: MutableMap<DimVar, Int>,
    val constant: DimensionVector
)

fun merge(
    a: LinearExpr,
    b: LinearExpr,
    sign: Int
): LinearExpr {

    val coeffs = a.coeffs.toMutableMap()

    for ((k, v) in b.coeffs) {
        coeffs[k] = (coeffs[k] ?: 0) + sign * v
    }

    val constant = a.constant + b.constant * sign

    return LinearExpr(coeffs, constant)
}

