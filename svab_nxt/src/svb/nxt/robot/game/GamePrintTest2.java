package svb.nxt.robot.game;

import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTConnectable;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.game.opencv.OpenCVColorView;
import svb.nxt.robot.logic.GameTemplateClass;
import svb.nxt.robot.logic.PenPrinterHelper;
import svb.nxt.robot.logic.img.ImageConvertClass;
import svb.nxt.robot.logic.img.ImageLog;
import android.hardware.Camera.Size;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 1) odfotit
 * 2) prevedenie do C/B pomocou canny operatora - zobrazia sa hrany
 * 3a) - moznost tlacit img 96x60 naraz
 * 3b) - moznost tlacit img 96x60 posielat po castiach
 * 
 * @author svab
 *
 */
public class GamePrintTest2 extends GameTemplateClass implements
		CvCameraViewListener2, BTConnectable {
		
	// image processing 
	private static final String TAG = "GamePrintFoto::Activity";
	private OpenCVColorView mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	
	// printer customize
	private boolean doCapture = false;
	private boolean sendImg = false;
	private boolean img_blackAndWhite = false; 
	private Mat capturedImage = null;
	private Mat printImage = null; // capture img + sqare area
	private int part = 0;
	
	// view
	private Button btnCaptureImage, btnSaveFull, btnCanny,
		btnSaveCrop, btnSendCrop, btnSendFull;
	private ProgressBar progressBar;
	private TextView progressText;
	private LinearLayout ll_progress;
	
	// 96 * 60 -> NXT display  8 binarnych cisel -> poseilam po byte-och	
	int cropX = 8*12; // 
	int cropY = 60;	
	int PART_SIZE = 100; 
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_printer_test2_layout);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		
		
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
		
		btnCaptureImage = (Button) findViewById(R.id.btnCapture);
		btnCaptureImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnCaptureImage.setText((capturedImage == null)? "reset" : "capture");
				if (capturedImage!=null){
					capturedImage = null;
					sendImg = false;
					img_blackAndWhite = false;
					toggleBtns(false);
					return;
				}								
				doCapture = true;
				toggleBtns(true);
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
					img_blackAndWhite = true;
					toggleBtns(true);
					Imgproc.Canny(capturedImage, capturedImage, 20, 120);
				}
			}
		});
				
		btnSaveCrop = (Button) findViewById(R.id.btnSaveCrop);
		btnSaveCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				ImageLog.saveImageToFile(GamePrintTest2.this, ImageConvertClass.cropImage(capturedImage, 0, 0, 96, 60));
			}
		});
		
		btnSendCrop = (Button) findViewById(R.id.btnSendCrop);
		btnSendCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendImgPart();
			}
		});
		btnSendFull = (Button) findViewById(R.id.btnSendFull);
		btnSendFull.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				if (isConnected()){
					PenPrinterHelper.sendImgTogether(capturedImage, 0, 0, cropX, cropY, GamePrintTest2.this);
				}else{
					Toast.makeText(GamePrintTest2.this, "not connected", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setProgress(0);
		
		progressText = (TextView) findViewById(android.R.id.text1);
		ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
		
		toggleBtns(false);
	}
	
	private void sendImgPart(){
		if (isConnected()){
			sendImg = true;
			toggleBtns(true);
			
			int partsTotal = PenPrinterHelper.getCountImageParts(capturedImage, 0, 0, cropX, cropY, PART_SIZE); 
			
			if (partsTotal > part){
				part++;
				Toast.makeText(this, "SENDING part: " + part, Toast.LENGTH_SHORT).show();			
				//sendImgPart(part, partsTotal);
				PenPrinterHelper.sendImgPart(capturedImage, 0, 0, cropX, cropY, part, partsTotal, PART_SIZE, this);
				updateProgress(part, partsTotal);
				
			}else{
				Toast.makeText(this, "partREQUEST ERR", Toast.LENGTH_SHORT).show();
			}	
		}else{
			Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
		}
	}				
	
	private void updateProgress(int part, int totalParts){		
		progressText.setText("part: " + part + " / " + totalParts);
		progressBar.setMax(totalParts);
		progressBar.setProgress(part);
	}	
	
	private void toggleBtns(boolean imageCaptured) {
		int show = (imageCaptured) ? View.VISIBLE : View.GONE; 
		btnSaveFull.setVisibility(show);
		btnCanny.setVisibility(show);
		btnSaveCrop.setVisibility(show);
		
		btnSendCrop.setVisibility((img_blackAndWhite==false) ? View.GONE : View.VISIBLE);
		btnSendFull.setVisibility((img_blackAndWhite==false) ? View.GONE : View.VISIBLE);
		 
		ll_progress.setVisibility((sendImg) ? View.VISIBLE: View.GONE ); 
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
			doCapture = false;
			capturedImage = src;
			printImage = src;
		}else{
			Core.rectangle(src, 
					new Point(0, 0), new Point(cropX, cropY), 
					new Scalar(255, 255, 255), 1, 1, 0);
		}
		if (capturedImage != null){		
			Core.rectangle(printImage, 
					new Point(0, 0), new Point(cropX, cropY), 
					new Scalar(255, 255, 255), 1, 1, 0);
			return printImage;
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

	@Override
	public void recieveMsgFromNxt(Message myMessage) {
		int type = myMessage.getData().getInt("message");
		//Toast.makeText(this, "msg: " + type , Toast.LENGTH_SHORT).show();	
		
		switch(type){
			case BTControls.FILE_NEW_PACKAGE_REQUEST:				
				sendImgPart();
				break;
			
			default:
				//Toast.makeText(this, "msg: " + type , Toast.LENGTH_SHORT).show();
				break;
		}
		
	}
}