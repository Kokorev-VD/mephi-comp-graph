package cgi.lab1

import common.Image8bpp
import common.addGaussianNoise
import common.psnr
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

private const val GLCM_TEX_SIZE = 256
private const val GLCM_TEX_SEED = 42L

private const val L = 256
private const val MAX_I = 255.0
private const val NOISE_VARIANCE = 100.0
private const val SEED = 42L

fun histogram(image: Image8bpp): IntArray {
    val h = IntArray(L)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            h[image.getPixel(x, y).toInt()]++
        }
    }
    return h
}

fun relativeHistogram(h: IntArray, n: Int): DoubleArray {
    val p = DoubleArray(L)
    for (j in 0 until L) p[j] = h[j].toDouble() / n
    return p
}

data class Stats(
    val mean: Double,
    val variance: Double,
    val q1: Double,
    val q2: Double,
    val q3: Double,
    val entropy: Double,
    val energy: Double,
    val skewness: Double,
    val kurtosis: Double,
)

fun computeStats(h: IntArray, n: Int): Stats {
    val p = relativeHistogram(h, n)

    var mean = 0.0
    for (j in 0 until L) mean += j * p[j]

    var m2 = 0.0; var m3 = 0.0; var m4 = 0.0
    for (j in 0 until L) {
        val d = j - mean
        val d2 = d * d
        m2 += d2 * p[j]
        m3 += d2 * d * p[j]
        m4 += d2 * d2 * p[j]
    }
    val sigma = sqrt(m2)

    val skewness = if (sigma > 0) m3 / (sigma * sigma * sigma) else 0.0
    val kurtosis = if (sigma > 0) m4 / (m2 * m2) - 3.0 else 0.0

    var energy = 0.0
    for (j in 0 until L) energy += p[j] * p[j]

    var entropy = 0.0
    for (j in 0 until L) if (p[j] > 0.0) entropy -= p[j] * log2(p[j])

    val q1 = quantile(h, n, 0.25)
    val q2 = quantile(h, n, 0.50)
    val q3 = quantile(h, n, 0.75)

    return Stats(mean, m2, q1, q2, q3, entropy, energy, skewness, kurtosis)
}

private fun quantile(h: IntArray, n: Int, q: Double): Double {
    val target = q * n
    var cum = 0
    for (j in 0 until L) {
        cum += h[j]
        if (cum >= target) return j.toDouble()
    }
    return (L - 1).toDouble()
}

fun glcm(image: Image8bpp, dr: Int, dc: Int): Array<DoubleArray> {
    val m = Array(L) { DoubleArray(L) }
    var nt = 0
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val y2 = y + dr
            val x2 = x + dc
            if (y2 in 0 until image.height && x2 in 0 until image.width) {
                val a = image.getPixel(x, y).toInt()
                val b = image.getPixel(x2, y2).toInt()
                m[a][b]++
                nt++
            }
        }
    }
    if (nt > 0) {
        for (a in 0 until L) for (b in 0 until L) m[a][b] /= nt
    }
    return m
}

fun glcmEnergy(m: Array<DoubleArray>): Double {
    var s = 0.0
    for (a in 0 until L) for (b in 0 until L) s += m[a][b] * m[a][b]
    return s
}

fun glcmToImage(m: Array<DoubleArray>, log: Boolean): Image8bpp {
    val img = Image8bpp(L, L)
    var mx = 0.0
    for (a in 0 until L) for (b in 0 until L) if (m[a][b] > mx) mx = m[a][b]
    if (mx <= 0.0) return img
    if (log) {
        val denom = ln(1.0 + mx)
        for (a in 0 until L) for (b in 0 until L) {
            val v = ln(1.0 + m[a][b]) / denom
            img.setPixel(b, a, (v * 255.0).roundToInt().coerceIn(0, 255).toUByte())
        }
    } else {
        for (a in 0 until L) for (b in 0 until L) {
            val v = m[a][b] / mx
            img.setPixel(b, a, (v * 255.0).roundToInt().coerceIn(0, 255).toUByte())
        }
    }
    return img
}

fun textureStripes(w: Int = GLCM_TEX_SIZE, h: Int = GLCM_TEX_SIZE, period: Int = 8): Image8bpp {
    val img = Image8bpp(w, h)
    for (y in 0 until h) for (x in 0 until w) {
        img.setPixel(x, y, if ((x / period) % 2 == 0) 0u else 255u)
    }
    return img
}

fun textureCheckerboard(w: Int = GLCM_TEX_SIZE, h: Int = GLCM_TEX_SIZE, cell: Int = 16): Image8bpp {
    val img = Image8bpp(w, h)
    for (y in 0 until h) for (x in 0 until w) {
        val cb = ((x / cell) + (y / cell)) % 2
        img.setPixel(x, y, if (cb == 0) 0u else 255u)
    }
    return img
}

fun textureNoise(w: Int = GLCM_TEX_SIZE, h: Int = GLCM_TEX_SIZE, seed: Long = GLCM_TEX_SEED): Image8bpp {
    val img = Image8bpp(w, h)
    val rnd = Random(seed)
    for (y in 0 until h) for (x in 0 until w) {
        img.setPixel(x, y, rnd.nextInt(L).toUByte())
    }
    return img
}

