data class Unit(
    val name: String,
    val factor: Double,     // to base unit
    val dimension: Dimension
)

val meter = Unit("m", 1.0, Dimension(length = 1))
val centimeter = Unit("cm", 0.01, Dimension(length = 1))
val second = Unit("s", 1.0, Dimension(time = 1))