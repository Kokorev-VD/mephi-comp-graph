package lab5

import kotlin.math.sqrt

data class Vector(
    var x: Double,
    var y: Double,
    var z: Double
) {
    fun toMatrix(): Matrix {
        return Matrix(listOf(listOf(x, y, z, 1.0)))
    }

    operator fun times(m: Matrix): Vector {
        val mat = this.toMatrix() * m
        val w = mat[0, 3]

        this.x = mat[0, 0] / w
        this.y = mat[0, 1] / w
        this.z = mat[0, 2] / w

        return this
    }

    operator fun times(d: Double): Vector {
        this.x *= d
        this.y *= d
        this.z *= d
        return this
    }

    operator fun plus(d: Double): Vector {
        this.x += d
        this.y += d
        this.z += d
        return this
    }

    operator fun plus(other: Vector): Vector {
        this.x += other.x
        this.y += other.y
        this.z += other.z
        return this
    }

    operator fun minus(other: Vector): Vector {
        return Vector(
            this.x - other.x,
            this.y - other.y,
            this.z - other.z
        )
    }

    fun length(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    infix fun vectorMultiple(other: Vector): Vector {
        return Vector(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    infix fun scalarMultiple(other: Vector): Double {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    fun copy(): Vector {
        return Vector(x, y, z)
    }
}
