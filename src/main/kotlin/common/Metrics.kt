package common

import kotlin.math.log10

fun psnr(a: Image8bpp, b: Image8bpp): Double {
    require(a.width == b.width && a.height == b.height) {
        "Размеры изображений не совпадают"
    }
    var mse = 0.0
    val n = a.width.toLong() * a.height
    for (y in 0 until a.height) {
        for (x in 0 until a.width) {
            val d = a.getPixel(x, y).toInt() - b.getPixel(x, y).toInt()
            mse += d.toDouble() * d
        }
    }
    mse /= n
    if (mse == 0.0) return Double.POSITIVE_INFINITY
    return 10.0 * log10(255.0 * 255.0 / mse)
}
