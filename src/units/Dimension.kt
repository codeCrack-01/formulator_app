package units

import dimensions.BaseDim
import dimensions.DimensionVector

data class Dimension(
    val length: Int = 0,
    val time: Int = 0,
    val mass: Int = 0,
) {
    operator fun plus(other: Dimension) = Dimension(
        length + other.length,
        time + other.time,
        mass + other.mass
    )

    operator fun minus(other: Dimension) = Dimension(
        length - other.length,
        time - other.time,
        mass - other.mass
    )

    fun isZero(): Boolean {
        return length == 0 && time == 0 && mass == 0
    }

    fun toVector(): DimensionVector {
        return DimensionVector(
            intArrayOf(
                this.length,  // index 0 → m
                this.mass,    // index 1 → kg
                this.time,    // index 2 → s
                0             // index 3 → money (not used yet)
            )
        )
    }

    val isDimensionless: Boolean
        get() = length == 0 && time == 0 && mass == 0
}