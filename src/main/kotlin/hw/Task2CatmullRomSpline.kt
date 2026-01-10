package hw

import common.Image8bpp
import common.Point
import common.drawLine
import common.drawPoint
import kotlin.math.sqrt

class CatmullRomSpline(private val controlPoints: List<Point>) {

    init {
        require(controlPoints.size >= 2) { "Необходимо минимум 2 контрольные точки" }
    }

    private fun calculateSteps(p1: Point, p2: Point): Int {
        val dx = (p2.x - p1.x).toDouble()
        val dy = (p2.y - p1.y).toDouble()
        val distance = sqrt(dx * dx + dy * dy)
        return (distance / 2.0).toInt().coerceIn(10, 200)
    }

    private fun getPoint(p0: Point, p1: Point, p2: Point, p3: Point, t: Double): Point {
        val t2 = t * t
        val t3 = t2 * t

        val x = 0.5 * (
                2.0 * p1.x +
                        (-p0.x + p2.x) * t +
                        (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * t2 +
                        (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * t3
                )

        val y = 0.5 * (
                2.0 * p1.y +
                        (-p0.y + p2.y) * t +
                        (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * t2 +
                        (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * t3
                )

        return Point(x.toInt(), y.toInt())
    }

    fun draw(image: Image8bpp, color: UByte) {
        if (controlPoints.size < 2) return

        for (i in 0 until controlPoints.size - 1) {
            val p0 = if (i > 0) controlPoints[i - 1] else controlPoints[i]
            val p1 = controlPoints[i]
            val p2 = controlPoints[i + 1]
            val p3 = if (i + 2 < controlPoints.size) controlPoints[i + 2] else controlPoints[i + 1]

            val steps = calculateSteps(p1, p2)
            var prevPoint = p1

            for (step in 1..steps) {
                val t = step.toDouble() / steps
                val currentPoint = getPoint(p0, p1, p2, p3, t)
                drawLine(image, prevPoint, currentPoint, color)
                prevPoint = currentPoint
            }
        }
    }

    fun drawControlPoints(image: Image8bpp, color: UByte) {
        for (point in controlPoints) {
            drawPoint(image, point, color)
        }

        for (i in 0 until controlPoints.size - 1) {
            drawLine(image, controlPoints[i], controlPoints[i + 1], color)
        }
    }
}
