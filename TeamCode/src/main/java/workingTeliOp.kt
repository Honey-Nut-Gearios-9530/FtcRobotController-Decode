package org.firstinspires.ftc.teamcode

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.modular.BaseOpMode
import kotlin.math.abs


@TeleOp(name = "war teli-op", group = "Iterative OpMode")
//@Disabled
class workingTeliOp : BaseOpMode() {
    // Declare OpMode members.

    // Declare OpMode members.


    var runtime : ElapsedTime = ElapsedTime()
    private var axialMotion = 0.0 // Note: pushing stick forward gives negative value
    private var lateralMotion = 0.0
    private var yawMotion = 0.0
    private var launchSpeed = 0.0
    //TODO implement
    private var powerSetting = 0.25

    // in this could be faster but their is no reason to make it faster
    // in the configuration of the robot as of nov 4 2700 is if anything too powerful
    // the absolute maxPowers 2770 this could cause issues with power getting to the motor

    private var maxLaunchSpeed = 2.6 * Math.PI //5796 ticks
    //TODO add to drive train
    private var avgVelocity = 0.0 // radians

    private var launchSpeedIncrement = (1.0/20.0) * Math.PI

    //zone launch speeds and could need fine toning by the diver!
    private val nearZoneLaunchSpeed = 1.5 * Math.PI

    private val farZoneLaunchSpeed = 2.1 * Math.PI

    private var maxDriveMotorPower = 8.5 // 2.7pi
    /*
     * Code to run ONCE when the driver hits INIT
     */

    override fun initialize() {
        telemetry.addData("Status", "Initialized")
        telemetry.update()
        runtime.reset()

    }

    override fun start() {
        runtime.reset()
    }

    /*
     * Code to run REPEATEDLY after the driver hits START but before they hit STOP
     */
    override fun loop() {
        // TODO get controls
        axialMotion = -gamepad1.left_stick_y.toDouble() // Note: pushing stick forward gives negative value
        lateralMotion = gamepad1.left_stick_x.toDouble()
        yawMotion = gamepad1.right_stick_x.toDouble()


        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        if(gamepad2.left_bumper || gamepad2.dpad_left){
            yawMotion = 0.5
        }
        if(gamepad2.right_bumper || gamepad2.dpad_right){
            yawMotion = -0.5
        }
        if(gamepad2.dpad_up){
            launchSpeed = farZoneLaunchSpeed
        }
        if(gamepad2.dpad_down){
            launchSpeed = nearZoneLaunchSpeed
        }

        val motorPowers = arrayOf(
            -gamepad1.left_stick_y + gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x - yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y + gamepad1.left_stick_x - yawMotion,
        )


        //todo switch to bumpers
        if( gamepad1.dpad_up){
            powerSetting = 0.25
        }else if(gamepad1.dpad_left){
            powerSetting = 0.5
        }else if(gamepad1.dpad_down){
            powerSetting = 0.66
        }


        // Normalize the values so no wheel power exceeds 100%

        for(p in motorPowers){
            if (p > abs(maxDriveMotorPower)){
                maxDriveMotorPower = abs(p)
            }
        }
        // power setting will come after

        // the motors should not be normalised to unless a index of motorPower is grater then one
        driveTrain.forEachIndexed {i, m -> m.power = motorPowers[i] * powerSetting}

        if(maxDriveMotorPower > 1){
            motorPowers.forEachIndexed {i, m -> motorPowers[i] /= abs(maxDriveMotorPower)}
        }


        /*
        TODO test this out with hardware people see what speeds work best.
        after you find the speeds the the driver wants put them map them to buttons.
         */

        if (gamepad2.aWasPressed()) {
            launchSpeed += (launchSpeedIncrement) * Math.PI
        } else if (gamepad2.bWasPressed()) {
            launchSpeed -= (launchSpeedIncrement) * Math.PI
        }

        if(launchSpeed > maxLaunchSpeed){
            launchSpeed = maxLaunchSpeed
        }
        if(launchSpeed < 0.0){
            launchSpeed = 0.0
        }
        //telemetry.addData("max speed ", maxLaunchSpeed)
       // telemetry.addData("speed ", launchSpeed)



        for(m in launcherMotors){
            m.setVelocity(launchSpeed, AngleUnit.RADIANS)
        }



        if(gamepad2.xWasPressed()){
            runtime.reset()
            while(runtime.seconds() < 1.0) {
                servoLauncher.position = 1.0
            }
            servoLauncher.position = 0.7
        }

        leftLauncherMotor.setVelocity(launchSpeed, AngleUnit.RADIANS)
        rightLauncherMotor.setVelocity(launchSpeed, AngleUnit.RADIANS)

       // avgVelocity = ((leftLauncherMotor.getVelocity(AngleUnit.RADIANS) + rightLauncherMotor.getVelocity(AngleUnit.RADIANS)) / 2)

        telemetry.addData("launchSpeedSet: ", launchSpeed)
        telemetry.addData("left launch speed radian: ", leftLauncherMotor.getVelocity(AngleUnit.RADIANS))
        telemetry.addData("left launch speed radian: ", rightLauncherMotor.getVelocity(AngleUnit.RADIANS))
        telemetry.addData("left launch speed tick: ", leftLauncherMotor.velocity)
        telemetry.addData("left launch speed tick: ", rightLauncherMotor.velocity)
        telemetry.addData("avg speed: ", avgVelocity)
        // TODO this is output as a irrational number have this be in the form of radians * PI
        // E.G 0.5π or (1/2)π both work fine
        telemetry. addData("power setting: ", powerSetting)

        telemetry.update()
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
}


/*
* This file contains an example of an iterative (Non-Linear) "OpMode".
* An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
* The names of OpModes appear on the menu of the FTC Driver Station.
* When a selection is made from the menu, the corresponding OpMode
* class is instantiated on the Robot Controller and executed.
*
* This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
* It includes all the skeletal structure that all iterative OpModes contain.
*
* Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
* Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
*/
