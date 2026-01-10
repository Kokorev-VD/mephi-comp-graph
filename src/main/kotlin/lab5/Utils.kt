package lab5

import java.awt.Graphics
import kotlin.math.roundToInt

object Utils {
    private fun lineBase(g: Graphics, x0: Int, y0: Int, x1: Int, y1: Int) {
        val dx = x1 - x0
        val dy = y1 - y0
        val sz = Math.max(Math.abs(dx), Math.abs(dy))
        if (sz == 0) {
            g.fillRect(x0, y0, 1, 1)
            return
        }
        for (k in 0..sz) {
            val x = fix(x0 * sz + dx * k, sz)
            val y = fix(y0 * sz + dy * k, sz)
            g.fillRect(x, y, 1, 1)
        }
    }

    private fun fix(x: Int, n: Int): Int {
        val r = (Math.abs(x) + n / 2) / n
        return if (x < 0) -r else r
    }

    fun line(g: Graphics, p0: Vector, p1: Vector, w: Int = 1) {
        line(g, p0.x.roundToInt(), p0.y.roundToInt(), p1.x.roundToInt(), p1.y.roundToInt(), w)
    }

    fun line(g: Graphics, x0: Int, y0: Int, x1: Int, y1: Int, w: Int = 1) {
        if (w == 1) {
            lineBase(g, x0, y0, x1, y1)
        } else {
            for (dx in -(w - 1) / 2..w / 2) {
                for (dy in -(w - 1) / 2..w / 2) {
                    lineBase(g, x0 + dx, y0 + dy, x1 + dx, y1 + dy)
                }
            }
        }
    }

    val CUBOID1_COLORS = listOf<UByte>(
        250u,
        240u,
        230u,
        220u,
        210u,
        200u
    )

    val CUBOID2_COLORS = listOf<UByte>(
        60u,
        80u,
        100u,
        120u,
        140u,
        160u
    )

}
