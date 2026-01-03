import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.modular.BaseOpMode
import kotlin.math.abs


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
@TeleOp(name = "war teli-op", group = "Iterative OpMode")
class TeliOpModeIterative : BaseOpMode() {
    var runtime : ElapsedTime = ElapsedTime()
    private var forwardMotion = 0.0 // Note: pushing stick forward gives negative value
    private var lateralMotion = 0.0
    private var yawMotion = 0.0
    private var launchSpeed = 0.0
    //TODO elaborate on the below todo statement
    //TODO implement

    private val powerSettings = arrayOf(0.25, 0.5, 0.66)
    private var powerSettingIndex = 0
    private var diverPower = powerSettings[powerSettingIndex]

    // in this could be faster but their is no reason to make it faster
    // in the configuration of the robot as of nov 4 2700 is if anything too powerful
    private var maxLaunchSpeed = 2700.0 // ticks
    //TODO add to drive train
    private var avgLaunchVelocity = 0.0 // ticks
    private var launchSpeedIncrement = 25

    //zone launch speeds and could need fine toning by the diver!
    private val nearZoneLaunchSpeed = 1600.0

    private val farZoneLaunchSpeed = 2300.0

    private var maxDriveMotorPower = 0.0
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
        forwardMotion = -gamepad1.left_stick_y.toDouble() // Note: pushing stick forward gives negative value
        lateralMotion = gamepad1.left_stick_x.toDouble()
        yawMotion = gamepad1.right_stick_x.toDouble()

        if (gamepad2.left_bumper || gamepad2.dpad_left) {
            yawMotion = 0.5
        } else if(gamepad2.right_bumper || gamepad2.dpad_right){
            yawMotion = -0.5
        }

        if(gamepad2.dpad_up) {
            launchSpeed = farZoneLaunchSpeed
        } else if(gamepad2.dpad_down){
            launchSpeed = nearZoneLaunchSpeed
        }

        val motorPowers = arrayOf(
            -gamepad1.left_stick_y + gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x - yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y + gamepad1.left_stick_x - yawMotion,
        )

        if (gamepad1.right_bumper) {
            powerSettingIndex++
            Thread.sleep(250L)
        }

        if (gamepad1.left_bumper) {
            powerSettingIndex--
            Thread.sleep(250L)
        }

        powerSettingIndex = abs(powerSettingIndex % powerSettings.size) // prevent index error

        // Normalize the values so no wheel power exceeds 100%

        // motorPower starts off as 0 at the start of the program
        maxDriveMotorPower = motorPowers.maxOf { abs(it) }

        // the motors should not be normalised to unless a index of motorPower is grater then one
        if(maxDriveMotorPower > 1){
            motorPowers.forEachIndexed {i, _ -> motorPowers[i] /= abs(maxDriveMotorPower)}
        }
        else {
            driveTrain.forEachIndexed {i, m -> m.power = motorPowers[i] * diverPower}
        }

        if (gamepad2.aWasPressed()) {
            launchSpeed += launchSpeedIncrement
        } else if (gamepad2.bWasPressed()) {
            launchSpeed -= launchSpeedIncrement
        }

        launchSpeed = minOf(launchSpeed, maxLaunchSpeed)
        launchSpeed = maxOf(0.0, launchSpeed)

        launcherMotors.forEach { it.velocity = launchSpeed }

        //TODO move to base op-mode
        if(gamepad2.xWasPressed()){
            runtime.reset()
            while(runtime.seconds() < 1.0) {
                servoLauncher.position = 1.0
            }
            servoLauncher.position = 0.7
        }

        // avgVelocity = ((leftLauncherMotor.getVelocity(AngleUnit.RADIANS) + rightLauncherMotor.getVelocity(AngleUnit.RADIANS)) / 2)

        telemetry.addData("launchSpeedSet: ", launchSpeed)
        telemetry.addData("left launch speed radian: ", leftLauncherMotor.getVelocity(AngleUnit.RADIANS))
        telemetry.addData("left launch speed radian: ", rightLauncherMotor.getVelocity(AngleUnit.RADIANS))
        telemetry.addData("left launch speed tick: ", leftLauncherMotor.velocity)
        telemetry.addData("right launch speed tick: ", rightLauncherMotor.velocity)
        telemetry.addData("avg speed: ", avgLaunchVelocity)
        // TODO this is output as a irrational number have this be in the form of radians * PI
        // E.G 0.5π or (1/2)π both work fine
        telemetry. addData("power setting: ", diverPower)

        telemetry.update()
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    override fun stop() {
        return
    }
}
