package cgi.hw

import common.Image8bpp
import common.addGaussianNoise

fun main() {
    val img = Image8bpp.load("src/main/resources/cgi/hw/new_test.png")

    val inverted = Image8bpp(img.width, img.height)
    for (y in 0 until img.height) {
        for (x in 0 until img.width) {
            val v = img.getPixel(x, y).toInt()
            inverted.setPixel(x, y, (255 - v).toUByte())
        }
    }
    inverted.save("new_test_inverted.png")

    val noisy = addGaussianNoise(inverted, sigma = 25.0, seed = 42L)
    noisy.save("new_test_noisy.png")
}
