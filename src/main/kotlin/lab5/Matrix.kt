package lab5

import kotlin.math.cos
import kotlin.math.sin

data class Matrix(
    val matrix: List<List<Double>>
) {
    operator fun get(row: Int, col: Int): Double {
        return matrix[row][col]
    }

    operator fun times(other: Matrix): Matrix {
        val result = mutableListOf<List<Double>>()
        for (r in this.matrix.indices) {
            val row = mutableListOf<Double>()
            for (c in other.matrix[0].indices) {
                var sum = 0.0
                for (k in this.matrix[0].indices) {
                    sum += this[r, k] * other[k, c]
                }
                row.add(sum)
            }
            result.add(row)
        }
        return Matrix(result)
    }

    companion object {
        fun projectionToXY(z: Double = 0.0): Matrix {
            return Matrix(
                listOf(
                    listOf(1.0, 0.0, 0.0, 0.0),
                    listOf(0.0, 1.0, 0.0, 0.0),
                    listOf(0.0, 0.0, 0.0, 0.0),
                    listOf(0.0, 0.0, z, 1.0)
                )
            )
        }

        fun rotateByVector(vec: Vector, grad: Double): Matrix {
            val len = vec.length()

            val ux = vec.x / len
            val uy = vec.y / len
            val uz = vec.z / len

            val rad = grad * Math.PI / 180.0
            val c = cos(rad)
            val s = sin(rad)

            return Matrix(
                listOf(
                    listOf(c + (1.0 - c) * ux * ux, s * uz + (1.0 - c) * ux * uy, -s * uy + (1.0 - c) * ux * uz, 0.0),
                    listOf(-s * uz + (1.0 - c) * ux * uy, c + (1.0 - c) * uy * uy, s * ux + (1.0 - c) * uy * uz, 0.0),
                    listOf(s * uy + (1.0 - c) * ux * uz, -s * ux + (1.0 - c) * uy * uz, c + (1.0 - c) * uz * uz, 0.0),
                    listOf(0.0, 0.0, 0.0, 1.0)
                )
            )
        }

        fun onePointPerspective(k: Double): Matrix {
            return Matrix(
                listOf(
                    listOf(1.0, 0.0, 0.0, 0.0),
                    listOf(0.0, 1.0, 0.0, 0.0),
                    listOf(0.0, 0.0, 0.0, -1.0 / k),
                    listOf(0.0, 0.0, 0.0, 1.0)
                )
            )
        }

        fun translate(dx: Double, dy: Double, dz: Double): Matrix {
            return Matrix(
                listOf(
                    listOf(1.0, 0.0, 0.0, 0.0),
                    listOf(0.0, 1.0, 0.0, 0.0),
                    listOf(0.0, 0.0, 1.0, 0.0),
                    listOf(dx, dy, dz, 1.0)
                )
            )
        }
    }
}
