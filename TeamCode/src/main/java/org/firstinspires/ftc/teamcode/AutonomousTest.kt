package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver.EncoderDirection
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.modular.AutoStageExecutor
import org.firstinspires.ftc.teamcode.modular.BaseOpMode
import org.firstinspires.ftc.teamcode.modular.Units
import org.firstinspires.ftc.teamcode.modular.AutoStageExecutor.Stage
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver.GoBildaOdometryPods
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.hardware.DcMotor.RunMode
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D
import org.firstinspires.ftc.teamcode.modular.Vector2
import kotlin.math.abs

@Disabled
@Autonomous(name = "AutonomousTest", group = "OpMode")
class AutonomousTest : BaseOpMode() {
    private lateinit var odometry: GoBildaPinpointDriver
    private lateinit var plan: AutoStageExecutor
    val velocity = 800.0
    var pose = Pose2D(DistanceUnit.CM, 0.0, 0.0, AngleUnit.RADIANS, 0.0)
    val directionVector = Vector2(0.0, 0.0)
    var isOpModeActive = true

    override fun initialize() {
        // TODO rewrite in java
        odometry = hardwareMap.get(GoBildaPinpointDriver::class.java, "goBildaPinpoint")
        odometry.setOffsets(6.0, -9.5, DistanceUnit.CM)
        odometry.setEncoderResolution(GoBildaOdometryPods.goBILDA_SWINGARM_POD)
        odometry.setEncoderDirections(EncoderDirection.REVERSED, EncoderDirection.FORWARD)
        odometry.resetPosAndIMU()

        driveTrain.forEach { m -> m.mode = RunMode.RUN_USING_ENCODER }

        plan = AutoStageExecutor (
            Stage(
                { pose.y < 1 },
                { directionVector.y = 1.0 }
            ),

            Stage(
                { pose.x < 1 },
                {
                    directionVector.x = 1.0
                    directionVector.y = 0.0
                }
            ),

            Stage(
                { pose.y > 0 },
                {
                    directionVector.x = 0.0
                    directionVector.y = -1.0
                }
            ),

            Stage(
                { pose.x > 0 },
                {
                    directionVector.x = -1.0
                    directionVector.y = 0.0
                }
            )
        )
    }

    override fun loop() {
        telemetry.addLine("Direction: $directionVector")
        telemetry.addLine("Stage Number: ${plan.getStageNumber()}")
        telemetry.addLine("Angle: ${pose.angle}")
        telemetry.addLine("Odometry (tile): \n" +
                "x -> ${pose.x}\n" +
                "y -> ${pose.y}"
        )
        telemetry.update()
        odometry.update()
        pose = odometry.position

        if (isOpModeActive && !plan.update()) {
            directionVector.x = 0.0
            directionVector.y = 0.0
            isOpModeActive = false
        }

        updateDriveTrain(directionVector)
    }

    private fun updateDriveTrain(v: Vector2<Double>) {
        val turnPower = 0

        val motorPowers = arrayOf(
            -v.y - v.x + turnPower,
            -v.y + v.x - turnPower,
            -v.y + v.x + turnPower,
            -v.y - v.x - turnPower,
        )

        val max = motorPowers.maxOf { i -> abs(i) }

        motorPowers.forEachIndexed { i, _ -> motorPowers[i] /= max }
        driveTrain.forEachIndexed { i, m -> m.velocity = motorPowers[i] * -velocity }
    }
}

private val Pose2D.x: Double
    get() {
        return Units.convert(getX(DistanceUnit.CM), Units.CM, Units.TILE)
    }

private val Pose2D.y: Double
    get() {
        return Units.convert(getY(DistanceUnit.CM), Units.CM, Units.TILE)
    }

private val Pose2D.angle: Double
    get() {
        return getHeading(AngleUnit.RADIANS)
    }
