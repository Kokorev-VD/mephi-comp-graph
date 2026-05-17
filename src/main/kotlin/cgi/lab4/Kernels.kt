package cgi.lab4

import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.roundToInt

fun gaussianKernel(size: Int = 5, sigma: Double? = null): Array<DoubleArray> {
    val s = sigma ?: ((size - 1) / 6.0)
    val radius = size / 2
    val s2 = 2.0 * s * s
    val k = Array(size) { DoubleArray(size) }
    var sum = 0.0
    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = (x - radius).toDouble()
            val dy = (y - radius).toDouble()
            val v = exp(-(dx * dx + dy * dy) / s2)
            k[y][x] = v
            sum += v
        }
    }
    for (y in 0 until size) for (x in 0 until size) k[y][x] /= sum
    return k
}

fun laplacianKernel(size: Int = 3, includeDiagonals: Boolean = false): Array<DoubleArray> {
    val center = size / 2
    val k = Array(size) { DoubleArray(size) { 0.0 } }
    var neighborCount = 0
    for (y in 0 until size) {
        for (x in 0 until size) {
            if (y == center && x == center) continue
            val dy = (y - center).let { if (it < 0) -it else it }
            val dx = (x - center).let { if (it < 0) -it else it }
            val isNeighbor = if (size == 3 && !includeDiagonals) {
                dy + dx <= 1
            } else {
                dy <= center && dx <= center
            }
            if (isNeighbor) {
                k[y][x] = 1.0
                neighborCount++
            }
        }
    }
    k[center][center] = -neighborCount.toDouble()
    return k
}

fun logKernel(sigma: Double = 1.4, size: Int? = null): Array<DoubleArray> {
    val sz = size ?: (2 * (3.0 * sigma).roundToInt().coerceAtLeast(2) + 1)
    val radius = sz / 2
    val s2 = sigma * sigma
    val s4 = s2 * s2
    val k = Array(sz) { DoubleArray(sz) }
    for (y in 0 until sz) {
        for (x in 0 until sz) {
            val dx = (x - radius).toDouble()
            val dy = (y - radius).toDouble()
            val r2 = dx * dx + dy * dy
            k[y][x] = -1.0 / (PI * s4) * (1.0 - r2 / (2.0 * s2)) * exp(-r2 / (2.0 * s2))
        }
    }
    var sum = 0.0
    for (y in 0 until sz) for (x in 0 until sz) sum += k[y][x]
    val mean = sum / (sz * sz)
    for (y in 0 until sz) for (x in 0 until sz) k[y][x] -= mean
    return k
}

fun sharpenKernel(size: Int = 3, alpha: Double = 1.0, includeDiagonals: Boolean = false): Array<DoubleArray> {
    val lap = laplacianKernel(size, includeDiagonals)
    val center = size / 2
    for (y in 0 until size) {
        for (x in 0 until size) {
            if (y == center && x == center) {
                lap[y][x] = 1.0 - alpha * lap[y][x]
            } else {
                lap[y][x] = -alpha * lap[y][x]
            }
        }
    }
    return lap
}
