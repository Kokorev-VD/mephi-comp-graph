package cgi.hw

import common.Image24bpp
import common.Image8bpp
import cgi.lab4.bilateralFilter
import cgi.lab5.grayscaleOpening
import cgi.lab5.seSquare
import cgi.lab6.RegionMoments
import cgi.lab6.binarize
import cgi.lab6.computeRegionMoments
import cgi.lab6.labelConnectedComponentsTwoPass
import cgi.lab6.otsuThreshold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


data class ShapeFeatures(
    val label: Int,
    val area: Long,
    val kr1: Double,
    val ke: Double,
    val kc: Double,
    val kf: Double,
)

private fun computePerimeters(labels: IntArray, w: Int, h: Int, count: Int): IntArray {
    val perims = IntArray(count + 1)
    val dx = intArrayOf(1, -1, 0, 0)
    val dy = intArrayOf(0, 0, 1, -1)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0) continue
            for (k in 0..3) {
                val nx = x + dx[k]
                val ny = y + dy[k]
                if (nx < 0 || ny < 0 || nx >= w || ny >= h || labels[ny * w + nx] != l) {
                    perims[l]++
                }
            }
        }
    }
    return perims
}

private fun convexHull(points: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
    if (points.size <= 2) return points
    val p0 = points.minWithOrNull(compareBy({ it.second }, { it.first })) ?: return points
    val sorted = points.filter { it != p0 }.sortedBy { pt ->
        atan2((pt.second - p0.second).toDouble(), (pt.first - p0.first).toDouble())
    }.toMutableList()
    sorted.add(0, p0)

    fun cross(o: Pair<Int, Int>, a: Pair<Int, Int>, b: Pair<Int, Int>): Long {
        return (a.first.toLong() - o.first.toLong()) * (b.second.toLong() - o.second.toLong()) -
               (a.second.toLong() - o.second.toLong()) * (b.first.toLong() - o.first.toLong())
    }

    val hull = mutableListOf<Pair<Int, Int>>()
    for (pt in sorted) {
        while (hull.size >= 2 && cross(hull[hull.size - 2], hull[hull.size - 1], pt) <= 0) {
            hull.removeAt(hull.size - 1)
        }
        hull.add(pt)
    }
    return hull
}

private fun convexHullArea(hull: List<Pair<Int, Int>>): Double {
    if (hull.size < 3) return 0.0
    var sum = 0.0
    for (i in hull.indices) {
        val j = (i + 1) % hull.size
        sum += hull[i].first.toLong() * hull[j].second.toLong() -
               hull[j].first.toLong() * hull[i].second.toLong()
    }
    return abs(sum) / 2.0
}

private fun computeConvexFeatures(
    labels: IntArray, w: Int, h: Int,
    regions: List<RegionMoments>, minArea: Int
): Map<Int, Double> {
    val result = HashMap<Int, Double>()
    val dx4 = intArrayOf(1, -1, 0, 0)
    val dy4 = intArrayOf(0, 0, 1, -1)
    for (r in regions) {
        if (r.area <= minArea) continue
        val boundary = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (labels[y * w + x] != r.label) continue
                var isBorder = false
                for (k in 0..3) {
                    val nx = x + dx4[k]
                    val ny = y + dy4[k]
                    if (nx < 0 || ny < 0 || nx >= w || ny >= h || labels[ny * w + nx] != r.label) {
                        isBorder = true; break
                    }
                }
                if (isBorder) boundary.add(x to y)
            }
        }
        val hull = convexHull(boundary)
        val hullArea = convexHullArea(hull)
        val kc = if (hullArea > 0) r.area.toDouble() / hullArea else 1.0
        result[r.label] = kc.coerceIn(0.0, 1.0)
    }
    return result
}

private fun computeBBoxAreas(labels: IntArray, w: Int, h: Int, count: Int): IntArray {
    val minX = IntArray(count + 1) { Int.MAX_VALUE }
    val minY = IntArray(count + 1) { Int.MAX_VALUE }
    val maxX = IntArray(count + 1) { Int.MIN_VALUE }
    val maxY = IntArray(count + 1) { Int.MIN_VALUE }
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0) continue
            if (x < minX[l]) minX[l] = x
            if (y < minY[l]) minY[l] = y
            if (x > maxX[l]) maxX[l] = x
            if (y > maxY[l]) maxY[l] = y
        }
    }
    return IntArray(count + 1) { i ->
        if (minX[i] <= maxX[i]) (maxX[i] - minX[i] + 1) * (maxY[i] - minY[i] + 1) else 0
    }
}

