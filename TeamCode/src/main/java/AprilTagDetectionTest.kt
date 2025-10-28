import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName

@TeleOp
class AprilTagDetectionTest : LinearOpMode() {
    lateinit var aprilTagDetector: AprilTagDetector

    override fun runOpMode() {
        aprilTagDetector = AprilTagDetector(hardwareMap.get("Webcam") as CameraName, debugMode = true) // Can't be set in the constructor because the hardwaremap won't be initialized yet
        waitForStart()

        while (opModeIsActive()) {
            val tags = aprilTagDetector.getTags()
            for (tag in tags) {
                telemetry.addData("ID: ", tag.id)
                telemetry.addData("Center point: ", tag.center)
                telemetry.addData("Tag corners ", tag.corners)
                telemetry.addData("Pose: ", tag.ftcPose)
                telemetry.addData("Hamming: ", tag.hamming)

                telemetry.addLine()

                telemetry.update()
            }
        }
    }
}