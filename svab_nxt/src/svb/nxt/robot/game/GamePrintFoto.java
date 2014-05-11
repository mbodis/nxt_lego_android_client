package svb.nxt.robot.game;

import java.io.File;
import java.io.FileOutputStream;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


public class GamePrintFoto extends GameTemplateClass implements
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
	private boolean doCanny = false;
	private Mat foto = null;
	
	public void capture(View view){		
		doCapture = true;
	}
	
	public void doLog(View view){		
		if (foto != null){
			// Log.d("SSS", "cols:"+foto.cols() + "rows:"+foto.rows());			
			Bitmap btm = Bitmap.createBitmap(foto.cols(), foto.rows(), Bitmap.Config.ARGB_8888);			
			org.opencv.android.Utils.matToBitmap(foto, btm);
			
			// Log.d("SSS", "w"+btm.getWidth()+" h"+btm.getHeight() )	
			// Bitmap res = Bitmap.createBitmap(btm, 0, 0, 100, 100); // crop image
			
			saveImageToFile(btm);
		}
		
	}	
	
	/**
	 * test ci je viac ciernej ako bielel
	 * //kedze ideme tlacit na c/b tlaciarni...
	 * @param bitmap
	 * @return
	 */
	private boolean hasImageMoreBlackThanWhite(Bitmap bitmap){
		int height = bitmap.getHeight();
        int width = bitmap.getWidth();
		int black = 0; int white = 0; int other = 0; int pixelColor;  int A, R, G, B;
        	for (int y = 0; y < height; y++) {
        		for (int x = 0; x < width; x++) {

	        		pixelColor = bitmap.getPixel(x, y);
					A = Color.alpha(pixelColor); 
					R = Color.red(pixelColor);
					G = Color.green(pixelColor);
					B = Color.blue(pixelColor);
	                  
					if (R==0 && G==0 && B==0){
						black++;
					}else if (R==255 && G==255 && B==255){
						white++;
					}else{
						other++;
					}
        		}
        	}
          //Log.d("SSS", "bl:"+bl + " wh:"+wh+" ot" + ot);
        if (black > white){
        	return true;
        }
        
        return false;
	}
	
	private void saveImageToFile(Bitmap bitmap){
		try {
			(new File(getExternalFilesDir(null) + "/log", "")).mkdirs();
			File file = new File(getExternalFilesDir(null) + "/log", "sd.png"); 
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (Exception e) {
	       e.printStackTrace();
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
	
	public GamePrintFoto() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	@Override
	public void setupLayout(){
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_printer_foto);

		mOpenCvCameraView = (OpenCVColorView) findViewById(R.id.tutorial3_activity_java_surface_view);
		

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
		

		if (foto != null){
//			Log.d("SSS", "kurava");
			if (!doCanny){
				Imgproc.Canny(foto, foto, 20, 120);
				doCanny = true;
			}
			return foto;
		}
				
		
		Mat src = inputFrame.rgba();
		if (doCapture){
			Log.d("SSS", "doCapture = true");
			doCapture = false;
			foto = src;
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
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_MOVE_OPEN_CV_COLOR, 0);		
	}

}