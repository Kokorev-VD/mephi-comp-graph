package common

import kotlin.math.abs

data class Point(val x: Int, val y: Int)

fun drawLine(image: Image8bpp, start: Point, end: Point, color: UByte) {
    var x0 = start.x
    var y0 = start.y
    val x1 = end.x
    val y1 = end.y

    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1
    var err = dx - dy

    while (true) {
        if (x0 >= 0 && x0 < image.width && y0 >= 0 && y0 < image.height) {
            image.setPixel(x0, y0, color)
        }

        if (x0 == x1 && y0 == y1) break

        val e2 = 2 * err
        if (e2 > -dy) {
            err -= dy
            x0 += sx
        }
        if (e2 < dx) {
            err += dx
            y0 += sy
        }
    }
}

fun drawPoint(image: Image8bpp, point: Point, color: UByte) {
    val size = 3
    for (dy in -size..size) {
        for (dx in -size..size) {
            val x = point.x + dx
            val y = point.y + dy
            if (x >= 0 && x < image.width && y >= 0 && y < image.height) {
                image.setPixel(x, y, color)
            }
        }
    }
}