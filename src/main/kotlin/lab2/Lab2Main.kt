package lab2

import common.Image8bpp
import common.createGradient
import java.io.File
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

    val inputDir = File("src/main/resources/common")
    val imageFiles = inputDir.listFiles { file ->
        file.extension.lowercase() in listOf("png", "jpg", "jpeg")
    } ?: emptyArray()

    for ((index, imageFile) in imageFiles.withIndex()) {
        val bpp = index + 1
        val image = Image8bpp.load(imageFile.absolutePath)
        val dithered = floydSteinbergDithering(image, bpp)
        dithered.save("${imageFile.nameWithoutExtension}_dithered_${bpp}bpp.png")
        println("Результат для $bpp bpp сохранен: ${imageFile.nameWithoutExtension}_dithered_${bpp}bpp.png")
    }

    println("\nРезультаты задач сохранены тут: src/main/resources/lab2/")
}

fun createPalette(levels: Int): List<Int> =
    when(levels) {
        1 -> listOf(0)
        2 -> listOf(0, 255)
        else -> List<Int>(levels) { i -> (i * 255.0 / (levels - 1)).roundToInt() }
    }

fun floydSteinbergDithering(image: Image8bpp, bitsPerPixel: Int): Image8bpp {
    val result = Image8bpp(image.width, image.height)
    val levels = 1 shl bitsPerPixel
    val palette = createPalette(levels)

    val lookupTable = IntArray(256) { pixelValue ->
        palette.minBy { color -> abs(color - pixelValue) }
    }

    var currentErrors = DoubleArray(image.width) { 0.0 }
    var nextErrors = DoubleArray(image.width) { 0.0 }

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val oldPixel = image.getPixel(x, y).toDouble() + currentErrors[x]
            val clampedPixel = oldPixel.coerceIn(0.0, 255.0).toInt()

            val newPixel = lookupTable[clampedPixel]
            result.setPixel(x, y, newPixel.toUByte())

            val quantError = oldPixel - newPixel

            if (x + 1 < image.width) {
                currentErrors[x + 1] += quantError * 7.0 / 16.0
            }

            if (x > 0) {
                nextErrors[x - 1] += quantError * 3.0 / 16.0
            }
            nextErrors[x] += quantError * 5.0 / 16.0
            if (x + 1 < image.width) {
                nextErrors[x + 1] += quantError * 1.0 / 16.0
            }
        }

        val temp = currentErrors
        currentErrors = nextErrors
        nextErrors = temp
        nextErrors.fill(0.0)
    }

    return result
}
