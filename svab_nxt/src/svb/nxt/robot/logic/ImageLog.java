package svb.nxt.robot.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageLog {

	/*
	 * ulozi bitmapu do 
	 */
	public static void saveImageToFile(Context ctx, Bitmap bitmap){
		
		Date date = new Date(System.currentTimeMillis());
		String time = new SimpleDateFormat("yyyy.MM.dd_HH_mm", Locale.US).format(date);
		
		try {
			(new File(ctx.getExternalFilesDir(null) + "/log", "")).mkdirs();
			File file = new File(ctx.getExternalFilesDir(null) + "/log", "log"+time+".png"); 
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
	       e.printStackTrace();
		}
	}
}
