package common

import kotlin.math.min
import kotlin.math.max

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
