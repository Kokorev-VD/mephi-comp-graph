package lab4

import common.Image8bpp
import common.Point
import common.drawLine
import common.drawPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class BezierCurve(
    private val p0: Point,
    private val p1: Point,
    private val p2: Point,
    private val p3: Point
) {
    private fun calculateSteps(): Int {
        val d1x = p0.x - 2 * p1.x + p2.x
        val d1y = p0.y - 2 * p1.y + p2.y
        val d2x = p1.x - 2 * p2.x + p3.x
        val d2y = p1.y - 2 * p2.y + p3.y

        val dist1 = abs(d1x) + abs(d1y)
        val dist2 = abs(d2x) + abs(d2y)

        val h = max(dist1, dist2).toDouble()
        val n = 1 + sqrt(3.0 * h)

        return n.toInt().coerceIn(10, 1000)
    }

    fun draw(image: Image8bpp, color: UByte) {
        val steps = calculateSteps()
        var prevPoint = getPoint(0.0)

        for (i in 1..steps) {
            val t = i.toDouble() / steps
            val currentPoint = getPoint(t)
            drawLine(image, prevPoint, currentPoint, color)
            prevPoint = currentPoint
        }
    }

    fun drawControlPoints(image: Image8bpp, color: UByte) {
        drawLine(image, p0, p1, color)
        drawLine(image, p1, p2, color)
        drawLine(image, p2, p3, color)

        drawPoint(image, p0, color)
        drawPoint(image, p1, color)
        drawPoint(image, p2, color)
        drawPoint(image, p3, color)
    }

    private fun getPoint(t: Double): Point {
        val mt = 1 - t
        val mt2 = mt * mt
        val mt3 = mt2 * mt
        val t2 = t * t
        val t3 = t2 * t

        val x = (mt3 * p0.x + 3 * mt2 * t * p1.x + 3 * mt * t2 * p2.x + t3 * p3.x).toInt()
        val y = (mt3 * p0.y + 3 * mt2 * t * p1.y + 3 * mt * t2 * p2.y + t3 * p3.y).toInt()

        return Point(x, y)
    }
}
