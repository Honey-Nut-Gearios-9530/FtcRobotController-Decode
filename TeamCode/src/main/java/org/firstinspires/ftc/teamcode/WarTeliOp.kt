package org.firstinspires.ftc.teamcode

import com.bylazar.telemetry.PanelsTelemetry
import com.qualcomm.hardware.limelightvision.Limelight3A
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.modular.Alliance
import org.firstinspires.ftc.teamcode.modular.BaseOpHelper
import org.firstinspires.ftc.teamcode.modular.BaseOpMode
import org.firstinspires.ftc.teamcode.modular.GoBildaPrismDriver.GoBildaPrismDriver
import org.firstinspires.ftc.teamcode.modular.Localization
import org.firstinspires.ftc.teamcode.modular.MutableReference
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
class WarTeliOp : BaseOpMode() {
    var runtime : ElapsedTime = ElapsedTime()
    private var forwardMotion = 0.0 // Note: pushing stick forward gives negative value
    private var lateralMotion = 0.0
    private var yawMotion = 0.0
    private var launchSpeed = 0.0
    private val powerSettings = arrayOf(0.25, 0.5, 0.7, 1.0)
    private var powerSettingIndex = 0
    private var drivePower = powerSettings[powerSettingIndex]

    // in this could be faster but their is no reason to make it faster
    // in the configuration of the robot as of nov 4 2700 is if anything too powerful
    private var maxLaunchSpeed = 2700.0 // ticks

    //zone launch speeds and could need fine toning by the diver!

    private var maxDriveMotorPower = 0.0


    private lateinit var ledDriver: GoBildaPrismDriver

    private lateinit var aimGide: AimingGuide

    private lateinit var limelight: Limelight3A

    private lateinit var localizationClass: Localization

    private lateinit var imu: IMU

    private var ally = MutableReference(Alliance.BLU)

    private var autoSpeed = true

    private val panelsTelemetry = PanelsTelemetry.telemetry



    /*
     * Code to run ONCE when the driver hits INIT
     */

    override fun initialize() {

        telemetry.addData("Status", "Initialized")
        telemetry.update()
        runtime.reset()
        limelight = hardwareMap.get(Limelight3A::class.java, "limelight")
        ledDriver = hardwareMap.get(GoBildaPrismDriver::class.java, "goBildaPrism")
        imu = hardwareMap.get(IMU::class.java, "imu")
        localizationClass = Localization(limelight, imu, ally)
        aimGide = AimingGuide(ledDriver, localizationClass)

    }



    override fun init_loop() {
        if(gamepad1.xWasPressed()){
            ally(Alliance.RED)
        }else if(gamepad1.bWasPressed()){
            ally(Alliance.BLU)
        }
        telemetry.addLine("Alliance $ally")
        telemetry.update()
    }

    override fun start() {
        runtime.reset()
    }


    /*
     * Code to run REPEATEDLY after the driver hits START but before they hit STOP
     */
    fun setLaunchSpeedOverride(){
        val nearZoneLaunchSpeed = 1600.0

        val midZoneLauchSpeed = 1800.0

        val farZoneLaunchSpeed = 2100.0

        val launchSpeedIncrement = 25

        if(gamepad2.dpad_up){
            launchSpeed = farZoneLaunchSpeed
        }
        if(gamepad2.dpad_down){
            launchSpeed = nearZoneLaunchSpeed
        }
        if(gamepad2.dpad_left){
            launchSpeed = midZoneLauchSpeed
        }

        if(gamepad2.yWasPressed()){
            launchSpeed = 0.0
        }
        if(launchSpeed > maxLaunchSpeed){
            launchSpeed = maxLaunchSpeed
        }

        if(launchSpeed < 0.0){
            launchSpeed = 0.0
        }


        if (gamepad2.right_bumper) {
            launchSpeed += launchSpeedIncrement
        } else if (gamepad2.left_bumper) {
            launchSpeed -= launchSpeedIncrement
        }
    }

    fun getYawOverride(): Double {
        return (gamepad2.left_trigger - gamepad2.right_trigger)*0.33

    }
    fun setDriveSpeedFromControl(){
        if (gamepad1.rightBumperWasPressed()) {
            powerSettingIndex++
        }
        if (gamepad1.leftBumperWasPressed()) {
            powerSettingIndex--

        }

        if(powerSettingIndex < 0){powerSettingIndex = powerSettings.size - 1}
        powerSettingIndex %= powerSettings.size // keep power setting
        powerSettingIndex = abs(powerSettingIndex)

        drivePower = powerSettings[powerSettingIndex]
    }
    override fun loop() {
        forwardMotion = -gamepad1.left_stick_y.toDouble() // Note: pushing stick forward gives negative value
        lateralMotion = gamepad1.left_stick_x.toDouble()
        yawMotion = gamepad1.right_stick_x.toDouble()

        yawMotion += getYawOverride()

        aimGide.update() // updates LED



        val motorPowers = arrayOf(
            -gamepad1.left_stick_y + gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x - yawMotion,
            -gamepad1.left_stick_y - gamepad1.left_stick_x + yawMotion,
            -gamepad1.left_stick_y + gamepad1.left_stick_x - yawMotion,
        )



        setDriveSpeedFromControl()

        // Normalize the values so no wheel power exceeds 100%
        for(p in motorPowers){
            if (abs(p) > maxDriveMotorPower){
                maxDriveMotorPower = abs(p)
            }
        }
        if(maxDriveMotorPower < 1){
            // if the drive motor power is less then one the motors will be set faster then 1.0
            maxDriveMotorPower = 1.0
        }
        //motorPowers.forEachIndexed {i ,m-> motorPowers[i] /= abs(maxDriveMotorPower)}//



        if(gamepad1.y){
            baseHelper.handBreak()
        }else if(gamepad1.yWasReleased()){
            baseHelper.releaseHandBrake()
        }else{
            for(i in motorPowers.indices){motorPowers[i] /= abs(maxDriveMotorPower)}
            driveTrain.forEachIndexed {i, m -> m.power = motorPowers[i] * drivePower}
        }

        if(gamepad2.aWasPressed()){
            baseHelper.launchBall()
        }
        if(gamepad2.bWasPressed()){
            baseHelper.rightGateServoCycle()
        }
        if(gamepad2.xWasPressed()){
            baseHelper.leftGateServoCycle()
        }
        if (gamepad2.yWasPressed()){
                autoSpeed = !autoSpeed
                launchSpeed= 0.0
            }


        if(autoSpeed){
            launchSpeed = localizationClass.estimatedTicks
        }else{
            setLaunchSpeedOverride()
        }

        for(m in launcherMotors){
            m.velocity = launchSpeed
        }





        telemetry.addData("autoSpeed", localizationClass.estimatedTicks)
        telemetry.addData("left flywheel", leftLauncherMotor.velocity)
        telemetry.addData("right flywheel", rightLauncherMotor.velocity)
        telemetry.addData("Power Setting",powerSettings[powerSettingIndex])
        telemetry.addData("allice", ally)
        telemetry.addData("aiming info", aimGide.toString())
        telemetry.addData("pow setting index", powerSettingIndex)
        telemetry.addLine(autoSpeed.toString())


        telemetry.update()
        panelsTelemetry.update(telemetry)
    }



    /*
     * Code to run ONCE after the driver hits STOP
     */
    override fun stop() {
        //stop behavior if needed
    }
}