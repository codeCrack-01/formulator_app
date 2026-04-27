package parser

class Parser(private val tokens: List<Token>) {

    private var pos = 0

    private inline fun <reified T : Token> consume(message: String): T {
        if (peek() is T) {
            return advance() as T
        }
        error(message)
    }

    private fun peek(): Token? {
        if (isAtEnd()) return null
        return tokens[pos]
    }

    private inline fun <reified T : Token> match(): Boolean {
        val current = peek()

        if (current is T) {
            advance()
            return true
        }

        return false
    }

    private fun advance(): Token {
        if (!isAtEnd()) pos++
        return previous()
    }

    private fun previous(): Token {
        return tokens[pos - 1]
    }

    private fun isAtEnd(): Boolean {
        return pos >= tokens.size
    }

    fun parse(): Expr {
        return parseExpression()
    }

    private fun parseExpression(): Expr {
        var expr = parseTerm()

        while (true) {
            when {
                match<Token.Plus>() -> {
                    val right = parseTerm()
                    expr = Expr.Binary(expr, Operator.ADD, right)
                }

                match<Token.Minus>() -> {
                    val right = parseTerm()
                    expr = Expr.Binary(expr, Operator.SUB, right)
                }

                else -> return expr
            }
        }
    }

    private fun parseTerm(): Expr {
        var expr = parsePower()

        while (true) {
            when {
                match<Token.Mul>() || match<Token.Dot>() -> {
                    val right = parsePower()
                    expr = Expr.Binary(expr, Operator.MUL, right)
                }

                match<Token.Div>() -> {
                    val right = parsePower()
                    expr = Expr.Binary(expr, Operator.DIV, right)
                }

                else -> return expr
            }
        }
    }

    private fun parsePower(): Expr {
        var expr = parseUnary()

        if (match<Token.Pow>()) {
            val right = parsePower()
            expr = Expr.Binary(expr, Operator.POW, right)
        }

        return expr
    }

    private fun parseUnary(): Expr {
        if (match<Token.Minus>()) {
            val right = parseUnary()

            return Expr.Binary(
                Expr.Number(0.0, "unitless"),
                Operator.SUB,
                right
            )
        }

        return parsePrimary()
    }

    private fun parsePrimary(): Expr {
        if (isAtEnd()) {
            error("Unexpected end of input")
        }

        return when (val token = advance()) {
            is Token.Number -> {
                val value = token.value
                val next = peek()

                if (next is Token.Variable) {
                    advance()
                    Expr.Number(value, next.name)
                } else {
                    Expr.Number(value, "")
                }
            }

            is Token.Variable -> {
                Expr.Variable(token.name)
            }

            is Token.LParen -> {
                val expr = parseExpression()
                consume<Token.RParen>("Expected ')'")
                expr
            }

            else -> error("Unexpected token: $token")
        }
    }
}