package cgi.lab3

import common.Image8bpp
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MAX_I = 255.0

typealias Sampler = (image: Image8bpp, xs: Double, ys: Double) -> Double

private val nearest: Sampler = { img, xs, ys ->
    val xi = floor(xs + 0.5).toInt()
    val yi = floor(ys + 0.5).toInt()
    if (xi in 0 until img.width && yi in 0 until img.height) {
        img.getPixel(xi, yi).toInt().toDouble()
    } else 0.0
}

private val bilinear: Sampler = { img, xs, ys ->
    val x0 = floor(xs).toInt()
    val y0 = floor(ys).toInt()
    val x = xs - x0
    val y = ys - y0
    val f00 = pixelOrZero(img, x0,     y0)
    val f10 = pixelOrZero(img, x0 + 1, y0)
    val f01 = pixelOrZero(img, x0,     y0 + 1)
    val f11 = pixelOrZero(img, x0 + 1, y0 + 1)
    f00 * (1 - x) * (1 - y) +
    f10 * x       * (1 - y) +
    f01 * (1 - x) * y       +
    f11 * x       * y
}

private const val A = -0.5

private fun cubicKernel(s: Double): Double {
    val a = if (s < 0) -s else s
    return when {
        a < 1.0 -> (A + 2) * a * a * a - (A + 3) * a * a + 1
        a < 2.0 -> A * a * a * a - 5 * A * a * a + 8 * A * a - 4 * A
        else -> 0.0
    }
}

private val bicubic: Sampler = { img, xs, ys ->
    val x0 = floor(xs).toInt()
    val y0 = floor(ys).toInt()
    val dx = xs - x0
    val dy = ys - y0
    var sum = 0.0
    for (m in -1..2) {
        val ky = cubicKernel(m - dy)
        if (ky == 0.0) continue
        var rowSum = 0.0
        for (n in -1..2) {
            val kx = cubicKernel(n - dx)
            if (kx == 0.0) continue
            rowSum += pixelOrZero(img, x0 + n, y0 + m) * kx
        }
        sum += rowSum * ky
    }
    sum
}

private fun pixelOrZero(img: Image8bpp, x: Int, y: Int): Double {
    if (x !in 0 until img.width || y !in 0 until img.height) return 0.0
    return img.getPixel(x, y).toInt().toDouble()
}

fun rotate(src: Image8bpp, angleRad: Double, sampler: Sampler): Image8bpp {
    val w = src.width
    val h = src.height
    val cx = (w - 1) / 2.0
    val cy = (h - 1) / 2.0
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        val dy = y - cy
        for (x in 0 until w) {
            val dx = x - cx
            val xs =  cosA * dx + sinA * dy + cx
            val ys = -sinA * dx + cosA * dy + cy
            val v = sampler(src, xs, ys).coerceIn(0.0, MAX_I).roundToInt().toUByte()
            out.setPixel(x, y, v)
        }
    }
    return out
}

fun psnrCentralCircle(a: Image8bpp, b: Image8bpp): Double {
    require(a.width == b.width && a.height == b.height)
    val w = a.width
    val h = a.height
    val cx = (w - 1) / 2.0
    val cy = (h - 1) / 2.0
    val r = (minOf(w, h) / 2.0) - 2.0
    val r2 = r * r
    var mse = 0.0
    var n = 0
    for (y in 0 until h) {
        val dy = y - cy
        for (x in 0 until w) {
            val dx = x - cx
            if (dx * dx + dy * dy > r2) continue
            val d = a.getPixel(x, y).toInt() - b.getPixel(x, y).toInt()
            mse += d.toDouble() * d
            n++
        }
    }
    if (n == 0) return Double.NaN
    mse /= n
    if (mse == 0.0) return Double.POSITIVE_INFINITY
    return 10.0 * log10(MAX_I * MAX_I / mse)
}

fun main() {
    println("Лабораторная работа No 3: Поворот изображения с интерполяцией\n")

    val src = "src/main/resources/common/cat.png"
    val image = Image8bpp.load(src)
    println("Изображение: $src, ${image.width} x ${image.height}\n")

    println("Задача 1. Поворот изображения на заданный угол с интерполяцией")
    val angleDeg = 30.0
    val angleRad = Math.toRadians(angleDeg)
    println("Угол поворота: %.2f°\n".format(angleDeg))

    val methods = listOf(
        Triple("nearest",  "nn",       nearest),
        Triple("bilinear", "bilinear", bilinear),
        Triple("bicubic",  "bicubic",  bicubic),
    )

    val taskResult = mutableListOf<String>()

    println("Метод           Поворот, мс   Туда+обратно, мс   PSNR, дБ")
    println("---------------------------------------------------------")
    for ((name, suffix, sampler) in methods) {
        val t0 = System.nanoTime()
        val rotated = rotate(image, angleRad, sampler)
        val tForward = (System.nanoTime() - t0) / 1e6

        val rotatedName = "rotated_${suffix}.png"
        rotated.save(rotatedName)
        taskResult.add(rotatedName)

        val tt0 = System.nanoTime()
        val back = rotate(rotated, -angleRad, sampler)
        val tBack = (System.nanoTime() - tt0) / 1e6

        val psnr = psnrCentralCircle(image, back)

        val roundtripName = "roundtrip_${suffix}.png"
        back.save(roundtripName)
        taskResult.add(roundtripName)

        val psnrStr = if (psnr.isInfinite()) "∞" else "%.3f".format(psnr)
        println(
            "%-13s   %9.2f      %12.2f       %s".format(
                name, tForward, tForward + tBack, psnrStr
            )
        )
    }

    println("\nРезультат задачи сохранен: $taskResult\n")
    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab3/")
}
