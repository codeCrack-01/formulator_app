package parser

sealed class Expr {
    data class Number(val value: Double, val unit: String) : Expr()
    data class Variable(val name: String) : Expr()

    data class Binary(
        val left: Expr,
        val op: Operator,
        val right: Expr
    ) : Expr()
}

enum class Operator {
    ADD, SUB, MUL, DIV, POW
}