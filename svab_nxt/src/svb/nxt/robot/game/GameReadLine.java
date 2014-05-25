package svb.nxt.robot.game;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTCommunicator;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.logic.GameTemplateClass;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GameReadLine extends GameTemplateClass{

	private boolean power = false;
	private int blackLimit;
	
	public void power(View view) {
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
					BTControls.POWER, 0);
			power = !power;
			if (power) {				
				((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
			} else {
				((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_media_stop));
			}
		}
	}
	
	public void sendValue(View view){
		if (isConnected()){
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
					BTControls.LIGHT_SET_MAX, blackLimit);
		}
	}
	
	@Override
	public void setupLayout() {		
		setContentView(R.layout.game_read_line_layout);

		final TextView tv = (TextView) findViewById(R.id.textView1);
		SeekBar sb = ((SeekBar) findViewById(R.id.seekBar1));
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				blackLimit = progress;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
				Editor mPrefsEditor = prefs.edit();
				mPrefsEditor.putInt("black", blackLimit);
				mPrefsEditor.commit();
				tv.setText(blackLimit + "/100");
			}
		});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
		blackLimit = prefs.getInt("black", 40);
		sb.setProgress(blackLimit);
		tv.setText(blackLimit + "/100");
	}
	
	@Override
	public void onConnectToDevice() {
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.GAME_TYPE, BTControls.PROGRAM_READ_LINE, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY,
				BTCommunicator.ROBOT_TYPE,
				robotType, 0);
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_ACTION,
				BTControls.LIGHT_SET_MAX, blackLimit);
	}

}

