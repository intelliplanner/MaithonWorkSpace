import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


public class WebCamImageCapture {

	public static String CAM_IP = "";
	public static String CAM_USERNAME="";
	public static String CAM_PWD="";

	public static void main(String[] args) throws IOException {

		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();
		List<Webcam> list = webcam.getWebcams();
		
		webcam.open();

		// get image
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("test.png"));
	}
}
