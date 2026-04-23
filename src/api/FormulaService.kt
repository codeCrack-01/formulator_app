package api

import currency.CurrencyConverter
import currency.ExchangeRateProvider
import units.Quantity

class FormulaService(
    private val engine: FormulaEngine,
    private val exchange: ExchangeRateProvider
) {

    fun evaluate(expr: parser.Expr): Quantity {
        return engine.evaluate(expr, null)
    }

    fun evaluateWithExpected(expr: parser.Expr, expected: units.Dimension): Quantity {
        return engine.evaluate(expr, expected)
    }

    fun convert(q: Quantity, targetCurrency: String): Quantity {
        return CurrencyConverter.convert(q, targetCurrency, exchange)
    }
}