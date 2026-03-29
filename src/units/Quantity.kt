package units

import units.format.UnitConverter
import units.format.UnitExprParser
import kotlin.math.abs
import kotlin.math.pow

data class Quantity(
    val value: Double,
    val unit: Unit
) {

    // Internal: value in base units
    private val baseValue: Double
        get() = value * unit.factor

    // ---------------- ADDITION ----------------
    operator fun plus(other: Quantity): Quantity {
        require(this.unit.dimension == other.unit.dimension) {
            "Dimension mismatch: cannot add ${this.unit.name} and ${other.unit.name}"
        }

        val resultBase = this.baseValue + other.baseValue
        val resultValue = resultBase / this.unit.factor
        return Quantity(resultValue, this.unit)
    }

    // ---------------- SUBTRACTION ----------------
    operator fun minus(other: Quantity): Quantity {
        require(this.unit.dimension == other.unit.dimension) {
            "Dimension mismatch: cannot subtract ${this.unit.name} and ${other.unit.name}"
        }

        val resultBase = this.baseValue - other.baseValue
        val resultValue = resultBase / this.unit.factor
        return Quantity(resultValue, this.unit)
    }

    // ---------------- MULTIPLICATION ----------------
    operator fun times(other: Quantity): Quantity {
        val newBaseValue = this.baseValue * other.baseValue
        val newDimension = this.unit.dimension + other.unit.dimension
        val newName = "${this.unit.name}*${other.unit.name}"

        // factor = 1 because value already in base units
        return Quantity(
            newBaseValue,
            Unit(newName, 1.0, newDimension)
        )
    }

    // ---------------- DIVISION ----------------
    operator fun div(other: Quantity): Quantity {
        require(abs(other.baseValue) > 1e-10) { throw ArithmeticException("Cannot divide by zero") }

        val newBaseValue = this.baseValue / other.baseValue
        val newDimension = this.unit.dimension - other.unit.dimension
        val newName = "${this.unit.name}/${other.unit.name}"

        return Quantity(
            newBaseValue,
            Unit(newName, 1.0, newDimension)
        )
    }

    // ---------------- POW ----------------
    fun pow(exp: Int): Quantity {
        val newBaseValue = baseValue.pow(exp.toDouble())
        val newDimension = Dimension(
            length = unit.dimension.length * exp,
            time = unit.dimension.time * exp,
            mass = unit.dimension.mass * exp
        )
        return Quantity(
            newBaseValue,
            Unit("${unit.name}^$exp", 1.0, newDimension)
        )
    }

    // ---------------- CONVERSION ----------------
    fun to(targetName: String): Quantity {
        val target = UnitRegistry.getOrNull(targetName)
        if (target == null) {
            val expr = UnitExprParser.parse(targetName)
            return UnitConverter.convert(this, expr)
        }

        require(unit.dimension == target.dimension) {
            "Dimension mismatch: cannot convert ${unit.name} to $targetName"
        }
        val convertedValue = baseValue / target.factor
        return Quantity(convertedValue, target)
    }

    // ---------------- UTILITY ----------------
    override fun toString(): String = "${value} ${unit.name}"
}