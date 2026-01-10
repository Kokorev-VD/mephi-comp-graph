package lab5

import common.Image8bpp
import lab5.Utils.CUBOID1_COLORS
import lab5.Utils.CUBOID2_COLORS
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

fun createColoredCuboid(w: Double, h: Double, d: Double, colors: List<UByte>): Parallelepiped {
    val halfW = w / 2.0
    val halfH = h / 2.0
    val halfD = d / 2.0

    val points = listOf(
        Vector(-halfW, -halfH, -halfD),
        Vector(halfW, -halfH, -halfD),
        Vector(halfW, halfH, -halfD),
        Vector(-halfW, halfH, -halfD),
        Vector(-halfW, -halfH, halfD),
        Vector(halfW, -halfH, halfD),
        Vector(halfW, halfH, halfD),
        Vector(-halfW, halfH, halfD)
    )

    val validColors = if (colors.size == 6) colors else CUBOID1_COLORS

    return Parallelepiped(points, validColors)
}

private fun createAndSaveImage(
    width: Int,
    height: Int,
    filename: String,
    renderAction: (Image8bpp) -> Unit
) {
    val image = Image8bpp(width, height)
    image.fill(255u)
    renderAction(image)
    image.save(filename)
}

private fun getVisibleFaces(
    parallelepiped: Parallelepiped,
    cuboidIndex: Int
): List<Triple<Int, Int, Side>> {
    return parallelepiped.sides.mapIndexedNotNull { index, side ->
        val edge1 = side[1] - side[0]
        val edge2 = side[3] - side[0]
        val normal = edge1 vectorMultiple edge2
        val cameraPos = Vector(0.0, 0.0, Renderer.cameraZ)
        val toCamera = cameraPos - side[0]
        if ((toCamera scalarMultiple normal) >= 0) {
            Triple(cuboidIndex, index, side)
        } else null
    }
}

private fun transformCuboid(
    baseCuboid: Parallelepiped,
    state: CuboidState,
    rotationAxis: Vector,
    sceneDepth: Double,
    perspectiveTransform: Matrix
): Pair<Parallelepiped, Parallelepiped> {
    val forDepth = baseCuboid.deepCopy()
    forDepth.apply {
        it * Matrix.rotateByVector(rotationAxis, state.rotationAngle)
        it * Matrix.translate(state.position.x, state.position.y, state.position.z)
        it * Matrix.translate(0.0, 0.0, sceneDepth)
    }

    val transformed = baseCuboid.deepCopy()
    transformed.apply {
        it * Matrix.rotateByVector(rotationAxis, state.rotationAngle)
        it * Matrix.translate(state.position.x, state.position.y, state.position.z)
        it * Matrix.translate(0.0, 0.0, sceneDepth)
        it * perspectiveTransform
        it * Matrix.projectionToXY()
    }

    return Pair(forDepth, transformed)
}

private fun applyScreenScale(parallelepiped: Parallelepiped, scale: Double, centerX: Double, centerY: Double) {
    parallelepiped.points.forEach {
        it.x = it.x * scale + centerX
        it.y = it.y * scale + centerY
    }
}

fun main() {
    println("Лабораторная работа No 5: 3D проекции и удаление невидимых ребер\n")

    println("Создаем статические изображения для отчета...\n")

    println("Задача 1. Параллельная проекция повернутого параллелепипеда")
    val task1Result = mutableListOf<String>()

    val par1 = createStandardParallelepiped(100.0, 80.0, 60.0, 0.0)
    par1.apply { it * Matrix.rotateByVector(Vector(1.0, 1.0, 0.0), 30.0) }

    createAndSaveImage(800, 600, "parallel_projection_with_hidden.png") { image ->
        Renderer.renderParallelProjection(image, par1, 0u, removeHidden = false)
    }
    task1Result.add("parallel_projection_with_hidden.png")

    createAndSaveImage(800, 600, "parallel_projection_no_hidden.png") { image ->
        Renderer.renderParallelProjection(image, par1, 0u, removeHidden = true)
    }
    task1Result.add("parallel_projection_no_hidden.png")

    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. Перспективная проекция повернутого параллелепипеда")
    val task2Result = mutableListOf<String>()

    val par2 = createStandardParallelepiped(100.0, 80.0, 60.0, -200.0)
    par2.apply { it * Matrix.rotateByVector(Vector(1.0, 1.0, 0.0), 30.0) }

    createAndSaveImage(800, 600, "perspective_projection_with_hidden.png") { image ->
        Renderer.renderPerspectiveProjection(image, par2, 300.0, 0u, removeHidden = false)
    }
    task2Result.add("perspective_projection_with_hidden.png")

    createAndSaveImage(800, 600, "perspective_projection_no_hidden.png") { image ->
        Renderer.renderPerspectiveProjection(image, par2, 300.0, 0u, removeHidden = true)
    }
    task2Result.add("perspective_projection_no_hidden.png")

    println("Результат задачи 2 сохранен: $task2Result\n")

    println("\nВсе результаты задач сохранены в: src/main/resources/lab5/")

    demonstration()
}

fun demonstration() {

    val baseCuboid1 = createColoredCuboid(1.0, 0.8, 0.6, CUBOID1_COLORS)
    val baseCuboid2 = createColoredCuboid(0.8, 1.0, 0.7, CUBOID2_COLORS)

    val orbit = OrbitAnimation(
        orbitRadius = 3.0,
        orbitSpeed = 2.0,
        rotationSpeed1 = 1.5,
        rotationSpeed2 = 2.0
    )

    val image = Image8bpp(800, 600)
    val kx = 15.0
    val ky = 15.0
    val kz = 15.0

    ImageScreen().displayImage(image, "Lab 5") { screen ->
        val timer = Timer(1000 / 30) {
            val (state1, state2) = orbit.step()

            val rotationAxis1 = Vector(1.0, 0.5, 0.0)
            val rotationAxis2 = Vector(0.0, 1.0, 0.5)
            val sceneDepth = -8.0
            val scale = 100.0
            val centerX = 400.0
            val centerY = 300.0

            val (forDepth1, transformed1) = transformCuboid(
                baseCuboid1, state1, rotationAxis1, sceneDepth, Matrix.twoPointPerspective(kx, kz)
            )
            val (forDepth2, transformed2) = transformCuboid(
                baseCuboid2, state2, rotationAxis2, sceneDepth, Matrix.threePointPerspective(kx, ky, kz)
            )

            applyScreenScale(transformed1, scale, centerX, centerY)
            applyScreenScale(transformed2, scale, centerX, centerY)

            image.fill(255u)

            val visibleFaces1 = getVisibleFaces(forDepth1, 0)
            val visibleFaces2 = getVisibleFaces(forDepth2, 1)

            val allFaces = (visibleFaces1 + visibleFaces2).sortedBy { it.third.minZ() }

            allFaces.forEach { (cuboidIndex, faceIndex, _) ->
                val cuboid = if (cuboidIndex == 0) transformed1 else transformed2
                val screenSide = cuboid.sides[faceIndex]
                Renderer.drawSideWithFill(image, screenSide, screenSide.color)
            }

            screen.repaint()
        }
        timer.start()
    }
}
