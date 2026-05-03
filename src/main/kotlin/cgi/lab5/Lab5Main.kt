package cgi.lab5

import common.Image8bpp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.random.Random

private fun reflect(coord: Int, size: Int): Int {
    var c = coord
    if (c < 0) c = -c - 1
    if (c >= size) c = 2 * size - c - 1
    return c.coerceIn(0, size - 1)
}

private fun gatherWindow(image: Image8bpp, x: Int, y: Int, kw: Int, kh: Int): IntArray {
    val rx = kw / 2
    val ry = kh / 2
    val window = IntArray(kw * kh)
    var idx = 0
    for (dy in -ry..ry) {
        for (dx in -rx..rx) {
            val sx = reflect(x + dx, image.width)
            val sy = reflect(y + dy, image.height)
            window[idx++] = image.getPixel(sx, sy).toInt()
        }
    }
    return window
}

fun rankFilter(image: Image8bpp, kw: Int, kh: Int, k: Int): Image8bpp {
    val size = kw * kh
    require(k in 0 until size) { "Неверный ранг k=$k для апертуры $kw x $kh" }
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val window = gatherWindow(image, x, y, kw, kh)
            window.sort()
            out.setPixel(x, y, window[k].toUByte())
        }
    }
    return out
}

fun averagingFilter(image: Image8bpp, kw: Int, kh: Int): Image8bpp {
    val out = Image8bpp(image.width, image.height)
    val size = kw * kh
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val window = gatherWindow(image, x, y, kw, kh)
            var sum = 0
            for (v in window) sum += v
            val avg = (sum + size / 2) / size
            out.setPixel(x, y, avg.coerceIn(0, 255).toUByte())
        }
    }
    return out
}

fun medianFilter(image: Image8bpp, kw: Int, kh: Int): Image8bpp {
    val k = (kw * kh) / 2
    return rankFilter(image, kw, kh, k)
}

fun trimmedMeanFilter(image: Image8bpp, kw: Int, kh: Int, d: Int): Image8bpp {
    val size = kw * kh
    require(d in 0 until size) { "Неверное число отбрасываемых d=$d для апертуры $kw x $kh" }
    val half = d / 2
    val keep = size - 2 * half
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val window = gatherWindow(image, x, y, kw, kh)
            window.sort()
            var sum = 0
            for (i in half until size - half) sum += window[i]
            val avg = (sum + keep / 2) / keep
            out.setPixel(x, y, avg.coerceIn(0, 255).toUByte())
        }
    }
    return out
}

fun grayscaleErosion(image: Image8bpp, structuringElement: List<Pair<Int, Int>>): Image8bpp {
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            var minVal = 255
            for ((dx, dy) in structuringElement) {
                val sx = reflect(x + dx, image.width)
                val sy = reflect(y + dy, image.height)
                val v = image.getPixel(sx, sy).toInt()
                if (v < minVal) minVal = v
            }
            out.setPixel(x, y, minVal.toUByte())
        }
    }
    return out
}

fun grayscaleDilation(image: Image8bpp, structuringElement: List<Pair<Int, Int>>): Image8bpp {
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            var maxVal = 0
            for ((dx, dy) in structuringElement) {
                val sx = reflect(x + dx, image.width)
                val sy = reflect(y + dy, image.height)
                val v = image.getPixel(sx, sy).toInt()
                if (v > maxVal) maxVal = v
            }
            out.setPixel(x, y, maxVal.toUByte())
        }
    }
    return out
}

fun grayscaleOpening(image: Image8bpp, se: List<Pair<Int, Int>>): Image8bpp =
    grayscaleDilation(grayscaleErosion(image, se), se)

fun grayscaleClosing(image: Image8bpp, se: List<Pair<Int, Int>>): Image8bpp =
    grayscaleErosion(grayscaleDilation(image, se), se)

fun seSquare(radius: Int): List<Pair<Int, Int>> {
    val list = mutableListOf<Pair<Int, Int>>()
    for (dy in -radius..radius) for (dx in -radius..radius) list += dx to dy
    return list
}

fun addImpulseNoise(image: Image8bpp, probability: Double, seed: Long = 42L): Image8bpp {
    val rnd = Random(seed)
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val v = image.getPixel(x, y)
            if (rnd.nextDouble() < probability) {
                val sp = if (rnd.nextDouble() < 0.5) 0u.toUByte() else 255u.toUByte()
                out.setPixel(x, y, sp)
            } else {
                out.setPixel(x, y, v)
            }
        }
    }
    return out
}

