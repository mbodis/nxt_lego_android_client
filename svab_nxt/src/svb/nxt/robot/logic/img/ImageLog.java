package svb.nxt.robot.logic.img;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLog {

	public static String PRINT_IMAGE = "print.jpg";
	
	/**
	 * @author SVB
	 * 
	 * @return 
	 * saves bitmap into sdcard<br/>
	 * android/data/package/log/<b>log_time.png</b> 
	 */
	public static boolean saveImageToFile(Context ctx, Bitmap bitmap, String name){
		
		Date date = new Date(System.currentTimeMillis());
		String time = new SimpleDateFormat("yyyy.MM.dd_HH_mm", Locale.US).format(date);
		
		try {
			(new File(ctx.getExternalFilesDir(null) + "/log", "")).mkdirs();
			
			File file;
			
			if (name != null){
				name = name.replace(" ", "");
				file = new File(ctx.getExternalFilesDir(null) + "/log", name);
			}else{
				file = new File(ctx.getExternalFilesDir(null) + "/log", "log" + time + ".jpg");
			}
			
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			return file.exists();
		} catch (Exception e) {
	       e.printStackTrace();
		}
		
		return false;
	}
	
	public static Bitmap getImageFromFile(Context ctx, String name){
		try {
			(new File(ctx.getExternalFilesDir(null) + "/log", "")).mkdirs();			
			File file = new File(ctx.getExternalFilesDir(null) + "/log", name);					
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			return bitmap;
						
		} catch (Exception e) {
	       e.printStackTrace();
		}
		return null;
	}
}
