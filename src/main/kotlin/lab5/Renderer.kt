package lab5

import common.Image8bpp
import common.Point
import common.drawLine
import java.awt.Graphics

object Renderer {
    val cameraZ: Double = 100.0
    val center = Vector(500.0, 500.0, cameraZ)
    val scale: Double = 100.0

    private fun isSideVisible(side: Side, cameraPosition: Vector): Boolean {
        val toCamera = cameraPosition - side[0]
        val edge1 = side[1] - side[0]
        val edge2 = side[3] - side[0]
        val normal = edge1 vectorMultiple edge2
        return (toCamera scalarMultiple normal) >= 0
    }

    private fun applyScreenTransform(points: List<Vector>, centerX: Double, centerY: Double, scale: Double) {
        points.forEach {
            it.x = it.x * scale + centerX
            it.y = it.y * scale + centerY
        }
    }

    fun render(g: Graphics, parallepiped__: Parallelepiped) {
        val par = parallepiped__.deepCopy()
        val parForCulling = parallepiped__.deepCopy()

        par.apply {
            it * Matrix.onePointPerspective(2.0)
        }
        parForCulling.apply {
            it * Matrix.onePointPerspective(2.0)
        }

        par.points.forEach {
            it.fix()
        }

        val cameraPos = Vector(0.0, 0.0, cameraZ)
        par.sides.forEachIndexed { index, side ->
            val sideCulling = parForCulling.sides[index]
            if (!isSideVisible(sideCulling, cameraPos)) return@forEachIndexed

            val w = 3
            Utils.line(g, side[0], side[1], w)
            Utils.line(g, side[1], side[2], w)
            Utils.line(g, side[2], side[3], w)
            Utils.line(g, side[3], side[0], w)
        }
    }

    fun Vector.fix(): Vector {
        val projected = this.copy()
        projected * Matrix.projectionToXY(cameraZ)
        projected.x = projected.x * scale + center.x
        projected.y = projected.y * scale + center.y
        projected.z = projected.z * scale + center.z

        this.x = projected.x
        this.y = projected.y
        this.z = projected.z
        return this
    }

    fun renderParallelProjection(
        image: Image8bpp,
        parallepiped: Parallelepiped,
        color: UByte,
        centerX: Double = 400.0,
        centerY: Double = 300.0,
        scale: Double = 1.0,
        removeHidden: Boolean = true
    ) {
        val par = parallepiped.deepCopy()
        val parOriginal = parallepiped.deepCopy()

        par.apply {
            it * Matrix.projectionToXY()
        }

        applyScreenTransform(par.points, centerX, centerY, scale)

        if (removeHidden) {
            val viewDir = Vector(0.0, 0.0, -1.0)
            par.sides.forEachIndexed { index, side ->
                val sideOriginal = parOriginal.sides[index]
                val edge1 = sideOriginal[1] - sideOriginal[0]
                val edge2 = sideOriginal[3] - sideOriginal[0]
                val normal = edge1 vectorMultiple edge2
                if ((normal scalarMultiple viewDir) < 0) return@forEachIndexed
                drawSide(image, side, color)
            }
        } else {
            par.sides.forEach { side ->
                drawSide(image, side, color)
            }
        }
    }

    fun renderPerspectiveProjection(
        image: Image8bpp,
        parallepiped: Parallelepiped,
        k: Double,
        color: UByte,
        centerX: Double = 400.0,
        centerY: Double = 300.0,
        scale: Double = 1.0,
        removeHidden: Boolean = true
    ) {
        val par = parallepiped.deepCopy()
        val parForCulling = parallepiped.deepCopy()

        // Apply perspective transformation to both copies
        par.apply {
            it * Matrix.onePointPerspective(k)
        }
        parForCulling.apply {
            it * Matrix.onePointPerspective(k)
        }

        // Apply projection and screen transformation only to render copy
        par.apply {
            it * Matrix.projectionToXY()
        }

        applyScreenTransform(par.points, centerX, centerY, scale)

        if (removeHidden) {
            val cameraPos = Vector(0.0, 0.0, cameraZ)
            par.sides.forEachIndexed { index, side ->
                val sideCulling = parForCulling.sides[index]
                if (!isSideVisible(sideCulling, cameraPos)) return@forEachIndexed
                drawSide(image, side, color)
            }
        } else {
            par.sides.forEach { side ->
                drawSide(image, side, color)
            }
        }
    }

    private fun drawSide(image: Image8bpp, side: Side, color: UByte) {
        for (i in 0 until 4) {
            val start = side[i]
            val end = side[(i + 1) % 4]
            drawLine(
                image,
                Point(start.x.toInt(), start.y.toInt()),
                Point(end.x.toInt(), end.y.toInt()),
                color
            )
        }
    }

    fun fillPolygon(image: Image8bpp, points: List<Point>, color: UByte) {
        if (points.size != 4) return

        val minY = points.minOf { it.y }.coerceAtLeast(0)
        val maxY = points.maxOf { it.y }.coerceAtMost(image.height - 1)

        for (y in minY..maxY) {
            val intersections = mutableListOf<Int>()

            for (i in 0 until 4) {
                val p1 = points[i]
                val p2 = points[(i + 1) % 4]

                val intersectX = getLineIntersection(p1, p2, y)
                if (intersectX != null) {
                    intersections.add(intersectX)
                }
            }

            intersections.sort()

            for (j in 0 until intersections.size - 1 step 2) {
                val x1 = intersections[j].coerceAtLeast(0)
                val x2 = intersections[j + 1].coerceAtMost(image.width - 1)

                for (x in x1..x2) {
                    if (x in 0 until image.width) {
                        image.setPixel(x, y, color)
                    }
                }
            }
        }
    }

    private fun getLineIntersection(p1: Point, p2: Point, y: Int): Int? {
        if (p1.y == p2.y) return null

        val (pMin, pMax) = if (p1.y < p2.y) Pair(p1, p2) else Pair(p2, p1)

        if (y < pMin.y || y >= pMax.y) return null

        val t = (y - pMin.y).toDouble() / (pMax.y - pMin.y)
        val x = pMin.x + (t * (pMax.x - pMin.x)).toInt()

        return x
    }

    fun drawSideWithFill(image: Image8bpp, side: Side, color: UByte) {
        val points = side.points.map { Point(it.x.toInt(), it.y.toInt()) }
        fillPolygon(image, points, color)
        drawSide(image, side, color)
    }
}
