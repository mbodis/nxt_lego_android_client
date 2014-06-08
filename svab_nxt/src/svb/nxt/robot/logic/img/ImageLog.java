package svb.nxt.robot.logic.img;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageLog {

	/**
	 * @author SVB
	 * 
	 * @return 
	 * saves bitmap into sdcard<br/>
	 * android/data/package/log/<b>log_time.png</b> 
	 */		
	public static boolean saveImageToFile(Context ctx, Bitmap bitmap){
		return ImageLog.saveImageToFile(ctx, bitmap, null);
	}
	public static boolean saveImageToFile(Context ctx, Bitmap bitmap, String name){
		
		Date date = new Date(System.currentTimeMillis());
		String time = new SimpleDateFormat("yyyy.MM.dd_HH_mm", Locale.US).format(date);
		
		try {
			(new File(ctx.getExternalFilesDir(null) + "/log", "")).mkdirs();			
			File file = new File(ctx.getExternalFilesDir(null) + "/log", "log" + time + ".png");
			if (name != null){
				name = name.replace(" ", "");
				file = new File(ctx.getExternalFilesDir(null) + "/log", "log_" + name + ".png");
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
}
