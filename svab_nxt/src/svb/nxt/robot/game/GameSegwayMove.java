package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class GameSegwayMove extends GameTemplateClass{	
	
	// controll arrows 
	// motor B left
	// motor C right
	private boolean forward = false; // motor B + motor C -> forward
	private boolean backward = false; // motor B + motor C -> backward
	private boolean rotateLeft = false; // motor B(backward) + motor C(forward)
	private boolean rotateRight = false; // motor B(forward) + motor C(backward)
	private boolean openClaws = false; // motor C
	private boolean closeClaws = false; // motor C

	private int moveSpeed = SPEED_MEDIUM_MOVE;		
	
	public static final int SPEED_NO_MOVE = 0;
	public static final int SPEED_MEDIUM_MOVE = 11;
	
	@Override
	public void setupLayout() {
		setContentView(R.layout.game_segway_layout);
		((ImageView) findViewById(R.id.button_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorControll(true, backward, rotateLeft,
									rotateRight, moveSpeed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorControll(false, backward, rotateLeft,
									rotateRight, SPEED_NO_MOVE);

						return true;
					}
				});

		((ImageView) findViewById(R.id.button_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorControll(forward, true, rotateLeft,
									rotateRight, moveSpeed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorControll(forward, false, rotateLeft,
									rotateRight, SPEED_NO_MOVE);
						return true;
					}
				});

		((ImageView) findViewById(R.id.button_left))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorControll(forward, backward, true,
									rotateRight, moveSpeed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorControll(forward, backward, false,
									rotateRight, SPEED_NO_MOVE);
						return true;
					}
				});
		((ImageView) findViewById(R.id.button_right))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorControll(forward, backward, rotateLeft,
									true, moveSpeed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorControll(forward, backward, rotateLeft,
									false, SPEED_NO_MOVE);
						return true;
					}
				});				
	}

	

	public void updateMotorControll(boolean forward, boolean backward,
			boolean rotateLeft, boolean rotateRight, int speed) {
		if (isConnected()){
			if (this.forward != forward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((forward) ? BTControls.GO_FORWARD_B_C_START
								: BTControls.GO_FORWARD_B_C_STOP), speed);
				this.forward = forward;
			}
			if (this.backward != backward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((backward) ? BTControls.GO_BACKWARD_B_C_START
								: BTControls.GO_BACKWARD_B_C_STOP), speed);
				this.backward = backward;
			}
			if (this.rotateLeft != rotateLeft) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((rotateLeft) ? BTControls.TURN_LEFT_START
								: BTControls.TURN_LEFT_STOP), speed);
				this.rotateLeft = rotateLeft;
			}
			if (this.rotateRight != rotateRight) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((rotateRight) ? BTControls.TURN_RIGHT_START
								: BTControls.TURN_RIGHT_STOP), speed);
				this.rotateRight = rotateRight;
			}
		}
	}

	public void updateCraws(boolean openClaws, boolean closeClaws, int speed) {
		if (isConnected()){
			if (this.openClaws != openClaws) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((openClaws) ? BTControls.MOTOR_A_FORWARD_START
								: BTControls.MOTOR_A_FORWARD_STOP), speed);
				this.openClaws = openClaws;
			}
			if (this.closeClaws != closeClaws) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((closeClaws) ? BTControls.MOTOR_A_BACKWARD_START
								: BTControls.MOTOR_A_BACKWARD_STOP), speed);
				this.closeClaws = closeClaws;
			}
		}
	}	
			
	@Override
	public void onConnectToDevice() {
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_SEGWAY, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.ROBOT_TYPE,
				robotType, 0);
	}

}
