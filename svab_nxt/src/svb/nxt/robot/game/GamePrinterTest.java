package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.view.View;
import android.widget.Toast;

public class GamePrinterTest extends GameTemplateClass{

	@Override
	public void setupLayout() {
		setContentView(R.layout.game_printer_test_layout);				
	}

	public void doTest(View view){		
		
		if (isConnected()){
			Toast.makeText(thisActivity, "testing ... ", Toast.LENGTH_SHORT).show();
			
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, 0);
			
			byte bval1 = (byte) Integer.parseInt("11111111", 2);
			byte bval2 = (byte) Integer.parseInt("11111110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval1);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			byte bval3 = (byte) Integer.parseInt("11000000", 2);
			byte bval4 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval3);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval4);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			byte bval5 = (byte) Integer.parseInt("11001100", 2);
			byte bval6 = (byte) Integer.parseInt("01100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval5);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval6);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			byte bval7 = (byte) Integer.parseInt("11001100", 2);
			byte bval8 = (byte) Integer.parseInt("01100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval7);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval8);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			byte bval9 = (byte) Integer.parseInt("11000000", 2);
			byte bval10 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval9);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval10);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			byte bval11 = (byte) Integer.parseInt("11001111", 2);
			byte bval12 = (byte) Integer.parseInt("11100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval11);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval12);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			byte bval13 = (byte) Integer.parseInt("11000000", 2);
			byte bval14 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval13);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval14);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			byte bval15 = (byte) Integer.parseInt("11111111", 2);
			byte bval16 = (byte) Integer.parseInt("11111110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval15);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval16);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, 0);
							
	//		byte bval = (byte) Integer.parseInt("01111111", 2);
	//	    Toast.makeText(getApplicationContext(), "m " + bval + " " + Integer.toBinaryString(bval), Toast.LENGTH_LONG).show();			    
	//	    byte bval2 = (byte) Integer.parseInt("10000000", 2);				
	//	    Toast.makeText(getApplicationContext(), "m " + bval2, Toast.LENGTH_LONG).show();
	//	    byte bval3  = (byte) Integer.parseInt("00000001", 2);				
	//	    Toast.makeText(getApplicationContext(), "m " + bval3, Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(thisActivity, "not connected ", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	@Override
	public void onConnectToDevice() {
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_PRINTER_TEST, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.ROBOT_TYPE,
				robotType, 0);
	}

}