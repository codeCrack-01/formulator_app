package units

import dimensions.DimensionVector

object UnitParser {

    fun parse(input: String): DimensionVector {
        val cleaned = input.replace(" ", "")

        if (cleaned.isEmpty()) {
            return DimensionVector() // unitless
        }

        return parseProduct(cleaned)
    }

    private fun parseProduct(expr: String): DimensionVector {
        var i = 0
        var sign = 1
        var result = DimensionVector()

        while (i < expr.length) {
            when (expr[i]) {

                '*' -> {
                    sign = 1
                    i++
                }

                '/' -> {
                    sign = -1
                    i++
                }

                else -> {
                    val (unit, nextIndex) = readUnit(expr, i)
                    val dim = UnitRegistry.parse(unit).dimension.toVector()

                    result = if (sign == 1) {
                        result + dim
                    } else {
                        result - dim
                    }

                    i = nextIndex
                }
            }
        }

        return result
    }

    private fun readUnit(expr: String, start: Int): Pair<String, Int> {
        val units = UnitRegistry.allUnitNamesSortedByLength()

        for (u in units) {
            if (expr.startsWith(u, start)) {
                return u to (start + u.length)
            }
        }

        error("Unknown unit in expression at: ${expr.substring(start)}")
    }
}