private fun computeShapeFeatures(
    regions: List<RegionMoments>,
    perimeters: IntArray,
    convexFeatures: Map<Int, Double>,
    bboxAreas: IntArray,
    minArea: Int,
): List<ShapeFeatures> {
    return regions.mapNotNull { r ->
        if (r.area <= minArea) return@mapNotNull null
        val S = r.area.toDouble()
        val P = perimeters[r.label].toDouble()

        val kr1 = P / (2.0 * sqrt(PI * S))

        val disc = sqrt(
            ((r.mu20 - r.mu02) * (r.mu20 - r.mu02) + 4.0 * r.mu11 * r.mu11).coerceAtLeast(0.0)
        )
        val leq = 2.0 * sqrt((2.0 * (r.mu20 + r.mu02 + disc) / r.area).coerceAtLeast(0.0))
        val weq = 2.0 * sqrt((2.0 * (r.mu20 + r.mu02 - disc) / r.area).coerceAtLeast(0.0))
        val ke = if (leq > 0 && weq > 0) 4.0 * S / (PI * leq * weq) else 0.0

        val kc = convexFeatures[r.label] ?: 1.0

        val kf = if (bboxAreas[r.label] > 0) S / bboxAreas[r.label].toDouble() else 0.0

        ShapeFeatures(r.label, r.area, kr1 = kr1, ke = ke, kc = kc, kf = kf)
    }
}

private fun kMeans(
    data: List<DoubleArray>, k: Int,
    maxIter: Int = 100, seed: Long = 42
): Pair<IntArray, List<DoubleArray>> {
    val n = data.size
    val dim = data[0].size
    val rng = java.util.Random(seed)

    val centroids = mutableListOf<DoubleArray>()
    centroids.add(data[rng.nextInt(n)].copyOf())
    for (c in 1 until k) {
        val dists = DoubleArray(n) { i ->
            centroids.minOf { cent ->
                var d = 0.0
                for (j in 0 until dim) d += (data[i][j] - cent[j]).pow(2)
                d
            }
        }
        var r = rng.nextDouble() * dists.sum()
        var chosen = n - 1
        for (i in 0 until n) {
            r -= dists[i]
            if (r <= 0.0) { chosen = i; break }
        }
        centroids.add(data[chosen].copyOf())
    }

    val assignments = IntArray(n) { -1 }

    for (iter in 0 until maxIter) {
        var changed = false
        for (i in 0 until n) {
            var bestC = 0
            var bestD = Double.MAX_VALUE
            for (c in 0 until k) {
                var d = 0.0
                for (j in 0 until dim) d += (data[i][j] - centroids[c][j]).pow(2)
                if (d < bestD) { bestD = d; bestC = c }
            }
            if (assignments[i] != bestC) { assignments[i] = bestC; changed = true }
        }
        if (!changed) break

        val sums = Array(k) { DoubleArray(dim) }
        val counts = IntArray(k)
        for (i in 0 until n) {
            counts[assignments[i]]++
            for (j in 0 until dim) sums[assignments[i]][j] += data[i][j]
        }
        for (c in 0 until k) {
            if (counts[c] > 0) {
                for (j in 0 until dim) centroids[c][j] = sums[c][j] / counts[c]
            }
        }
    }
    return assignments to centroids
}

private fun wcss(data: List<DoubleArray>, assignments: IntArray, centroids: List<DoubleArray>): Double {
    var s = 0.0
    for (i in data.indices) {
        val c = assignments[i]
        for (j in data[i].indices) s += (data[i][j] - centroids[c][j]).pow(2)
    }
    return s
}

private fun findElbowK(wcssValues: List<Pair<Int, Double>>): Int {
    if (wcssValues.size <= 1) return wcssValues[0].first
    val first = wcssValues.first()
    val last = wcssValues.last()
    val dx = (last.first - first.first).toDouble()
    val dy = last.second - first.second
    val norm = sqrt(dy * dy + dx * dx)
    var maxDist = -1.0
    var elbowK = first.first
    for ((k, w) in wcssValues) {
        val dist = abs(dy * k - dx * w + last.first * first.second - last.second * first.first) / norm
        if (dist > maxDist) {
            maxDist = dist
            elbowK = k
        }
    }
    return elbowK
}

