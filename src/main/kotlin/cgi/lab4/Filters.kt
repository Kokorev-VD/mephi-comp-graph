package cgi.lab4

import common.Image8bpp
import kotlin.math.abs

fun lowPassGaussian(image: Image8bpp): Image8bpp {
    val data = convolve(image, gaussianKernel5())
    return toImage(data)
}

fun averagingWithThreshold(image: Image8bpp, threshold: Int = 20): Image8bpp {
    val smoothed = convolve(image, gaussianKernel5())
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

fun highPassLaplacian(image: Image8bpp): Image8bpp {
    val data = convolve(image, laplacianKernel())
    return toImageNormalized(data)
}

fun highPassLoG(image: Image8bpp, sigma: Double = 1.4): Image8bpp {
    val data = convolve(image, logKernel(sigma))
    return toImageNormalized(data)
}

fun sharpen(image: Image8bpp, alpha: Double = 1.0): Image8bpp {
    val lap = convolve(image, laplacianKernel())
    val w = image.width
    val h = image.height
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val orig = image.getPixel(x, y).toInt().toDouble()
            val v = (orig - alpha * lap[y][x]).toInt().coerceIn(0, 255)
            out.setPixel(x, y, v.toUByte())
        }
    }
    return out
}

fun edgesByZeroCrossing(image: Image8bpp, sigma: Double = 1.4): Image8bpp {
    val ie = convolve(image, logKernel(sigma))
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
