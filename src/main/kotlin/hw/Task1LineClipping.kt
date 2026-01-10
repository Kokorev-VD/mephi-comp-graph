package hw

import common.Point
import common.Polygon
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun clipLineByArbitraryPolygon(
    lineStart: Point,
    lineEnd: Point,
    polygon: Polygon
): List<Pair<Point, Point>> {
    val intersections = mutableListOf<Pair<Point, Double>>()

    val lx = lineEnd.x - lineStart.x
    val ly = lineEnd.y - lineStart.y

    for (i in polygon.vertices.indices) {
        val v1 = polygon.vertices[i]
        val v2 = polygon.vertices[(i + 1) % polygon.vertices.size]

        val ex = v2.x - v1.x
        val ey = v2.y - v1.y

        val denom = lx.toDouble() * ey - ly.toDouble() * ex
        if (abs(denom) < 1e-10) continue

        val dx = v1.x - lineStart.x
        val dy = v1.y - lineStart.y

        val t = (dx.toDouble() * ey - dy.toDouble() * ex) / denom
        val s = (dx.toDouble() * ly - dy.toDouble() * lx) / denom

        if (t >= 0.0 && t <= 1.0 && s >= 0.0 && s <= 1.0) {
            val x = (lineStart.x + t * lx).toInt()
            val y = (lineStart.y + t * ly).toInt()
            val point = Point(x, y)
            if (intersections.none { abs(it.second - t) < 1e-9 }) {
                intersections.add(point to t)
            }
        }
    }

    intersections.sortBy { it.second }

    val segments = mutableListOf<Pair<Point, Point>>()

    val allPoints = mutableListOf<Pair<Point, Double>>()
    allPoints.add(lineStart to 0.0)
    allPoints.addAll(intersections)
    allPoints.add(lineEnd to 1.0)

    for (i in 0 until allPoints.size - 1) {
        val (p1, t1) = allPoints[i]
        val (p2, t2) = allPoints[i + 1]

        val midT = (t1 + t2) / 2.0
        val dx = (lineEnd.x - lineStart.x).toDouble()
        val dy = (lineEnd.y - lineStart.y).toDouble()
        val midPoint = Point(
            (lineStart.x + midT * dx).toInt(),
            (lineStart.y + midT * dy).toInt()
        )

        if (isPointInside(midPoint, polygon)) {
            segments.add(p1 to p2)
        }
    }

    return segments
}

private fun isPointInside(point: Point, polygon: Polygon): Boolean {
    var crossings = 0
    for (i in polygon.vertices.indices) {
        val v1 = polygon.vertices[i]
        val v2 = polygon.vertices[(i + 1) % polygon.vertices.size]

        val minY = min(v1.y, v2.y)
        val maxY = max(v1.y, v2.y)

        if (point.y >= minY && point.y < maxY) {
            val xInt = v1.x + (point.y - v1.y) * (v2.x - v1.x) / (v2.y - v1.y)
            if (point.x < xInt) crossings++
        }
    }
    return crossings % 2 == 1
}
