package svb.nxt.robot.logic;

import org.opencv.core.Mat;

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
		// TODO
		return 0;
	}

}
