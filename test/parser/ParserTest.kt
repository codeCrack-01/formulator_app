package parser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun `should respect standard BODMAS precedence`() {
        val tokens = Tokenizer("2 + 3 * 4").tokenize()
        val ast = Parser(tokens).parse()

        // Root should be the ADD
        Assertions.assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.ADD, root.op)

        // Left = 2
        Assertions.assertTrue(root.left is Expr.Number)
        Assertions.assertEquals(2.0, (root.left as Expr.Number).value)

        // Right = 3 * 4
        Assertions.assertTrue(root.right is Expr.Binary)
        val right = root.right as Expr.Binary
        Assertions.assertEquals(Operator.MUL, right.op)
        Assertions.assertEquals(3.0, (right.left as Expr.Number).value)
        Assertions.assertEquals(4.0, (right.right as Expr.Number).value)
    }

    @Test
    fun `should handle nested parentheses`() {
        val tokens = Tokenizer("((2 + 3) * (4 - 1))").tokenize()
        val ast = Parser(tokens).parse()

        Assertions.assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.MUL, root.op)
        Assertions.assertTrue(root.left is Expr.Binary)  // (2 + 3)
        Assertions.assertTrue(root.right is Expr.Binary) // (4 - 1)
    }

    @Test
    fun `should handle power right associativity`() {
        val tokens = Tokenizer("2 ^ 3 ^ 2").tokenize()
        val ast = Parser(tokens).parse()

        Assertions.assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.POW, root.op)

        // Right subtree = 3^2
        Assertions.assertTrue(root.right is Expr.Binary)
        val right = root.right as Expr.Binary
        Assertions.assertEquals(Operator.POW, right.op)
        Assertions.assertEquals(3.0, (right.left as Expr.Number).value)
        Assertions.assertEquals(2.0, (right.right as Expr.Number).value)
    }

    @Test
    fun `should handle unary minus correctly`() {
        val tokens = Tokenizer("-5 + 3").tokenize()
        val ast = Parser(tokens).parse()

        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.ADD, root.op)

        // Left = -5 → Binary(Number(0), SUB, Number(5))
        Assertions.assertTrue(root.left is Expr.Binary)
        val left = root.left as Expr.Binary
        Assertions.assertEquals(Operator.SUB, left.op)
        Assertions.assertEquals(0.0, (left.left as Expr.Number).value)
        Assertions.assertEquals(5.0, (left.right as Expr.Number).value)

        // Right = 3
        Assertions.assertTrue(root.right is Expr.Number)
        Assertions.assertEquals(3.0, (root.right as Expr.Number).value)
    }

    @Test
    fun `should handle nested unary operators`() {
        val tokens = Tokenizer("--5").tokenize()
        val ast = Parser(tokens).parse()

        // --5 → 0 - (0 - 5)
        Assertions.assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.SUB, root.op)
        val inner = root.right as Expr.Binary
        Assertions.assertEquals(Operator.SUB, inner.op)
        Assertions.assertEquals(5.0, (inner.right as Expr.Number).value)
    }

    @Test
    fun `should parse single variable`() {
        val tokens = Tokenizer("x").tokenize()
        val ast = Parser(tokens).parse()
        Assertions.assertTrue(ast is Expr.Variable)
        Assertions.assertEquals("x", (ast as Expr.Variable).name)
    }

    @Test
    fun `should parse single number`() {
        val tokens = Tokenizer("42").tokenize()
        val ast = Parser(tokens).parse()
        Assertions.assertTrue(ast is Expr.Number)
        Assertions.assertEquals(42.0, (ast as Expr.Number).value)
    }

    @Test
    fun `should handle complex mixed expression`() {
        val tokens = Tokenizer("2 + 3 * (4 + 5) ^ 2 - 7 / x").tokenize()
        val ast = Parser(tokens).parse()
        Assertions.assertTrue(ast is Expr.Binary)
        // Root = subtraction
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.SUB, root.op)
    }

    @Test
    fun `should throw on unmatched parentheses`() {
        val tokens = Tokenizer("(2 + 3").tokenize()
        Assertions.assertThrows(IllegalStateException::class.java) { Parser(tokens).parse() }
    }

    @Test
    fun `should throw on empty input`() {
        val tokens = Tokenizer("").tokenize()
        Assertions.assertThrows(IllegalStateException::class.java) { Parser(tokens).parse() }
    }

    @Test
    fun `should handle parentheses with unary inside`() {
        val tokens = Tokenizer("-(2 + 3)").tokenize()
        val ast = Parser(tokens).parse()

        // -(2+3) → 0 - (2+3)
        Assertions.assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        Assertions.assertEquals(Operator.SUB, root.op)
        Assertions.assertTrue(root.right is Expr.Binary)
        val inner = root.right as Expr.Binary
        Assertions.assertEquals(Operator.ADD, inner.op)
    }
}