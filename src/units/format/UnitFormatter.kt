package units.format

import units.Dimension
import units.Quantity
import units.Unit
import units.UnitRegistry

object UnitFormatter {

    // 🔹 MAIN ENTRY (STRING, backward compatible)
    fun format(q: Quantity, target: String? = null): String {
        val structured = formatStructured(q, target)
        return if (structured.unitName.isBlank())
            structured.value.toString()
        else
            "${structured.value} ${structured.unitName}"
    }

    // 🔹 NEW: STRUCTURED OUTPUT (what you needed)
    fun formatStructured(q: Quantity, target: String? = null): FormattedQuantity {

        val finalQuantity = if (target != null) {
            val expr = UnitExprParser.parse(target)
            UnitConverter.convert(q, expr)
        } else {
            autoSelect(q) // 🔥 smart fallback
        }

        val dim = finalQuantity.unit.dimension

        if (dim.isZero()) {
            return FormattedQuantity(
                finalQuantity.value,
                "",
                dim
            )
        }

        val unitStr = target ?: finalQuantity.unit.name.ifBlank {formatFromDimension(dim)}

        return FormattedQuantity(
            finalQuantity.value,
            unitStr,
            dim
        )
    }

    // 🔥 AUTO UNIT SELECTION (basic but extensible)
    private fun autoSelect(q: Quantity): Quantity {
        val dim = q.unit.dimension

        return when {
            // Speed → km/hr
            dim.length == 1 && dim.time == -1 && dim.mass == 0 -> {
                val base = q // already in base (m/s)

                // Convert manually: m/s → km/hr
                val convertedValue = base.value * 3.6

                Quantity(
                    convertedValue,
                    Unit(
                        "km/hr",
                        UnitRegistry.get("km").factor / UnitRegistry.get("hr").factor,
                        dim
                    )
                )
            }

            // Area → m² (keep base)
            dim.length == 2 -> q

            // Volume → m³
            dim.length == 3 -> q

            // Length → autoscale (m → km if large)
            dim.length == 1 && dim.time == 0 -> {
                when {
                    q.value >= 1000 -> Quantity(q.value / 1000.0, q.unit.copy(name = "km", factor = 1000.0))
                    q.value < 1 -> Quantity(q.value * 100.0, q.unit.copy(name = "cm", factor = 0.01))
                    else -> q
                }
            }

            else -> q
        }
    }

    // 🔹 DIMENSION → STRING (fallback)
    private fun formatFromDimension(dim: Dimension): String {
        val num = mutableListOf<String>()
        val den = mutableListOf<String>()

        fun add(symbol: String, power: Int, target: MutableList<String>) {
            if (power == 1) target.add(symbol)
            else target.add("$symbol^$power")
        }

        if (dim.length > 0) add("m", dim.length, num)
        if (dim.length < 0) add("m", -dim.length, den)

        if (dim.time > 0) add("s", dim.time, num)
        if (dim.time < 0) add("s", -dim.time, den)

        if (dim.mass > 0) add("kg", dim.mass, num)
        if (dim.mass < 0) add("kg", -dim.mass, den)

        val numStr = if (num.isEmpty()) "1" else num.joinToString("*")
        val denStr = den.joinToString("*")

        return if (den.isEmpty()) numStr else "$numStr/$denStr"
    }
}