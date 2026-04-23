package currency

interface ExchangeRateProvider {
    fun rate(from: String, to: String): Double
}