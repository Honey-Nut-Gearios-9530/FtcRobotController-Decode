import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp
class AprilTagDetectionTest : LinearOpMode() {
    lateinit var aprilTagDetector: AprilTagDetector

    init {
        aprilTagDetector = AprilTagDetector(hardwareMap, "webcam", debugMode = true) // Can't be set in the constructor because the hardwaremap won't be initialized yet
        waitForStart()
    }

    override fun runOpMode() {
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