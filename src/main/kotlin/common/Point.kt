package common


data class Point(val x: Int, val y: Int)

fun drawLine(image: Image8bpp, start: Point, end: Point, color: UByte) {
    var x = start.x
    var y = start.y
    var dx = end.x - start.x
    var dy = end.y - start.y

    val ix = when {
        dx > 0 -> 1
        dx < 0 -> { dx = -dx; -1 }
        else -> 0
    }

    val iy = when {
        dy > 0 -> 1
        dy < 0 -> { dy = -dy; -1 }
        else -> 0
    }

    val steps = if (dx >= dy) dx else dy
    var e = if (dx >= dy) 2 * dy - dx else 2 * dx - dy

    repeat(steps + 1) {
        if (x in 0 until image.width && y in 0 until image.height) {
            image.setPixel(x, y, color)
        }

        if (dx >= dy) {
            val shouldChange = if (iy >= 0) e >= 0 else e > 0
            if (shouldChange) {
                y += iy
                e -= 2 * dx
            }
            x += ix
            e += 2 * dy
        } else {
            val shouldChange = if (ix >= 0) e >= 0 else e > 0
            if (shouldChange) {
                x += ix
                e -= 2 * dy
            }
            y += iy
            e += 2 * dx
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