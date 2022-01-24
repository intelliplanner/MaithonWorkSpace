import com.teamdev.jxcapture.CompressionQuality;
import com.teamdev.jxcapture.ImageCapture;
import com.teamdev.jxcapture.image.ImageFormat;
import com.teamdev.jxcapture.video.FullScreen;

public class CaptureVedioImage {
public static void main(String s[]){
	ImageCapture imageCapture = ImageCapture.create(new FullScreen());
    long before = System.currentTimeMillis();
    imageCapture.takeSnapshot().save(new File("FullScreen.jpg"), ImageFormat.JPEG, CompressionQuality.HIGH);   
    long after = System.currentTimeMillis();
    imageCapture.release();
    System.out.println("Operation took " + (after - before) + " milliseconds.");
} 
}
