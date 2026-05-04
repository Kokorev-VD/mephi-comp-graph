package cgi.lab6

import common.Image8bpp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val MIN_AREA = 30

private const val CIRCLE_KR2_TOLERANCE = 0.03

fun main() {
    println("Лабораторная работа No 6: Бинаризация (Оцу), разметка областей, моменты\n")

    println("Задача 1. Бинаризация изображения по порогу Оцу")
    val task1Result = mutableListOf<String>()
    val input = generateTestImage(256, 256)
    input.save("input.png")
    task1Result.add("input.png")
    val threshold = otsuThreshold(input)
    println("Порог Оцу: $threshold")
    val binary = binarize(input, threshold)
    binary.save("binary.png")
    task1Result.add("binary.png")
    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Разметка 4-связных областей (FloodFill)")
    val task2Result = mutableListOf<String>()
    val labels = labelConnectedComponents4(binary)
    val regionsCount = labels.maxOrNull() ?: 0
    println("Найдено областей: $regionsCount")
    val labelImage = renderLabels(labels, binary.width, binary.height)
    labelImage.save("labels.png")
    task2Result.add("labels.png")
    println("Результат задачи 2 сохранен: $task2Result\n")

    println("Задача 3. Геометрические моменты областей и отбор круглых (S > $MIN_AREA)")
    val regions = computeRegionMoments(labels, binary.width, binary.height, regionsCount)
    var circular = 0
    println("  №  площадь    xc        yc        mu20       mu02       mu11      Kr2    круг?")
    for (r in regions) {
        val kr2 = (r.mu20 + r.mu02) * 2.0 * PI / (r.area.toDouble() * r.area.toDouble())
        val isCircle = r.area > MIN_AREA && abs(kr2 - 1.0) <= CIRCLE_KR2_TOLERANCE
        if (isCircle) circular++
        println(
            "%3d  %6d  %8.2f  %8.2f  %10.2f %10.2f %10.2f  %5.3f  %s".format(
                r.label, r.area, r.xc, r.yc, r.mu20, r.mu02, r.mu11, kr2,
                if (isCircle) "да" else "нет"
            )
        )
    }
    println("Круглых областей с площадью более $MIN_AREA пикселей: $circular")
    println("Результат задачи 3: значения моментов и количество круглых областей напечатаны выше\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab6/")
}

private fun generateTestImage(width: Int, height: Int): Image8bpp {
    val img = Image8bpp(width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            img.setPixel(x, y, (40 + ((x + y) % 11)).toUByte())
        }
    }
    fillCircle(img, 50, 60, 25, 220u)
    fillCircle(img, 130, 60, 12, 230u)
    fillCircle(img, 180, 60, 3, 215u)
    fillRect(img, 30, 130, 80, 180, 200u)
    fillRect(img, 100, 140, 220, 160, 210u)
    fillEllipse(img, 60, 220, 40, 18, 225u)
    fillTriangle(img, 160, 200, 230, 200, 195, 245, 215u)
    fillCircle(img, 210, 110, 18, 235u)
    return img
}

private fun fillCircle(img: Image8bpp, cx: Int, cy: Int, r: Int, value: UByte) {
    for (y in max(0, cy - r) until min(img.height, cy + r + 1)) {
        for (x in max(0, cx - r) until min(img.width, cx + r + 1)) {
            val dx = (x - cx).toDouble()
            val dy = (y - cy).toDouble()
            if (dx * dx + dy * dy <= (r * r).toDouble()) img.setPixel(x, y, value)
        }
    }
}

private fun fillRect(img: Image8bpp, x0: Int, y0: Int, x1: Int, y1: Int, value: UByte) {
    for (y in y0..y1) for (x in x0..x1) {
        if (x in 0 until img.width && y in 0 until img.height) img.setPixel(x, y, value)
    }
}

private fun fillEllipse(img: Image8bpp, cx: Int, cy: Int, a: Int, b: Int, value: UByte) {
    for (y in max(0, cy - b) until min(img.height, cy + b + 1)) {
        for (x in max(0, cx - a) until min(img.width, cx + a + 1)) {
            val dx = (x - cx).toDouble() / a
            val dy = (y - cy).toDouble() / b
            if (dx * dx + dy * dy <= 1.0) img.setPixel(x, y, value)
        }
    }
}

