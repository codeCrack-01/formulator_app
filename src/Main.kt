fun main() {
    val cm = Unit("cm", 0.01, Dimension(length = 1))
    val m = Unit("m", 1.0, Dimension(length = 1))

    val q1 = Quantity(100.0, cm)
    val q2 = Quantity(1.0, m)

    val result = q1 * q2
    println(result) // Expect: 2.0 m
}