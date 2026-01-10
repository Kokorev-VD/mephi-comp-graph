package lab5

import common.Image8bpp
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class ImageScreen {
    private fun createAndShowWindow(
        title: String,
        dimension: Dimension,
        paintLogic: (Graphics) -> Unit,
        func: (JComponent) -> Unit
    ) {
        val show = Runnable {
            val screen = object : JComponent() {
                override fun getPreferredSize(): Dimension = dimension

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    paintLogic(g)
                }
            }

            val frame = JFrame(title)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.isResizable = false
            frame.contentPane = screen
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true

            func(screen)
        }

        if (SwingUtilities.isEventDispatchThread()) {
            show.run()
        } else {
            SwingUtilities.invokeLater(show)
        }
    }

    fun displayImage(
        image: Image8bpp,
        title: String = "Lab 5 Extended",
        func: (JComponent) -> Unit
    ) {
        createAndShowWindow(
            title = title,
            dimension = Dimension(image.width, image.height),
            paintLogic = { g ->
                val bufferedImage = BufferedImage(
                    image.width,
                    image.height,
                    BufferedImage.TYPE_BYTE_GRAY
                )
                for (y in 0 until image.height) {
                    for (x in 0 until image.width) {
                        val gray = image.getPixel(x, y).toInt()
                        val rgb = (gray shl 16) or (gray shl 8) or gray
                        bufferedImage.setRGB(x, y, rgb)
                    }
                }
                g.drawImage(bufferedImage, 0, 0, null)
            },
            func = func
        )
    }
}
