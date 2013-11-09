package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GameMoveMotorActivity extends GameTemplateClass{
	
	public static final int ACTION_BUTTON_SHORT = 0;
	public static final int ACTION_BUTTON_LONG = 1;

	private boolean forwardMotorA = false;// open claws
	private boolean backwardMotorA = false;// close claws

	private boolean forwardMotorB = false;// left
	private boolean backwardMotorB = false;// left

	private boolean forwardMotorC = false;// right
	private boolean backwardMotorC = false;// right

	private int speed = 0;

	@Override 
	public void setupLayout() {
		setContentView(R.layout.game_move_motor_layout);
		((ImageView) findViewById(R.id.motor_A_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorA(forwardMotorA, true, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorA(forwardMotorA, false, speed);
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_A_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorA(true, backwardMotorA, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorA(false, backwardMotorA, speed);
						return true;
					}

				});

		((ImageView) findViewById(R.id.motor_B_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorB(forwardMotorB, true, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorB(forwardMotorB, false, speed);
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_B_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorB(true, backwardMotorB, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorB(false, backwardMotorB, speed);
						return true;
					}

				});

		((ImageView) findViewById(R.id.motor_C_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorC(forwardMotorC, true, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorC(forwardMotorC, false, speed);
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_C_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotorC(true, backwardMotorC, speed);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotorC(false, backwardMotorC, speed);
						return true;
					}

				});		

		SeekBar sb = ((SeekBar) findViewById(R.id.seekBarA));
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				speed = progress;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
				Editor mPrefsEditor = prefs.edit();
				mPrefsEditor.putInt("speed", speed);
				mPrefsEditor.commit();
			}
		});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
		speed = prefs.getInt("speed", 20);
		sb.setProgress(speed);
	}	

	public void updateMotorA(boolean forward, boolean backward, int speed) {
		if (isConnected()){
			if (this.forwardMotorA != forward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((forward) ? BTControls.MOTOR_A_FORWARD_START
								: BTControls.MOTOR_A_FORWARD_STOP), speed);
				this.forwardMotorA = forward;
			}
			if (this.backwardMotorA != backward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((backward) ? BTControls.MOTOR_A_BACKWARD_START
								: BTControls.MOTOR_A_BACKWARD_STOP), speed);
				this.backwardMotorA = backward;
			}
		}
	}

	public void updateMotorB(boolean forward, boolean backward, int speed) {
		if (isConnected()){
			if (this.forwardMotorB != forward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((forward) ? BTControls.MOTOR_B_FORWARD_START
								: BTControls.MOTOR_B_FORWARD_STOP), speed);
				this.forwardMotorB = forward;
			}
			if (this.backwardMotorB != backward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((backward) ? BTControls.MOTOR_B_BACKWARD_START
								: BTControls.MOTOR_B_BACKWARD_STOP), speed);
				this.backwardMotorB = backward;
			}
		}
	}

	public void updateMotorC(boolean forward, boolean backward, int speed) {
		if (isConnected()){
			if (this.forwardMotorC != forward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((forward) ? BTControls.MOTOR_C_FORWARD_START
								: BTControls.MOTOR_C_FORWARD_STOP), speed);
				this.forwardMotorC = forward;
			}
			if (this.backwardMotorC != backward) {
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
						((backward) ? BTControls.MOTOR_C_BACKWARD_START
								: BTControls.MOTOR_C_BACKWARD_STOP), speed);
				this.backwardMotorC = backward;
			}
		}
	}

	@Override
	public void onConnectToDevice(){
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_MOVE_MOTOR, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.ROBOT_TYPE,
				robotType, 0);
	}

}