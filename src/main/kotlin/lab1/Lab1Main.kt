package lab1

import common.Image8bpp
import common.createCircle
import common.createGradient
import java.io.File
import kotlin.math.sqrt

fun main() {
    println("Лабораторная работа No 1: Смешивание изображений\n")

    println("Задача 1. Круглое полутоновое изображение")
    val firstTaskResult = mutableListOf<String>()
    val circleImage = createCircle(
        sizeX = 400,
        sizeY = 400,
        centerBrightness = 255u,
        edgeBrightness = 0u
    )
    circleImage.save("circle_gradient.png")
    firstTaskResult.add("circle_gradient.png")

    val inputDir = File("src/main/resources/common")
    val imageFiles = inputDir.listFiles { file ->
        file.extension.lowercase() in listOf("png", "jpg", "jpeg")
    } ?: emptyArray()

    for (imageFile in imageFiles) {
        val image = Image8bpp.load(imageFile.absolutePath)
        val masked = applyCircularMask(image, backgroundColor = 255u)
        val path = "${imageFile.nameWithoutExtension}_circular.png"
        masked.save(path)
        firstTaskResult.add(path)
    }

    println("Результат 1 задачи сохранен: $firstTaskResult\n")

    println("Задача 2. Смешивание двух изображений с альфа-каналом")
    val secondTaskResult = mutableListOf<String>()
    val image1 = Image8bpp(400, 400)
    image1.fill(200u)

    val image2 = Image8bpp(400, 400)
    image2.fill(50u)

    val alphaChannel = circleImage

    val blendedImage = blendImages(image1, image2, alphaChannel)
    blendedImage.save("blended_result.png")
    secondTaskResult.add("blended_result.png")

    if (imageFiles.size >= 2) {
        val image1 = Image8bpp.load(imageFiles[0].absolutePath)
        val image2 = Image8bpp.load(imageFiles[1].absolutePath)

        val alpha1 = createCircle(
            sizeX = image1.width,
            sizeY = image1.height,
            centerBrightness = 255u,
            edgeBrightness = 0u,
        )

        val alpha2 = createGradient(image1.width, image1.height)

        for (mode in BlendMode.entries) {
            val blended = generalizedBlend(image1, image2, alpha1, alpha2, mode)
            val path = "blend_${imageFiles[0].nameWithoutExtension}_${imageFiles[1].nameWithoutExtension}_${mode.name.lowercase()}.png"
            blended.save(path)
            secondTaskResult.add(path)
        }
    }
    println("Результат 2 задачи сохранен: ${secondTaskResult}\n")

    println("\nРезультаты задачи сохранены тут: src/main/resources/lab1/")
}

// 1 ЗАДАЧА
fun applyCircularMask(image: Image8bpp, backgroundColor: UByte): Image8bpp {
    val result = Image8bpp(image.width, image.height)
    val centerX = image.width / 2.0
    val centerY = image.height / 2.0
    val radius = minOf(image.width, image.height) / 2.0

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val dx = x - centerX
            val dy = y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= radius) {
                result.setPixel(x, y, image.getPixel(x, y))
            } else {
                result.setPixel(x, y, backgroundColor)
            }
        }
    }

    return result
}

// 2 ЗАДАЧА
fun blendImages(image1: Image8bpp, image2: Image8bpp, alphaChannel: Image8bpp): Image8bpp {
    val result = Image8bpp(image1.width, image1.height)

    for (y in 0 until image1.height) {
        for (x in 0 until image1.width) {
            val pixel1 = image1.getPixel(x, y)
            val pixel2 = image2.getPixel(x, y)
            val alpha = alphaChannel.getPixel(x, y).toDouble() / 255.0

            val blended = (pixel1.toDouble() * (1 - alpha) + pixel2.toDouble() * alpha).toUInt().toUByte()
            result.setPixel(x, y, blended)
        }
    }

    return result
}

enum class BlendMode(val transformation: (Double, Double) -> Double) {
    NORMAL({ c1, c2 -> c2 }),
    MULTIPLY({ c1, c2 -> c1 * c2 }),
    SCREEN({ c1, c2 -> 1.0 - (1.0 - c1) * (1.0 - c2) }),
    DARKEN({ c1, c2 -> minOf(c1, c2) }),
    LIGHTEN({ c1, c2 -> maxOf(c1, c2) })
}

fun generalizedBlend(
    image1: Image8bpp,
    image2: Image8bpp,
    alpha1: Image8bpp,
    alpha2: Image8bpp,
    mode: BlendMode
): Image8bpp {
    val result = Image8bpp(image1.width, image1.height)

    for (y in 0 until image1.height) {
        for (x in 0 until image1.width) {
            val c1 = image1.getPixel(x, y).toDouble() / 255.0
            val c2 = image2.getPixel(x, y).toDouble() / 255.0
            val a1 = alpha1.getPixel(x, y).toDouble() / 255.0
            val a2 = alpha2.getPixel(x, y).toDouble() / 255.0

            val blended = mode.transformation(c1, c2)

            val finalAlpha = a1 * a2
            val finalColor = blended * finalAlpha + c1 * (1 - finalAlpha)

            result.setPixel(x, y, (finalColor * 255.0).toInt().coerceIn(0, 255).toUByte())
        }
    }

    return result
}
