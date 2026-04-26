package api

sealed class FormulaError {
    data class InvalidOperation(val message: String) : FormulaError()
    data class MissingVariable(val name: String) : FormulaError()
    data class DimensionMismatch(val message: String) : FormulaError()
    data class UnknownCurrency(val message: String) : FormulaError()
    data class InternalError(val message: String) : FormulaError()
}