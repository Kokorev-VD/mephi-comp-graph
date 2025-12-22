package lab5

data class Side(
    val points: List<Vector>
) {
    operator fun get(idx: Int): Vector {
        return points[idx]
    }
}

data class Parallelepiped(
    val points: List<Vector>
) {
    val sides: List<Side>

    init {
        val p = points

        sides = listOf(
            Side(listOf(p[0], p[3], p[2], p[1])),
            Side(listOf(p[4], p[5], p[6], p[7])),
            Side(listOf(p[0], p[1], p[5], p[4])),
            Side(listOf(p[1], p[2], p[6], p[5])),
            Side(listOf(p[2], p[3], p[7], p[6])),
            Side(listOf(p[3], p[0], p[4], p[7]))
        )
    }

    fun apply(transformation: (Vector) -> Unit): Parallelepiped {
        this.points.forEach {
            transformation(it)
        }
        return this
    }

    fun deepCopy(): Parallelepiped {
        return Parallelepiped(points.map { it.copy() })
    }
}
