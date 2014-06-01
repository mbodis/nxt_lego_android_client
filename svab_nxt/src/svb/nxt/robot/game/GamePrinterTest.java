package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

public class GamePrinterTest extends GameTemplateClass{

	@Override
	public void setupLayout() {
		setContentView(R.layout.game_printer_test_layout);				
	}

	
	public void penHeadDown(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.PEN_DOWN, 0);
		}
	}
	public void penHeadUp(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.PEN_UP, 0);
		}
	}
	public void penHeadDownUp(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.PEN_DISTANCE_CHECK, 0);
		}
	}
	public void penHeadDisplay(View view){
		sendTestContent(false);
	}
	public void penHeadPrint(View view){
		sendTestContent(true);
	}
	
	
	
	//NXT 100x60 pixels
	private void sendTestContent(boolean doPrint){
		if (isConnected()){
			Toast.makeText(thisActivity, "testing ... ", Toast.LENGTH_SHORT).show();
			
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, BTControls.ACTION_PACKAGE_NEW_CONTENT);
			
			/*
			drawLine("1111111111111110");
			drawLine("1100000000000110");
			drawLine("1100110001100110");
			drawLine("1100110001100110");
			drawLine("1100000000000110");
			drawLine("1100111111100110");
			drawLine("1100000000000110");
			drawLine("1111111111111110");
			*/			
			
			// drawLine("1111111111111110");			
			byte bval1 = (byte) Integer.parseInt("11111111", 2);
			byte bval2 = (byte) Integer.parseInt("11111110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval1);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			// drawLine("1100000000000110");			
			byte bval3 = (byte) Integer.parseInt("11000000", 2);
			byte bval4 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval3);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval4);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			// drawLine("1100110001100110");			
			byte bval5 = (byte) Integer.parseInt("11001100", 2);
			byte bval6 = (byte) Integer.parseInt("01100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval5);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval6);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			// drawLine("1100110001100110");			
			byte bval7 = (byte) Integer.parseInt("11001100", 2);
			byte bval8 = (byte) Integer.parseInt("01100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval7);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval8);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			// drawLine("1100000000000110");			
			byte bval9 = (byte) Integer.parseInt("11000000", 2);
			byte bval10 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval9);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval10);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);				
			
			// drawLine("1100111111100110");			
			byte bval11 = (byte) Integer.parseInt("11001111", 2);
			byte bval12 = (byte) Integer.parseInt("11100110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval11);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval12);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			// drawLine("1100000000000110");			
			byte bval13 = (byte) Integer.parseInt("11000000", 2);
			byte bval14 = (byte) Integer.parseInt("00000110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval13);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval14);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			// drawLine("1111111111111110");
			byte bval15 = (byte) Integer.parseInt("11111111", 2);
			byte bval16 = (byte) Integer.parseInt("11111110", 2);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval15);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval16);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
			
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, doPrint? BTControls.ACTION_PRINT_AND_DISPLAY : BTControls.ACTION_DISPLAY);
							
		}else{
			Toast.makeText(thisActivity, "not connected ", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void recieveMsgFromNxt(Message myMessage) {
			
		int type = myMessage.getData().getInt("message");
		
		switch(type){
			case BTControls.FILE_NEW_PACKAGE_REQUEST:
				Toast.makeText(this, "MSG - REQUEST next part", Toast.LENGTH_SHORT).show();
				break;
			
			default:
				//Toast.makeText(this, "msg: " + type , Toast.LENGTH_SHORT).show();
				break;
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