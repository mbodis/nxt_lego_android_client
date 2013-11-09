/*
 * berenoune
 * http://berenoune.blogspot.com/
 * 10/27/X2
 * 
 */

package svb.nxt.robot.game.opencv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import svb.nxt.robot.logic.ArrowClass;


/**
 * trieda pre detekciu farby
 * size limit je pocet min poce pixlov pre bounding-box
 * @author svab
 *
 */
public class ColorDetection {

	public static final int COLOR_BLUE_HSV_FORMAT = 0; 
	public static final int COLOR_RED_HSV_FORMAT = 1;
	public static final int COLOR_GREEN_HSV_FORMAT = 2;
	public static final int COLOR_YELLOW_HSV_FORMAT = 3;
	
	
	private static int BOUNDARY_SIZE_LIMIT_HEIGHT = 10;	
	private static int BOUNDARY_SIZE_LIMIT_WIDTH = 10;
	private static Mat mSrc;

	/**
	 * zmena farebneho foarmstu x YUV cez RGB do HSV 
	 * @param src
	 * @param dst
	 */
	public static void cvt_YUVtoRGBtoHSV(Mat src, Mat dst) {
		mSrc = new Mat();
		src.copyTo(mSrc);
		Imgproc.cvtColor(mSrc, dst, Imgproc.COLOR_YUV420sp2RGB);
		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2HSV);
	}

	/**
	 * definuej farebny rozsah zvolenej farby v HSV foramte
	 * @param src vystup, dolna hranica farebnej prispustnosti
	 * @param dst vystup, horna hranica farebnej pripustnosti
	 * @param color zvolena farba
	 */
	public static void getColorMat(Mat src, Mat dst, int color){
		switch (color){
		case COLOR_BLUE_HSV_FORMAT:
			Core.inRange(src, new Scalar(100, 100, 100), new Scalar(120, 255, 255),
					dst);
			break;
			
		case COLOR_GREEN_HSV_FORMAT:
			Core.inRange(src, new Scalar(60, 100, 100), new Scalar(75, 255, 255),
					dst);
			break;
			
		case COLOR_RED_HSV_FORMAT:
			Core.inRange(src, new Scalar(0, 100, 30), new Scalar(5, 255, 255), 
					dst);
			break;
			
		case COLOR_YELLOW_HSV_FORMAT:
			//30->40
			Core.inRange(src, new Scalar(20, 100, 100), new Scalar(30, 255, 255),
					dst);
			break;
		
		}
	}

	/**
	 * detekcia jedneho 
	 * @param src
	 * @param image
	 * @param text
	 * @param dst
	 */
	public static void detectSingleBlob(Mat src, Mat image, String text, Mat dst) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); 
																
		Mat hierarchy = new Mat();
		src.copyTo(dst);

		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		int k = getBiggestContourIndex(contours);
		Rect boundRect = setContourRect(contours, k);

		Point center = new Point();
		getCenterPoint(boundRect.tl(), boundRect.br(), center);
		Core.rectangle(dst, boundRect.tl(), boundRect.br(), new Scalar(255,
				255, 0), 2, 8, 0);
		
		Scalar color = new Scalar(120, 120, 120, 120);;
		Core.putText(dst, text, boundRect.tl(), 0/* font */, 1, color, 3);
	}
	
	/**
	 * detekcia 3 farieb v zdrojovej matici, vyhlada najvacsie oblastio zodpovedajuce<br>
	 * zvolemny fabram ktore maju nastavenu minimalnu velkost konstantou<br>
	 * na vystup sa vykresli ohranicujuci obdlznik pre zvolenu farbu
	 * @param src zdrojova matica
	 * @param imageRed farba
	 * @param imageYellow farba
	 * @param imageBlue farba
	 * @param dst vysledny obraz 
	 * @param arrow pomocna trida hovori o naleze farieb
	 */
	public static void detectMultipleBlob(Mat src, Mat imageRed, Mat imageYellow, 
			Mat imageBlue, Mat dst, ArrowClass arrow) {
		List<MatOfPoint> contoursRed = new ArrayList<MatOfPoint>();
		List<MatOfPoint> contoursYellow = new ArrayList<MatOfPoint>(); 
		List<MatOfPoint> contoursBlue = new ArrayList<MatOfPoint>(); 		
		
		Mat hierarchyRed = new Mat();
		Mat hierarchyYellow = new Mat();
		Mat hierarchyBlue = new Mat();
		src.copyTo(dst);

		Imgproc.findContours(imageRed, contoursRed, hierarchyRed, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(imageYellow, contoursYellow, hierarchyYellow, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(imageBlue, contoursBlue, hierarchyBlue, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		int kRed = getBiggestContourIndex(contoursRed);
		boolean showRed = isConturBigEnought(contoursRed, kRed);
		Rect boundRectRed = setContourRect(contoursRed, kRed);
		
		int kGrenn = getBiggestContourIndex(contoursYellow);
		boolean showYellow = isConturBigEnought(contoursYellow, kGrenn);
		Rect boundRectGrenn = setContourRect(contoursYellow, kGrenn);
		
		int kBlue = getBiggestContourIndex(contoursBlue);
		boolean showBlue = isConturBigEnought(contoursBlue, kBlue);
		Rect boundRectBlue = setContourRect(contoursBlue, kBlue);

		Point center = new Point();
		Scalar color = new Scalar(0, 0, 255);
		
		if (showRed){
			getCenterPoint(boundRectRed.tl(), boundRectRed.br(), center);
			Core.rectangle(dst, boundRectRed.tl(), boundRectRed.br(), new Scalar(255,
					255, 0), 2, 8, 0);
			Core.putText(dst, "red", boundRectRed.tl(), 0/* font */, 1, color, 3);
			arrow.setRed(showRed);
		}
			
		if (showYellow){
			getCenterPoint(boundRectGrenn.tl(), boundRectGrenn.br(), center);
			Core.rectangle(dst, boundRectGrenn.tl(), boundRectGrenn.br(), new Scalar(255,
					255, 0), 2, 8, 0);
			Core.putText(dst, "yellow", boundRectGrenn.tl(), 0/* font */, 1, color, 3);
			arrow.setYellow(showYellow);
		}
		
		if (showBlue){
			getCenterPoint(boundRectBlue.tl(), boundRectBlue.br(), center);
			Core.rectangle(dst, boundRectBlue.tl(), boundRectBlue.br(), new Scalar(255,
					255, 0), 2, 8, 0);
			Core.putText(dst, "blue", boundRectBlue.tl(), 0/* font */, 1, color, 3);
			arrow.setBlue(showBlue);
		}		
		
	}

	/**
	 * test ci su oblasti dostatocne velke
	 */
	private static boolean isConturBigEnought(List<MatOfPoint> contours,
			int k) {
		Rect boundRect = new Rect();
		Iterator<MatOfPoint> each = contours.iterator();
		int j = 0;
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			if (j == k) {
				boundRect = Imgproc.boundingRect(wrapper);				
				if (boundRect.width > BOUNDARY_SIZE_LIMIT_WIDTH &&
				boundRect.height > BOUNDARY_SIZE_LIMIT_HEIGHT)
					return true;
				else
					return false;
			}
			j++;
		}			
		return false;
	}

	/**
	 * vracia stred dvoch bodov oznacujucich boudary-box
	 */
	private static void getCenterPoint(Point tl, Point br, Point dst) {
		dst.x = (tl.x + br.x) / 2;
		dst.y = (tl.y + br.y) / 2;
	}

	/**
	 * vrat najvacasi index z listu zoznamu maic bodov
	 */
	private static int getBiggestContourIndex(List<MatOfPoint> contours) {
		double maxArea = 0;
		Iterator<MatOfPoint> each = contours.iterator();
		int j = 0;
		int k = -1;
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea) {
				maxArea = area;
				k = j;
			}
			j++;
		}
		return k;
	}

	/**
	 * ohranicenie matice bodov obdlznikom (bouding-box)
	 */
	private static Rect setContourRect(List<MatOfPoint> contours, int k) {
		Rect boundRect = new Rect();
		Iterator<MatOfPoint> each = contours.iterator();
		int j = 0;
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			if (j == k) {
				return Imgproc.boundingRect(wrapper);
			}
			j++;
		}
		return boundRect;
	}
}