package api

import units.Quantity

sealed class FormulaResult {
    data class Success(val quantity: Quantity) : FormulaResult()
    data class Error(val error: FormulaError) : FormulaResult()
}