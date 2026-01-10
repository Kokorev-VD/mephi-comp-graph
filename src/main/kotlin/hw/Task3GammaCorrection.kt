package hw

import common.Image24bpp
import kotlin.math.abs

fun findOptimalGamma(
    image: Image24bpp,
    targetBrightness: Double = 128.0,
    epsilon: Double = 0.5
): Double {
    var gammaLow = 0.1
    var gammaHigh = 5.0
    var optimalGamma = 1.0

    while (gammaHigh - gammaLow > 0.01) {
        val gammaMid = (gammaLow + gammaHigh) / 2.0

        val testImage = image.copy()
        testImage.applyGammaCorrection(gammaMid)

        val brightness = testImage.getAverageBrightness()

        if (abs(brightness - targetBrightness) < epsilon) {
            optimalGamma = gammaMid
            break
        }

        if (brightness < targetBrightness) {
            gammaHigh = gammaMid
        } else {
            gammaLow = gammaMid
        }

        optimalGamma = gammaMid
    }

    return optimalGamma
}

fun applyAutoGammaCorrection(
    inputFilename: String,
    outputFilename: String,
    targetBrightness: Double = 128.0
): Double {
    val image = Image24bpp.load(inputFilename)

    val originalBrightness = image.getAverageBrightness()

    println("Исходная средняя яркость: ${"%.2f".format(originalBrightness)}")

    val gamma = findOptimalGamma(image, targetBrightness)

    println("Найденное значение гаммы: ${"%.4f".format(gamma)}")

    image.applyGammaCorrection(gamma)

    val finalBrightness = image.getAverageBrightness()

    println("Конечная средняя яркость: ${"%.2f".format(finalBrightness)}")

    image.save(outputFilename)

    return gamma
}
