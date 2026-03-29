import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry

fun initUnits() {
    val length = Dimension(length = 1)

    UnitRegistry.register(Unit("m", 1.0, length))
    UnitRegistry.register(Unit("cm", 0.01, length))
    UnitRegistry.register(Unit("km", 1000.0, length))

    val time = Dimension(time = 1)

    UnitRegistry.register(Unit("s", 1.0, time))
    UnitRegistry.register(Unit("min", 60.0, time))
    UnitRegistry.register(Unit("hr", 3600.0, time))

    val mass = Dimension(mass = 1)

    UnitRegistry.register(Unit("g", 0.001, mass))
    UnitRegistry.register(Unit("kg", 1.0, mass))
    UnitRegistry.register(Unit("tonne", 1000.0, mass))
    UnitRegistry.register(Unit("ton", 40.0, mass)) // The pk_ton
}

fun main() {
    initUnits()

    val q1 = Quantity(1.0, UnitRegistry.get("km"))
    val q2 = Quantity(2.0, UnitRegistry.get("m"))

    val q3 = Quantity(120.0, UnitRegistry.get("cm"))

    val result = q1 + q2 + q3
    println(result) // Expect: 2.0 m
}