package cgi.lab2

import common.Image8bpp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin

private data class Cx(val re: Double, val im: Double) {
    operator fun plus(o: Cx) = Cx(re + o.re, im + o.im)
    operator fun minus(o: Cx) = Cx(re - o.re, im - o.im)
    operator fun times(o: Cx) = Cx(re * o.re - im * o.im, re * o.im + im * o.re)
    fun abs(): Double = hypot(re, im)
}

private fun nextPowerOfTwo(n: Int): Int {
    var p = 1
    while (p < n) p = p shl 1
    return p
}

private fun bitReverse(a: Array<Cx>) {
    val n = a.size
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j and bit != 0) {
            j = j xor bit
            bit = bit shr 1
        }
        j = j or bit
        if (i < j) {
            val tmp = a[i]; a[i] = a[j]; a[j] = tmp
        }
    }
}

private fun fft1d(a: Array<Cx>) {
    val n = a.size
    require(n > 0 && (n and (n - 1)) == 0) { "Размер должен быть степенью 2" }
    bitReverse(a)
    var len = 2
    while (len <= n) {
        val half = len / 2
        val ang = -2.0 * PI / len
        val wStepRe = cos(ang)
        val wStepIm = sin(ang)
        var i = 0
        while (i < n) {
            var wRe = 1.0
            var wIm = 0.0
            for (k in 0 until half) {
                val u = a[i + k]
                val tRe = wRe * a[i + k + half].re - wIm * a[i + k + half].im
                val tIm = wRe * a[i + k + half].im + wIm * a[i + k + half].re
                a[i + k] = Cx(u.re + tRe, u.im + tIm)
                a[i + k + half] = Cx(u.re - tRe, u.im - tIm)
                val nwRe = wRe * wStepRe - wIm * wStepIm
                val nwIm = wRe * wStepIm + wIm * wStepRe
                wRe = nwRe; wIm = nwIm
            }
            i += len
        }
        len = len shl 1
    }
    val inv = 1.0 / n
    for (i in 0 until n) a[i] = Cx(a[i].re * inv, a[i].im * inv)
}

private fun fft2d(input: Array<DoubleArray>): Triple<Array<Array<Cx>>, Int, Int> {
    val h = input.size
    val w = input[0].size
    val ph = nextPowerOfTwo(h)
    val pw = nextPowerOfTwo(w)
    val data = Array(ph) { y ->
        Array(pw) { x ->
            if (y < h && x < w) Cx(input[y][x], 0.0) else Cx(0.0, 0.0)
        }
    }
    for (y in 0 until ph) {
        val row = data[y]
        fft1d(row)
    }
    val col = Array(ph) { Cx(0.0, 0.0) }
    for (x in 0 until pw) {
        for (y in 0 until ph) col[y] = data[y][x]
        fft1d(col)
        for (y in 0 until ph) data[y][x] = col[y]
    }
    return Triple(data, ph, pw)
}

private fun fftShift2d(spec: Array<Array<Cx>>) {
    val h = spec.size
    val w = spec[0].size
    val hh = h / 2
    val hw = w / 2
    for (y in 0 until hh) {
        for (x in 0 until hw) {
            val t1 = spec[y][x]
            spec[y][x] = spec[y + hh][x + hw]
            spec[y + hh][x + hw] = t1
            val t2 = spec[y][x + hw]
            spec[y][x + hw] = spec[y + hh][x]
            spec[y + hh][x] = t2
        }
    }
}

private fun renderLogMagnitude(spec: Array<Array<Cx>>): Image8bpp {
    val h = spec.size
    val w = spec[0].size
    val mag = Array(h) { y -> DoubleArray(w) { x -> ln(1.0 + spec[y][x].abs()) } }
    var minV = Double.POSITIVE_INFINITY
    var maxV = Double.NEGATIVE_INFINITY
    for (y in 0 until h) for (x in 0 until w) {
        val v = mag[y][x]
        if (v < minV) minV = v
        if (v > maxV) maxV = v
    }
    val img = Image8bpp(w, h)
    val span = if (maxV > minV) maxV - minV else 1.0
    for (y in 0 until h) {
        for (x in 0 until w) {
            val g = ((mag[y][x] - minV) / span * 255.0).toInt().coerceIn(0, 255).toUByte()
            img.setPixel(x, y, g)
        }
    }
    return img
}

private fun render1dSpectrumAsImage(spec1d: Array<Cx>, barHeight: Int): Image8bpp {
    val w = spec1d.size
    val h = barHeight
    val mag = DoubleArray(w) { ln(1.0 + spec1d[it].abs()) }
    val minV = mag.min()
    val maxV = mag.max()
    val span = if (maxV > minV) maxV - minV else 1.0
    val img = Image8bpp(w, h)
    for (x in 0 until w) {
        val g = ((mag[x] - minV) / span * 255.0).toInt().coerceIn(0, 255).toUByte()
        for (y in 0 until h) img.setPixel(x, y, g)
    }
    return img
}

