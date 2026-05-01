package common

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Image8bpp(val width: Int, val height: Int) {

    private val pixels: Array<UByte> = Array<UByte>(width * height) { 0u }

    companion object {
        fun load(filename: String): Image8bpp {
            val file = File(filename)
            val bufferedImage = ImageIO.read(file)
            val image = Image8bpp(bufferedImage.width, bufferedImage.height)

            for (y in 0 until bufferedImage.height) {
                for (x in 0 until bufferedImage.width) {
                    val rgb = bufferedImage.getRGB(x, y)
                    val r = (rgb shr 16) and 0xFF
                    val g = (rgb shr 8) and 0xFF
                    val b = rgb and 0xFF
                    val gray = (0.299*r + 0.587*g + 0.114*b).toInt().toUByte()
                    image.setPixel(x, y, gray)
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

    fun getPixel(x: Int, y: Int): UByte {
        validate(x, y)
        return pixels[y * width + x]
    }

    fun setPixel(x: Int, y: Int, value: UByte) {
        validate(x, y)
        pixels[y * width + x] = value
    }

    fun fill(value: UByte) {
        for (i in pixels.indices) {
            pixels[i] = value
        }
    }

    fun save(filename: String) {
        val stackTrace = Thread.currentThread().stackTrace
        var callerPackage = ""

        for (element in stackTrace) {
            val className = element.className
            if (className.contains("Image8bpp")) continue

            val resolved = when {
                className.startsWith("cgi.") || className.startsWith("legacy.") -> {
                    val parts = className.split(".")
                    if (parts.size >= 2) "${parts[0]}/${parts[1]}" else null
                }
                className.startsWith("lab") || className.startsWith("hw") -> className.substringBefore(".")
                else -> null
            }
            if (resolved != null) {
                callerPackage = resolved
                break
            }
        }

        val outputDir = File("src/main/resources/$callerPackage")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val gray = getPixel(x, y).toInt()
                val rgb = (gray shl 16) or (gray shl 8) or gray
                bufferedImage.setRGB(x, y, rgb)
            }
        }
        ImageIO.write(bufferedImage, "png", File(outputDir, filename))
    }
}