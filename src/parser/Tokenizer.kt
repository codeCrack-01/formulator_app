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
                    // REMOVED: The consecutive operator check.
                    // This allows "--5" to be tokenized correctly.
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

                c == '(' -> { tokens.add(Token.LParen); pos++ }
                c == ')' -> { tokens.add(Token.RParen); pos++ }

                else -> error("Unexpected character: $c")
            }
        }

        return tokens
    }

    private fun readNumber(): Token.Number {
        val start = pos
        var decimalCount = 0

        while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
            if (input[pos] == '.') {
                decimalCount++
                if (decimalCount > 1) error("Invalid number format: multiple decimals at $pos")
            }
            pos++
        }

        if (pos < input.length && (input[pos] == 'e' || input[pos] == 'E')) {
            pos++
            if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
            if (pos >= input.length || !input[pos].isDigit()) error("Invalid scientific notation")
            while (pos < input.length && input[pos].isDigit()) pos++
        }

        return Token.Number(input.substring(start, pos).toDouble())
    }

    private fun readVariable(): Token.Variable {
        val start = pos
        while (pos < input.length && (input[pos].isLetter() || input[pos].isDigit() || input[pos] == '_')) {
            pos++
        }
        return Token.Variable(input.substring(start, pos))
    }
}