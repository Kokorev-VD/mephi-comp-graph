package common

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow

class Image24bpp(val width: Int, val height: Int) {

    private val pixels: Array<Triple<UByte, UByte, UByte>> = Array(width * height) { Triple(0u, 0u, 0u) }

    companion object {
        fun load(filename: String): Image24bpp {
            val file = File(filename)
            val bufferedImage = ImageIO.read(file)
            val image = Image24bpp(bufferedImage.width, bufferedImage.height)

            for (y in 0 until bufferedImage.height) {
                for (x in 0 until bufferedImage.width) {
                    val rgb = bufferedImage.getRGB(x, y)
                    val r = ((rgb shr 16) and 0xFF).toUByte()
                    val g = ((rgb shr 8) and 0xFF).toUByte()
                    val b = (rgb and 0xFF).toUByte()
                    image.setPixel(x, y, r, g, b)
                }
            }

            return image
        }
    }

    private fun validate(x: Int, y: Int) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw IndexOutOfBoundsException("Координаты ($x, $y) вне границ изображения")
        }
    }

    fun getPixel(x: Int, y: Int): Triple<UByte, UByte, UByte> {
        validate(x, y)
        return pixels[y * width + x]
    }

    fun setPixel(x: Int, y: Int, r: UByte, g: UByte, b: UByte) {
        validate(x, y)
        pixels[y * width + x] = Triple(r, g, b)
    }

    fun getAverageBrightness(): Double {
        var sum = 0.0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val (r, g, b) = getPixel(x, y)
                val brightness = 0.299 * r.toInt() + 0.587 * g.toInt() + 0.114 * b.toInt()
                sum += brightness
            }
        }
        return sum / (width * height)
    }

    fun applyGammaCorrection(gamma: Double) {
        val invGamma = 1.0 / gamma
        for (y in 0 until height) {
            for (x in 0 until width) {
                val (r, g, b) = getPixel(x, y)

                val newR = (255.0 * (r.toDouble() / 255.0).pow(invGamma)).toInt().coerceIn(0, 255).toUByte()
                val newG = (255.0 * (g.toDouble() / 255.0).pow(invGamma)).toInt().coerceIn(0, 255).toUByte()
                val newB = (255.0 * (b.toDouble() / 255.0).pow(invGamma)).toInt().coerceIn(0, 255).toUByte()

                setPixel(x, y, newR, newG, newB)
            }
        }
    }

    fun save(filename: String) {
        val stackTrace = Thread.currentThread().stackTrace
        var callerPackage = ""

        for (element in stackTrace) {
            val className = element.className
            if (className.startsWith("hw") && !className.contains("Image24bpp")) {
                callerPackage = className.substringBefore(".")
                break
            }
        }

        val outputDir = File("src/main/resources/$callerPackage")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val (r, g, b) = getPixel(x, y)
                val rgb = (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
                bufferedImage.setRGB(x, y, rgb)
            }
        }
        ImageIO.write(bufferedImage, "png", File(outputDir, filename))
    }

}
