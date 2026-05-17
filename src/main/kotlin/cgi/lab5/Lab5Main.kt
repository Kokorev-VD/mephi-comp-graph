package cgi.lab5

import common.Image8bpp
import common.addGaussianNoise
import common.addSaltAndPepperNoise
import common.benchmark
import common.convolve
import common.psnr
import common.toImageNormalized

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

private fun partialSort(a: IntArray, k: Int, from: Int = 0, to: Int = a.size - 1) {
    var lo = from
    var hi = to
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        if (a[lo] > a[mid]) { val t = a[lo]; a[lo] = a[mid]; a[mid] = t }
        if (a[lo] > a[hi])  { val t = a[lo]; a[lo] = a[hi];  a[hi] = t }
        if (a[mid] > a[hi]) { val t = a[mid]; a[mid] = a[hi]; a[hi] = t }
        val pivot = a[mid]
        if (hi - lo < 3) break
        a[mid] = a[hi - 1]; a[hi - 1] = pivot
        var i = lo
        var j = hi - 1
        while (true) {
            while (a[++i] < pivot) { /* nothing */ }
            while (a[--j] > pivot) { /* nothing */ }
            if (i >= j) break
            val t = a[i]; a[i] = a[j]; a[j] = t
        }
        a[hi - 1] = a[i]; a[i] = pivot
        if (k < i) hi = i - 1 else lo = i + 1
    }
}

private fun partialSortRange(a: IntArray, left: Int, right: Int, from: Int = 0, to: Int = a.size - 1) {
    partialSort(a, left, from, to)
    if (right > left) {
        partialSort(a, right, left + 1, to)
    }
}

fun rankFilterPartial(image: Image8bpp, kw: Int, kh: Int, k: Int): Image8bpp {
    val size = kw * kh
    require(k in 0 until size) { "Неверный ранг k=$k для апертуры $kw x $kh" }
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val window = gatherWindow(image, x, y, kw, kh)
            partialSort(window, k)
            out.setPixel(x, y, window[k].toUByte())
        }
    }
    return out
}

fun rankFilterFullSort(image: Image8bpp, kw: Int, kh: Int, k: Int): Image8bpp {
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

fun trimmedMeanFilterPartial(image: Image8bpp, kw: Int, kh: Int, d: Int): Image8bpp {
    val size = kw * kh
    require(d in 0 until size) { "Неверное число отбрасываемых d=$d для апертуры $kw x $kh" }
    val half = d / 2
    val keep = size - 2 * half
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val window = gatherWindow(image, x, y, kw, kh)
            partialSortRange(window, half, size - 1 - half)
            var sum = 0
            for (i in half until size - half) sum += window[i]
            val avg = (sum + keep / 2) / keep
            out.setPixel(x, y, avg.coerceIn(0, 255).toUByte())
        }
    }
    return out
}

fun trimmedMeanFilterFullSort(image: Image8bpp, kw: Int, kh: Int, d: Int): Image8bpp {
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

fun rankFilter(image: Image8bpp, kw: Int, kh: Int, k: Int): Image8bpp = rankFilterPartial(image, kw, kh, k)
fun trimmedMeanFilter(image: Image8bpp, kw: Int, kh: Int, d: Int): Image8bpp = trimmedMeanFilterPartial(image, kw, kh, d)

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

fun morphologicalGradient(image: Image8bpp, se: List<Pair<Int, Int>>): Image8bpp {
    val dilated = grayscaleDilation(image, se)
    val eroded = grayscaleErosion(image, se)
    val out = Image8bpp(image.width, image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val d = dilated.getPixel(x, y).toInt()
            val e = eroded.getPixel(x, y).toInt()
            out.setPixel(x, y, (d - e).coerceIn(0, 255).toUByte())
        }
    }
    return out
}

fun seSquare(radius: Int): List<Pair<Int, Int>> {
    val list = mutableListOf<Pair<Int, Int>>()
    for (dy in -radius..radius) for (dx in -radius..radius) list += dx to dy
    return list
}

private fun laplacianKernel3(): Array<DoubleArray> = arrayOf(
    doubleArrayOf(0.0,  1.0, 0.0),
    doubleArrayOf(1.0, -4.0, 1.0),
    doubleArrayOf(0.0,  1.0, 0.0)
)

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
    val noisyImpulse = addSaltAndPepperNoise(clean, pImpulse, seed = 7L)
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

    println("Задача 4. Полная сортировка vs частичная (quickselect), O(n log n) vs O(n)")

    val benchKernels = listOf(3 to 3, 5 to 5, 7 to 7)

    for ((bk, bh) in benchKernels) {
        val bsz = bk * bh
        val medianK = bsz / 2
        val trimD = maxOf(2, bsz / 4)

        val timeFullRank = benchmark(repeats = 3) {
            rankFilterFullSort(clean, bk, bh, medianK)
        }
        val timePartRank = benchmark(repeats = 3) {
            rankFilterPartial(clean, bk, bh, medianK)
        }
        val speedupRank = timeFullRank / timePartRank

        val timeFullTrim = benchmark(repeats = 3) {
            trimmedMeanFilterFullSort(clean, bk, bh, trimD)
        }
        val timePartTrim = benchmark(repeats = 3) {
            trimmedMeanFilterPartial(clean, bk, bh, trimD)
        }
        val speedupTrim = timeFullTrim / timePartTrim

        println("Апертура ${bk}x${bh} ($bsz элементов):")
        println(String.format("  Ранговый (медиана):         полная %7.2f мс | частичная %7.2f мс | ускорение %.2fx",
            timeFullRank, timePartRank, speedupRank))
        println(String.format("  Усечённое среднее (d=%2d):   полная %7.2f мс | частичная %7.2f мс | ускорение %.2fx",
            trimD, timeFullTrim, timePartTrim, speedupTrim))
        println()
    }

    println("Задача 5. Морфологический градиент (D−E) vs ФВЧ (лапласиан)")
    val task5Result = mutableListOf<String>()
    val seGrad = seSquare(1)

    val morphGrad = morphologicalGradient(clean, seGrad)
    morphGrad.save("morph_gradient.png"); task5Result.add("morph_gradient.png")

    val lapData = convolve(clean, laplacianKernel3())
    val laplacianImg = toImageNormalized(lapData)
    laplacianImg.save("hpf_laplacian.png"); task5Result.add("hpf_laplacian.png")

    val morphGradBin = Image8bpp(clean.width, clean.height)
    val lapBin = Image8bpp(clean.width, clean.height)
    val threshold = 30
    for (y in 0 until clean.height) {
        for (x in 0 until clean.width) {
            val mg = morphGrad.getPixel(x, y).toInt()
            morphGradBin.setPixel(x, y, if (mg > threshold) 255u else 0u)
            val lp = laplacianImg.getPixel(x, y).toInt()
            lapBin.setPixel(x, y, if (lp > threshold) 255u else 0u)
        }
    }
    morphGradBin.save("morph_gradient_bin.png"); task5Result.add("morph_gradient_bin.png")
    lapBin.save("hpf_laplacian_bin.png"); task5Result.add("hpf_laplacian_bin.png")

    println("СЭ = квадрат 3x3, лапласиан 3x3, порог бинаризации: $threshold")
    println("Результат задачи 5 сохранен: $task5Result\n")

    println("Импульсный шум → медиана; AWGN → усредняющий; смешанный → усеч. среднее;")
    println("толстые контуры → морфоградиент; тонкие → LoG; резкость → unsharp mask.\n")

    println("Результаты всех задач сохранены: src/main/resources/cgi/lab5/")
}
