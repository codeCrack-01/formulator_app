package units.format

import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry
import kotlin.math.pow

object UnitConverter {

    fun convert(q: Quantity, targetExpr: UnitExpr): Quantity {
        var targetFactor = 1.0
        var targetDimension = Dimension()

        for ((name, power) in targetExpr.units) {
            val unit = UnitRegistry.get(name)

            targetFactor *= unit.factor.pow(power)
            targetDimension += Dimension(
                length = unit.dimension.length * power,
                time = unit.dimension.time * power,
                mass = unit.dimension.mass * power
            )
        }

        require(q.unit.dimension == targetDimension) {
            "Dimension mismatch in conversion"
        }

        val newValue = q.value / targetFactor

        return Quantity(newValue, Unit("", targetFactor, targetDimension))
    }
}