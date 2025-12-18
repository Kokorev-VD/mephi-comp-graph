package lab4

import common.Point
import common.Polygon

private fun crossProduct(o: Point, a: Point, b: Point): Double {
    return (a.x - o.x).toDouble() * (b.y - o.y) - (a.y - o.y).toDouble() * (b.x - o.x)
}

private fun isInside(point: Point, edgeStart: Point, edgeEnd: Point, clockwise: Boolean): Boolean {
    val cp = crossProduct(edgeStart, edgeEnd, point)
    return if (clockwise) cp <= 0 else cp >= 0
}

private fun lineIntersection(p1: Point, p2: Point, p3: Point, p4: Point): Point? {
    val x1 = p1.x.toDouble()
    val y1 = p1.y.toDouble()
    val x2 = p2.x.toDouble()
    val y2 = p2.y.toDouble()
    val x3 = p3.x.toDouble()
    val y3 = p3.y.toDouble()
    val x4 = p4.x.toDouble()
    val y4 = p4.y.toDouble()

    val denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
    if (kotlin.math.abs(denom) < 1e-10) return null

    val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom

    val x = x1 + t * (x2 - x1)
    val y = y1 + t * (y2 - y1)

    return Point(x.toInt(), y.toInt())
}

fun sutherlandHodgmanClip(subject: Polygon, clipPolygon: Polygon): List<Polygon> {
    val clipVertices = clipPolygon.vertices
    val clipClockwise = clipPolygon.isClockwise()
    val subjectClockwise = subject.isClockwise()

    var outputList = if (clipClockwise != subjectClockwise) {
        subject.vertices.reversed().toMutableList()
    } else {
        subject.vertices.toMutableList()
    }

    for (i in clipVertices.indices) {
        if (outputList.isEmpty()) break

        val edgeStart = clipVertices[i]
        val edgeEnd = clipVertices[(i + 1) % clipVertices.size]

        val inputList = outputList
        outputList = mutableListOf()

        for (j in inputList.indices) {
            val currentVertex = inputList[j]
            val previousVertex = if (j == 0) inputList[inputList.size - 1] else inputList[j - 1]

            val currentInside = isInside(currentVertex, edgeStart, edgeEnd, clipClockwise)
            val previousInside = isInside(previousVertex, edgeStart, edgeEnd, clipClockwise)

            if (previousInside && currentInside) {
                outputList.add(currentVertex)
            } else if (previousInside) {
                val intersection = lineIntersection(previousVertex, currentVertex, edgeStart, edgeEnd)
                if (intersection != null) {
                    outputList.add(intersection)
                }
            } else if (currentInside) {
                val intersection = lineIntersection(previousVertex, currentVertex, edgeStart, edgeEnd)
                if (intersection != null) {
                    outputList.add(intersection)
                }
                outputList.add(currentVertex)
            }
        }
    }
    return listOf(Polygon(outputList))
}
