package hw

import common.Image24bpp
import kotlin.math.ln

fun calculateOptimalGamma(
    avgBrightness: Double,
    targetBrightness: Double = 128.0
): Double {
    val normalizedInput = avgBrightness / 255.0
    val normalizedTarget = targetBrightness / 255.0

    return ln(normalizedInput) / ln(normalizedTarget)
}

fun applyAutoGammaCorrection(
    inputFilename: String,
    outputFilename: String,
    targetBrightness: Double = 128.0
): Double {
    val image = Image24bpp.load(inputFilename)

    val originalBrightness = image.getAverageBrightness()

    println("Исходная средняя яркость: ${"%.2f".format(originalBrightness)}")

    val gamma = calculateOptimalGamma(originalBrightness, targetBrightness)

    println("Вычисленное значение гаммы: ${"%.4f".format(gamma)}")

    image.applyGammaCorrection(gamma)

    val finalBrightness = image.getAverageBrightness()

    println("Конечная средняя яркость: ${"%.2f".format(finalBrightness)}")

    image.save(outputFilename)

    return gamma
}
