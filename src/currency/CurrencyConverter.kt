package currency

import units.Quantity
import units.UnitRegistry

object CurrencyConverter {

    fun convert(
        q: Quantity,
        targetCurrency: String,
        exchange: ExchangeRateProvider
    ): Quantity {

        val sourceCurrency = q.unit.dimension.currency.keys.firstOrNull()
            ?: throw IllegalArgumentException("No currency in quantity")

        // Same currency → no-op
        if (sourceCurrency == targetCurrency) return q

        // Validate target exists
        val targetUnit = UnitRegistry.getOrNull(targetCurrency)
            ?: throw IllegalArgumentException("Target currency not registered: $targetCurrency")

        if (targetUnit.dimension.currency.isEmpty()) {
            throw IllegalArgumentException("$targetCurrency is not a currency")
        }

        // Get rate
        val rate = exchange.rate(sourceCurrency, targetCurrency)

        val newValue = q.value * rate

        return Quantity(newValue, targetUnit)
    }
}