fun renderHistogram(h: IntArray, height: Int = 200): Image8bpp {
    val img = Image8bpp(L, height)
    img.fill(255u)
    val maxH = (h.max().takeIf { it > 0 } ?: 1)
    for (j in 0 until L) {
        val barH = (h[j].toDouble() / maxH * (height - 1)).roundToInt().coerceIn(0, height - 1)
        for (y in (height - barH) until height) img.setPixel(j, y, 0u)
    }
    return img
}

fun main() {
    println("Лабораторная работа No 1: Статистики изображения, GLCM, AWGN и PSNR\n")

    val src = "src/main/resources/common/cat.png"
    val image = Image8bpp.load(src)
    val n = image.width * image.height
    println("Изображение: $src, ${image.width}×${image.height}")

    println("Задача 1. Гистограмма яркостей и скалярные статистики")
    val task1Result = mutableListOf<String>()

    val h = histogram(image)
    renderHistogram(h).save("histogram.png")
    task1Result.add("histogram.png")

    val s = computeStats(h, n)
    println("Среднее            : %.4f".format(s.mean))
    println("Дисперсия          : %.4f".format(s.variance))
    println("Квартиль Q1 (25%%) : %.1f".format(s.q1))
    println("Квартиль Q2 (50%%) : %.1f".format(s.q2))
    println("Квартиль Q3 (75%%) : %.1f".format(s.q3))
    println("Межквартильный раз.: %.1f".format(s.q3 - s.q1))
    println("Энтропия Hn        : %.4f бит".format(s.entropy))
    println("Энергия En         : %.6f".format(s.energy))
    println("Асимметрия A       : %.4f".format(s.skewness))
    println("Эксцесс E          : %.4f".format(s.kurtosis))

    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Матрица совместной встречаемости (GLCM) и её энергия")
    for ((dr, dc) in listOf(0 to 1, 1 to 0)) {
        val m = glcm(image, dr, dc)
        val e = glcmEnergy(m)
        println("GLCM (dr=%d, dc=%d): энергия BE = %.6e".format(dr, dc, e))
    }
    println("Результат задачи 2: значения энергии выше\n")

    println("Задача 3. Аддитивный белый гауссов шум (AWGN) и PSNR")
    val task3Result = mutableListOf<String>()

    val noisy = addGaussianNoise(image, sqrt(NOISE_VARIANCE), SEED)
    noisy.save("noisy.png")
    task3Result.add("noisy.png")

    val psnrValue = psnr(image, noisy)
    println("AWGN: σ² = %.1f (σ = %.2f), seed = %d".format(NOISE_VARIANCE, sqrt(NOISE_VARIANCE), SEED))
    println("PSNR оригинал/зашумлённое: %.4f дБ".format(psnrValue))

    val hNoisy = histogram(noisy)
    renderHistogram(hNoisy).save("histogram_noisy.png")
    task3Result.add("histogram_noisy.png")

    println("Результат задачи 3 сохранен: $task3Result\n")

    println("Задача 4 (доработка). GLCM как grayscale изображение: фото vs текстуры")
    val task4Result = mutableListOf<String>()

    val sources = mutableListOf<Triple<String, Image8bpp, Boolean>>()
    for (photoName in listOf("cat", "evening", "squirrel")) {
        sources += Triple(photoName, Image8bpp.load("src/main/resources/common/$photoName.png"), false)
    }
    sources += Triple("tex_stripes_v", textureStripes(), true)
    sources += Triple("tex_stripes_h", textureStripes(), true)
    sources += Triple("tex_checker", textureCheckerboard(), true)
    sources += Triple("tex_noise", textureNoise(), true)

    val dr4 = 0
    val dc4 = 1
    println("Смещение: dr=$dr4, dc=$dc4 (горизонтальный сосед)")
    println("%-16s %-12s %-14s %-12s %-10s".format("источник", "размер", "энергия GLCM", "max P", "≥1% max"))

    for ((srcName, srcImage, saveSrc) in sources) {
        if (saveSrc) {
            srcImage.save("$srcName.png")
            task4Result.add("$srcName.png")
        }
        val m = glcm(srcImage, dr4, dc4)
        val e = glcmEnergy(m)
        var mx = 0.0
        for (a in 0 until L) for (b in 0 until L) if (m[a][b] > mx) mx = m[a][b]
        var occupied = 0
        if (mx > 0.0) for (a in 0 until L) for (b in 0 until L) if (m[a][b] >= 0.01 * mx) occupied++
        println("%-16s %-12s %-14.6e %-12.6f %-10d".format(
            srcName, "${srcImage.width}x${srcImage.height}", e, mx, occupied))
        val lin = "glcm_${srcName}_lin.png"
        val log = "glcm_${srcName}_log.png"
        glcmToImage(m, log = false).save(lin)
        glcmToImage(m, log = true).save(log)
        task4Result.add(lin)
        task4Result.add(log)
    }
    println("Результат задачи 4 сохранен (${task4Result.size} файлов): см. префикс glcm_*\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab1/")
}
