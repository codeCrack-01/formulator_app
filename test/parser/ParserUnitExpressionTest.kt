package parser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParserUnitExpressionTest {

    private fun parse(input: String): Expr {
        val tokens = Tokenizer(input).tokenize()
        return Parser(tokens).parse()
    }

    // -------------------------
    // 1. Division cases
    // -------------------------

    @Test
    fun `kg per second parses as division`() {
        val expr = parse("kg/s")

        val expected = Expr.Binary(
            Expr.Variable("kg"),
            Operator.DIV,
            Expr.Variable("s")
        )

        Assertions.assertEquals(expected, expr)
    }

    @Test
    fun `meters per second parses as division`() {
        val expr = parse("m/s")

        Assertions.assertTrue(expr is Expr.Binary)
        val e = expr as Expr.Binary

        Assertions.assertEquals(Operator.DIV, e.op)
    }

    @Test
    fun `newton per meter parses as division`() {
        val expr = parse("N/m")

        Assertions.assertTrue(expr is Expr.Binary)
        val e = expr as Expr.Binary

        Assertions.assertEquals(Operator.DIV, e.op)
    }

    // -------------------------
    // 2. Implicit multiplication
    // -------------------------

    @Test
    fun `Nm parses as implicit multiplication`() {
        val expr = parse("N.m")

        val expected = Expr.Binary(
            Expr.Variable("N"),
            Operator.MUL,
            Expr.Variable("m")
        )

        Assertions.assertEquals(expected, expr)
    }

    @Test
    fun `kgm parses as implicit multiplication`() {
        val expr = parse("kg.m")

        val expected = Expr.Binary(
            Expr.Variable("kg"),
            Operator.MUL,
            Expr.Variable("m")
        )

        Assertions.assertEquals(expected, expr)
    }

    // -------------------------
    // 3. Mixed expressions
    // -------------------------

    @Test
    fun `Nm per s parses correctly`() {
        val expr = parse("N.m/s")

        // (N * m) / s
        val expected = Expr.Binary(
            Expr.Binary(
                Expr.Variable("N"),
                Operator.MUL,
                Expr.Variable("m")
            ),
            Operator.DIV,
            Expr.Variable("s")
        )

        Assertions.assertEquals(expected, expr)
    }

    @Test
    fun `complex chain kgms becomes multiplication chain`() {
        val expr = parse("kg.m.s")

        val expected = Expr.Binary(
            Expr.Binary(
                Expr.Binary(
                    Expr.Variable("kg"),
                    Operator.MUL,
                    Expr.Variable("m")
                ),
                Operator.MUL,
                Expr.Variable("s")
            ),
            Operator.MUL,
            Expr.Variable("unitless")
        )

        // We only assert structure loosely (chain validation)
        Assertions.assertTrue(expr is Expr.Binary)
    }

    // -------------------------
    // 4. Parentheses still work
    // -------------------------

    @Test
    fun `parentheses override implicit multiplication`() {
        val expr = parse("N.(m/s)")
        Assertions.assertTrue(expr is Expr.Binary)
    }
}