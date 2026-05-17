package common

fun benchmark(repeats: Int = 10, warmup: Int = 3, block: () -> Unit): Double {
    repeat(warmup) { block() }
    System.gc()
    var total = 0.0
    repeat(repeats) {
        val t = System.nanoTime()
        block()
        total += (System.nanoTime() - t) / 1e6
    }
    return total / repeats
}
