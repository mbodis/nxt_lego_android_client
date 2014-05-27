package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GameMoveMotorActivity extends GameTemplateClass{
	
	private int speed = 0;
	private int acc = 0;
	
	private TextView speedValue, accValue;

	@Override 
	public void setupLayout() {
		setContentView(R.layout.game_move_motor_layout);
		((ImageView) findViewById(R.id.motor_A_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {						
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotor(BTControls.MOTOR_A_FORWARD_START);							
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_A_FORWARD_STOP);							
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_A_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {						
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotor(BTControls.MOTOR_A_BACKWARD_START);							
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_A_BACKWARD_STOP);
						return true;
					}

				});

		((ImageView) findViewById(R.id.motor_B_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotor(BTControls.MOTOR_B_FORWARD_START);							
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_B_FORWARD_STOP);							
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_B_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)						
							updateMotor(BTControls.MOTOR_B_BACKWARD_START);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_B_BACKWARD_STOP);
						
						return true;
					}

				});

		((ImageView) findViewById(R.id.motor_C_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotor(BTControls.MOTOR_C_FORWARD_START);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_C_FORWARD_STOP);
						return true;
					}
				});

		((ImageView) findViewById(R.id.motor_C_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateMotor(BTControls.MOTOR_C_BACKWARD_START);
							
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateMotor(BTControls.MOTOR_C_BACKWARD_STOP);
						return true;
					}

				});		
		
		speedValue = (TextView) findViewById(R.id.seek_speed_value);
		accValue = (TextView) findViewById(R.id.seek_acc_value);

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
				speedValue.setText(""+(speed*5)+" deg/sec");
			}
		});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
		speed = prefs.getInt("speed", 20);
		sb.setProgress(speed);
		speedValue.setText(""+(speed*5)+" deg/sec");
		
		SeekBar sb2 = ((SeekBar) findViewById(R.id.seekBarB));
		sb2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				acc = progress;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
				Editor mPrefsEditor = prefs.edit();
				mPrefsEditor.putInt("acc", acc);
				mPrefsEditor.commit();
				accValue.setText(""+(acc*100)+" deg/sec/sec");
			}
		});
		SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(thisActivity);
		acc = prefs2.getInt("acc", 60);
		sb2.setProgress(acc);
		accValue.setText(""+(acc*100)+" deg/sec/sec");
		
	}	
	
	public void setSpeed (View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
					BTControls.MOTOR_SET_SPEED, speed);
		}		
	}
	
	public void setAcc(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
					BTControls.MOTOR_SET_ACC, acc);
		}
	}
	
	
	public void updateMotor(int motor_state) {		
		if (isConnected()){			
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					motor_state, 0);
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