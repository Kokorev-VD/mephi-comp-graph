package cgi.lab4

import kotlin.math.exp
import kotlin.math.PI

fun gaussianKernel5(): Array<DoubleArray> {
    val raw = arrayOf(
        doubleArrayOf(1.0, 4.0, 7.0, 4.0, 1.0),
        doubleArrayOf(4.0, 16.0, 26.0, 16.0, 4.0),
        doubleArrayOf(7.0, 26.0, 41.0, 26.0, 7.0),
        doubleArrayOf(4.0, 16.0, 26.0, 16.0, 4.0),
        doubleArrayOf(1.0, 4.0, 7.0, 4.0, 1.0),
    )
    val sum = 273.0
    return Array(5) { y -> DoubleArray(5) { x -> raw[y][x] / sum } }
}

fun laplacianKernel(): Array<DoubleArray> = arrayOf(
    doubleArrayOf(0.0, 1.0, 0.0),
    doubleArrayOf(1.0, -4.0, 1.0),
    doubleArrayOf(0.0, 1.0, 0.0),
)

fun logKernel(sigma: Double = 1.4): Array<DoubleArray> {
    val radius = (sigma * 3.0).toInt().coerceAtLeast(2)
    val size = 2 * radius + 1
    val s2 = sigma * sigma
    val s4 = s2 * s2
    val k = Array(size) { DoubleArray(size) }
    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = (x - radius).toDouble()
            val dy = (y - radius).toDouble()
            val r2 = dx * dx + dy * dy
            k[y][x] = -1.0 / (PI * s4) * (1.0 - r2 / (2.0 * s2)) * exp(-r2 / (2.0 * s2))
        }
    }
    var sum = 0.0
    for (y in 0 until size) for (x in 0 until size) sum += k[y][x]
    val mean = sum / (size * size)
    for (y in 0 until size) for (x in 0 until size) k[y][x] -= mean
    return k
}
