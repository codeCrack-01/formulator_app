package units.format

import units.Dimension

data class FormattedQuantity(
    val value: Double,
    val unitName: String,
    val dimension: Dimension
)