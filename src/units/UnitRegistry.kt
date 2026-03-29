package units

object UnitRegistry {
    private val units = mutableMapOf<String, Unit>()

    fun register(unit: Unit) {
        units[unit.name] = unit
    }

    fun get(name: String): Unit {
        return units[name]
            ?: error("units.Unit $name not found")
    }

    fun getOrNull(name: String): Unit? = units[name]

    fun clear() {
        units.clear()
    }
}