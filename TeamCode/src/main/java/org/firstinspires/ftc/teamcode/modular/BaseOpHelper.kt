package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo

class BaseOpHelper(
    private val leftGateServo : Servo,
    private val rightGateServo: Servo,
    private val leftLauncherMotor: DcMotorEx,
    private val rightLauncherMotor: DcMotorEx,
    private val servoLauncher: Servo,
    private val driveTrain: Array<DcMotorEx>
) {
    private val delay: Long = 700
    fun leftGateServoCycle(){
        leftGateServo.position = 0.4
        Thread.sleep(delay)
        leftGateServo.position = 0.6
    }

    // cycles the left servo position to briefly let a ball go though.
    // uses a delay the robot so uncontrolled for half a second.
    fun rightGateServoCycle(){
        rightGateServo.position = 0.55
        Thread.sleep(delay)
        rightGateServo.position = 0.35
    }
    // set power to zero
    // set zero power to break
    // this will cause the robot to reist being pushed
    fun handBreak(){
        for (m in driveTrain){
            m.zeroPowerBehavior = ZeroPowerBehavior.BRAKE
            m.power = 0.0 // dose not overwrite motorPowers
        }
    }
    fun releaseHandBrake() {
        for (m in driveTrain) {
            m.zeroPowerBehavior = ZeroPowerBehavior.FLOAT
        }
    }


    var launchMotorsVelocity: Double
        get() {
            if (leftLauncherMotor.velocity - rightLauncherMotor.velocity in -1.0..1.0) {
                return leftLauncherMotor.velocity
            }

            return -1.0
        }

        set(velocity) {
            leftLauncherMotor.velocity = velocity
            rightLauncherMotor.velocity = velocity
        }

    fun launchBall() {
        // fires the ball with a condition for high and low speed launching
        val firePos = 0.9
        val restingPos = 0.7
        /*if (leftLauncherMotor.velocity > 1950) {
            servoLauncher.position = firePos
            Thread.sleep(500)
            servoLauncher.position = restingPos
        } else {
            // offset to account for wheels being smaller at slow speed
            servoLauncher.position = firePos + 0.1
            Thread.sleep(500)
            servoLauncher.position = restingPos
        }*/

        servoLauncher.position = firePos
        Thread.sleep(500)
        servoLauncher.position = restingPos
    }
}