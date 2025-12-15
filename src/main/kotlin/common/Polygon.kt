package common

import kotlin.math.max
import kotlin.math.min

enum class FillRule {
    EVEN_ODD,
    NON_ZERO_WINDING
}

class Polygon(val vertices: List<Point>) {

    fun draw(image: Image8bpp, color: UByte) {
        for (i in vertices.indices) {
            val start = vertices[i]
            val end = vertices[(i + 1) % vertices.size]
            drawLine(image, start, end, color)
        }
    }

    fun fill(image: Image8bpp, rule: FillRule, color: UByte) {
        val minY = vertices.minOf { it.y }.coerceAtLeast(0)
        val maxY = vertices.maxOf { it.y }.coerceAtMost(image.height - 1)

        for (y in minY..maxY) {
            val intersections = findIntersections(y)

            when (rule) {
                FillRule.EVEN_ODD -> fillEvenOdd(image, y, intersections, color)
                FillRule.NON_ZERO_WINDING -> fillNonZeroWinding(image, y, intersections, color)
            }
        }
    }

    fun isClockwise(): Boolean {
        var area = 0.0
        for (i in vertices.indices) {
            val j = (i + 1) % vertices.size
            area += (vertices[j].x - vertices[i].x) * (vertices[j].y + vertices[i].y)
        }
        return area > 0
    }

    fun isSimple(): Boolean {
        for (i in vertices.indices) {
            for (j in i + 2 until vertices.size) {
                val (a, b, c, d) = listOf(vertices[i], vertices[(i + 1) % vertices.size], vertices[j], vertices[(j + 1) % vertices.size])

                val (p1, p2, p3, p4) = listOf(prod(a, b, c), prod(a, b, d), prod(c, d, a), prod(c, d, b))

                if (p1 * p2 < 0 && p3 * p4 < 0) {
                    return false
                }

                if (p1 == 0 && p2 == 0 && p3 == 0 && p4 == 0) {
                    if (doCollinearSegmentsOverlap(a, b, c, d)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    // проверка для вырожденных случаев
    private fun doCollinearSegmentsOverlap(a: Point, b: Point, c: Point, d: Point): Boolean {
        val overlaps: (Int, Int, Int, Int) -> Boolean = lambda@{ a1, a2, b1, b2 ->
            val minA = min(a1, a2)
            val maxA = max(a1, a2)
            val minB = min(b1, b2)
            val maxB = max(b1, b2)

            if (minA == maxA && minB == maxB) {
                return@lambda minA == minB
            }

            return@lambda maxA > minB && maxB > minA
        }

        val xOverlap = overlaps(a.x, b.x, c.x, d.x)
        val yOverlap = overlaps(a.y, b.y, c.y, d.y)

        if (a.y == b.y && c.y == d.y && a.y == c.y) {
            return xOverlap
        }

        if (a.x == b.x && c.x == d.x && a.x == c.x) {
            return yOverlap
        }

        return xOverlap && yOverlap
    }

    fun isConvex(): Boolean {
        if (vertices.size < 3) return false

        if (!isSimple()) return false

        val f = sign(prod(vertices[0], vertices[1], vertices[2]))

        for (i in 1 until vertices.size) {
            val m = prod(vertices[i], vertices[(i + 1) % vertices.size], vertices[(i + 2) % vertices.size])
            if (sign(m) != f) return false
        }
        return true
    }

    private fun prod(a: Point, b: Point, c: Point): Int {
        return (c.x - a.x) * (b.y - a.y) - (b.x - a.x) * (c.y - a.y)
    }

    private fun findIntersections(y: Int): List<Pair<Int, Int>> {
        val intersections = mutableListOf<Pair<Int, Int>>()

        for (i in vertices.indices) {
            val v1 = vertices[i]
            val v2 = vertices[(i + 1) % vertices.size]

            val minY = min(v1.y, v2.y)
            val maxY = max(v1.y, v2.y)

            if (y >= minY && y < maxY) {
                val x = v1.x + (y - v1.y) * (v2.x - v1.x) / (v2.y - v1.y)
                val winding = if (v2.y > v1.y) 1 else -1
                intersections.add(x to winding)
            }
        }

        return intersections.sortedBy { it.first }
    }

    private fun fillEvenOdd(image: Image8bpp, y: Int, intersections: List<Pair<Int, Int>>, color: UByte) {
        for (i in 0 until intersections.size - 1 step 2) {
            val x1 = intersections[i].first.coerceIn(0, image.width - 1)
            val x2 = intersections[i + 1].first.coerceIn(0, image.width - 1)

            for (x in x1..x2) {
                if (x >= 0 && x < image.width && y >= 0 && y < image.height) {
                    image.setPixel(x, y, color)
                }
            }
        }
    }

    private fun fillNonZeroWinding(image: Image8bpp, y: Int, intersections: List<Pair<Int, Int>>, color: UByte) {
        var windingNumber = 0
        var lastX = intersections.firstOrNull()?.first ?: return

        for ((x, winding) in intersections) {
            if (windingNumber != 0) {
                val x1 = lastX.coerceIn(0, image.width - 1)
                val x2 = x.coerceIn(0, image.width - 1)

                for (px in x1..x2) {
                    if (px >= 0 && px < image.width && y >= 0 && y < image.height) {
                        image.setPixel(px, y, color)
                    }
                }
            }

            windingNumber += winding
            lastX = x
        }
    }
}
