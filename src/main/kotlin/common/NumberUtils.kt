package common

fun sign(value: Int): Int {
    return when {
        value > 0 -> 1
        value < 0 -> -1
        else -> 0
    }
}