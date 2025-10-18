import android.util.Size
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection

class AprilTagDetector(hardwareMap: HardwareMap, name: String, debugMode: Boolean) {

    // The debugMode bool determines if it draws everything, which could use up more processor time
    var processor: AprilTagProcessor = AprilTagProcessor.Builder()
        // Only show the overlay if we need it,
        .setDrawTagID(debugMode)
        .setDrawTagOutline(debugMode)
        .setDrawAxes(debugMode)
        .setDrawCubeProjection(debugMode)
        .build()

    var visionPortal: VisionPortal = VisionPortal.Builder()
        .setCamera(hardwareMap.get(WebcamName::class.java, name))
        .addProcessor(processor)
        .setCameraResolution(Size(640, 480)) // For the Logitech C270, this is the only resolution that FTC provides a calibration
        .setStreamFormat(VisionPortal.StreamFormat.MJPEG) // YUY2 is the default but MJPEG uses less bandwidth
        .enableLiveView(debugMode)
        .setAutoStopLiveView(true)
        .build()

    fun getTags() : ArrayList<AprilTagDetection> {
        visionPortal.setProcessorEnabled(processor, true)

        Thread.sleep(100) // Brief sleep to give the processor time to be slow

        val detections = processor.detections

        visionPortal.setProcessorEnabled(processor, false)

        return detections
    }
}