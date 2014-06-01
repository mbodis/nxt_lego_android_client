package svb.nxt.robot.logic;

import java.io.IOException;

import svb.nxt.robot.R;
import svb.nxt.robot.activity.DeviceListActivity;
import svb.nxt.robot.activity.MainMenuActivity;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTConnectable;
import svb.nxt.robot.bt.LCPMessage;
import svb.nxt.robot.dialog.ErrorBtDialog;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public abstract class GameTemplateClass extends Activity implements BTConnectable, GameInterface {

	public static final int MENU_TOGGLE_CONNECT = Menu.FIRST;

	protected static final int REQUEST_CONNECT_DEVICE = 1000;
	private static final int REQUEST_ENABLE_BT = 2000;

	private BTCommunicator myBTCommunicator = null;	
	private boolean connected = false;
	private ProgressDialog connectingProgressDialog;
	private Handler btcHandler;
	private Menu myMenu;
	protected Activity thisActivity;
	private boolean btErrorPending = false;
	private boolean pairing;
	protected int robotType;
	
	/**
	 * @return true, when currently pairing
	 */
	@Override
	public boolean isPairing() {
		return pairing;
	}

	/**
	 * Called when the activity is first created. Inititializes all the
	 * graphical views.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisActivity = this;		
		robotType = MainMenuActivity.getRobotType(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		
		setupLayout();
	}

	/**
	 * Updates the menus and possible buttons when connection status changed.
	 */
	protected void updateButtonsAndMenu() {

		if (myMenu == null)
			return;

		myMenu.removeItem(MENU_TOGGLE_CONNECT);

		if (connected) {
			myMenu.add(0, MENU_TOGGLE_CONNECT, 1,
					getResources().getString(R.string.disconnect));

		} else {
			myMenu.add(0, MENU_TOGGLE_CONNECT, 1,
					getResources().getString(R.string.connect));
		}

	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and
	 * fetches the corresponding handler.
	 */
	private void createBTCommunicator() {
		// interestingly BT adapter needs to be obtained by the UI thread - so
		// we pass it in in the constructor
		myBTCommunicator = new BTCommunicator(this, myHandler,
				BluetoothAdapter.getDefaultAdapter(), getResources());
		btcHandler = myBTCommunicator.getHandler();
	}

	/**
	 * Creates and starts the a thread for communication via bluetooth to the
	 * NXT robot.
	 * 
	 * @param mac_address
	 *            The MAC address of the NXT robot.
	 */
	private void startBTCommunicator(String mac_address) {
		connected = false;
		connectingProgressDialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.connecting_please_wait), true);

		if (myBTCommunicator != null) {
			try {
				myBTCommunicator.destroyNXTconnection();
			} catch (IOException e) {
			}
		}
		createBTCommunicator();
		myBTCommunicator.setMACAddress(mac_address);
		myBTCommunicator.start();
		updateButtonsAndMenu();
	}

	/**
	 * Sends a message for disconnecting to the communcation thread.
	 */
	public void destroyBTCommunicator() {

		if (myBTCommunicator != null) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT,
					0, 0);
			myBTCommunicator = null;
		}

		connected = false;
		updateButtonsAndMenu();
	}

	/**
	 * Gets the current connection status.
	 * 
	 * @return the current connection status to the robot.
	 */
	public boolean isConnected() {
		return connected;
	}	

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * 
	 * @param delay
	 *            time to wait before sending the message.
	 * @param message
	 *            the message type (as defined in BTCommucator)
	 * @param value1
	 *            first parameter
	 * @param value2
	 *            second parameter
	 */
	protected void sendBTCmessage(int delay, int message, int value1, int value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putInt("value2", value2);
		Message myMessage = myHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);

		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * 
	 * @param delay
	 *            time to wait before sending the message.
	 * @param message
	 *            the message type (as defined in BTCommucator)
	 * @param String
	 *            a String parameter
	 */
	void sendBTCmessage(int delay, int message, String name) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putString("name", name);
		Message myMessage = myHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);
		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// no bluetooth available
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			Toast.makeText(thisActivity,
					getString(R.string.bt_initialization_failure),
					Toast.LENGTH_SHORT).show();
			destroyBTCommunicator();
			finish();
			return;
		}

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			selectNXT();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyBTCommunicator();
	}

	/**
	 * Creates the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		myMenu = menu;
		myMenu.add(0, MENU_TOGGLE_CONNECT, 1,
				getResources().getString(R.string.connect));
		updateButtonsAndMenu();
		return true;
	}	

	/**
	 * Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_TOGGLE_CONNECT:

			if (myBTCommunicator == null || connected == false) {
				selectNXT();

			} else {
				destroyBTCommunicator();
				updateButtonsAndMenu();
			}

			return true;
		}

		return false;
	}	
	
	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			
			recieveMsgFromNxt(myMessage);
			
			switch (myMessage.getData().getInt("message")) {				
			
				case BTCommunicator.DISPLAY_TOAST:
					Toast.makeText(thisActivity,
							myMessage.getData().getString("toastText"),
							Toast.LENGTH_SHORT).show();
					break;
				case BTCommunicator.STATE_CONNECTED:
					connected = true;
					connectingProgressDialog.dismiss();
					updateButtonsAndMenu();
					sendBTCmessage(BTCommunicator.NO_DELAY,
							BTCommunicator.GET_FIRMWARE_VERSION, 0, 0);
					sendBTCmessage(BTCommunicator.NO_DELAY,
							BTCommunicator.ROBOT_TYPE,
							robotType, 0);
					onConnectToDevice();
					
					break;
				case BTCommunicator.MOTOR_STATE:
	
					if (myBTCommunicator != null) {
						byte[] motorMessage = myBTCommunicator.getReturnMessage();
						int position = byteToInt(motorMessage[21])
								+ (byteToInt(motorMessage[22]) << 8)
								+ (byteToInt(motorMessage[23]) << 16)
								+ (byteToInt(motorMessage[24]) << 24);
						Toast.makeText(thisActivity,
								getString(R.string.current_position),
								Toast.LENGTH_SHORT).show();
					}
	
					break;
	
				case BTCommunicator.STATE_CONNECTERROR_PAIRING:
					connectingProgressDialog.dismiss();
					destroyBTCommunicator();
					break;
	
				case BTCommunicator.STATE_CONNECTERROR:
					connectingProgressDialog.dismiss();
				case BTCommunicator.STATE_RECEIVEERROR:
				case BTCommunicator.STATE_SENDERROR:
	
					destroyBTCommunicator();
					if (isBtErrorPending() == false) {
						setBtErrorPending(true);
						// inform the user of the error with an AlertDialog
						DialogFragment ad = ErrorBtDialog.newInstance();
						ad.show(getFragmentManager(), ErrorBtDialog.TAG);					
					}
	
					break;
	
				case BTCommunicator.FIRMWARE_VERSION:
	
					if (myBTCommunicator != null) {
						byte[] firmwareMessage = myBTCommunicator
								.getReturnMessage();
						// check if we know the firmware
						boolean isLejosMindDroid = true;
						for (int pos = 0; pos < 4; pos++) {
							if (firmwareMessage[pos + 3] != LCPMessage.FIRMWARE_VERSION_LEJOSMINDDROID[pos]) {
								isLejosMindDroid = false;
								break;
							}
						}
						if (isLejosMindDroid) {
							//do something
						}
						// afterwards we search for all files on the robot
						sendBTCmessage(BTCommunicator.NO_DELAY,
								BTCommunicator.FIND_FILES, 0, 0);
					}
	
					break;
	
				case BTCommunicator.VIBRATE_PHONE:
					if (myBTCommunicator != null) {
						byte[] vibrateMessage = myBTCommunicator.getReturnMessage();
						Vibrator myVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						myVibrator.vibrate(vibrateMessage[2] * 10);
					}
	
					break;
				}			
		}
	};

	private int byteToInt(byte byteValue) {
		int intValue = (byteValue & (byte) 0x7f);

		if ((byteValue & (byte) 0x80) != 0)
			intValue |= 0x80;

		return intValue;
	}

	public void selectNXT() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:

			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address and start a new bt communicator
				// thread
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				pairing = data.getExtras().getBoolean(
						DeviceListActivity.PAIRING);
				startBTCommunicator(address);
			}

			break;

		case REQUEST_ENABLE_BT:

			// When the request to enable Bluetooth returns
			switch (resultCode) {
			case Activity.RESULT_OK:
				selectNXT();
				break;
			case Activity.RESULT_CANCELED:
				Toast.makeText(thisActivity,
						getString(R.string.bt_needs_to_be_enabled),
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			default:
				Toast.makeText(thisActivity,
						getString(R.string.problem_at_connecting),
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			}

			break;

		}
	}

	public boolean isBtErrorPending() {
		return btErrorPending;
	}

	public void setBtErrorPending(boolean btErrorPending) {
		this.btErrorPending = btErrorPending;
	}
	
	public BTCommunicator getMyBTCommunicator() {
		return myBTCommunicator;
	}

	public void setMyBTCommunicator(BTCommunicator myBTCommunicator) {
		this.myBTCommunicator = myBTCommunicator;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}
