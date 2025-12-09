package lab4

import common.FillRule
import common.Image8bpp
import common.Point
import common.Polygon
import common.drawLine

fun main() {
    println("Лабораторная работа No 4: Кривые Безье и отсечение отрезков\n")

    println("Задача 1. Построение кривых Безье третьего порядка")
    val task1Result = mutableListOf<String>()

    val bezier1 = BezierCurve(
        Point(50, 350),
        Point(100, 100),
        Point(250, 100),
        Point(300, 350)
    )

    val bezier2 = BezierCurve(
        Point(450, 250),
        Point(500, 50),
        Point(650, 450),
        Point(700, 250)
    )

    val bezier3 = BezierCurve(
        Point(850, 100),
        Point(950, 50),
        Point(1050, 450),
        Point(1150, 100)
    )

    drawCurves(bezier1, bezier2, bezier3, task1Result, "bezier_curves.png")

    val bezier4 = BezierCurve(
        Point(50, 250),
        Point(150, 100),
        Point(200, 400),
        Point(300, 250)
    )

    val bezier5 = BezierCurve(
        Point(450, 100),
        Point(500, 400),
        Point(650, 400),
        Point(700, 100)
    )

    val bezier6 = BezierCurve(
        Point(850, 250),
        Point(950, 400),
        Point(1050, 100),
        Point(1150, 250)
    )
    drawCurves(bezier4, bezier5, bezier6, task1Result, "bezier_curves_2.png")

    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Отсечение отрезков алгоритмом Кируса-Бека")
    val task2Result = mutableListOf<String>()

    val clipPolygon = Polygon(
        listOf(
            Point(200, 150),
            Point(400, 150),
            Point(450, 300),
            Point(350, 400),
            Point(150, 350)
        )
    )

    val lines = listOf(
        Point(100, 100) to Point(500, 400),
        Point(100, 400) to Point(500, 100),
        Point(50, 250) to Point(550, 250),
        Point(300, 50) to Point(300, 450),
        Point(250, 200) to Point(350, 300),
        Point(100, 200) to Point(200, 300),
        Point(450, 200) to Point(550, 300)
    )
    drawClipping(clipPolygon, lines, task2Result, "cyrus_beck_clipping.png")

    val clipPolygonCCW = Polygon(
        listOf(
            Point(200, 150),
            Point(150, 350),
            Point(350, 400),
            Point(450, 300),
            Point(400, 150)
        )
    )

    val linesDegenerate = listOf(
        Point(250, 200) to Point(350, 300),
        Point(300, 250) to Point(300, 250),
        Point(200, 200) to Point(400, 200),
        Point(250, 100) to Point(250, 450),
        Point(100, 250) to Point(150, 250),
        Point(450, 250) to Point(500, 250),
        Point(280, 300) to Point(320, 300),
        Point(200, 150) to Point(400, 150),
        Point(50, 150) to Point(550, 150),
        Point(300, 300) to Point(300, 300)
    )
    drawClipping(clipPolygonCCW, linesDegenerate, task2Result, "cyrus_beck_ccw_degenerate.png")

    val clipPolygon2 = Polygon(
        listOf(
            Point(150, 100),
            Point(300, 80),
            Point(450, 100),
            Point(500, 250),
            Point(450, 400),
            Point(300, 420),
            Point(150, 400),
            Point(100, 250)
        )
    )

    val lines2 = listOf(
        Point(50, 50) to Point(550, 450),
        Point(550, 50) to Point(50, 450),
        Point(300, 30) to Point(300, 470),
        Point(30, 250) to Point(570, 250),
        Point(100, 150) to Point(500, 350),
        Point(150, 300) to Point(450, 200),
        Point(200, 100) to Point(400, 400),
        Point(250, 350) to Point(350, 150)
    )

    drawClipping(clipPolygon2, lines2, task2Result, "cyrus_beck_clipping_2.png")

    println("Результат задачи 2 сохранен: $task2Result\n")

    println("Задача 3. Отсечение полигона алгоритмом Сазерленда-Ходжмана")
    val task3Result = mutableListOf<String>()

    val clipConvex = Polygon(
        listOf(
            Point(200, 100),
            Point(400, 150),
            Point(450, 350),
            Point(300, 450),
            Point(150, 400)
        )
    )

    val subjectPolygon1 = Polygon(
        listOf(
            Point(100, 200),
            Point(300, 100),
            Point(500, 200),
            Point(450, 400),
            Point(200, 450),
            Point(150, 300)
        )
    )

    drawPolygonClipping(clipConvex, subjectPolygon1, task3Result, "sutherland_hodgman_1.png")

    val subjectPolygon2 = Polygon(
        listOf(
            Point(250, 150),
            Point(350, 200),
            Point(320, 320),
            Point(280, 280)
        )
    )

    drawPolygonClipping(clipConvex, subjectPolygon2, task3Result, "sutherland_hodgman_2.png")

    val subjectPolygon3 = Polygon(
        listOf(
            Point(100, 250),
            Point(500, 100),
            Point(550, 300),
            Point(400, 250),
            Point(450, 150)
        )
    )

    drawPolygonClipping(clipConvex, subjectPolygon3, task3Result, "sutherland_hodgman_3.png")

    println("Результат задачи 3 сохранен: $task3Result\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/lab4/")
}

fun drawCurves(bezier1: BezierCurve, bezier2: BezierCurve, bezier3: BezierCurve, task1Result: MutableList<String>, imageName: String) {
    val imageBezier = Image8bpp(1200, 500)
    imageBezier.fill(255u)
    bezier1.draw(imageBezier, 0u)
    bezier1.drawControlPoints(imageBezier, 128u)
    bezier2.draw(imageBezier, 0u)
    bezier2.drawControlPoints(imageBezier, 128u)
    bezier3.draw(imageBezier, 0u)
    bezier3.drawControlPoints(imageBezier, 128u)
    imageBezier.save(imageName)
    task1Result.add(imageName)
}

fun drawClipping(clipPoligon: Polygon, lines: List<Pair<Point, Point>>, task2Result: MutableList<String>, imageName: String) {
    val imageClipping = Image8bpp(600, 500)
    imageClipping.fill(255u)

    for ((p1, p2) in lines) {
        drawLine(imageClipping, p1, p2, 200u)
    }

    clipPoligon.draw(imageClipping, 0u)

    for ((p1, p2) in lines) {
        val clipped = cyrusBeckClip(p1, p2, clipPoligon)
        if (clipped != null) {
            drawLine(imageClipping, clipped.first, clipped.second, 50u)
        }
    }

    imageClipping.save(imageName)
    task2Result.add(imageName)
}

fun drawPolygonClipping(clipPolygon: Polygon, subjectPolygon: Polygon, task3Result: MutableList<String>, imageName: String) {
    val imagePolygonClipping = Image8bpp(600, 500)
    imagePolygonClipping.fill(255u)

    clipPolygon.draw(imagePolygonClipping, 0u)
    subjectPolygon.draw(imagePolygonClipping, 200u)

    val clippedPolygons = sutherlandHodgmanClip(subjectPolygon, clipPolygon)

    for (clipped in clippedPolygons) {
        clipped.draw(imagePolygonClipping, 100u)
        clipped.fill(imagePolygonClipping, FillRule.EVEN_ODD, 150u)
    }

    imagePolygonClipping.save(imageName)
    task3Result.add(imageName)
}