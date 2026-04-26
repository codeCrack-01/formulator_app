package api

import currency.CurrencyConverter
import currency.ExchangeRateProvider
import parser.Expr
import units.Dimension
import units.Quantity

class FormulaService(
    private val engine: FormulaEngine,
    private val exchangeRateProvider: ExchangeRateProvider
) {

    fun evaluate(expr: Expr): FormulaResult {
        return try {
            FormulaResult.Success(engine.evaluate(expr, null))
        } catch (e: IllegalArgumentException) {
            FormulaResult.Error(
                FormulaError.InvalidOperation(e.message ?: "Invalid operation")
            )
        } catch (e: IllegalStateException) {

            val msg = e.message ?: ""

            val error = when {
                msg.contains("No dimension inference") ->
                    FormulaError.DimensionMismatch(msg)

                msg.contains("Dimension mismatch") ->
                    FormulaError.DimensionMismatch(msg)

                msg.contains("variable") ->
                    FormulaError.MissingVariable(msg)

                else ->
                    FormulaError.InvalidOperation(msg)
            }

            FormulaResult.Error(error)
        }
    }

    fun evaluateWithExpected(
        expr: Expr,
        expected: Dimension
    ): FormulaResult {
        return try {
            FormulaResult.Success(
                engine.evaluate(expr, expected)
            )
        } catch (e: IllegalArgumentException) {
            FormulaResult.Error(
                FormulaError.InvalidOperation(e.message ?: "Invalid operation")
            )
        }
        catch (e: IllegalStateException) {
            FormulaResult.Error(
                FormulaError.MissingVariable(e.message ?: "Missing state")
            )
        }
        catch (e: Exception) {
            FormulaResult.Error(
                FormulaError.InternalError(e.message ?: "Unknown error")
            )
        }
    }

    fun convert(
        q: Quantity,
        targetCurrency: String
    ): FormulaResult {
        return try {
            FormulaResult.Success(
                CurrencyConverter.convert(
                    q,
                    targetCurrency,
                    exchangeRateProvider
                )
            )
        } catch (e: IllegalArgumentException) {
            FormulaResult.Error(
                FormulaError.InvalidOperation(e.message ?: "Invalid operation")
            )
        }
        catch (e: IllegalStateException) {
            FormulaResult.Error(
                FormulaError.MissingVariable(e.message ?: "Missing state")
            )
        }
        catch (e: Exception) {
            FormulaResult.Error(
                FormulaError.InternalError(e.message ?: "Unknown error")
            )
        }
    }
}