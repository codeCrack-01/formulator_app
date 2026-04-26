package api

import parser.Parser
import parser.Expr
import parser.Tokenizer
import units.Dimension
import units.Quantity

class FormulaApi(
    private val service: FormulaService
) {

    /**
     * Core entry point for UI.
     * Accepts raw expression string.
     */
    fun evaluate(input: String): FormulaResult {
        return try {
            val tokens = Tokenizer(input).tokenize()
            val expr = Parser(tokens).parse()

            service.evaluate(expr)
        } catch (e: Exception) {
            FormulaResult.Error(
                FormulaError.InvalidOperation(
                    e.message ?: "Parse or evaluation failed"
                )
            )
        }
    }

    /**
     * Evaluate with expected dimension constraint.
     */
    fun evaluate(input: String, expected: Dimension): FormulaResult {
        return try {
            val tokens = Tokenizer(input).tokenize()
            val expr = Parser(tokens).parse()

            service.evaluateWithExpected(expr, expected)
        } catch (e: Exception) {
            FormulaResult.Error(
                FormulaError.DimensionMismatch(
                    e.message ?: "Failed with expected dimension"
                )
            )
        }
    }

    /**
     * Optional: currency conversion API for UI layer
     */
    fun convert(
        quantity: Quantity,
        targetCurrency: String
    ): FormulaResult {
        return service.convert(quantity, targetCurrency)
    }
}