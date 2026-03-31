package dimensions

enum class BaseDim { LENGTH, MASS, TIME, MONEY }

data class DimensionVector(
    val exponents: IntArray = IntArray(BaseDim.values().size)
) {
    operator fun plus(other: DimensionVector): DimensionVector =
        DimensionVector(exponents.zip(other.exponents) { a, b -> a + b }.toIntArray())

    operator fun minus(other: DimensionVector): DimensionVector =
        DimensionVector(exponents.zip(other.exponents) { a, b -> a - b }.toIntArray())

    operator fun times(k: Int): DimensionVector =
        DimensionVector(exponents.map { it * k }.toIntArray())

    companion object {
        val ZERO = DimensionVector()
    }
}