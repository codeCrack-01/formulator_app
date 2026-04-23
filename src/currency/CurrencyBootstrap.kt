import units.Dimension
import units.Unit
import units.UnitRegistry

object CurrencyBootstrap {

    val currencies = mapOf(
        "USD" to 1.0,
        "PKR" to 280.0,
        "INR" to 83.0,
        "SAR" to 3.75,
        "AUD" to 1.5,
        "IRR" to 42000.0
    )

    fun registerAll() {
        val dim = Dimension()

        currencies.forEach { (code, factor) ->
            UnitRegistry.register(
                Unit(code, factor, Dimension(currency = mapOf(code to 1)))
            )
        }
    }
}