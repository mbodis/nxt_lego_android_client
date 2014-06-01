package svb.nxt.robot.game;

import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTConnectable;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.game.opencv.ColorDetection;
import svb.nxt.robot.game.opencv.OpenCVColorView;
import svb.nxt.robot.logic.ArrowClass;
import svb.nxt.robot.logic.GameTemplateClass;
import android.hardware.Camera.Size;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class GameMoveColorActivity extends GameTemplateClass implements
		CvCameraViewListener2, BTConnectable {
	
	/**
	 * IMAGE PROCESSING
	 */
	private static final String TAG = "OCVSample::Activity";
	private OpenCVColorView mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	private ImageView imgLeft, imgUp, imgRight;
	
	
	/**
	 * NXT ROBOT
	 */
	int i = 0;
	int red = 0, yellow = 0, blue = 0;
	// controll arrows
	private boolean forward = false;
	private boolean backward = false;
	private boolean rotateLeft = false;
	private boolean rotateRight = false;	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};
	
	public GameMoveColorActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_move_open_cv_color_layout);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		imgLeft = (ImageView) findViewById(R.id.imageLeft);
		imgUp = (ImageView) findViewById(R.id.imageUp);
		imgRight = (ImageView) findViewById(R.id.imageRigth);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyBTCommunicator();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	}

	
	@Override
	public void onCameraViewStopped() {
	}

	/**
	 * pri sputenej kamera opakovane volame nasledove:
	 *  staticke hodnoty pre farby a sputime detekciu - > detectMultiBlob 
	 * 
	 */
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat src = null;

		src = inputFrame.rgba();
		Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB);
		Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2HSV);

		// all colors (Red Yellow Blue)
		Mat matRed = new Mat();
		ColorDetection.getColorMat(src, matRed, ColorDetection.COLOR_RED_HSV_FORMAT);

		Mat matYellow = new Mat();
		ColorDetection.getColorMat(src, matYellow, ColorDetection.COLOR_YELLOW_HSV_FORMAT);

		Mat matBlue = new Mat();
		ColorDetection.getColorMat(src, matBlue, ColorDetection.COLOR_BLUE_HSV_FORMAT);

		Mat mResult = new Mat();
		ArrowClass arrows = new ArrowClass(false, false, false);
		ColorDetection.detectMultipleBlob(src, matRed, matYellow, matBlue,
				mResult, arrows);

		setArrows(arrows.isRed(), arrows.isYellow(), arrows.isBlue());
		if (arrows.isRed())
			red++;
		if (arrows.isYellow())
			yellow++;
		if (arrows.isBlue())
			blue++;

		if (isConnected() && (i == 6)) {
			updateMotorControll3Ways((red > 3) ? true : false,
					(yellow > 3) ? true : false, (blue > 3) ? true : false);
		}
		if (i == 6) {
			i = 0;
			red = 0;
			yellow = 0;
			blue = 0;
		}
		i++;
		Imgproc.cvtColor(mResult, src, Imgproc.COLOR_HSV2RGB);

		return src;
	}

	/**
	 * vizualizacia sipiek nastavencyh pre jednotive farby
	 * @param red
	 * @param yellow
	 * @param blue
	 */
	private void setArrows(final boolean red, final boolean yellow,
			final boolean blue) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (red)
					imgUp.setVisibility(View.VISIBLE);
				else
					imgUp.setVisibility(View.INVISIBLE);

				if (yellow)
					imgLeft.setVisibility(View.VISIBLE);
				else
					imgLeft.setVisibility(View.INVISIBLE);

				if (blue)
					imgRight.setVisibility(View.VISIBLE);
				else
					imgRight.setVisibility(View.INVISIBLE);
			}
		});
	}

	
	/**
	 * update robot motros
	 */
	private void updateMotorControll3Ways(boolean goUp, boolean goLeft,
			boolean goRight) {
		updateMotorControll(goUp, backward, rotateLeft, rotateRight, 15);
		updateMotorControll(forward, backward, goLeft, rotateRight, 15);
		updateMotorControll(forward, backward, rotateLeft, goRight, 15);
	}

	/**
	 *  update robota podla najdenych farieb 
	 * 
	 */
	public void updateMotorControll(boolean forward, boolean backward,
			boolean rotateLeft, boolean rotateRight, int speed) {
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

	/**
	 * menu ponuka okrem pripojenia na robota aj zmenu rozlisenia displeja a zmenu farebneho nasatenia 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		List<String> effects = mOpenCvCameraView.getEffectList();

		if (effects == null) {
			Log.e(TAG, "Color effects are not supported by device!");
			return true;
		}

		mColorEffectsMenu = menu.addSubMenu(getString(R.string.colors_color_effect));
		mEffectMenuItems = new MenuItem[effects.size()];

		int idx = 0;
		ListIterator<String> effectItr = effects.listIterator();
		while (effectItr.hasNext()) {
			String element = effectItr.next();
			mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE,
					element);
			idx++;
		}
 
		mResolutionMenu = menu.addSubMenu(getString(R.string.colors_resolution));
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Size> resolutionItr = mResolutionList.listIterator();
		idx = 0;
		while (resolutionItr.hasNext()) {
			Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x"
							+ Integer.valueOf(element.height).toString());
			idx++;
		}
		menu.add(3, 3, 3, getString(R.string.connect));

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item.getGroupId() == 1) {
			mOpenCvCameraView.setEffect((String) item.getTitle());
			Toast.makeText(this, mOpenCvCameraView.getEffect(),
					Toast.LENGTH_SHORT).show();
		} else if (item.getGroupId() == 2) {
			int id = item.getItemId();
			Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			String caption = Integer.valueOf(resolution.width).toString() + "x"
					+ Integer.valueOf(resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
		} else if (item.getGroupId() == 3){
			if (getMyBTCommunicator() == null || isConnected() == false) {
				selectNXT();
			} else {
				destroyBTCommunicator();
				updateButtonsAndMenu();
			}			
		}

		return true;
	}		
	
	@Override
	public void onConnectToDevice() {
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_MOVE_OPEN_CV_COLOR, 0);		
	}

	@Override
	public void recieveMsgFromNxt(Message myMessage) {
		// TODO Auto-generated method stub
		
	}
}