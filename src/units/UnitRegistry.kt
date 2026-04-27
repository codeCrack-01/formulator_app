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

    fun allUnitNamesSortedByLength(): List<String> {
        return units.keys.sortedByDescending { it.length }
    }

    fun snapshotUnitNames(): List<String> {
        return units.keys.sortedByDescending { it.length }
    }

    private fun parseProduct(part: String): Unit {
        val tokens = part.split("*")

        var result = get("unitless")

        for (token in tokens) {

            if (token.contains("^")) {
                val (base, expStr) = token.split("^")
                val exp = expStr.toInt()

                val unit = get(base)

                // ❗ BLOCK currency exponentiation HERE
                if (unit.dimension.currency.isNotEmpty()) {
                    throw IllegalArgumentException("Currency units cannot have exponents")
                }

                result *= unit.pow(exp)
            } else {

                if (isCurrency(token)) {
                    val dim = Dimension(currency = mapOf(token to 1))
                    result *= Unit(token, 1.0, dim)
                } else {
                    result *= get(token)
                }
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

    private fun isCurrency(symbol: String): Boolean {
        return symbol in setOf("USD", "PKR", "EUR")
    }
}