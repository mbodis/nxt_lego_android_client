package svb.nxt.robot.logic;

import java.util.ArrayList;

import org.opencv.core.Mat;

import svb.lib.log.MyLogger;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
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
	public static boolean sendImgPart(Mat capturedImage, int cropStartX,
			int cropStartY, int cropWidth, int cropHeight, int part, int partTotal,
			int PART_SIZE, GameTemplateClass game) {
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropStartX, cropStartY, cropWidth, cropHeight);
		ArrayList<Integer> list = ImageConvertClass.getImagetoIntList(b);
		// Log.d("SVB", "res: " + sb.toString());		
		
		if (part == 1){
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
			 MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW START");
		}else{
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START_PACKAGE, BTControls.ACTION_PACKAGE_NEW_CONTENT);
			 MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW PACKAGE - " + part + "/" + partTotal);
		}		
		
		int reading_part = 1;
		int partSize = 0; 
		
		boolean end_row = false;
		for (int r=0; r < cropHeight; r++){
			for (int c=0; c < cropWidth; c++){
				int val = list.get(c + r * cropWidth);
				byte bval = (byte)val; 
				
				partSize++;
				
				if (reading_part == part){					
					if (end_row){
						game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
						 MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW LINE ");
					}
				}
				
				end_row = false;
								
				if (reading_part == part){
					game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval);
					 MyLogger.addLog(game, "sending.txt", "data:" + val+" list["+(c + r * cropWidth)+"]");					 
				}
				
				if (partSize == PART_SIZE){
					partSize = 0;
					reading_part++;
				}			
								
			}
			end_row = true;
		}
		
		if (partTotal == part){
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, BTControls.ACTION_PRINT);
			 MyLogger.addLog(game.getApplicationContext(), "sending.txt", "END FILE");			
			return true;
		}else{			
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END_PACKAGE, BTControls.ACTION_PRINT);
			 MyLogger.addLog(game.getApplicationContext(), "sending.txt", "END PACKAGE - " + part + "/" + partTotal);			
		}
		return false;
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
		ArrayList<Integer> list = ImageConvertClass.getImagetoIntList(b);
				
		int totalParts = list.size()/(PART_SIZE) + (((list.size() % (PART_SIZE))>0)? 1 : 0);
		// Log.d("SVB", "totalParts: " + totalParts);
		
		return totalParts;
	}

}
