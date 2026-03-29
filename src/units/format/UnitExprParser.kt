package units.format

object UnitExprParser {

    fun parse(input: String): UnitExpr {
        val units = mutableMapOf<String, Int>()

        val parts = input.split("/")

        // numerator
        parsePart(parts[0], units, +1)

        // denominator
        if (parts.size > 1) {
            parsePart(parts[1], units, -1)
        }

        return UnitExpr(units)
    }

    private fun parsePart(part: String, units: MutableMap<String, Int>, sign: Int) {
        val tokens = part.split("*")

        for (token in tokens) {
            if (token.isBlank()) continue

            val (name, power) = if ("^" in token) {
                val split = token.split("^")
                split[0] to split[1].toInt()
            } else {
                token to 1
            }

            units[name] = units.getOrDefault(name, 0) + sign * power
        }
    }
}