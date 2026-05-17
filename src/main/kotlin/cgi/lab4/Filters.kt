package cgi.lab4

import common.Image8bpp
import common.convolve
import common.toImage
import common.toImageNormalized
import kotlin.math.abs
import kotlin.math.exp

fun lowPassGaussian(image: Image8bpp, kernelSize: Int = 5, sigma: Double? = null): Image8bpp {
    val kernel = gaussianKernel(kernelSize, sigma)
    return toImage(convolve(image, kernel))
}

fun averagingWithThreshold(image: Image8bpp, threshold: Int = 20, kernelSize: Int = 5): Image8bpp {
    val smoothed = convolve(image, gaussianKernel(kernelSize))
    val w = image.width
    val h = image.height
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val orig = image.getPixel(x, y).toInt()
            val smv = smoothed[y][x].toInt().coerceIn(0, 255)
            val v = if (abs(smv - orig) <= threshold) smv else orig
            out.setPixel(x, y, v.toUByte())
        }
    }
    return out
}

fun highPassLaplacian(image: Image8bpp, kernelSize: Int = 3): Image8bpp {
    val data = convolve(image, laplacianKernel(kernelSize))
    return toImageNormalized(data)
}

fun highPassLoG(image: Image8bpp, sigma: Double = 1.4, kernelSize: Int? = null): Image8bpp {
    val data = convolve(image, logKernel(sigma, kernelSize))
    return toImageNormalized(data)
}

fun sharpenConvolution(image: Image8bpp, kernelSize: Int = 3, alpha: Double = 1.0): Image8bpp {
    val kernel = sharpenKernel(kernelSize, alpha)
    val data = convolve(image, kernel)
    return toImage(data)
}

fun bilateralFilter(
    image: Image8bpp,
    kernelSize: Int = 5,
    sigmaSpatial: Double = 2.0,
    sigmaRange: Double = 30.0
): Image8bpp {
    require(kernelSize % 2 == 1 && kernelSize >= 3) { "Размер ядра должен быть нечётным и ≥ 3" }
    val w = image.width
    val h = image.height
    val radius = kernelSize / 2
    val s2Spatial = 2.0 * sigmaSpatial * sigmaSpatial
    val s2Range = 2.0 * sigmaRange * sigmaRange
    val out = Image8bpp(w, h)

    for (y in 0 until h) {
        for (x in 0 until w) {
            val centerVal = image.getPixel(x, y).toInt().toDouble()
            var sumWeighted = 0.0
            var sumWeights = 0.0
            for (ky in -radius..radius) {
                val sy = (y + ky).coerceIn(0, h - 1)
                for (kx in -radius..radius) {
                    val sx = (x + kx).coerceIn(0, w - 1)
                    val neighborVal = image.getPixel(sx, sy).toInt().toDouble()
                    val spatialDist = (kx * kx + ky * ky).toDouble()
                    val rangeDist = (neighborVal - centerVal) * (neighborVal - centerVal)
                    val wSpatial = exp(-spatialDist / s2Spatial)
                    val wRange = exp(-rangeDist / s2Range)
                    val weight = wSpatial * wRange
                    sumWeighted += weight * neighborVal
                    sumWeights += weight
                }
            }
            val v = (sumWeighted / sumWeights).toInt().coerceIn(0, 255).toUByte()
            out.setPixel(x, y, v)
        }
    }
    return out
}

fun edgesByZeroCrossing(image: Image8bpp, sigma: Double = 1.4, kernelSize: Int? = null): Image8bpp {
    val ie = convolve(image, logKernel(sigma, kernelSize))
    val w = image.width
    val h = image.height

    var sumAbs = 0.0
    for (y in 0 until h) for (x in 0 until w) sumAbs += abs(ie[y][x])
    val t = 3.0 / (4.0 * w * h) * sumAbs

    val out = Image8bpp(w, h)
    out.fill(0u)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = ie[y][x]
            var isEdge = false
            if (x + 1 < w) {
                val r = ie[y][x + 1]
                if (abs(c - r) > t && c * r < 0.0) isEdge = true
            }
            if (!isEdge && x - 1 >= 0) {
                val l = ie[y][x - 1]
                if (abs(c - l) > t && c * l < 0.0) isEdge = true
            }
            if (!isEdge && y + 1 < h) {
                val d = ie[y + 1][x]
                if (abs(c - d) > t && c * d < 0.0) isEdge = true
            }
            if (!isEdge && y - 1 >= 0) {
                val u = ie[y - 1][x]
                if (abs(c - u) > t && c * u < 0.0) isEdge = true
            }
            if (isEdge) out.setPixel(x, y, 255u)
        }
    }
    return out
}
