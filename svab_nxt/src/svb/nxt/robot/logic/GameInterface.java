package svb.nxt.robot.logic;

public interface GameInterface {

	/**
	 * TODO DEFINE NEW IN GAME ACTIVITY
	 */ 
	public abstract void setupLayout();
	
	/**
	 * TODO on Connect to device send some init you need
	 */
	public abstract void onConnectToDevice();
//	{
//		//example
//		sendBTCmessage(BTCommunicator.NO_DELAY,
//		BTCommunicator.GAME_TYPE, BTControls.PROGRAM_MOVE_DIRECTION, 0);	
//	}
}
