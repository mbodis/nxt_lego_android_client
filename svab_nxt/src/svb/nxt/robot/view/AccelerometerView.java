package svb.nxt.robot.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class AccelerometerView extends SurfaceView implements Callback {

	private CanvasThread canvasThread;
	
	Paint c_red, c_bl;
	int x = 0;
	int y = 0;

	public AccelerometerView(Context context) {
		super(context);
		// TODO Auto-generated method stub
	}

	public AccelerometerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
		this.getHolder().addCallback(this);
		this.canvasThread = new CanvasThread(getHolder());
		this.setFocusable(true);
		setWillNotDraw(false);
	
	}

	public AccelerometerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated method stub
	}

	private void init(){
		c_red = new Paint();
		c_red.setColor(Color.RED);
		
		c_bl = new Paint();
		c_bl.setColor(Color.BLACK);
	}
	
	
	protected void myDraw(Canvas canvas) {
						
		canvas.drawARGB(255, 150, 150, 10);
		canvas.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight(), c_bl);
		canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, c_bl);
		
		int xx = (int)((double)canvas.getWidth()/80 * x);
		int yy = (int)((double)canvas.getHeight()/80 * y);
		canvas.drawCircle(canvas.getWidth()/2 + xx , canvas.getHeight()/2 + yy , 10, c_red);		

	}
	
	public void setCoords(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	public void startDrawImage() {
		canvasThread.setRunning(true);
		canvasThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		canvasThread.setRunning(false);
		while (retry) {
			try {
				canvasThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class CanvasThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private boolean isRun = false;

		public CanvasThread(SurfaceHolder holder) {
			this.surfaceHolder = holder;
		}

		public void setRunning(boolean run) {
			this.isRun = run;
		}
		
		public boolean isRunning(){
			return this.isRun;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Canvas c;

			while (isRun) {
				c = null;
				try {					
					c = this.surfaceHolder.lockCanvas(null);
					if(c != null){						
						synchronized (this.surfaceHolder) {
							AccelerometerView.this.myDraw(c);
						}
					}
				} finally {
					surfaceHolder.unlockCanvasAndPost(c);
				}
				
				try {
//					sleep(1000);
					sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
