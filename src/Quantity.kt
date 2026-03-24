data class Quantity(
    val value: Double,
    val unit: Unit
) {
    operator fun plus(other: Quantity): Quantity {
        require(this.unit.dimension == other.unit.dimension) {
            "Dimension mismatch: cannot add ${this.unit.name} and ${other.unit.name}"
        }

        val v1 = this.value * this.unit.factor
        val v2 = other.value * other.unit.factor

        val resultBase = v1 + v2
        return Quantity(resultBase / other.unit.factor, other.unit)
    }
    operator fun times(other: Quantity): Quantity {
        // 1. Calculate how many 'this.unit' are in one 'other.unit'
        // Example: (1m factor is 1.0) / (1cm factor is 0.01) = 100
        val conversionRatio = other.unit.factor / this.unit.factor

        // 2. Convert the second value to the first unit's scale
        // Example: 1m becomes 100cm
        val normalizedOtherValue = other.value * conversionRatio

        // 3. Multiply the values now that they are in the same scale
        // 100cm * 100cm = 10000
        val newValue = this.value * normalizedOtherValue

        // 4. The new unit's factor must be the physical product of both
        // so that it still relates correctly to the SI base (meters)
        return Quantity(
            newValue,
            Unit(
                "${this.unit.name}*${other.unit.name}",
                this.unit.factor * other.unit.factor,
                this.unit.dimension + other.unit.dimension
            )
        )
    }

    operator fun div(other: Quantity): Quantity {
        // 1. Normalize the second value to the first unit's scale
        val conversionRatio = other.unit.factor / this.unit.factor
        val normalizedOtherValue = other.value * conversionRatio

        // 2. Divide
        val newValue = this.value / normalizedOtherValue

        return Quantity(
            newValue,
            Unit(
                "${this.unit.name}/${other.unit.name}",
                this.unit.factor / other.unit.factor,
                this.unit.dimension - other.unit.dimension
            )
        )
    }
}
