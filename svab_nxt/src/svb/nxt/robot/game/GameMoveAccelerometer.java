package svb.nxt.robot.game;

import java.util.List;

import svb.nxt.robot.R;
import svb.nxt.robot.activity.DeviceListActivity;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import svb.nxt.robot.view.AccelerometerView;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameMoveAccelerometer extends GameTemplateClass{

	public static final int UPDATE_TIME = 200;

	public static final int ACTION_BUTTON_SHORT = 0;
	public static final int ACTION_BUTTON_LONG = 1;

	// sensor
	private SensorManager mSensorManager;
	private boolean isAccelerometerRegistered = false;
	private Thread runThred;
	private boolean run = false;

	// accelerometer moving
	private int forwardBackward = 0;
	private int leftRight = 0;
	private boolean forward = false;
	private boolean backward = false;
	private boolean left = false;
	private boolean right = false;
	private boolean openClaws = false; // motor C
	private boolean closeClaws = false; // motor C
	private int moveClaws = 8;

	public static final int SPEED_NO_MOVE = 0;

	// view
	private TextView t1, t2, t3;
	private boolean power = false;	
	private AccelerometerView mAccelerometerView;

	public void power(View view) {
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
					BTControls.POWER, 0);
			power = !power;
			if (power) {				
				((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
			} else {
				((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_media_stop));
			}
		}
	}

	@Override
	public void setupLayout() {
		setContentView(R.layout.game_move_accelerometer_layout);
				
		t1 = (TextView) findViewById(R.id.textView1);
		t2 = (TextView) findViewById(R.id.textView2);
		t3 = (TextView) findViewById(R.id.textView3);
		mAccelerometerView = (AccelerometerView) findViewById(R.id.sv);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerView.startDrawImage();
		
		((ImageView) findViewById(R.id.button_claw_up))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateCraws(openClaws, true, moveClaws);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateCraws(openClaws, false, SPEED_NO_MOVE);
						return true;
					}
				});

		((ImageView) findViewById(R.id.button_claw_down))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							updateCraws(true, closeClaws, moveClaws);
						if (event.getAction() == MotionEvent.ACTION_UP)
							updateCraws(false, closeClaws, SPEED_NO_MOVE);
						return true;
					}
				});
	}

	public void updateCraws(boolean openClaws, boolean closeClaws, int speed) {
		if (isConnected()) {
			if (this.openClaws != openClaws) {
				sendBTCmessage(BTCommunicator.NO_DELAY,
						BTCommunicator.DO_ACTION,
						((openClaws) ? BTControls.MOTOR_A_FORWARD_START
								: BTControls.MOTOR_A_FORWARD_STOP), speed);
				this.openClaws = openClaws;
			}
			if (this.closeClaws != closeClaws) {
				sendBTCmessage(BTCommunicator.NO_DELAY,
						BTCommunicator.DO_ACTION,
						((closeClaws) ? BTControls.MOTOR_A_BACKWARD_START
								: BTControls.MOTOR_A_BACKWARD_STOP), speed);
				this.closeClaws = closeClaws;
			}
		}
	}


	@Override
	public void onResume() {		
		super.onResume();
		try {
			registerSensor();
		} catch (IndexOutOfBoundsException ex) {
			Toast.makeText(thisActivity,
					getString(R.string.sensor_initialization_failure),
					Toast.LENGTH_SHORT).show();
			destroyBTCommunicator();
			finish();
		}
	}

	@Override
	public void onPause() {		
		unregisterSensor();
		destroyBTCommunicator();
		super.onStop();
	}

	@Override
	public void onConnectToDevice(){
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE,
				BTControls.PROGRAM_MOVE_ACCELEROMETER, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.ROBOT_TYPE, robotType, 0);
		createRunThred();
	}	

	
	@Override
	public void selectNXT() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	private final SensorEventListener mSensorAccelerometerEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// mAccelX = 0 - event.values[2];
			// mAccelY = 0 - event.values[1];
			// mAccelZ = event.values[0];

			if (event == null)
				return;
			if (t1 != null) {

				int forwBack = (int) (event.values[1]);
				if (forwBack > 40)
					forwBack = 40;
				if (forwBack < -40)
					forwBack = -40;
				t2.setText(getString(R.string.accelerometer_up_down) + " : " + forwBack);
				// forward(+40) // //backward(-40)
				forwardBackward = forwBack;

				int lefRig = (int) (event.values[2]);
				if (lefRig > 40)
					lefRig = 40;
				if (lefRig < -40)
					lefRig = -40;
				t3.setText(getString(R.string.accelerometer_left_right) + " : " + lefRig);
				// left(+40) //right(-40)
				leftRight = lefRig;
				
				mAccelerometerView.setCoords(-lefRig, -forwBack);
			}
		}

	};

	private void registerSensor() {
		try {
			// showAllSensors();
			List<Sensor> sensorList = mSensorManager
					.getSensorList(Sensor.TYPE_ORIENTATION);
			mSensorManager.registerListener(mSensorAccelerometerEventListener,
					sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
			isAccelerometerRegistered = true;
		} catch (Exception e) {
			isAccelerometerRegistered = false;
		}
	}	

	private void unregisterSensor() {
		if (!isAccelerometerRegistered)
			mSensorManager
					.unregisterListener(mSensorAccelerometerEventListener);
		if (run)
			cancelRunThred();

	}

	private void createRunThred() {

		run = true;
		runThred = new Thread(new Runnable() {

			@Override
			public void run() {
				while (run) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}

					// FORWARD
					if (forwardBackward > 0) {
						forward = true;
						sendBTCmessage(BTCommunicator.NO_DELAY,
								BTCommunicator.DO_ACTION,
								BTControls.GO_FORWARD_B_C_START,
								forwardBackward);
					} else {
						if (forward) {
							sendBTCmessage(BTCommunicator.NO_DELAY,
									BTCommunicator.DO_ACTION,
									BTControls.GO_FORWARD_B_C_STOP,
									forwardBackward);
							forward = false;
						}
					}

					// BACKWARD
					if (forwardBackward < 0) {
						backward = true;
						sendBTCmessage(BTCommunicator.NO_DELAY,
								BTCommunicator.DO_ACTION,
								BTControls.GO_BACKWARD_B_C_START,
								forwardBackward * -1);
					} else {
						if (backward) {
							sendBTCmessage(BTCommunicator.NO_DELAY,
									BTCommunicator.DO_ACTION,
									BTControls.GO_BACKWARD_B_C_STOP,
									forwardBackward * -1);
							backward = false;
						}
					}

					// LEFT
					if (leftRight > 0) {
						left = true;
						sendBTCmessage(BTCommunicator.NO_DELAY,
								BTCommunicator.DO_ACTION,
								BTControls.TURN_LEFT_START, leftRight);
					} else {
						if (left) {
							sendBTCmessage(BTCommunicator.NO_DELAY,
									BTCommunicator.DO_ACTION,
									BTControls.TURN_LEFT_STOP, leftRight);
							left = false;
						}
					}

					// RIGHT
					if (leftRight < 0) {
						right = true;
						sendBTCmessage(BTCommunicator.NO_DELAY,
								BTCommunicator.DO_ACTION,
								BTControls.TURN_RIGHT_START, leftRight * -1);
					} else {
						if (right) {
							sendBTCmessage(BTCommunicator.NO_DELAY,
									BTCommunicator.DO_ACTION,
									BTControls.TURN_RIGHT_STOP, leftRight * -1);
							right = false;
						}
					}
				}

			}

		});
		runThred.start();

	}

	private void cancelRunThred() {
		run = false;
//		runThred.stop();
		runThred = null;
	}
	
	@Override
	public void recieveMsgFromNxt(Message myMessage) {
		// TODO Auto-generated method stub
		
	}
}