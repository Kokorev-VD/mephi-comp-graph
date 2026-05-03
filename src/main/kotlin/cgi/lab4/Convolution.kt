package cgi.lab4

import common.Image8bpp

fun convolve(image: Image8bpp, kernel: Array<DoubleArray>): Array<DoubleArray> {
    val kh = kernel.size
    val kw = kernel[0].size
    require(kh % 2 == 1 && kw % 2 == 1) { "Размер ядра должен быть нечётным" }

    val ay = kh / 2
    val ax = kw / 2

    val w = image.width
    val h = image.height
    val out = Array(h) { DoubleArray(w) }

    for (y in 0 until h) {
        for (x in 0 until w) {
            var sum = 0.0
            for (ky in 0 until kh) {
                val sy = (y + ky - ay).coerceIn(0, h - 1)
                for (kx in 0 until kw) {
                    val sx = (x + kx - ax).coerceIn(0, w - 1)
                    sum += kernel[ky][kx] * image.getPixel(sx, sy).toInt()
                }
            }
            out[y][x] = sum
        }
    }
    return out
}

fun toImage(data: Array<DoubleArray>): Image8bpp {
    val h = data.size
    val w = data[0].size
    val img = Image8bpp(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val v = data[y][x].toInt().coerceIn(0, 255).toUByte()
            img.setPixel(x, y, v)
        }
    }
    return img
}

fun toImageNormalized(data: Array<DoubleArray>): Image8bpp {
    val h = data.size
    val w = data[0].size
    var minV = Double.POSITIVE_INFINITY
    var maxV = Double.NEGATIVE_INFINITY
    for (y in 0 until h) for (x in 0 until w) {
        if (data[y][x] < minV) minV = data[y][x]
        if (data[y][x] > maxV) maxV = data[y][x]
    }
    val range = if (maxV - minV < 1e-9) 1.0 else maxV - minV
    val img = Image8bpp(w, h)
    for (y in 0 until h) for (x in 0 until w) {
        val v = ((data[y][x] - minV) / range * 255.0).toInt().coerceIn(0, 255).toUByte()
        img.setPixel(x, y, v)
    }
    return img
}
