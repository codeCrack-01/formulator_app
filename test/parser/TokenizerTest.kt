package parser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TokenizerTest {
    @Test
    fun `should handle all token types`() {
        val input = "2.5 + var * (1 / 2) ^ 3"
        val tokens = Tokenizer(input).tokenize()

        val expected = listOf(
            Token.Number(2.5), Token.Plus, Token.Variable("var"), Token.Mul,
            Token.LParen, Token.Number(1.0), Token.Div, Token.Number(2.0), Token.RParen,
            Token.Pow, Token.Number(3.0)
        )
        Assertions.assertEquals(expected, tokens)
    }

    @Test
    fun `should ignore multiple whitespaces`() {
        val tokens = Tokenizer("L    * W").tokenize()
        Assertions.assertEquals(3, tokens.size)

        val expected = listOf(Token.Variable("L"), Token.Mul, Token.Variable("W"))
        Assertions.assertEquals(expected, tokens)
    }

    @Test
    fun `should throw error on unknown characters`() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            Tokenizer("2 $ 2").tokenize()
        }
    }

    @Test
    fun `should handle scientific notation`() {
        val tokens = Tokenizer("1.23e4 + 5").tokenize()
        val expected = listOf(Token.Number(12300.0), Token.Plus, Token.Number(5.0))
        Assertions.assertEquals(expected, tokens)
    }

    @Test
    fun `should handle multi-character variable names`() {
        val tokens = Tokenizer("var123 + L_W").tokenize()
        val expected = listOf(Token.Variable("var123"), Token.Plus, Token.Variable("L_W"))
        Assertions.assertEquals(expected, tokens)
    }

    @Test
    fun `should ignore leading and trailing whitespace`() {
        val tokens = Tokenizer("   2 + 3   ").tokenize()
        val expected = listOf(Token.Number(2.0), Token.Plus, Token.Number(3.0))
        Assertions.assertEquals(expected, tokens)
    }

    @Test
    fun `should handle nested parentheses`() {
        val tokens = Tokenizer("((a+b)*c)").tokenize()
        val expected = listOf(
            Token.LParen, Token.LParen, Token.Variable("a"), Token.Plus, Token.Variable("b"),
            Token.RParen, Token.Mul, Token.Variable("c"), Token.RParen
        )
        Assertions.assertEquals(expected, tokens)
    }
}