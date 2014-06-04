package svb.nxt.robot.logic;

import org.opencv.core.Mat;

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
public class PenPrinterHelper {
	
	/**
	 * posielanie suboru po castiach
	 * @param capturedImage povodny obrazok
	 * @param cropStartX zacaitok vyrezu X
	 * @param cropStartY zaciatok vyrezu Y
	 * @param cropX sirka vyrezu od bodu [cropStartX, cropStartY]
	 * @param cropY vyska vyrezu od bodu [cropStartX, cropStartY]
	 * @param part ktora cast sa posiela
	 * @param partTotal pocet casti spolu
	 * @param game referencia na triedu hry pre zaielanie dat cez BT
	 */
	public static void sendImgViaPart(Mat capturedImage, int cropStartX, int cropStartY, 
			int cropX, int cropY, int part, int partTotal, 
			int PART_SIZE, GameTemplateClass game){	
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropX, cropY);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		//Log.d("SVB", "res: " + sb.toString());		
		
		if (part == 1){
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
		}else{
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START_PACKAGE, BTControls.ACTION_PACKAGE_NEW_CONTENT);
		}		
		
		int reading_part = 1;
		int partSize = 0; 
		
		boolean end_row = false;
		for (int r=0;r<cropY;r++){
			for (int i=0;i<cropX/8;i++){
				int from = r*cropX + i*8;
				int to = from + 8;
				byte bval = (byte) Integer.parseInt(sb.substring(from, to), 2);
				
				partSize++;							
				
				if (reading_part == part){
					// MyLogger.addLog(this, "sending.txt", "part: " + part + " tot:" + partTotal + " reading:" + reading_part + " partSize:" + partSize);					
					game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval);
										
					// Log.d("SVB", "form:" + from + "to:" + to + "bin:" + sb.substring(from, to));
					if (end_row){
						game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
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
			// Log.d("SVB", "FILE END");
		}else{			
			game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END_PACKAGE, BTControls.ACTION_PRINT);			
			// Log.d("SVB", "PART END");
		}
	}
	
	/**
	 * 
	 * @param capturedImage
	 * @param cropX
	 * @param cropY
	 * @param game
	 */
	public static void sendFullImg(Mat capturedImage, int cropX, int cropY, GameTemplateClass game){		
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropX, cropY);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		//Log.d("SVB", "res: " + sb.toString());		
		
		game.sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
		
		boolean end_row = false;
		
		for (int r=0;r<cropY;r++){
			for (int i=0;i<cropX/8;i++){
				
				int from = r*cropX + i*8;
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
	 * @param cropX sirka vyrezu
	 * @param cropY vyska vyrezu
	 * @param PART_SIZE velkost jednej casti
	 * @return pocet casti kolko obshuje vyrez 
	 */
	public static int getCountImageParts(Mat capturedImage, int cropX, int cropY, int PART_SIZE){
		
		Bitmap b = ImageConvertClass.cropImage(capturedImage, cropX, cropY);
		StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
		int len = sb.toString().length() / 8;
		int totalParts = len/(PART_SIZE) + (((len % (PART_SIZE))>0)? 1 : 0);
		// Log.d("SVB", "totalParts: " + totalParts);
		
		return totalParts;		
	}
	
}
