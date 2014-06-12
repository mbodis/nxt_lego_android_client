package svb.nxt.robot.logic;

import org.opencv.core.Mat;

import svb.nxt.robot.logic.img.ImageConvertClass;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * 
 * @author svab
 *
 */
public class DrillPrinterHelper {

	/**
	 * 
	 * @param capturedImage
	 * @param cutFromX
	 * @param cutFromY
	 * @param cropWidth
	 * @param cropHeight
	 * @param part
	 * @param partTotal
	 * @param PART_SIZE
	 * @param game
	 * @return
	 */
	public static boolean sendImgPart(Mat capturedImage, int cutFromX,
			int cutFromY, int cropWidth, int cropHeight, int part, int partTotal,
			int PART_SIZE, GameTemplateClass game) {
		// TODO
		return false;
	}

	/**
	 * 
	 * @param capturedImage
	 * @param cutFromX
	 * @param cutFromY
	 * @param cropWidth
	 * @param cropHeight
	 * @param game
	 */
	public static void sendImgTogether(Mat capturedImage, int cutFromX,
			int cutFromY, int cropWidth, int cropHeight, GameTemplateClass game) {
		// TODO
	}

	/**
	 * 
	 * @param capturedImage
	 * @param cutFromX
	 * @param cutFromY
	 * @param cropWidth
	 * @param cropHeight
	 * @param PART_SIZE
	 * @return
	 */
	public static int getCountImageParts(Mat capturedImage, int cutFromX,
			int cutFromY, int cropWidth, int cropHeight, int PART_SIZE) {
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cutFromX, cutFromY, cropWidth, cropHeight);
		int[] arr = ImageConvertClass.getImagetoIntArr(b);
		
		int len = arr.length;
		int totalParts = len/(PART_SIZE) + (((len % (PART_SIZE))>0)? 1 : 0);
		Log.d("SVB", "totalParts: " + totalParts);
		
		return totalParts;
	}

}
