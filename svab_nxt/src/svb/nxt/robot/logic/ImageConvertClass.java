package svb.nxt.robot.logic;

import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class ImageConvertClass {

	/*
	 * convertuje maticu do Bitmapy
	 * 1 pixel 4 bity: A R G B
	 */
	public static Bitmap matToBitmap(Mat mat){
		// Log.d("SSS", "cols:"+foto.cols() + "rows:"+foto.rows());			
		Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);			
		org.opencv.android.Utils.matToBitmap(mat, btm);
					
		// Log.d("SSS", "w"+btm.getWidth()+" h"+btm.getHeight() )	
		// Bitmap res = Bitmap.createBitmap(btm, 0, 0, 100, 100); // crop image
					
		return btm;			
	}
	
	/**
	 * test ci je viac ciernej ako bielel
	 * 
	 */
	public static boolean hasImageMoreBlackThanWhite(Bitmap bitmap){
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
	
//	public static Bitmap invertBWImage(Mat mat){
//		Bitmap bitmap = matToBitmap(mat);
//		
//		Bitmap mBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//						
//    	for (int y = 0; y < bitmap.getHeight(); y++) {
//    		for (int x = 0; x < bitmap.getWidth(); x++) {
//    
//    			mBitmap.setPixel(x, y, ((bitmap.getPixel(x, y)==0)? 1:0) );					  					
//    		}
//    	}
//        
//        return mBitmap;
//	}
	
//	public static Mat BitmapToMat(Bitmap mBitmap){
//		Mat m;
//		m.mul(m)
//		return null;
//	}
	
	public static Bitmap cropImage(Mat mat, int w, int h){
		// Log.d("SSS", "cols:"+foto.cols() + "rows:"+foto.rows());			
		Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);			
		org.opencv.android.Utils.matToBitmap(mat, btm);
					
		Bitmap res = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		for (int y = 0; y < h; y++) {
    		for (int x = 0; x < w; x++) {

        		res.setPixel(x, y, btm.getPixel(x, y));				
    		}
		}
					
		return res;	
	}

	public static StringBuilder getImagetoBinaryStr(Bitmap b) {
		StringBuilder sb = new StringBuilder();
		int pixelColor;
		for (int y = 0; y < b.getHeight(); y++) {
    		for (int x = 0; x < b.getWidth(); x++) {
    			pixelColor = b.getPixel(x, y);
    			
//        		05-17 21:28:11.011: D/SVB(30027): A:255 R:0 G:0 B:0
//        		05-17 21:28:11.011: D/SVB(30027): A:255 R:255 G:255 B:255
    			
//				int A = Color.alpha(pixelColor); 
//				int R = Color.red(pixelColor);
//				int G = Color.green(pixelColor);
//				int B = Color.blue(pixelColor);
//				Log.d("SVB","A:"+A+" R:"+R+" G:"+G+" B:"+B);
        		sb.append(	String.valueOf( 
        				((Color.red(pixelColor)==0) && (Color.green(pixelColor)==0) && (Color.blue(pixelColor)==0)
        				? 0 : 1) ));	
        		

    		}
		}
		
		return sb;
	}
}
