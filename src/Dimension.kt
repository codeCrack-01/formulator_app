data class Dimension(
    val length: Int = 0,
    val time: Int = 0,
    val mass: Int = 0
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
}