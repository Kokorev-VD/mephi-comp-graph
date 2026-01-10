package lab5

data class Side(
    val points: List<Vector>,
    val color: UByte = 255u
) {
    operator fun get(idx: Int): Vector {
        return points[idx]
    }
    fun minZ(): Double {
        return points.minOf { it.z }
    }
}

data class Parallelepiped(
    val points: List<Vector>,
    val sideColors: List<UByte> = listOf(255u, 255u, 255u, 255u, 255u, 255u)
) {
    val sides: List<Side>

    init {
        val p = points
        val c = if (sideColors.size == 6) sideColors else listOf<UByte>(255u, 255u, 255u, 255u, 255u, 255u)

        sides = listOf(
            Side(listOf(p[0], p[3], p[2], p[1]), c[0]),
            Side(listOf(p[4], p[5], p[6], p[7]), c[1]),
            Side(listOf(p[0], p[1], p[5], p[4]), c[2]),
            Side(listOf(p[1], p[2], p[6], p[5]), c[3]),
            Side(listOf(p[2], p[3], p[7], p[6]), c[4]),
            Side(listOf(p[3], p[0], p[4], p[7]), c[5])
        )
    }

    fun apply(transformation: (Vector) -> Unit): Parallelepiped {
        this.points.forEach {
            transformation(it)
        }
        return this
    }

    fun deepCopy(): Parallelepiped {
        return Parallelepiped(points.map { it.copy() }, sideColors)
    }
}
