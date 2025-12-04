package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.teamcode.modular.AutoStageExecutor
import org.firstinspires.ftc.teamcode.modular.AutoStageExecutor.Stage
import org.firstinspires.ftc.teamcode.modular.BaseAutonomous
import java.lang.Thread.sleep

@Autonomous(name = "BlueCenterAutonomous", group = "BlueTeam")
class BlueCenterAutonomous : BaseAutonomous() {
    override val plan = AutoStageExecutor(
        Stage(
            { pose.y < 3.375 },// magic nubers, put units
            {
                directionVector.x = 0.0 // units
                directionVector.y = 3.375
                turnPower = 0.0
            }
        ),

        Stage(
            { pose.angle < 0.65 },
            {
                directionVector.x = 0.0
                directionVector.y = 0.0
                turnPower = 0.75
            }
        ),

        Stage(
            { leftLauncherMotor.velocity < motorLaunchVelocity && rightLauncherMotor.velocity < motorLaunchVelocity },
            {
                leftLauncherMotor.velocity = motorLaunchVelocity
                rightLauncherMotor.velocity = motorLaunchVelocity

                directionVector.x = 0.0 // units
                directionVector.y = 0.0
                turnPower = 0.0
            }
        ),

        Stage(
            { ballsLaunched < 3 },
            {
                directionVector.x = 0.0
                directionVector.y = 0.0
                turnPower = 0.0

                if (servoLauncher.position < servoLaunch) {
                    servoLauncher.position = servoLaunch
                    ballsLaunched++
                    sleep(launchBallDelay)
                }

                if (servoLauncher.position > servoRetract) {
                    servoLauncher.position = servoRetract
                    sleep(rackBallDelay)
                }
            }
        ),

        Stage(
            { leftLauncherMotor.velocity > (motorLaunchVelocity - 1) && rightLauncherMotor.velocity > (motorLaunchVelocity - 1) },
            {
                leftLauncherMotor.velocity = 0.0
                rightLauncherMotor.velocity = 0.0

                directionVector.x = 0.0
                directionVector.y = 0.0
                turnPower = 0.0
            }
        ),

        Stage(
            { pose.angle > 0.2 },
            {
                directionVector.x = 0.0
                directionVector.y = 0.0
                turnPower = -0.75
            }
        ),

        Stage(
            { pose.y > 2.25},
            {
                directionVector.x = 0.0
                directionVector.y = -2.25
                turnPower = 0.0
            }
        )
    )
}