private fun silhouetteScore(data: List<DoubleArray>, assignments: IntArray, k: Int): Double {
    val n = data.size
    if (k <= 1 || n <= 1) return -1.0
    val dist = Array(n) { i ->
        DoubleArray(n) { j ->
            if (i == j) 0.0 else {
                var d = 0.0
                for (f in data[i].indices) d += (data[i][f] - data[j][f]).pow(2)
                sqrt(d)
            }
        }
    }
    var total = 0.0
    for (i in 0 until n) {
        val ci = assignments[i]
        val sameCluster = (0 until n).filter { j -> j != i && assignments[j] == ci }
        val a = if (sameCluster.isNotEmpty()) sameCluster.sumOf { j -> dist[i][j] } / sameCluster.size else 0.0
        var bMin = Double.MAX_VALUE
        for (c in 0 until k) {
            if (c == ci) continue
            val otherCluster = (0 until n).filter { j -> assignments[j] == c }
            if (otherCluster.isEmpty()) continue
            val bAvg = otherCluster.sumOf { j -> dist[i][j] } / otherCluster.size
            if (bAvg < bMin) bMin = bAvg
        }
        if (bMin == Double.MAX_VALUE) bMin = 0.0
        total += if (maxOf(a, bMin) > 0) (bMin - a) / maxOf(a, bMin) else 0.0
    }
    return total / n
}

private val clusterColors = listOf(
    Triple(220.toUByte(), 50.toUByte(), 50.toUByte()),
    Triple(50.toUByte(), 200.toUByte(), 50.toUByte()),
    Triple(50.toUByte(), 100.toUByte(), 255.toUByte()),
    Triple(230.toUByte(), 200.toUByte(), 30.toUByte()),
    Triple(200.toUByte(), 50.toUByte(), 200.toUByte()),
    Triple(30.toUByte(), 200.toUByte(), 200.toUByte()),
)

private fun bestKMeans(
    data: List<DoubleArray>, k: Int, trials: Int = 50
): Pair<IntArray, List<DoubleArray>> {
    var bestA = IntArray(0)
    var bestC = emptyList<DoubleArray>()
    var bestW = Double.MAX_VALUE
    for (trial in 0 until trials) {
        val (a, c) = kMeans(data, k, seed = 42L + trial * 17)
        val w = wcss(data, a, c)
        if (w < bestW) { bestW = w; bestA = a; bestC = c }
    }
    return bestA to bestC
}

private fun colorByCluster(
    labels: IntArray, w: Int, h: Int,
    features: List<ShapeFeatures>, assignments: IntArray
): Image24bpp {
    val labelToCluster = HashMap<Int, Int>()
    features.forEachIndexed { idx, f -> labelToCluster[f.label] = assignments[idx] }
    val out = Image24bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0 || l !in labelToCluster) {
                out.setPixel(x, y, 0u, 0u, 0u)
            } else {
                val cl = labelToCluster[l]!!
                val c = clusterColors[cl % clusterColors.size]
                out.setPixel(x, y, c.first, c.second, c.third)
            }
        }
    }
    return out
}

private fun renderLabels(labels: IntArray, w: Int, h: Int): Image24bpp {
    val out = Image24bpp(w, h)
    val ub: (Int) -> UByte = { it.toUByte() }
    val palette = arrayOf(
        Triple(ub(255), ub(0), ub(0)), Triple(ub(0), ub(255), ub(0)), Triple(ub(0), ub(0), ub(255)),
        Triple(ub(255), ub(255), ub(0)), Triple(ub(255), ub(0), ub(255)), Triple(ub(0), ub(255), ub(255)),
        Triple(ub(255), ub(128), ub(0)), Triple(ub(128), ub(0), ub(255)), Triple(ub(0), ub(128), ub(255)),
        Triple(ub(128), ub(255), ub(0)), Triple(ub(255), ub(0), ub(128)), Triple(ub(0), ub(255), ub(128)),
    )
    for (y in 0 until h) {
        for (x in 0 until w) {
            val l = labels[y * w + x]
            if (l == 0) continue
            val c = palette[(l - 1) % palette.size]
            out.setPixel(x, y, c.first, c.second, c.third)
        }
    }
    return out
}

