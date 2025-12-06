package lab3

import common.FillRule
import common.Image8bpp
import common.Point
import common.Polygon
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

fun main() {
    println("Лабораторная работа No 3: Построение и заполнение полигонов\n")

    val polygons = listOf(
        "triangle" to createTriangle(300, 250, 120.0),
        "star" to createStar(300, 250, 100.0),
        "complex1" to createComplexShape1(300, 250, 100.0),
        "complex2" to createComplexShape2(300, 250, 100.0),
        "duplicated_vertices" to createDuplicatedVertices(200, 200),
        "degenerate_line" to duplicatedLine(300, 250),
    )

    println("Задача 1-2. Вычерчивание отрезков и вывод полигона")
    val task1Result = mutableListOf<String>()
    for ((name, polygon) in polygons) {
        val image = Image8bpp(600, 500)
        image.fill(255u)
        polygon.draw(image, 0u)
        image.save("${name}_outline.png")
        task1Result.add("${name}_outline.png")
    }
    println("Результат задачи 1-2 сохранен: $task1Result\n")

    println("Задача 3-4. Заполнение полигона (even-odd vs non-zero-winding)")
    val task2Result = mutableListOf<String>()
    for ((name, polygon) in polygons) {
        val imageEvenOdd = Image8bpp(600, 500)
        imageEvenOdd.fill(255u)
        polygon.fill(imageEvenOdd, FillRule.EVEN_ODD, 128u)
        polygon.draw(imageEvenOdd, 0u)
        imageEvenOdd.save("${name}_even_odd.png")
        task2Result.add("${name}_even_odd.png")

        val imageNonZero = Image8bpp(600, 500)
        imageNonZero.fill(255u)
        polygon.fill(imageNonZero, FillRule.NON_ZERO_WINDING, 128u)
        polygon.draw(imageNonZero, 0u)
        imageNonZero.save("${name}_non_zero.png")
        task2Result.add("${name}_non_zero.png")
    }
    println("Результат задачи 3-4 сохранен: $task2Result\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/lab3/")
}

fun createTriangle(cx: Int, cy: Int, size: Double): Polygon {
    val vertices = mutableListOf<Point>()

    vertices.add(Point(cx, (cy - size * 0.6).toInt()))
    vertices.add(Point((cx + size * 0.9).toInt(), (cy + size * 0.5).toInt()))
    vertices.add(Point((cx - size * 0.9).toInt(), (cy + size * 0.5).toInt()))

    return Polygon(vertices)
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

fun createDuplicatedVertices(cx: Int, cy: Int): Polygon = Polygon(listOf(
        Point(cx, cy),
        Point(cx, cy),
        Point(cx, cy + 100),
        Point(cx, cy + 100),
        Point(cx + 100, cy),
        Point(cx + 100, cy)
    )
)

fun duplicatedLine(cx: Int, cy: Int): Polygon = Polygon(listOf(
        Point(cx - 100, cy),
        Point(cx - 50, cy),
        Point(cx, cy),
        Point(cx + 50, cy),
        Point(cx + 100, cy)
    )
)

fun createComplexShape1(cx: Int, cy: Int, size: Double): Polygon {
    val vertices = mutableListOf<Point>()

    vertices.add(Point((cx - size).toInt(), (cy - size * 0.5).toInt()))
    vertices.add(Point((cx + size).toInt(), (cy + size * 0.5).toInt()))
    vertices.add(Point((cx - size * 0.5).toInt(), (cy + size).toInt()))
    vertices.add(Point((cx + size * 0.5).toInt(), (cy - size).toInt()))
    vertices.add(Point((cx - size).toInt(), (cy + size * 0.5).toInt()))
    vertices.add(Point((cx + size).toInt(), (cy - size * 0.5).toInt()))

    return Polygon(vertices)
}

fun createComplexShape2(cx: Int, cy: Int, size: Double): Polygon {
    val vertices = mutableListOf<Point>()

    vertices.add(Point((cx - size * 0.8).toInt(), (cy - size).toInt()))
    vertices.add(Point((cx + size * 0.8).toInt(), (cy + size).toInt()))
    vertices.add(Point((cx - size).toInt(), (cy + size * 0.3).toInt()))
    vertices.add(Point((cx + size).toInt(), (cy - size * 0.3).toInt()))
    vertices.add(Point((cx - size * 0.3).toInt(), (cy - size).toInt()))
    vertices.add(Point((cx + size * 0.3).toInt(), (cy + size).toInt()))

    return Polygon(vertices)
}
