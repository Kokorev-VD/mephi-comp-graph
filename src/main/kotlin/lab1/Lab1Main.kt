package lab1

import Image8bpp
import kotlin.math.sqrt

fun main() {
    println("Лабораторная работа No 1: Смешивание изображений\n")

    println("Задача 1. Круглое полутоновое изображение")
    val circleImage = createCircle(
        size = 400,
        centerBrightness = 255u,
        edgeBrightness = 0u
    )
    circleImage.save("circle_gradient.png")
    println("Результат 1 задачи сохранен: circle_gradient.png\n")

    println("Задача 2. Смешивание двух изображений с альфа-каналом")

    val image1 = Image8bpp(400, 400)
    image1.fill(200u)

    val image2 = Image8bpp(400, 400)
    image2.fill(50u)

    val alphaChannel = circleImage

    val blendedImage = blendImages(image1, image2, alphaChannel)
    blendedImage.save("blended_result.png")
    println("Результат 2 задачи сохранен: blended_result.png\n")

    println("Результаты задачи сохранены тут: src/main/resources/lab1/")
}

// 1 ЗАДАЧА
fun createCircle(
    size: Int,
    centerBrightness: UByte = 255u,
    edgeBrightness: UByte = 0u
): Image8bpp {
    val image = Image8bpp(size, size)
    val centerX = size / 2.0
    val centerY = size / 2.0
    val maxRadius = size / 2.0

    for (y in 0 until size) {
        for (x in 0 until size) {
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

// 2 ЗАДАЧА
fun blendImages(image1: Image8bpp, image2: Image8bpp, alphaChannel: Image8bpp): Image8bpp {
    val result = Image8bpp(image1.width, image1.height)

    for (y in 0 until image1.height) {
        for (x in 0 until image1.width) {
            val pixel1 = image1.getPixel(x, y)
            val pixel2 = image2.getPixel(x, y)
            val alpha = alphaChannel.getPixel(x, y).toDouble() / 255.0

            val blended = (pixel1.toDouble() * (1 - alpha) + pixel2.toDouble() * alpha).toUInt().toUByte()
            result.setPixel(x, y, blended)
        }
    }

    return result
}