private fun fftShift1d(a: Array<Cx>) {
    val n = a.size
    val half = n / 2
    for (i in 0 until half) {
        val t = a[i]; a[i] = a[i + half]; a[i + half] = t
    }
}

private fun render1dSignal(signal: DoubleArray, width: Int, height: Int): Image8bpp {
    val img = Image8bpp(width, height)
    img.fill(255u)
    val minV = signal.min()
    val maxV = signal.max()
    val span = if (maxV > minV) maxV - minV else 1.0
    val n = signal.size
    for (x in 0 until width) {
        val idx = (x.toLong() * n / width).toInt().coerceAtMost(n - 1)
        val v = (signal[idx] - minV) / span
        val y = ((1.0 - v) * (height - 1)).toInt().coerceIn(0, height - 1)
        img.setPixel(x, y, 0u)
    }
    return img
}

private const val RES_COMMON = "src/main/resources/common"

private fun runImageExample(name: String, fileName: String, results: MutableList<String>) {
    val src = Image8bpp.load("$RES_COMMON/$fileName")
    val h = src.height
    val w = src.width
    val data = Array(h) { y -> DoubleArray(w) { x -> src.getPixel(x, y).toInt().toDouble() } }
    val (spec, ph, pw) = fft2d(data)
    println("Изображение $name: ${w}x${h}, дополнение нулями до ${pw}x${ph}")
    fftShift2d(spec)
    val img = renderLogMagnitude(spec)
    val outName = "spectrum_$name.png"
    img.save(outName)
    results.add(outName)
}

private fun runSyntheticSinusoidsExample(results: MutableList<String>) {
    val w = 256
    val h = 256
    val data = Array(h) { y ->
        DoubleArray(w) { x ->
            val s1 = cos(2.0 * PI * (8.0 * x) / w)
            val s2 = cos(2.0 * PI * (16.0 * y) / h)
            val s3 = cos(2.0 * PI * (4.0 * x + 4.0 * y) / w)
            val s4 = sin(2.0 * PI * (24.0 * x - 12.0 * y) / w)
            128.0 + 30.0 * (s1 + s2 + s3 + s4)
        }
    }
    val src = Image8bpp(w, h)
    for (y in 0 until h) for (x in 0 until w) {
        src.setPixel(x, y, data[y][x].toInt().coerceIn(0, 255).toUByte())
    }
    src.save("synthetic_sinusoids.png")
    results.add("synthetic_sinusoids.png")

    val (spec, ph, pw) = fft2d(data)
    println("Синтетическая смесь 2D синусоид: ${w}x${h}, дополнение нулями до ${pw}x${ph}")
    fftShift2d(spec)
    val img = renderLogMagnitude(spec)
    img.save("spectrum_synthetic_sinusoids.png")
    results.add("spectrum_synthetic_sinusoids.png")
}

private fun run1dSignalExample(results: MutableList<String>) {
    val n = 500
    val signal = DoubleArray(n) { i ->
        val t = i.toDouble() / n
        sin(2.0 * PI * 5.0 * t) + 0.5 * sin(2.0 * PI * 23.0 * t) + 0.3 * sin(2.0 * PI * 60.0 * t)
    }

    val sigImg = render1dSignal(signal, 512, 128)
    sigImg.save("signal_1d.png")
    results.add("signal_1d.png")

    val pn = nextPowerOfTwo(n)
    println("1D сигнал: $n отсчётов, дополнение нулями до $pn")

    val a = Array(pn) { i -> if (i < n) Cx(signal[i], 0.0) else Cx(0.0, 0.0) }
    fft1d(a)
    fftShift1d(a)

    val specImg = render1dSpectrumAsImage(a, barHeight = 64)
    specImg.save("spectrum_signal_1d.png")
    results.add("spectrum_signal_1d.png")
}

fun main() {

    println("Лабораторная работа No 2: Быстрое двумерное преобразование Фурье\n")

    println("Задача 1. БПФ одномерного сигнала и логарифм амплитудного спектра")
    val task1Result = mutableListOf<String>()
    run1dSignalExample(task1Result)
    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. БПФ двумерного изображения произвольного размера")
    val task2Result = mutableListOf<String>()
    runSyntheticSinusoidsExample(task2Result)
    runImageExample("cat", "cat.png", task2Result)
    runImageExample("evening", "evening.png", task2Result)
    runImageExample("squirrel", "squirrel.png", task2Result)
    println("Результат задачи 2 сохранен: $task2Result\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab2/")
}
