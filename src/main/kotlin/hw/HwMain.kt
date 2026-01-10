package hw

import common.Image8bpp
import common.Point
import common.Polygon
import common.drawLine
import java.io.File

fun main() {
    println("Домашнее задание по компьютерной графике\n")

    println("Задание 1. Отсечение отрезка прямой по произвольному полигону (алгоритм Кируса-Бека)")
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

fun demonstrateLineClipping() {
    val image = Image8bpp(800, 600)
    image.fill(255u)

    val clipPolygon = Polygon(
        listOf(
            Point(200, 150),
            Point(600, 150),
            Point(650, 400),
            Point(400, 500),
            Point(150, 400)
        )
    )

    val lines = listOf(
        Point(100, 100) to Point(700, 500),
        Point(100, 500) to Point(700, 100),
        Point(50, 300) to Point(750, 300),
        Point(400, 50) to Point(400, 550),
        Point(300, 250) to Point(500, 350),
        Point(100, 250) to Point(250, 300),
        Point(600, 250) to Point(750, 300)
    )

    for ((p1, p2) in lines) {
        drawLine(image, p1, p2, 200u)
    }

    clipPolygon.draw(image, 0u)

    for ((p1, p2) in lines) {
        val clipped = cyrusBeckClip(p1, p2, clipPolygon)
        if (clipped != null) {
            drawLine(image, clipped.first, clipped.second, 50u)
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
