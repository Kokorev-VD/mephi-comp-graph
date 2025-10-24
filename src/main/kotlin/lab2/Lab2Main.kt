package lab2

import Image8bpp
import kotlin.math.abs
import kotlin.math.roundToInt

fun main() {
    println("Лабораторная работа No 2: Алгоритм рассеивания ошибки Флойда-Стенберга\n")

    val originalImage = createGradient(400, 100)
    originalImage.save("original_gradient.png")

    for (n in 1..4) {
        val ditheredImage = floydSteinbergDithering(originalImage, n)
        ditheredImage.save("dithered_${n}bpp.png")
        println("Результат для $n bpp сохранен: dithered_${n}bpp.png")
    }
    println("Результаты задач сохранены тут: src/main/resources/lab2/")
}

fun floydSteinbergDithering(image: Image8bpp, bitsPerPixel: Int): Image8bpp {
    val result = Image8bpp(image.width, image.height)

    val levels = 1 shl bitsPerPixel

    val palette = createPalette(levels)

    val errors = Array(image.height) { DoubleArray(image.width) { 0.0 } }

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val oldPixel = image.getPixel(x, y).toDouble() + errors[y][x]

            val newPixel = palette.minBy { color -> abs(color - oldPixel) }

            result.setPixel(x, y, newPixel.toUInt().toUByte())

            val quantError = oldPixel - newPixel

            if (x + 1 < image.width) {
                errors[y][x + 1] += quantError * 7.0 / 16.0
            }

            if (y + 1 < image.height) {
                if (x - 1 >= 0) {
                    errors[y + 1][x - 1] += quantError * 3.0 / 16.0
                }

                errors[y + 1][x] += quantError * 5.0 / 16.0

                if (x + 1 < image.width) {
                    errors[y + 1][x + 1] += quantError * 1.0 / 16.0
                }
            }
        }
    }

    return result
}

fun createPalette(levels: Int): List<Int> =
    when(levels) {
        1 -> listOf(0)
        2 -> listOf(0, 255)
        else -> List<Int>(levels) { i -> (i * 255.0 / (levels - 1)).roundToInt() }
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
