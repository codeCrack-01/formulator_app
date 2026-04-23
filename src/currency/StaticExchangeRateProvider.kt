package currency

class StaticExchangeRateProvider : ExchangeRateProvider {

    private val usdRates = mapOf(
        "USD" to 1.0,
        "PKR" to 280.0,
        "SAR" to 3.75,
        "INR" to 83.0,
        "AUD" to 1.5,
        "IRR" to 42000.0
    )

    override fun rate(from: String, to: String): Double {
        if (from == to) return 1.0

        val fromUsd = usdRates[from]
            ?: error("Unknown currency: $from")

        val toUsd = usdRates[to]
            ?: error("Unknown currency: $to")

        // USD pivot normalization
        return toUsd / fromUsd
    }
}