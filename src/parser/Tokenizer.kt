package parser

class Tokenizer(private val input: String) {

    private var pos = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        while (pos < input.length) {
            val c = input[pos]

            when {
                c.isWhitespace() -> pos++

                c.isDigit() -> tokens.add(readNumber())

                c.isLetter() || c == '_' -> tokens.add(readVariable())

                c in "+-*/^" -> {
                    val token = when (c) {
                        '+' -> Token.Plus
                        '-' -> Token.Minus
                        '*' -> Token.Mul
                        '/' -> Token.Div
                        '^' -> Token.Pow
                        else -> error("Unreachable")
                    }

                    tokens.add(token)
                    pos++
                }

                c == '.' -> {
                    tokens.add(Token.Dot)
                    pos++
                }

                c == '(' -> {
                    tokens.add(Token.LParen)
                    pos++
                }

                c == ')' -> {
                    tokens.add(Token.RParen)
                    pos++
                }

                else -> error("Unexpected character: $c")
            }
        }

        return tokens
    }

    private fun readNumber(): Token.Number {
        val start = pos

        // integer part
        while (pos < input.length && input[pos].isDigit()) {
            pos++
        }

        // decimal part only if '.' is followed by digit
        if (
            pos + 1 < input.length &&
            input[pos] == '.' &&
            input[pos + 1].isDigit()
        ) {
            pos++ // consume '.'

            while (pos < input.length && input[pos].isDigit()) {
                pos++
            }
        }

        // scientific notation
        if (
            pos < input.length &&
            (input[pos] == 'e' || input[pos] == 'E')
        ) {
            pos++

            if (
                pos < input.length &&
                (input[pos] == '+' || input[pos] == '-')
            ) {
                pos++
            }

            if (pos >= input.length || !input[pos].isDigit()) {
                error("Invalid scientific notation")
            }

            while (pos < input.length && input[pos].isDigit()) {
                pos++
            }
        }

        return Token.Number(
            input.substring(start, pos).toDouble()
        )
    }

    private fun readVariable(): Token {
        val start = pos

        while (
            pos < input.length &&
            (input[pos].isLetterOrDigit() || input[pos] == '_')
        ) {
            pos++
        }

        return Token.Variable(
            input.substring(start, pos)
        )
    }
}