fun addGaussianNoise(image: Image8bpp, sigma: Double, seed: Long = 42L): Image8bpp {
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

fun psnr(reference: Image8bpp, test: Image8bpp): Double {
    require(reference.width == test.width && reference.height == test.height) {
        "Размеры изображений не совпадают"
    }
    var mse = 0.0
    val n = reference.width.toLong() * reference.height
    for (y in 0 until reference.height) {
        for (x in 0 until reference.width) {
            val d = reference.getPixel(x, y).toInt() - test.getPixel(x, y).toInt()
            mse += (d * d).toDouble()
        }
    }
    mse /= n
    if (mse == 0.0) return Double.POSITIVE_INFINITY
    val max = 255.0
    return 10.0 * log10(max * max / mse)
}

fun main() {
    println("Лабораторная работа No 5: Ранговая фильтрация и полутоновая морфология\n")

    val srcPath = "src/main/resources/common/cat.png"
    val clean = Image8bpp.load(srcPath)
    println("Изображение: $srcPath, ${clean.width} x ${clean.height}\n")
    clean.save("clean.png")

    println("Задача 1. Сравнение фильтров по PSNR (усредняющий, медианный, усечённое среднее)")
    val task1Result = mutableListOf<String>()
    val kw = 3
    val kh = 3
    val d = 2
    println("Апертура 3x3, фильтр усечённого среднего: d=$d (по ${d / 2} с каждой стороны)")

    val pImpulse = 0.05
    val noisyImpulse = addImpulseNoise(clean, pImpulse, seed = 7L)
    noisyImpulse.save("noisy_impulse.png"); task1Result.add("noisy_impulse.png")
    val avgImpulse = averagingFilter(noisyImpulse, kw, kh)
    val medImpulse = medianFilter(noisyImpulse, kw, kh)
    val trimImpulse = trimmedMeanFilter(noisyImpulse, kw, kh, d)
    avgImpulse.save("filt_impulse_avg.png"); task1Result.add("filt_impulse_avg.png")
    medImpulse.save("filt_impulse_median.png"); task1Result.add("filt_impulse_median.png")
    trimImpulse.save("filt_impulse_trimmed.png"); task1Result.add("filt_impulse_trimmed.png")

    val sigma = 15.0
    val noisyGauss = addGaussianNoise(clean, sigma, seed = 11L)
    noisyGauss.save("noisy_gauss.png"); task1Result.add("noisy_gauss.png")
    val avgGauss = averagingFilter(noisyGauss, kw, kh)
    val medGauss = medianFilter(noisyGauss, kw, kh)
    val trimGauss = trimmedMeanFilter(noisyGauss, kw, kh, d)
    avgGauss.save("filt_gauss_avg.png"); task1Result.add("filt_gauss_avg.png")
    medGauss.save("filt_gauss_median.png"); task1Result.add("filt_gauss_median.png")
    trimGauss.save("filt_gauss_trimmed.png"); task1Result.add("filt_gauss_trimmed.png")

    println()
    println("PSNR (дБ) относительно эталона:")
    println(String.format("%-30s %10s %10s", "Изображение", "Импульс", "Гаусс"))
    println(String.format("%-30s %10.3f %10.3f", "Зашумлённое (без фильтрации)",
        psnr(clean, noisyImpulse), psnr(clean, noisyGauss)))
    println(String.format("%-30s %10.3f %10.3f", "Усредняющий (3x3)",
        psnr(clean, avgImpulse), psnr(clean, avgGauss)))
    println(String.format("%-30s %10.3f %10.3f", "Медианный (3x3)",
        psnr(clean, medImpulse), psnr(clean, medGauss)))
    println(String.format("%-30s %10.3f %10.3f", "Усечённое среднее (3x3, d=2)",
        psnr(clean, trimImpulse), psnr(clean, trimGauss)))
    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Ранговая фильтрация (апертура 5x5)")
    val task2Result = mutableListOf<String>()
    val r5 = 5
    val sz = r5 * r5
    val rankMin = rankFilter(clean, r5, r5, 0)
    val rankMed = rankFilter(clean, r5, r5, sz / 2)
    val rankMax = rankFilter(clean, r5, r5, sz - 1)
    rankMin.save("rank_5x5_min.png"); task2Result.add("rank_5x5_min.png")
    rankMed.save("rank_5x5_median.png"); task2Result.add("rank_5x5_median.png")
    rankMax.save("rank_5x5_max.png"); task2Result.add("rank_5x5_max.png")
    println("k=0 (минимум), k=${sz / 2} (медиана), k=${sz - 1} (максимум)")
    println("Результат задачи 2 сохранен: $task2Result\n")

    println("Задача 3. Полутоновая морфология (СЭ — квадрат 3x3)")
    val task3Result = mutableListOf<String>()
    val se = seSquare(1)
    val erosion = grayscaleErosion(clean, se)
    val dilation = grayscaleDilation(clean, se)
    val opening = grayscaleOpening(clean, se)
    val closing = grayscaleClosing(clean, se)
    erosion.save("morph_erosion.png"); task3Result.add("morph_erosion.png")
    dilation.save("morph_dilation.png"); task3Result.add("morph_dilation.png")
    opening.save("morph_opening.png"); task3Result.add("morph_opening.png")
    closing.save("morph_closing.png"); task3Result.add("morph_closing.png")
    println("Результат задачи 3 сохранен: $task3Result\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab5/")
}
