package units

import dimensions.DimensionVector

data class Dimension(
    val length: Int = 0,
    val time: Int = 0,
    val mass: Int = 0,
    val currency: Map<String, Int> = emptyMap()
) {

    operator fun plus(other: Dimension) = Dimension(
        length + other.length,
        time + other.time,
        mass + other.mass,
        merge(currency, other.currency)
    )

    operator fun minus(other: Dimension) = Dimension(
        length - other.length,
        time - other.time,
        mass - other.mass,
        merge(currency, negate(other.currency))
    )

    private fun merge(a: Map<String, Int>, b: Map<String, Int>): Map<String, Int> {
        val result = a.toMutableMap()
        for ((k, v) in b) {
            result[k] = (result[k] ?: 0) + v
            if (result[k] == 0) result.remove(k)
        }
        return result
    }

    private fun negate(map: Map<String, Int>): Map<String, Int> =
        map.mapValues { -it.value }

    fun validate() {
        for ((currency, power) in currency) {
            require(power == 1 || power == -1) {
                throw IllegalArgumentException("Invalid currency exponent for $currency: $power")
            }
        }
    }

    fun isZero(): Boolean {
        return length == 0 &&
                time == 0 &&
                mass == 0 &&
                currency.isEmpty()
    }

    val isDimensionless: Boolean
        get() = isZero()

    fun isPureCurrency(): Boolean {
        return length == 0 &&
                time == 0 &&
                mass == 0 &&
                currency.isNotEmpty()
    }

    fun toVector(): DimensionVector {
        return DimensionVector(
            intArrayOf(
                this.length,
                this.mass,
                this.time,
                0 // currency handled separately
            )
        )
    }
}