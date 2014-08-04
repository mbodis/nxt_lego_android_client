package svb.nxt.robot.game;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

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
import svb.nxt.robot.dialog.HelpDialog;
import svb.nxt.robot.game.opencv.OpenCVColorView;
import svb.nxt.robot.logic.DrillPrinterHelper;
import svb.nxt.robot.logic.GameTemplateClass;
import svb.nxt.robot.logic.img.ImageConvertClass;
import svb.nxt.robot.logic.img.ImageLog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author mbodis
 *
 * drill printer
 * 1) select image from camera or choose form file
 * 2) image filters(equalize histogram, invert image)
 * 3) customize printer setting (drill head position, speed, movement rotation)
 * 4) do the print - sending image by parts (because of small memory in NXT)   
 */
public class GameDrillPrinter extends GameTemplateClass implements
		CvCameraViewListener2, BTConnectable {
	
	private static final int SELECT_PHOTO = 100;
	
	public static final int IMAGE_LOAD_FORM_FILE_SIZE = 900;
	
	/**
	 * CAMERA VIEW PREFS
	 */ 
	private static final String TAG = "GamePrintFoto::Activity";
	private OpenCVColorView mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	
	/**
	 * PRINTER STATES
	 */
	
	/** just for one interval - catch frame form camera view - toggle token **/
	private boolean doCapture = false; 
	/** show send button **/
	private boolean sendImg = false;
	/** if image was send - disable everything **/
	private boolean isPrinting = false;
	
	
	/** 
	 * VIEWS IN ORDER IN LAYOUT
	 */
	private Button btnCaptureImage, btnLoadImg, btnReset;
	private Button btnHist, btnInvert, btnSendCrop; 	
	private ProgressBar progressBar;
	private TextView progressTv, statusTv;
	private ImageView ivImageFile;
	private LinearLayout llProgress;
	private EditText editX, editY;
	private EditText drillMin, drillMax, drillHeadMove, drillSpeed;
	
	/**
	 * SELECTED IMAGE
	 */
	
	/** selected image - capturing **/
	private boolean imgCaptured = false;
	private Mat capturedImage = null;
	private Mat capturePrintImage = null; // capture img + sqare area
	
	/** selected image - select form folder **/
	private boolean imgLoaded = false;
	private Mat loadImage = null;
	private Mat loadPrintImage = null; // capture img + sqare area	
	private int width_tmp = 0; // origin image width
	private int height_tmp = 0; // origin image height
	private int scale = 1; // scale factor
	
	/** img to printing **/
	private Mat finalImage = null;
	
	// 96 * 60 -> NXT display  8 binarnych cisel -> poseilam po byte-och	
	int cropWidth = 8*12; // default size
	int cropHeight = 60;	// default size
	int PART_SIZE = 100; 
	int cutFromX = 0;
	int cutFromY = 0;
	private int part = 0; // current sending part	
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_drill_printer);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchEventStuffCropImage(event, v);
				return false;
			}
		});
				
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);		
		llProgress = (LinearLayout) findViewById(R.id.ll_progress);		
		progressTv = (TextView) findViewById(android.R.id.text1);		
		progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setProgress(0);
				
		btnReset = (Button) findViewById(R.id.btnReset);
		btnReset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				imgCaptured = false;
				imgLoaded = false;
				
				ivImageFile.setVisibility(View.GONE);
	            mOpenCvCameraView.setVisibility(View.VISIBLE);
				
				capturedImage = null;				
				capturePrintImage = null;
				
				loadImage = null;
				loadPrintImage = null;
				
				finalImage = null;
				toggleImgBtns(false);
				updateView(false);
			}
		});
		
		btnLoadImg = (Button) findViewById(R.id.btnLoadImg);
		btnLoadImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);    								
			}
		});
		
		btnCaptureImage = (Button) findViewById(R.id.btnCapture);
		btnCaptureImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleImgBtns(true);
				doCapture = true;
				imgCaptured = true;
				updateView(true);
			}

		});	
		
		btnHist = (Button) findViewById(R.id.btnHist);
		btnHist.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				if (imgCaptured && capturedImage != null){					
					updateView(true);
					Imgproc.equalizeHist(capturedImage, capturedImage);	
					updateImgArea();
				}
				if (imgLoaded && loadImage != null){					
					updateView(true);
					Imgproc.equalizeHist(loadImage, loadImage);	
					updateImgArea();
				}
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
		drillMin = (EditText) findViewById(R.id.drillmin);
		drillMax = (EditText) findViewById(R.id.drillmax);
		drillHeadMove = (EditText) findViewById(R.id.drillhead);
		drillSpeed = (EditText) findViewById(R.id.drillspeed);
		
		btnSendCrop = (Button) findViewById(R.id.btnSendCrop);
		btnSendCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				View f_view = (imgCaptured) ? mOpenCvCameraView : ivImageFile; 
				
				// sending by bytes - quick and easy workaround :)
				if (cropWidth % 8 != 0){
					Toast.makeText(thisActivity, "X mod 8  != 0  MOD="+ (cropWidth%8), Toast.LENGTH_SHORT).show();
					
				// capture img borders	
				}else if(imgCaptured && ( cutFromX + cropWidth > f_view.getWidth() 
						|| cutFromY + cropHeight > f_view.getHeight() )){
					Toast.makeText(thisActivity, "selected crop is out of screen, please remove it", Toast.LENGTH_SHORT).show();
					
				// loaded img borders	
				}else if(imgLoaded && (cutFromX + cropWidth > width_tmp 
						|| cutFromY + cropHeight > height_tmp) ){
					Toast.makeText(thisActivity, "selected crop is out of screen, please remove it", Toast.LENGTH_SHORT).show();

				// print
				}else{
					if (imgCaptured){
						ImageLog.saveImageToFile(thisActivity, ImageConvertClass.matToBitmap(capturedImage), ImageLog.PRINT_IMAGE);
					}else if (imgLoaded){
						ImageLog.saveImageToFile(thisActivity, ImageConvertClass.matToBitmap(loadImage), ImageLog.PRINT_IMAGE);
					}
					sendImgPart();
					updateView(true);
				}
			}
		});		
		btnInvert = (Button) findViewById(R.id.btnInvert);
		btnInvert .setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (imgCaptured && capturedImage != null){
					capturedImage = ImageConvertClass.invertImage(capturedImage);
					updateImgArea();
				}
				
				if (imgLoaded && loadImage != null){
					loadImage = ImageConvertClass.invertImage(loadImage);
					updateImgArea();
				}
			}
		});
		
		statusTv = ((TextView) findViewById(R.id.status));
		statusTv.setText("");
		((LinearLayout) findViewById(R.id.help_ll)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.help_ll_detail)).setVisibility(View.GONE);		
		updateView(false);		
		
		ivImageFile = (ImageView)findViewById(R.id.iv_from_file);
		ivImageFile.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchEventStuffCropImage(event, v);
				return false;
			}
		});
	}
	
	private void touchEventStuffCropImage(MotionEvent event, View view){
	
		if (!isPrinting){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				
				if (imgCaptured){
					cutFromX = (int)event.getX() - 60 - cropWidth/2;// soft border camera
					cutFromY = (int)event.getY() - cropHeight/2;
					
					cutFromX = (cutFromX<0)? 0 : cutFromX;
					cutFromX = (cutFromX>view.getWidth())? view.getWidth()-1 : cutFromX;
					
					cutFromY = (cutFromY<0)? 0 : cutFromY;
					cutFromY = (cutFromY>view.getHeight())? view.getHeight() : cutFromY;
					
				}else{
					double ratioX = (double) width_tmp  / view.getWidth();
					double ratioY = (double) height_tmp / view.getHeight();
					
					cutFromX = (int)((double)((double)event.getX()*ratioX) - cropWidth/2) ;// soft border camera
					cutFromY = (int)((double)((double)event.getY()*ratioY) - cropHeight/2);
					
					cutFromX = (cutFromX<0)? 0 : cutFromX;
					cutFromX = (cutFromX>width_tmp / scale)? width_tmp / scale : cutFromX;
					
					cutFromY = (cutFromY<0)? 0 : cutFromY;
					cutFromY = (cutFromY>height_tmp /scale)? height_tmp /scale : cutFromY;
				}
							
				updateImgArea();
			}
		}
	}
	
	private void toggleImgBtns(boolean imgLoaded){
		if (false == imgLoaded){
			btnCaptureImage.setVisibility(View.VISIBLE);
			btnLoadImg.setVisibility(View.VISIBLE);
			btnReset.setVisibility(View.GONE);
			sendImg = false;
		}else{
			btnCaptureImage.setVisibility(View.GONE);
			btnLoadImg.setVisibility(View.GONE);
			btnReset.setVisibility(View.VISIBLE);			
			sendImg = true;	
		}
	}
	
	public void moreOptions(View v){
		int visi = (findViewById(R.id.help_ll).getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
		
		((LinearLayout) findViewById(R.id.help_ll)).setVisibility(visi);
		((LinearLayout) findViewById(R.id.help_ll_detail)).setVisibility(visi);
		
		((LinearLayout) findViewById(R.id.help_ll)).setVisibility(visi);
		((LinearLayout) findViewById(R.id.help_ll_detail)).setVisibility(visi);
	} 
	
	public void showInfo(View v){
		DialogFragment newFragment = HelpDialog.newInstance("Black color == drill low\nWhite color == drill deep\n\n 320x320 max size", HelpDialog.TYPE_HELP);
	    newFragment.show(getFragmentManager(), HelpDialog.TAG);
	}
	
	public void toggle(View v){
		int vis =  ((findViewById(R.id.linearLayout1).getVisibility() == View.GONE) ? View.VISIBLE : View.GONE); 
		findViewById(R.id.linearLayout1).setVisibility(vis);		
	}
	
	public void penHeadDownMin(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.DRILL_MIN_DOWN, Integer.parseInt(drillMin.getText().toString().trim())/2);
		}
	}
	public void penHeadUpMin(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.DRILL_MIN_UP, Integer.parseInt(drillMin.getText().toString().trim())/2);
		}
	}
	public void penHeadDownMax(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.DRILL_MAX_DOWN, Integer.parseInt(drillMax.getText().toString().trim())/2);
		}
	}
	public void penHeadUpMax(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION, 
					BTControls.DRILL_MAX_UP, Integer.parseInt(drillMax.getText().toString().trim())/2);
		}
	}
	
	public void minusX(View view){
		if (cropWidth>0){
			cropWidth --;
		}
		editX.setText(cropWidth+"");
		updateImgArea();
	}
	public void plusX(View view){
		cropWidth ++;
		editX.setText(cropWidth+"");
		updateImgArea();
	}
	
	public void minusY(View view){
		if (cropHeight>0){
			cropHeight --;
		}
		editY.setText(cropHeight+"");
		updateImgArea();
	}			
	public void plusY(View view){
		cropHeight ++;		
		editY.setText(cropHeight+"");
		updateImgArea();
	}
	
	private void updateImgArea(){

		if (editX.getText().length() == 0){
			cropWidth = 0;
		}else{
			cropWidth = Integer.parseInt(editX.getText().toString().trim());
		}
		
		if (editY.getText().length() == 0){
			cropHeight = 0;
		}else{
			cropHeight = Integer.parseInt(editY.getText().toString().trim());
		}

		if (imgCaptured){			
			
			if (capturedImage != null){
				capturePrintImage = capturedImage.clone();
			}
		}else if (imgLoaded){
			
			if (loadImage != null){
				loadPrintImage = loadImage.clone();
				addCropAreaToCaptureView(loadPrintImage);
				ivImageFile.setImageBitmap(ImageConvertClass.matToBitmap(loadPrintImage) );
			}			
		}
	}
	
	private void sendImgPart(){
		
		finalImage = ImageConvertClass.bitmapToMat(ImageLog.getImageFromFile(thisActivity, ImageLog.PRINT_IMAGE));
		
		//log full image
		Bitmap b1 = ImageConvertClass.matToBitmap(finalImage);
		ImageLog.saveImageToFile(getApplicationContext(), b1, "last_image.jpg");		
		// log crop image
		Bitmap b2 = ImageConvertClass.cropImage(finalImage, cutFromX, cutFromY, cropWidth, cropHeight);
		ImageLog.saveImageToFile(getApplicationContext(), b2, "last_image_print.jpg");
		
		
		//debug log test		
		/*ArrayList<Integer> l = ImageConvertClass.getImagetoIntList(b2);
		Log.d("SVB", "l.size="+ l.size());
		for (int i = 0; i < 10; i++) {
			Log.d("SVB", "l["+i+"].size="+ l.get(i));
		}
		for (int i = l.size()-10; i < l.size(); i++) {
			Log.d("SVB", "l["+i+"].size="+ l.get(i));
		}
		int pT= DrillPrinterHelper.getCountImageParts(capturedImage, cutFromX, cutFromY, cropWidth, cropHight, PART_SIZE);
		Log.d("SVB", "partsTotal:" + pT);
		
		int cnt = 0; 
		for (int a : l) {
			cnt += a;
		}		
		Log.d("SVB", "count:" + cnt);*/
		
		
		if (isConnected()){
			isPrinting = true;
			sendImg = true;
			updateView(true);
			
			int partsTotal = DrillPrinterHelper.getCountImageParts(finalImage, cutFromX, cutFromY, cropWidth, cropHeight, PART_SIZE); 
			
			if (partsTotal > part){
				part++;
				Toast.makeText(this, "SENDING part: " + part, Toast.LENGTH_SHORT).show();
															
				boolean res = DrillPrinterHelper.sendImgPart(finalImage, cutFromX, cutFromY, cropWidth, cropHeight, part, partsTotal, PART_SIZE, this);
				if (res){
					Date date = new Date(System.currentTimeMillis());
					String time = new SimpleDateFormat("HH:mm", Locale.US).format(date);
					statusTv.append("\n end: " + time + "\n\n");
				}
				updateProgress(part, partsTotal);
				
			}else{
				Toast.makeText(this, "partREQUEST ERR", Toast.LENGTH_SHORT).show();
			}	
		}else{
			Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
		}
	}				
	
	private void updateProgress(int part, int totalParts){		
		progressTv.setText("part: " + part + " / " + totalParts);
		progressBar.setMax(totalParts);
		progressBar.setProgress(part);
	}	
	
	private void updateView(boolean imageCaptured) {
		
		int show = (imageCaptured) ? View.VISIBLE : View.GONE; 
		
		btnHist.setVisibility(show);				
		btnSendCrop.setVisibility(show);
		btnInvert.setVisibility(show);
		llProgress.setVisibility((sendImg) ? View.VISIBLE: View.GONE);										
		
		if (isPrinting){
			btnHist.setEnabled(false);
			btnSendCrop.setEnabled(false);
			btnInvert.setEnabled(false);
			btnCaptureImage.setEnabled(false);
			editX.setEnabled(false);
			editY.setEnabled(false);
			((Button)findViewById(R.id.plusx)).setEnabled(false);
			((Button)findViewById(R.id.minusx)).setEnabled(false);
			((Button)findViewById(R.id.plusy)).setEnabled(false);
			((Button)findViewById(R.id.minusy)).setEnabled(false);
	
			if (statusTv.getText().length() == 0){
				Date date = new Date(System.currentTimeMillis());
				String time = new SimpleDateFormat("HH:mm", Locale.US).format(date);
				statusTv.setText("log:\n"
						+ " KEEP DEVICE ENOUGHT POWER !\n\n"
						+ " xStart: " + cutFromX + "\n"
						+ " yStart: " + cutFromY + "\n\n"
						+ " start: " + time);
			}
		}		
		
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
			addCropAreaToCaptureView(capturePrintImage);
			return capturePrintImage;
		}
		
		Mat src = inputFrame.rgba();
		
		if (doCapture){
			doCapture = false;
			capturedImage = src;	
			Imgproc.cvtColor(capturedImage, capturedImage, Imgproc.COLOR_RGB2GRAY); 
			capturePrintImage = capturedImage.clone();
		}else{			
			addCropAreaToCaptureView(src);
		}
		
		
		return src;
	}

	private void addCropAreaToCaptureView(Mat img){
	
		Core.rectangle(img, 
				new Point(cutFromX-1, cutFromY-1), new Point(cutFromX+cropWidth+1, cutFromY+cropHeight+1), 
				new Scalar(255, 255, 255), 1, 1, 0);
		Core.rectangle(img, 
				new Point(cutFromX, cutFromY), new Point(cutFromX+cropWidth, cutFromY+cropHeight), 
				new Scalar(0, 0, 0), 1, 1, 0);
		Core.rectangle(img, 
				new Point(cutFromX+1, cutFromY+1), new Point(cutFromX+cropWidth-1, cutFromY+cropHeight-1), 
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
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_DRILL_PRINTER, 0);		
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.DRILL_HEAD_MOVE, Integer.parseInt(drillHeadMove.getText().toString().trim()));
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.DRILL_SPEED, Integer.parseInt(drillSpeed.getText().toString().trim()));
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
				// Toast.makeText(this, "msg: " + type , Toast.LENGTH_SHORT).show();
				break;
		}
				
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            try {
					Bitmap b = decodeUri(this, selectedImage, this.IMAGE_LOAD_FORM_FILE_SIZE);
					
					// --convert to grayscale
					loadImage = ImageConvertClass.bitmapToMat(b);
					Imgproc.cvtColor(loadImage, loadImage, Imgproc.COLOR_RGB2GRAY);
					b = ImageConvertClass.matToBitmap(loadImage);
					// --convert to grayscale
					
					ivImageFile.setImageBitmap(b);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	            
	            ivImageFile.setVisibility(View.VISIBLE);
	            mOpenCvCameraView.setVisibility(View.GONE);
	            imgLoaded = true;
	            
	            toggleImgBtns(true);
	            updateView(true);

	        }
	    }
	}

	/**
	 * resize image, load by Uri
	 * thank you stackowerflow : <br>
	 * <b>http://stackoverflow.com/questions/10773511/how-to-resize-an-image-i-picked-from-the-gallery-in-android</b>	
	 */
	public Bitmap decodeUri(Context c, Uri uri, final int requiredSize) 
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        width_tmp = o.outWidth;
        height_tmp = o.outHeight;
        scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }  
}

