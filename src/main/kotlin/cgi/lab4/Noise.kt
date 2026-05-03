package cgi.lab4

import common.Image8bpp
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.PI
import kotlin.random.Random

fun addGaussianNoise(image: Image8bpp, sigma: Double = 20.0, seed: Long = 42L): Image8bpp {
    val rnd = Random(seed)
    val w = image.width
    val h = image.height
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val u1 = rnd.nextDouble().coerceAtLeast(1e-12)
            val u2 = rnd.nextDouble()
            val z = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
            val v = (image.getPixel(x, y).toInt() + z * sigma).toInt().coerceIn(0, 255)
            out.setPixel(x, y, v.toUByte())
        }
    }
    return out
}

fun addSaltAndPepperNoise(image: Image8bpp, probability: Double = 0.05, seed: Long = 42L): Image8bpp {
    val rnd = Random(seed)
    val w = image.width
    val h = image.height
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
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
