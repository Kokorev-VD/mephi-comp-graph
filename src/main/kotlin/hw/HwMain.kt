package hw

import common.Image8bpp
import common.Point
import common.Polygon
import common.drawLine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    println("Домашнее задание по компьютерной графике\n")

    println("Задание 1. Отсечение отрезка прямой по произвольному полигону (even-odd rule)")
    demonstrateLineClipping()
    println()

    println("Задание 2. Составная сплайновая кривая Catmull-Rom")
    demonstrateCatmullRomSpline()
    println()

    println("Задание 3. Гамма-коррекция цветного 24 bpp изображения")
    demonstrateGammaCorrection()
    println()

    println("\nВсе результаты сохранены в src/main/resources/hw/")
}

fun createStar(cx: Int, cy: Int, radius: Double): Polygon {
    val vertices = mutableListOf<Point>()
    val points = 5
    val angleStep = 2 * PI / points

    for (i in 0 until points) {
        val angle = i * angleStep - PI / 2
        val x = (cx + radius * cos(angle)).toInt()
        val y = (cy + radius * sin(angle)).toInt()
        vertices.add(Point(x, y))
    }

    val reordered = mutableListOf<Point>()
    reordered.add(vertices[0])
    reordered.add(vertices[2])
    reordered.add(vertices[4])
    reordered.add(vertices[1])
    reordered.add(vertices[3])

    return Polygon(reordered)
}

fun demonstrateLineClipping() {
    val image = Image8bpp(600, 500)
    image.fill(255u)

    val star = createStar(300, 250, 100.0)

    val lines = listOf(
        Point(50, 50) to Point(550, 450),
        Point(50, 450) to Point(550, 50),
        Point(50, 250) to Point(550, 250),
        Point(300, 50) to Point(300, 450),
        Point(150, 150) to Point(450, 350),
        Point(150, 350) to Point(450, 150),
        Point(100, 100) to Point(200, 200),
        Point(400, 100) to Point(500, 200)
    )

    for ((p1, p2) in lines) {
        drawLine(image, p1, p2, 180u)
    }

    star.draw(image, 0u)

    for ((p1, p2) in lines) {
        val clipped = clipLineByArbitraryPolygon(p1, p2, star)
        for ((cp1, cp2) in clipped) {
            drawLine(image, cp1, cp2, 0u)
        }
    }

    image.save("task1_line_clipping.png")
    println("Результат сохранен: task1_line_clipping.png")
}

fun demonstrateCatmullRomSpline() {
    val image = Image8bpp(1200, 600)
    image.fill(255u)

    val spline1 = CatmullRomSpline(
        listOf(
            Point(50, 300),
            Point(150, 150),
            Point(300, 450),
            Point(450, 150)
        )
    )

    val spline2 = CatmullRomSpline(
        listOf(
            Point(550, 250),
            Point(650, 150),
            Point(750, 250),
            Point(750, 400),
            Point(650, 500),
            Point(550, 400)
        )
    )

    val spline3 = CatmullRomSpline(
        listOf(
            Point(850, 450),
            Point(900, 300),
            Point(950, 150),
            Point(1000, 300),
            Point(1050, 450),
            Point(1100, 300),
            Point(1150, 150)
        )
    )

    spline1.draw(image, 0u)
    spline1.drawControlPoints(image, 128u)

    spline2.draw(image, 0u)
    spline2.drawControlPoints(image, 128u)

    spline3.draw(image, 0u)
    spline3.drawControlPoints(image, 128u)

    image.save("task2_catmull_rom_spline.png")
    println("Результат сохранен: task2_catmull_rom_spline.png")
}

fun demonstrateGammaCorrection() {
    val images = listOf("cat.png", "evening.png", "squirrel.png")

    println("Обработка изображений из src/main/resources/common/:")
    println()

    for (imageName in images) {
        val inputPath = "src/main/resources/common/$imageName"
        val outputName = "task3_${imageName.substringBefore(".")}_corrected.png"

        println("Обработка: $imageName")

        val gamma = applyAutoGammaCorrection(
            inputPath,
            outputName,
            targetBrightness = 128.0
        )

        println("  → Результат: $outputName (γ = ${"%.4f".format(gamma)})")
        println()
    }

    println("Все результаты сохранены в src/main/resources/hw/")
}
