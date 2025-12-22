package lab5

import common.Image8bpp
import javax.swing.Timer

fun createStandardParallelepiped(w: Double, h: Double, d: Double, zCenter: Double): Parallelepiped {
    val halfW = w / 2.0
    val halfH = h / 2.0
    val halfD = d / 2.0
    return Parallelepiped(
        listOf(
            Vector(-halfW, -halfH, zCenter - halfD),
            Vector(halfW, -halfH, zCenter - halfD),
            Vector(halfW, halfH, zCenter - halfD),
            Vector(-halfW, halfH, zCenter - halfD),
            Vector(-halfW, -halfH, zCenter + halfD),
            Vector(halfW, -halfH, zCenter + halfD),
            Vector(halfW, halfH, zCenter + halfD),
            Vector(-halfW, halfH, zCenter + halfD)
        )
    )
}

fun main() {
    println("Лабораторная работа No 5: 3D проекции и удаление невидимых ребер\n")

    val parBase = Parallelepiped(
        listOf(
            Vector(-1.0, -1.0, -1.0),
            Vector(1.0, -1.0, -1.0),
            Vector(1.0, 1.0, -1.0),
            Vector(-1.0, 1.0, -1.0),
            Vector(-1.0, -1.0, 1.0),
            Vector(1.0, -1.0, 1.0),
            Vector(1.0, 1.0, 1.0),
            Vector(-1.0, 1.0, 1.0)
        )
    )

    val par = parBase.deepCopy()

    ImageScreen().displayImage(
        parallelepiped = par
    ) { screen ->
        var angle = 0.0
        var xPos = 0.0
        var xDirection = 1.0
        val xSpeed = 0.05
        val xMin = -3.0
        val xMax = 3.0

        val t = Timer(1000 / 30) {
            angle += 1.0
            xPos += xSpeed * xDirection
            if (xPos > xMax || xPos < xMin) {
                xDirection *= -1.0
            }

            val parNew = parBase.deepCopy()
            parNew.apply {
                it * Matrix.rotateByVector(Vector(1.0, 1.0, 1.0), angle)
                it * Matrix.translate(xPos, 0.0, -5.0)
            }

            par.points.forEachIndexed { i, point ->
                point.x = parNew.points[i].x
                point.y = parNew.points[i].y
                point.z = parNew.points[i].z
            }

            screen.repaint()
        }
        t.start()
    }

    println("\nОкно открыто. Закройте его для продолжения или Ctrl+C для выхода.\n")

    println("Также создаем статические изображения для отчета...\n")

    println("Задача 1. Параллельная проекция повернутого параллелепипеда")
    val task1Result = mutableListOf<String>()

    val par1 = createStandardParallelepiped(100.0, 80.0, 60.0, 0.0)

    par1.apply { it * Matrix.rotateByVector(Vector(1.0, 1.0, 0.0), 30.0) }

    val imageParallel1 = Image8bpp(800, 600)
    imageParallel1.fill(255u)
    Renderer.renderParallelProjection(imageParallel1, par1, 0u, removeHidden = false)
    imageParallel1.save("parallel_projection_with_hidden.png")
    task1Result.add("parallel_projection_with_hidden.png")

    val imageParallel2 = Image8bpp(800, 600)
    imageParallel2.fill(255u)
    Renderer.renderParallelProjection(imageParallel2, par1, 0u, removeHidden = true)
    imageParallel2.save("parallel_projection_no_hidden.png")
    task1Result.add("parallel_projection_no_hidden.png")

    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Перспективная проекция повернутого параллелепипеда")
    val task2Result = mutableListOf<String>()

    val par2 = createStandardParallelepiped(100.0, 80.0, 60.0, -200.0)

    par2.apply { it * Matrix.rotateByVector(Vector(1.0, 1.0, 0.0), 30.0) }

    val imagePerspective1 = Image8bpp(800, 600)
    imagePerspective1.fill(255u)
    Renderer.renderPerspectiveProjection(imagePerspective1, par2, 300.0, 0u, removeHidden = false)
    imagePerspective1.save("perspective_projection_with_hidden.png")
    task2Result.add("perspective_projection_with_hidden.png")

    val imagePerspective2 = Image8bpp(800, 600)
    imagePerspective2.fill(255u)
    Renderer.renderPerspectiveProjection(imagePerspective2, par2, 300.0, 0u, removeHidden = true)
    imagePerspective2.save("perspective_projection_no_hidden.png")
    task2Result.add("perspective_projection_no_hidden.png")

    println("Результат задачи 2 сохранен: $task2Result\n")

    println("\nВсе результаты задач сохранены в: src/main/resources/lab5/")
}