fun pipeline(filename: String, iteration: Int) {
    println("1. Загрузка изображения")
    val input = Image8bpp.load(filename)
    input.save("test_${iteration}_00_input.png")
    println("   ${input.width} x ${input.height}\n")

    println("2. Билатеральный фильтр")
    val denoised = bilateralFilter(input, kernelSize = 7, sigmaSpatial = 3.0, sigmaRange = 40.0)
    denoised.save("test_${iteration}_01_bilateral.png")
    println("   σ_s=3.0, σ_r=40.0, ядро 7x7\n")

    println("3. Бинаризация Оцу")
    val threshold = otsuThreshold(denoised)
    println("   Порог: $threshold")
    val binary = binarize(denoised, threshold)
    binary.save("test_${iteration}_02_otsu.png")
    println()

    println("4. Морфологическое открытие")
    val se = seSquare(1)
    val opened = grayscaleOpening(binary, se)
    opened.save("test_${iteration}_03_morph_opening.png")

    println("5. Разметка связных областей (двухпроходный алгоритм, 4-связность)")
    val labels = labelConnectedComponentsTwoPass(opened)
    val numRegions = labels.maxOrNull() ?: 0
    println("   Найдено областей: $numRegions")
    val labelsImg = renderLabels(labels, opened.width, opened.height)
    labelsImg.save("test_${iteration}_04_labels.png")
    println()

    println("6. Коэффициенты формы")
    val minArea = 1000
    val regions = computeRegionMoments(labels, opened.width, opened.height, numRegions)
    val perimeters = computePerimeters(labels, opened.width, opened.height, numRegions)
    val convexFeatures = computeConvexFeatures(labels, opened.width, opened.height, regions, minArea)
    val bboxAreas = computeBBoxAreas(labels, opened.width, opened.height, numRegions)
    val features = computeShapeFeatures(regions, perimeters, convexFeatures, bboxAreas, minArea)
    println("   Значимых областей (S > $minArea): ${features.size}")
    println("   %3s %6s %6s %6s %6s %6s".format("№", "S", "Kr1", "Ke", "Kc", "Kf"))
    for (f in features) {
        println("   %3d %6d %6.3f %6.3f %6.3f %6.3f".format(f.label, f.area, f.kr1, f.ke, f.kc, f.kf))
    }
    println()

    println("7. Кластеризация K-means (признаки: Kr1, Ke, Kc, Kf)")
    if (features.isEmpty()) {
        println("   Нет значимых областей для кластеризации")
        return
    }

    fun zScore(vals: List<Double>): List<Double> {
        val mean = vals.average()
        val std = sqrt(vals.map { (it - mean).pow(2) }.average()).coerceAtLeast(1e-10)
        return vals.map { (it - mean) / std }
    }

    val dim = 4
    val rawVectors = features.map { f ->
        doubleArrayOf(f.kr1, f.ke, f.kc, f.kf)
    }
    val normalized = (0 until dim).map { j -> zScore(rawVectors.map { it[j] }) }
    val vectors = features.mapIndexed { idx, _ ->
        DoubleArray(dim) { j -> normalized[j][idx] }
    }

    val maxK = minOf(6, features.size)

    println("   Перебор k = 2..$maxK\n")
    println("   %3s %10s %8s  %s".format("k", "WCSS", "Silh", "Состав кластеров"))
    println("   %3s %10s %8s  %s".format("---", "----------", "--------", "--------------------"))

    val kResults = mutableListOf<Triple<Int, Double, Double>>()
    for (k in 2..maxK) {
        val (assignments, centroids) = bestKMeans(vectors, k)
        val w = wcss(vectors, assignments, centroids)
        val silh = silhouetteScore(vectors, assignments, k)
        kResults.add(Triple(k, w, silh))

        val clusterDesc = (0 until k).joinToString(" | ") { cl ->
            val cnt = features.indices.count { idx -> assignments[idx] == cl }
            "C${cl + 1}($cnt)"
        }
        println("   %3d %10.4f %7.4f  %s".format(k, w, silh, clusterDesc))
    }

    val bestSilhK = kResults.maxByOrNull { it.third }?.first ?: 2
    val elbowK = findElbowK(kResults.map { it.first to it.second })
    val (bestAssign, _) = bestKMeans(vectors, bestSilhK)
    val singletonCount = (0 until bestSilhK).count { c ->
        features.indices.count { idx -> bestAssign[idx] == c } == 1
    }
    val optimalK = if (singletonCount > 1) elbowK else bestSilhK

    println("\n   Оптимальное k = $optimalK (локоть=$elbowK, silhouette=$bestSilhK)\n")

    println("8. Детальный разбор для k = $optimalK")
    val (optAssignments, _) = bestKMeans(vectors, optimalK, trials = 300)
    for (c in 0 until optimalK) {
        val objs = features.filterIndexed { idx, _ -> optAssignments[idx] == c }
        println("   Кластер ${c + 1}: ${objs.size} объектов")
        for (o in objs) {
            println("      метка=${o.label}  S=${o.area}  Kr1=${"%.3f".format(o.kr1)}  Ke=${"%.3f".format(o.ke)}  Kc=${"%.3f".format(o.kc)}  Kf=${"%.3f".format(o.kf)}")
        }
    }

    colorByCluster(labels, input.width, input.height, features, optAssignments)
        .save("test_${iteration}_05_colored.png")
}

fun main() {
    println("ДЗ: распознавание и классификация форм объектов (коэффициенты формы)")

    val testCases = listOf("src/main/resources/cgi/hw/new_test_noisy.png", "src/main/resources/cgi/hw/base.jpg")

    for ((index, fileName) in testCases.withIndex()) {
        println("\nТест $index, изображение: $fileName")
        pipeline(fileName, index)
    }

    println("\nРезультаты сохранены: src/main/resources/cgi/hw/")
}