private fun fillTriangle(
    img: Image8bpp,
    x0: Int, y0: Int, x1: Int, y1: Int, x2: Int, y2: Int, value: UByte
) {
    val minX = max(0, minOf(x0, x1, x2))
    val maxX = min(img.width - 1, maxOf(x0, x1, x2))
    val minY = max(0, minOf(y0, y1, y2))
    val maxY = min(img.height - 1, maxOf(y0, y1, y2))
    for (y in minY..maxY) for (x in minX..maxX) {
        val s1 = (x1 - x0) * (y - y0) - (y1 - y0) * (x - x0)
        val s2 = (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1)
        val s3 = (x0 - x2) * (y - y2) - (y0 - y2) * (x - x2)
        val hasNeg = s1 < 0 || s2 < 0 || s3 < 0
        val hasPos = s1 > 0 || s2 > 0 || s3 > 0
        if (!(hasNeg && hasPos)) img.setPixel(x, y, value)
    }
}

private fun otsuThreshold(img: Image8bpp): Int {
    val hist = IntArray(256)
    for (y in 0 until img.height) {
        for (x in 0 until img.width) hist[img.getPixel(x, y).toInt()]++
    }
    val total = img.width * img.height
    var sumAll = 0.0
    for (i in 0..255) sumAll += i * hist[i].toDouble()

    var sumB = 0.0
    var wB = 0
    var bestSigma = -1.0
    var bestT = 0
    for (t in 0..255) {
        wB += hist[t]
        if (wB == 0) continue
        val wF = total - wB
        if (wF == 0) break
        sumB += t * hist[t].toDouble()
        val mB = sumB / wB
        val mF = (sumAll - sumB) / wF
        val sigma = wB.toDouble() * wF.toDouble() * (mB - mF) * (mB - mF)
        if (sigma > bestSigma) {
            bestSigma = sigma
            bestT = t
        }
    }
    return bestT
}

private fun binarize(img: Image8bpp, threshold: Int): Image8bpp {
    val out = Image8bpp(img.width, img.height)
    for (y in 0 until img.height) {
        for (x in 0 until img.width) {
            val v = img.getPixel(x, y).toInt()
            out.setPixel(x, y, if (v > threshold) 255u else 0u)
        }
    }
    return out
}

private fun labelConnectedComponents4(binary: Image8bpp): IntArray {
    val w = binary.width
    val h = binary.height
    val labels = IntArray(w * h)
    var current = 0
    val stack = ArrayDeque<Int>()
    val dx = intArrayOf(1, -1, 0, 0)
    val dy = intArrayOf(0, 0, 1, -1)

    for (y in 0 until h) {
        for (x in 0 until w) {
            val idx = y * w + x
            if (labels[idx] != 0) continue
            if (binary.getPixel(x, y).toInt() != 255) continue
            current++
            labels[idx] = current
            stack.addLast(idx)
            while (stack.isNotEmpty()) {
                val p = stack.removeLast()
                val px = p % w
                val py = p / w
                for (k in 0..3) {
                    val nx = px + dx[k]
                    val ny = py + dy[k]
                    if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
                    val ni = ny * w + nx
                    if (labels[ni] != 0) continue
                    if (binary.getPixel(nx, ny).toInt() != 255) continue
                    labels[ni] = current
                    stack.addLast(ni)
                }
            }
        }
    }
    return labels
}

private fun renderLabels(labels: IntArray, w: Int, h: Int): Image8bpp {
    val out = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            val v = if (l == 0) 0 else 50 + (l * 37) % 200
            out.setPixel(x, y, v.toUByte())
        }
    }
    return out
}

private data class RegionMoments(
    val label: Int,
    val area: Long,
    val m10: Double,
    val m01: Double,
    val xc: Double,
    val yc: Double,
    val mu20: Double,
    val mu02: Double,
    val mu11: Double
)

private fun computeRegionMoments(labels: IntArray, w: Int, h: Int, count: Int): List<RegionMoments> {
    if (count == 0) return emptyList()
    val area = LongArray(count + 1)
    val sx = DoubleArray(count + 1)
    val sy = DoubleArray(count + 1)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0) continue
            area[l]++
            sx[l] += x
            sy[l] += y
        }
    }
    val xc = DoubleArray(count + 1)
    val yc = DoubleArray(count + 1)
    for (l in 1..count) {
        if (area[l] > 0) {
            xc[l] = sx[l] / area[l]
            yc[l] = sy[l] / area[l]
        }
    }
    val mu20 = DoubleArray(count + 1)
    val mu02 = DoubleArray(count + 1)
    val mu11 = DoubleArray(count + 1)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0) continue
            val dx = x - xc[l]
            val dy = y - yc[l]
            mu20[l] += dx * dx
            mu02[l] += dy * dy
            mu11[l] += dx * dy
        }
    }
    return (1..count).map { l ->
        RegionMoments(
            label = l,
            area = area[l],
            m10 = sx[l],
            m01 = sy[l],
            xc = xc[l],
            yc = yc[l],
            mu20 = mu20[l],
            mu02 = mu02[l],
            mu11 = mu11[l]
        )
    }
}
