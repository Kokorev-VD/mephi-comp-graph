package common

import kotlin.math.sqrt

fun createCircle(
    sizeX: Int,
    sizeY: Int,
    centerBrightness: UByte = 255u,
    edgeBrightness: UByte = 0u
): Image8bpp {
    val image = Image8bpp(sizeX, sizeY)
    val centerX = sizeX / 2.0
    val centerY = sizeY / 2.0
    val maxRadius = sizeX / 2.0

    for (y in 0 until sizeY) {
        for (x in 0 until sizeX) {
            val dx = x - centerX
            val dy = y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= maxRadius) {
                val t = distance / maxRadius
                val brightness = (centerBrightness.toDouble() * (1 - t) + edgeBrightness.toDouble() * t).toUInt().toUByte()
                image.setPixel(x, y, brightness)
            } else {
                image.setPixel(x, y, edgeBrightness)
            }
        }
    }
    return image
}

fun createGradient(width: Int, height: Int): Image8bpp {
    val image = Image8bpp(width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value = (x.toUInt() * 255u / width.toUInt()).toUByte()
            image.setPixel(x, y, value)
        }
    }
    return image
}