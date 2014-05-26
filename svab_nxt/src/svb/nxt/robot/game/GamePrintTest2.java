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
import svb.nxt.robot.game.opencv.OpenCVColorView;
import svb.nxt.robot.logic.GameTemplateClass;
import svb.nxt.robot.logic.ImageConvertClass;
import svb.nxt.robot.logic.ImageLog;
import android.graphics.Bitmap;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class GamePrintTest2 extends GameTemplateClass implements
		CvCameraViewListener2, BTConnectable {
	
	/**
	 * IMAGE PROCESSING
	 */
	private static final String TAG = "GamePrintFoto::Activity";
	private OpenCVColorView mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	
	private boolean doCapture = false;	
	private Mat capturedImage = null;
	
	private Button btnCaptureImage, btnSaveFull, btnCanny,
		btnSaveCrop, btnSendCrop;
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_printer_test2_layout);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		btnCaptureImage = (Button) findViewById(R.id.btnCapture);
		btnCaptureImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnCaptureImage.setText((capturedImage == null)? "reset" : "capture");
				if (capturedImage!=null){
					capturedImage = null;
					toggleBtns();
					return;
				}				
				
				doCapture = true;
				toggleBtns();
			}

		});
		btnSaveFull = (Button) findViewById(R.id.btnSaveFull);
		btnSaveFull.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (capturedImage != null){			
					ImageLog.saveImageToFile(GamePrintTest2.this, ImageConvertClass.matToBitmap(capturedImage));
				}								
			}
		});		
		btnCanny = (Button) findViewById(R.id.btnCanny);
		btnCanny.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				if (capturedImage != null){
					Imgproc.Canny(capturedImage, capturedImage, 20, 120);
				}
			}
		});
				
		btnSaveCrop = (Button) findViewById(R.id.btnSaveCrop);
		btnSaveCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				ImageLog.saveImageToFile(GamePrintTest2.this, ImageConvertClass.cropImage(capturedImage, 100, 60));
			}
		});
		
		btnSendCrop = (Button) findViewById(R.id.btnSendCrop);
		btnSendCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//96 * 30 * 8 binarnych cisel - max
				int cX = 12;
				int cropX = 8*cX;
				int cropy = 60;
				
				Bitmap b = ImageConvertClass.cropImage(capturedImage, cropX, cropy);
				StringBuilder sb = ImageConvertClass.getImagetoBinaryStr(b);
				Log.d("SVB", "res: " + sb.toString());
				
				
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_START, 0);
				boolean end_row = false;
				for (int r=0;r<cropy;r++){
					for (int i=0;i<cX;i++){
						int from = r*cropX + i*8;
						int to = from + 8;
						byte bval = (byte) Integer.parseInt(sb.substring(from, to), 2);
						sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_DATA, bval);
						//String binn = sb.substring(from, to);
						//Log.d("SVB", "form:" + from + "to:" + to + "bin:" + binn);
						if (end_row){
							sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_NEW_LINE, 0);
						}
						end_row = false;
					}
					end_row = true;
				}
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, BTControls.FILE_END, 0);
			}
		});
		
		toggleBtns();
	}
	
	private void toggleBtns() {
		int show = (doCapture==false) ? View.GONE : View.VISIBLE; 
		btnSaveFull.setVisibility(show);
		btnCanny.setVisibility(show);
		btnSaveCrop.setVisibility(show);
		btnSendCrop.setVisibility(show);
	}
	

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
	
	public GamePrintTest2() {
		Log.i(TAG, "Instantiated new " + this.getClass());
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
		
		if (capturedImage != null){
			return capturedImage;
		}
		
		Mat src = inputFrame.rgba();
		if (doCapture){
			Log.d("SSS", "doCapture = true");
			doCapture = false;
			capturedImage = src;			
		}
		if (capturedImage != null){
			return capturedImage;
		}
			
		
		return src;
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
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_PRINTER_TEST_2, 0);		
	}

}