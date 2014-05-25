package svb.nxt.robot.logic;

public interface GameInterface {

	/**
	 * define new game layout for activity
	 */ 
	public abstract void setupLayout();
	
	/**
	 * on Connect to device send some init you need<br/><br/>
	 * <b>example:</b><br/>
	 * sendBTCmessage(BTCommunicator.NO_DELAY,<br/>
	 * BTCommunicator.GAME_TYPE, BTControls.PROGRAM_MOVE_DIRECTION, 0);
	 */
	public abstract void onConnectToDevice();

}
