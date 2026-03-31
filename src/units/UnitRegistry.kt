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

    private fun parseProduct(part: String): Unit {
        val tokens = part.split("*")

        var result = get("unitless")

        for (token in tokens) {
            if (token.contains("^")) {
                val (base, expStr) = token.split("^")
                val exp = expStr.toInt()
                result *= get(base).pow(exp)
            } else {
                result *= get(token)
            }
        }

        return result
    }

    fun parse(unitStr: String): Unit {
        if (unitStr == "unitless") return get("unitless")

        val parts = unitStr.split("/")

        val numerator = parts[0]
        val denominator = if (parts.size > 1) parts[1] else null

        var result = parseProduct(numerator)

        if (denominator != null) {
            val denomUnit = parseProduct(denominator)
            result /= denomUnit
        }

        return result
    }
}