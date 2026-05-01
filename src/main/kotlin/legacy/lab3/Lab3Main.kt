package legacy.lab3


fun main() {
    val n = 123

}

fun rev(v: Int, len: Int): Int {
    if (len == 1) return v and 1

    val half = len shr 1
    val mask = (1 shl half) - 1
    val lo = v and mask
    val hi = v ushr half

    return (rev(lo, half) shl half) or rev(hi, half)
}