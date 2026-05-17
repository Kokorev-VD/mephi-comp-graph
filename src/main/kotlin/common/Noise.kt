package common

import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

fun addGaussianNoise(image: Image8bpp, sigma: Double = 20.0, seed: Long = 42L): Image8bpp {
    val rnd = Random(seed)
    val out = Image8bpp(image.width, image.height)
    var spare: Double? = null
    fun nextGauss(): Double {
        spare?.let { spare = null; return it }
        while (true) {
            val u = 2.0 * rnd.nextDouble() - 1.0
            val v = 2.0 * rnd.nextDouble() - 1.0
            val s = u * u + v * v
            if (s > 0.0 && s < 1.0) {
                val factor = sqrt(-2.0 * ln(s) / s)
                spare = v * factor
                return u * factor
            }
        }
    }
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val v = image.getPixel(x, y).toInt()
            val n = nextGauss() * sigma
            val nv = (v + n).toInt().coerceIn(0, 255)
            out.setPixel(x, y, nv.toUByte())
        }
    }
    return out
}

fun addSaltAndPepperNoise(image: Image8bpp, probability: Double = 0.05, seed: Long = 42L): Image8bpp {
    val rnd = Random(seed)
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val r = rnd.nextDouble()
            val v = when {
                r < probability / 2.0 -> 0
                r < probability -> 255
                else -> image.getPixel(x, y).toInt()
            }
            out.setPixel(x, y, v.toUByte())
        }
    }
    return out
}
