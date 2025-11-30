package lab4

import common.Image8bpp
import common.Point
import common.drawLine
import common.drawPoint

class BezierCurve(
    private val p0: Point,
    private val p1: Point,
    private val p2: Point,
    private val p3: Point
) {
    fun draw(image: Image8bpp, color: UByte, steps: Int = 100) {
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
