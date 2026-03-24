object UnitRegistry {
    private val units = mutableMapOf<String, Unit>()

    fun register(unit: Unit) {
        units[unit.name] = unit
    }

    fun get(name: String): Unit {
        return units[name]
            ?: error("Unit $name not found")
    }
}