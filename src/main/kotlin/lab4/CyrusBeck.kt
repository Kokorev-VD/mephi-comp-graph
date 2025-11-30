package lab4

import common.Point
import common.Polygon
import kotlin.math.max
import kotlin.math.min

fun cyrusBeckClip(p1: Point, p2: Point, polygon: Polygon): Pair<Point, Point>? {
    val d = Point(p2.x - p1.x, p2.y - p1.y)

    var tEnter = 0.0
    var tLeave = 1.0

    val vertices = polygon.vertices

    for (i in vertices.indices) {
        val edgeStart = vertices[i]
        val edgeEnd = vertices[(i + 1) % vertices.size]

        val edge = Point(edgeEnd.x - edgeStart.x, edgeEnd.y - edgeStart.y)
        val normal = Point(-edge.y, edge.x)

        val w = Point(p1.x - edgeStart.x, p1.y - edgeStart.y)

        val numerator = -(normal.x * w.x + normal.y * w.y)
        val denominator = normal.x * d.x + normal.y * d.y

        if (denominator != 0) {
            val t = numerator.toDouble() / denominator

            if (denominator > 0) {
                tEnter = max(tEnter, t)
            } else {
                tLeave = min(tLeave, t)
            }
        } else {
            if (numerator < 0) {
                return null
            }
        }
    }

    if (tEnter > tLeave) {
        return null
    }

    val clippedP1 = Point(
        (p1.x + tEnter * d.x).toInt(),
        (p1.y + tEnter * d.y).toInt()
    )

    val clippedP2 = Point(
        (p1.x + tLeave * d.x).toInt(),
        (p1.y + tLeave * d.y).toInt()
    )

    return clippedP1 to clippedP2
}
