package parser

sealed class Token {
    data class Number(val value: Double) : Token()
    data class Variable(val name: String) : Token()

    object Plus : Token()
    object Minus : Token()
    object Mul : Token()
    object Div : Token()
    object Pow : Token()
    object Dot : Token()

    object LParen : Token()
    object RParen : Token()

    val isOperator: Boolean
        get() = this is Plus ||
                this is Minus ||
                this is Mul ||
                this is Div ||
                this is Pow ||
                this is Dot
}