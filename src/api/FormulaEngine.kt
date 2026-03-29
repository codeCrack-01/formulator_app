package api

import evaluator.Evaluator
import parser.Parser
import parser.Tokenizer
import units.Quantity
import units.format.UnitFormatter

class FormulaEngine {

    fun evaluate(
        expression: String,
        variables: Map<String, Quantity>
    ): Quantity {

        val tokens = Tokenizer(expression).tokenize()
        val ast = Parser(tokens).parse()

        return Evaluator(variables).eval(ast)
    }

    fun evaluateFormatted(
        expression: String,
        variables: Map<String, Quantity>,
        targetUnit: String? = null
    ): String {
        val result = evaluate(expression, variables)
        return UnitFormatter.format(result, targetUnit)
    }
}