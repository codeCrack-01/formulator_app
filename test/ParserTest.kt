import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import parser.Expr
import parser.Operator
import parser.Parser
import parser.Tokenizer

class ParserTest {

    @Test
    fun `should respect standard BODMAS precedence`() {
        val tokens = Tokenizer("2 + 3 * 4").tokenize()
        val ast = Parser(tokens).parse()

        // Root should be the ADD
        assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        assertEquals(Operator.ADD, root.op)

        // Left = 2
        assertTrue(root.left is Expr.Number)
        assertEquals(2.0, (root.left as Expr.Number).value)

        // Right = 3 * 4
        assertTrue(root.right is Expr.Binary)
        val right = root.right as Expr.Binary
        assertEquals(Operator.MUL, right.op)
        assertEquals(3.0, (right.left as Expr.Number).value)
        assertEquals(4.0, (right.right as Expr.Number).value)
    }

    @Test
    fun `should handle nested parentheses`() {
        val tokens = Tokenizer("((2 + 3) * (4 - 1))").tokenize()
        val ast = Parser(tokens).parse()

        assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        assertEquals(Operator.MUL, root.op)
        assertTrue(root.left is Expr.Binary)  // (2 + 3)
        assertTrue(root.right is Expr.Binary) // (4 - 1)
    }

    @Test
    fun `should handle power right associativity`() {
        val tokens = Tokenizer("2 ^ 3 ^ 2").tokenize()
        val ast = Parser(tokens).parse()

        assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        assertEquals(Operator.POW, root.op)

        // Right subtree = 3^2
        assertTrue(root.right is Expr.Binary)
        val right = root.right as Expr.Binary
        assertEquals(Operator.POW, right.op)
        assertEquals(3.0, (right.left as Expr.Number).value)
        assertEquals(2.0, (right.right as Expr.Number).value)
    }

    @Test
    fun `should handle unary minus correctly`() {
        val tokens = Tokenizer("-5 + 3").tokenize()
        val ast = Parser(tokens).parse()

        val root = ast as Expr.Binary
        assertEquals(Operator.ADD, root.op)

        // Left = -5 → Binary(Number(0), SUB, Number(5))
        assertTrue(root.left is Expr.Binary)
        val left = root.left as Expr.Binary
        assertEquals(Operator.SUB, left.op)
        assertEquals(0.0, (left.left as Expr.Number).value)
        assertEquals(5.0, (left.right as Expr.Number).value)

        // Right = 3
        assertTrue(root.right is Expr.Number)
        assertEquals(3.0, (root.right as Expr.Number).value)
    }

    @Test
    fun `should handle nested unary operators`() {
        val tokens = Tokenizer("--5").tokenize()
        val ast = Parser(tokens).parse()

        // --5 → 0 - (0 - 5)
        assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        assertEquals(Operator.SUB, root.op)
        val inner = root.right as Expr.Binary
        assertEquals(Operator.SUB, inner.op)
        assertEquals(5.0, (inner.right as Expr.Number).value)
    }

    @Test
    fun `should parse single variable`() {
        val tokens = Tokenizer("x").tokenize()
        val ast = Parser(tokens).parse()
        assertTrue(ast is Expr.Variable)
        assertEquals("x", (ast as Expr.Variable).name)
    }

    @Test
    fun `should parse single number`() {
        val tokens = Tokenizer("42").tokenize()
        val ast = Parser(tokens).parse()
        assertTrue(ast is Expr.Number)
        assertEquals(42.0, (ast as Expr.Number).value)
    }

    @Test
    fun `should handle complex mixed expression`() {
        val tokens = Tokenizer("2 + 3 * (4 + 5) ^ 2 - 7 / x").tokenize()
        val ast = Parser(tokens).parse()
        assertTrue(ast is Expr.Binary)
        // Root = subtraction
        val root = ast as Expr.Binary
        assertEquals(Operator.SUB, root.op)
    }

    @Test
    fun `should throw on unmatched parentheses`() {
        val tokens = Tokenizer("(2 + 3").tokenize()
        assertThrows(IllegalStateException::class.java) { Parser(tokens).parse() }
    }

    @Test
    fun `should throw on empty input`() {
        val tokens = Tokenizer("").tokenize()
        assertThrows(IllegalStateException::class.java) { Parser(tokens).parse() }
    }

    @Test
    fun `should handle parentheses with unary inside`() {
        val tokens = Tokenizer("-(2 + 3)").tokenize()
        val ast = Parser(tokens).parse()

        // -(2+3) → 0 - (2+3)
        assertTrue(ast is Expr.Binary)
        val root = ast as Expr.Binary
        assertEquals(Operator.SUB, root.op)
        assertTrue(root.right is Expr.Binary)
        val inner = root.right as Expr.Binary
        assertEquals(Operator.ADD, inner.op)
    }
}