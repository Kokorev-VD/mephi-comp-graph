package lab5

import common.Image8bpp
import common.Point
import common.drawLine
import java.awt.Graphics

object Renderer {
    val cameraZ: Double = 100.0
    val center = Vector(500.0, 500.0, cameraZ)
    val scale: Double = 100.0

    fun render(g: Graphics, parallepiped__: Parallelepiped) {
        val par = parallepiped__.deepCopy()
        val parForCulling = parallepiped__.deepCopy()

        par.apply {
            it * Matrix.onePointPerspective(2.0)
        }
        parForCulling.apply {
            it * Matrix.onePointPerspective(2.0)
        }

        par.points.forEach {
            it.fix()
        }

        par.sides.forEachIndexed { index, side ->
            val sideCulling = parForCulling.sides[index]
            val cameraPos = Vector(0.0, 0.0, cameraZ)
            val toCamera = cameraPos - sideCulling[0]

            val edge1 = sideCulling[1] - sideCulling[0]
            val edge2 = sideCulling[3] - sideCulling[0]
            val normal = edge1 vectorMultiple edge2

            if ((toCamera scalarMultiple normal) < 0) return@forEachIndexed

            val w = 3
            Utils.line(g, side[0], side[1], w)
            Utils.line(g, side[1], side[2], w)
            Utils.line(g, side[2], side[3], w)
            Utils.line(g, side[3], side[0], w)
        }
    }

    fun Vector.fix(): Vector {
        val projected = this.copy()
        projected * Matrix.projectionToXY(cameraZ)
        projected.x = projected.x * scale + center.x
        projected.y = projected.y * scale + center.y
        projected.z = projected.z * scale + center.z

        this.x = projected.x
        this.y = projected.y
        this.z = projected.z
        return this
    }

    fun renderParallelProjection(
        image: Image8bpp,
        parallepiped: Parallelepiped,
        color: UByte,
        centerX: Double = 400.0,
        centerY: Double = 300.0,
        scale: Double = 1.0,
        removeHidden: Boolean = true
    ) {
        val par = parallepiped.deepCopy()
        val parOriginal = parallepiped.deepCopy()

        par.apply {
            it * Matrix.projectionToXY()
        }

        par.points.forEach {
            it.x = it.x * scale + centerX
            it.y = it.y * scale + centerY
        }

        if (removeHidden) {
            par.sides.forEachIndexed { index, side ->
                val sideOriginal = parOriginal.sides[index]
                val viewDir = Vector(0.0, 0.0, -1.0)

                val edge1 = sideOriginal[1] - sideOriginal[0]
                val edge2 = sideOriginal[3] - sideOriginal[0]
                val normal = edge1 vectorMultiple edge2

                if ((normal scalarMultiple viewDir) < 0) return@forEachIndexed

                drawSide(image, side, color)
            }
        } else {
            par.sides.forEach { side ->
                drawSide(image, side, color)
            }
        }
    }

    fun renderPerspectiveProjection(
        image: Image8bpp,
        parallepiped: Parallelepiped,
        k: Double,
        color: UByte,
        centerX: Double = 400.0,
        centerY: Double = 300.0,
        scale: Double = 1.0,
        removeHidden: Boolean = true
    ) {
        val par = parallepiped.deepCopy()
        val parForCulling = parallepiped.deepCopy()

        // Apply perspective transformation to both copies
        par.apply {
            it * Matrix.onePointPerspective(k)
        }
        parForCulling.apply {
            it * Matrix.onePointPerspective(k)
        }

        // Apply projection and screen transformation only to render copy
        par.apply {
            it * Matrix.projectionToXY()
        }

        par.points.forEach {
            it.x = it.x * scale + centerX
            it.y = it.y * scale + centerY
        }

        if (removeHidden) {
            par.sides.forEachIndexed { index, side ->
                // Use post-perspective coordinates for culling (matches render() and example)
                val sideCulling = parForCulling.sides[index]
                // Camera position in perspective space - use consistent cameraZ like render()
                val cameraPos = Vector(0.0, 0.0, cameraZ)

                val edge1 = sideCulling[1] - sideCulling[0]
                val edge2 = sideCulling[3] - sideCulling[0]
                val normal = edge1 vectorMultiple edge2

                val toCamera = cameraPos - sideCulling[0]
                if ((toCamera scalarMultiple normal) < 0) return@forEachIndexed

                drawSide(image, side, color)
            }
        } else {
            par.sides.forEach { side ->
                drawSide(image, side, color)
            }
        }
    }

    private fun drawSide(image: Image8bpp, side: Side, color: UByte) {
        for (i in 0 until 4) {
            val start = side[i]
            val end = side[(i + 1) % 4]
            drawLine(
                image,
                Point(start.x.toInt(), start.y.toInt()),
                Point(end.x.toInt(), end.y.toInt()),
                color
            )
        }
    }
}
