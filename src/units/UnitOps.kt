package units


private fun combineNames(a: Unit, b: Unit, op: String): String {
    return when {
        a.name == "unitless" -> b.name
        b.name == "unitless" -> a.name
        op == "*" -> "${a.name}*${b.name}"
        else -> "${a.name}/${b.name}"
    }
}

operator fun Unit.times(other: Unit): Unit {
    if ((this.dimension.currency + other.dimension.currency)
            .values.any { it !in -1..1 }) {
        throw IllegalArgumentException("Invalid currency exponent")
    }

    val newDim = this.dimension + other.dimension

    if (newDim.currency.values.any { it !in setOf(-1, 0, 1) }) {
        throw IllegalArgumentException("Invalid currency exponent")
    }

    return Unit(
        name = combineNames(this, other, "*"),
        factor = this.factor * other.factor,
        dimension = newDim
    )
}

operator fun Unit.div(other: Unit): Unit {
    return Unit(
        name = combineNames(this, other, "/"),
        factor = this.factor / other.factor,
        dimension = this.dimension - other.dimension
    )
}

fun Unit.pow(exp: Int): Unit {

    if (exp == 0) return UnitRegistry.get("unitless")

    // ❗ CRITICAL FIX
    if (this.dimension.currency.isNotEmpty()) {
        throw IllegalArgumentException("Cannot raise currency units to a power")
    }

    var result = UnitRegistry.get("unitless")
    val times = kotlin.math.abs(exp)

    repeat(times) {
        result *= this
    }

    return if (exp > 0) result else UnitRegistry.get("unitless") / result
}