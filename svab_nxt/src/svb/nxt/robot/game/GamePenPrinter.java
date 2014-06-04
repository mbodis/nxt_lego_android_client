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
import android.hardware.Camera.Size;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ciernobiele tlacenie z aktualnej fotografie
 * 1) odfotenie
 * 2) nasvavenie velkosti obrazku + dotyk premiestnenie
 * 3a) hranovy operator -> cierno biela - zvyraznene hrany
 * 3b) fotka sa premietne do sedej a pouzije sa eqHistogramu
 * 4b) nasavenie hranice pre prechod do cierno bielej
 * 5) posielanie po castiach do NXT pre tlac
 * 
 * @author svab
 *
 */
public class GamePenPrinter extends GameTemplateClass implements
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
	private boolean BWcanny = false; 
	private boolean BWtrashold = false;
	
	private Mat capturedImage = null;
	private Mat printImage = null; // capture img + sqare area
	
	private int part = 0; // current sending part 
	private int thrashold = 50;
	
	// view
	private Button btnCaptureImage, btnCanny, btnThreshold,
		btnSendCrop;
	private SeekBar bw_threshold;
	private ProgressBar progressBar;
	private TextView progressText;
	private LinearLayout ll_progress;
	private EditText editX, editY;
	
	// 96 * 60 -> NXT display  8 binarnych cisel -> poseilam po byte-och	
	int cropX = 8*12; // default size
	int cropY = 60;	// default size
	int PART_SIZE = 100; 
	int cutSX = 0;
	int cutSY = 0;
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_pen_printer);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					cutSX = (int)event.getX() - 60 - cropX/2;// soft border camera
					cutSY = (int)event.getY() - cropY/2;					
					updateImgArea();
				}
					
				return false;
			}
		});
				
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		ll_progress = (LinearLayout) findViewById(R.id.ll_progress);		
		progressText = (TextView) findViewById(android.R.id.text1);		
		progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setProgress(0);
				
		btnCaptureImage = (Button) findViewById(R.id.btnCapture);
		btnCaptureImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnCaptureImage.setText((capturedImage == null)? "reset" : "capture");
				if (capturedImage!=null){
					capturedImage = null;
					sendImg = false;
					BWcanny = false;
					BWtrashold = false;
					toggleViews(false);
					return;
				}								
				doCapture = true;
				toggleViews(true);
			}

		});					
		btnCanny = (Button) findViewById(R.id.btnCanny);
		btnCanny.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				if (capturedImage != null){
					BWcanny = true;
					toggleViews(true);
					Imgproc.Canny(capturedImage, capturedImage, 20, 120);
					updateImgArea();
				}
			}
		});
		
		btnThreshold = (Button) findViewById(R.id.btnThreshold);
		btnThreshold.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BWtrashold = true;
				toggleViews(true);				
				Imgproc.cvtColor(capturedImage, capturedImage, Imgproc.COLOR_RGB2GRAY);				
				Imgproc.equalizeHist(capturedImage, capturedImage);				
				updateImgArea();
			}
		});
		
		bw_threshold = (SeekBar) findViewById(R.id.bw_threshold);
		bw_threshold.setProgress(thrashold);
		bw_threshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {										
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Log.d("SVB", "progress: " + progress);
				thrashold = progress;
				updateImgArea();
			}
		});
				
		TextWatcher tv = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateImgArea();				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {				
			}
		};
		editX = (EditText) findViewById(R.id.cropX);
		editX.addTextChangedListener(tv);
		editY = (EditText) findViewById(R.id.cropY);
		editY.addTextChangedListener(tv);
		
		btnSendCrop = (Button) findViewById(R.id.btnSendCrop);
		btnSendCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cropX % 8 != 0){
					Toast.makeText(thisActivity, "X mod 8  != 0  MOD="+ (cropX%8), Toast.LENGTH_SHORT).show();
				}else{
					sendImgPart();
				}
			}
		});			
		
		toggleViews(false);
	}
	
	public void minusX(View view){
		if (cropX>0){
			cropX --;
		}
		editX.setText(cropX+"");
		updateImgArea();
	}
	public void plusX(View view){
		cropX ++;
		editX.setText(cropX+"");
	}
	
	public void minusY(View view){
		if (cropY>0){
			cropY --;
		}
		editY.setText(cropY+"");
		updateImgArea();
	}			
	public void plusY(View view){
		cropY ++;		
		editY.setText(cropY+"");
		updateImgArea();
	}
	
	private void updateImgArea(){
		if (editX.getText().length() == 0){
			cropX = 0;
		}else{
			cropX = Integer.parseInt(editX.getText().toString().trim());
		}
		
		if (editY.getText().length() == 0){
			cropY = 0;
		}else{
			cropY = Integer.parseInt(editY.getText().toString().trim());
		}
		
		if (capturedImage != null){
			printImage = capturedImage.clone();
			if (BWtrashold){
				Imgproc.threshold(printImage, printImage, thrashold, 255, Imgproc.THRESH_BINARY);
			}
		}
	}
	
	private void sendImgPart(){
		if (isConnected()){
			sendImg = true;
			toggleViews(true);
			
			int partsTotal = PenPrinterHelper.getCountImageParts(capturedImage, cropX, cropY, PART_SIZE); 
			
			if (partsTotal > part){
				part++;
				Toast.makeText(this, "SENDING part: " + part, Toast.LENGTH_SHORT).show();			
				//sendImgPart(part, partsTotal);
				PenPrinterHelper.sendImgViaPart(capturedImage, cutSX, cutSY, cutSX+cropX, cutSY+cropY, part, partsTotal, PART_SIZE, this);
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
	
	private void toggleViews(boolean imageCaptured) {
		
		int show = (imageCaptured) ? View.VISIBLE : View.GONE; 
		
		btnCanny.setVisibility(show);
		
		btnThreshold.setVisibility(show);
		ll_progress.setVisibility((sendImg) ? View.VISIBLE: View.GONE);
		bw_threshold.setVisibility(BWtrashold ? View.VISIBLE: View.GONE);		
							
		btnSendCrop.setVisibility((!BWcanny && !BWtrashold) ? View.GONE : View.VISIBLE);
		
		btnCanny.setEnabled(!(BWcanny || BWtrashold));
		btnThreshold.setEnabled(!(BWcanny || BWtrashold));		
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
			addSelectArea(printImage);
			return printImage;
		}
		
		Mat src = inputFrame.rgba();
		
		if (doCapture){
			doCapture = false;
			capturedImage = src;	
			printImage = capturedImage.clone();
		}else{			
			addSelectArea(src);
		}
		
		
		return src;
	}

	private void addSelectArea(Mat img){
		Core.rectangle(img, 
				new Point(cutSX-1, cutSY-1), new Point(cutSX+cropX+1, cutSY+cropY+1), 
				new Scalar(255, 255, 255), 1, 1, 0);
		Core.rectangle(img, 
				new Point(cutSX, cutSY), new Point(cutSX+cropX, cutSY+cropY), 
				new Scalar(0, 0, 0), 1, 1, 0);
		Core.rectangle(img, 
				new Point(cutSX+1, cutSY+1), new Point(cutSX+cropX-1, cutSY+cropY-1), 
				new Scalar(255, 255, 255), 1, 1, 0);
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
