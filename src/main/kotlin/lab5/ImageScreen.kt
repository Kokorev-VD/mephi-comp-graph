package lab5

import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class ImageScreen {
    fun displayImage(
        parallelepiped: Parallelepiped,
        func: (JComponent) -> Unit
    ) {
        val show = Runnable {
            val screen = object : JComponent() {
                override fun getPreferredSize(): Dimension {
                    return Dimension(1000, 1000)
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    Renderer.render(g, parallelepiped)
                }
            }

            val frame = JFrame("Lab 5 - 3D Parallelepiped")
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
}
