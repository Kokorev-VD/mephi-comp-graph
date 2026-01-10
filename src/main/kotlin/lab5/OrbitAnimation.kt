package lab5

import kotlin.math.cos
import kotlin.math.sin

data class CuboidState(
    val position: Vector,
    val rotationAngle: Double
)

class OrbitAnimation(
    val orbitRadius: Double = 3.0,
    val orbitSpeed: Double = 2.0,
    val rotationSpeed1: Double = 1.5,
    val rotationSpeed2: Double = 2.0
) {
    private var orbitAngle = 70.0
    private var rotationAngle1 = 0.0
    private var rotationAngle2 = 0.0

    fun step(): Pair<CuboidState, CuboidState> {
        orbitAngle += orbitSpeed
        rotationAngle1 += rotationSpeed1
        rotationAngle2 += rotationSpeed2

        orbitAngle = orbitAngle % 360.0
        rotationAngle1 = rotationAngle1 % 360.0
        rotationAngle2 = rotationAngle2 % 360.0

        val angle1Rad = Math.toRadians(orbitAngle)
        val angle2Rad = Math.toRadians(orbitAngle + 90.0)

        val position1 = Vector(
            x = orbitRadius * cos(angle1Rad),
            y = 0.0,
            z = orbitRadius * sin(angle1Rad)
        )

        val position2 = Vector(
            x = orbitRadius * cos(angle2Rad),
            y = 0.0,
            z = orbitRadius * sin(angle2Rad)
        )

        return Pair(
            CuboidState(position1, rotationAngle1),
            CuboidState(position2, rotationAngle2)
        )
    }

}
