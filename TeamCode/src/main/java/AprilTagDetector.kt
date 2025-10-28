import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection

enum class Pattern(val id: Int) {
    GPP(21), // Green purple purple
    PGP(22), // Purple green purple
    PPG(23) // Purple purple green
}

class AprilTagDetector(webcam: CameraName, private val debugMode: Boolean) {

    // The debugMode bool determines if it draws everything, which could use up more processor time
    val processor: AprilTagProcessor = AprilTagProcessor.Builder()
        // Only show the overlay if we need it,
        .setDrawTagID(debugMode)
        .setDrawTagOutline(debugMode)
        .setDrawAxes(debugMode)
        .setDrawCubeProjection(debugMode)
        .build()

    val visionPortal: VisionPortal = VisionPortal.Builder()
        .setCamera(webcam)
        .addProcessor(processor)
        .setCameraResolution(Size(640, 480)) // For the Logitech C270, this is the only resolution that FTC provides a calibration
        .setStreamFormat(VisionPortal.StreamFormat.MJPEG) // YUY2 is the default but MJPEG uses less bandwidth
        .enableLiveView(debugMode)
        .setAutoStopLiveView(true)
        .build()

    fun getTags() : ArrayList<AprilTagDetection> {
        if (!debugMode) {
            visionPortal.setProcessorEnabled(processor, true)
            Thread.sleep(100)
        }

        val detections = processor.detections

        if (!debugMode) visionPortal.setProcessorEnabled(processor, false)

        return detections
    }

    fun getPattern() : ArrayList<Pattern> {
        if (!debugMode) {
            visionPortal.setProcessorEnabled(processor, true)
            Thread.sleep(100)
        }

        var result = ArrayList<Pattern>()

        val detections = processor.detections

        if (!debugMode) visionPortal.setProcessorEnabled(processor, false)

        if (detections.isEmpty()) throw Exception("No AprilTags detected")

        for (detection in detections) {
            when (detection.id) {
                21 -> result.add(Pattern.GPP)
                22 -> result.add(Pattern.PGP)
                23 -> result.add(Pattern.PPG)
                else -> continue
            }
        }

        return result
    }
}