package svb.nxt.robot.logic;

import org.opencv.core.Mat;

import svb.lib.log.MyLogger;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.img.ImageConvertClass;
import android.graphics.Bitmap;

/**
 * pomocna trieda pre perovu tlaciaren
 *  
 * @author svab
 *
 */
public class PenPrinterHelper{
	
	/**
	 * posielanie suboru po castiach
	 * @param capturedImage povodny obrazok
	 * @param cropStartX zacaitok vyrezu X
	 * @param cropStartY zaciatok vyrezu Y
	 * @param cropWidth sirka vyrezu od bodu [cropStartX, cropStartY]
	 * @param cropHeight vyska vyrezu od bodu [cropStartX, cropStartY]
	 * @param part ktora cast sa posiela
	 * @param partTotal pocet casti spolu
	 * @param game referencia na triedu hry pre zaielanie dat cez BT
	 */
	public static boolean sendImgPart(Mat capturedImage, int cropStartX, int cropStartY, 
			int cropWidth, int cropHeight, int part, int partTotal, 
			int PART_SIZE, GameTemplateClass game){	
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropStartX, cropStartY, cropWidth, cropHeight);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		//Log.d("SVB", "res: " + sb.toString());		
		
		if (part == 1){
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
			// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW START");
		}else{
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START_PACKAGE, BTControls.ACTION_PACKAGE_NEW_CONTENT);
			// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW PACKAGE");
		}		
		
		int reading_part = 1;
		int partSize = 0; 
		
		boolean end_row = false;
		for (int r=0; r < cropHeight; r++){
			for (int c=0; c < cropWidth / 8; c++){
				int from = r*cropWidth + c*8;
				int to = from + 8;
				byte bval = (byte) Integer.parseInt(sb.substring(from, to), 2);
				
				partSize++;							
				
				if (reading_part == part){
					// MyLogger.addLog(this, "sending.txt", "part: " + part + " tot:" + partTotal + " reading:" + reading_part + " partSize:" + partSize);					
					game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval);
					// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "DATA: " + sb.substring(from, to));
										
					// Log.d("SVB", "form:" + from + "to:" + to + "bin:" + sb.substring(from, to));
					if (end_row){
						game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
						// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "NEW LINE ");
					}
				}
				
				if (partSize == PART_SIZE){
					partSize = 0;
					reading_part++;
				}				
				
				end_row = false;
			}
			end_row = true;
		}
		
		if (partTotal == part){
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, BTControls.ACTION_PRINT);
			// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "END FILE");
			// Log.d("SVB", "FILE END");
			return true;
		}else{			
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END_PACKAGE, BTControls.ACTION_PRINT);
			// MyLogger.addLog(game.getApplicationContext(), "sending.txt", "END PACKAGE");
			// Log.d("SVB", "PART END");			
		}
		return false;
	}
	
	/**
	 * 
	 * @param capturedImage
	 * @param cropStartX
	 * @param cropStartY
	 * @param cropWidth
	 * @param cropHeight
	 * @param game
	 */
	public static void sendImgTogether(Mat capturedImage, int cropStartX, int cropStartY, int cropWidth, int cropHeight, GameTemplateClass game){		
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropStartX, cropStartY, cropWidth, cropHeight);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		//Log.d("SVB", "res: " + sb.toString());
		
		game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
		
		boolean end_row = false;
		
		for (int r=0;r<cropHeight;r++){
			for (int i=0;i<cropWidth/8;i++){
				
				int from = r*cropWidth + i*8;
				int to = from + 8;				
				byte bval = (byte) Integer.parseInt(sb.substring(from, to), 2);
				
				game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval);
				//String binn = sb.substring(from, to);
				//Log.d("SVB", "form:" + from + "to:" + to + "bin:" + binn);
				
				if (end_row){
					game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
				}
				end_row = false;
			}
			end_row = true;
		}
		game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, BTControls.ACTION_PRINT_AND_DISPLAY);		
	}
	
	/**
	 * 
	 * na kolko casti sa bude posielat cierno-biely obrazok 
	 * podla vyrezu a velkosti<br> 
	 * <b>obrazok sa poseila po bytoch - t.j. plati len pre B/W obr</b>
	 * 
	 * @param capturedImage binarny obrazok
	 * @param cutFromX zaciatok vyrezu X
	 * @param cutFromY zaciatok vyrezu Y
	 * @param cropWidthX sirka vyrezu
	 * @param cropHeightY vyska vyrezu
	 * @param PART_SIZE velkost jednej casti
	 * @return pocet casti kolko obshuje vyrez 
	 */
	public static int getCountImageParts(Mat capturedImage, int cutFromX, int cutFromY,  int cropWidthX, int cropHeightY, int PART_SIZE){
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cutFromX, cutFromY, cropWidthX, cropHeightY);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		
		int len = sb.toString().length() / 8;
		int totalParts = len/(PART_SIZE) + (((len % (PART_SIZE))>0)? 1 : 0);
		// Log.d("SVB", "totalParts: " + totalParts);
		
		return totalParts;		
	}
	
}
