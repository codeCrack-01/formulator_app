package dimensions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SolverTest {

    @Test
    fun `single unknown is solved correctly`() {

        val x = DimVar("x")

        // x + [L] = [M]
        val constraint = Constraint(
            DimAdd(DimUnknown(x), DimConst(dim(length = 1))),
            DimConst(dim(mass = 1))
        )

        val result = Solver.solve(listOf(constraint))

        val solved = result[x]

        Assertions.assertNotNull(solved)

        // expected: x = M - L
        val expected = dim(mass = 1, length = -1)

        Assertions.assertArrayEquals(expected.exponents, solved!!.exponents)
    }

    private fun dim(
        length: Int = 0,
        mass: Int = 0,
        time: Int = 0,
        money: Int = 0
    ): DimensionVector {
        return DimensionVector(intArrayOf(length, mass, time, money))
    }
}