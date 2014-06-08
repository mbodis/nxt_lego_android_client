package svb.nxt.robot.activity;

import java.util.ArrayList;
import java.util.List;

import svb.nxt.robot.R;
import svb.nxt.robot.bt.BTControls;
import svb.nxt.robot.dialog.ChooseRobotDialog;
import svb.nxt.robot.dialog.HelpDialog;
import svb.nxt.robot.game.GameDrillPrinter;
import svb.nxt.robot.game.GameMoveAccelerometer;
import svb.nxt.robot.game.GameMoveColorActivity;
import svb.nxt.robot.game.GameMoveDirectionActivity;
import svb.nxt.robot.game.GameMoveMotorActivity;
import svb.nxt.robot.game.GamePenPrinter;
import svb.nxt.robot.game.GamePrintTest2;
import svb.nxt.robot.game.GamePrinterTest;
import svb.nxt.robot.game.GameReadLine;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

	public static final String KEY_ROBOT_TYPE = "robotType";

	public static final int ROBOT_ID_TRIBOT = R.id.robot_type_tribot;
	public static final int ROBOT_ID_LEJOS = R.id.robot_type_lejos;
	public static final int ROBOT_ID_PRINTER = R.id.robot_type_printer;
	public static final int ROBOT_ID_SEGWAY = R.id.robot_type_segway;
	
	private String selectedProgram;
	
	private ImageView select;
	private TextView robotName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BluetoothAdapter.getDefaultAdapter().enable();

		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main_activity);
		select = ((ImageView) findViewById(R.id.select_robot_btn));
		robotName = (TextView) findViewById(R.id.robotName);
		select.setOnLongClickListener(new OnLongClickListener() {
			
			@Override 
			public boolean onLongClick(View v) {
				showHelp();
				return true;
			}
		});
		(findViewById(R.id.version)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				showAbout();
			}
		});
		
		refreshAdapter();
		setRobotType();
	}
	
	public void refreshAdapter() {
		Spinner spinner = (Spinner) findViewById(R.id.program_spinner);
		List<String> list = new ArrayList<String>();
		int robotType = getRobotType(this);
		if (robotType == ROBOT_ID_TRIBOT) {
			list.add(getString(R.string.program_move_direction));
			list.add(getString(R.string.program_move_motor));
			list.add(getString(R.string.program_move_by_color));
			list.add(getString(R.string.program_move_accelerometer));
			list.add(getString(R.string.program_read_line));			
		} else if (robotType == ROBOT_ID_LEJOS) {
			list.add(getString(R.string.program_move_motor));
		} else if (robotType == ROBOT_ID_PRINTER) {
			list.add(getString(R.string.program_print_test));
			list.add(getString(R.string.program_print_test2));
			list.add(getString(R.string.program_pen_print_photo));
			list.add(getString(R.string.program_drill_print_photo));
		} else if (robotType == ROBOT_ID_SEGWAY) {
			list.add(getString(R.string.program_segway));			
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_spinner_item,
				list) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextSize(16);
				((TextView) v).setTextColor(getResources().getColorStateList(
						android.R.color.black));
				return v;
			}

			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.item_white_gray_selector));
				((TextView) v).setTextColor(getResources().getColorStateList(
						android.R.color.black));
				((TextView) v).setGravity(Gravity.CENTER);

				return v;
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedProgram = adapter.getItem(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
	}

	public void start(View view) {
		Intent playGame = null;
		if (selectedProgram.equals(getString(R.string.program_move_direction))) {
			playGame = new Intent(getApplicationContext(),
					GameMoveDirectionActivity.class);
		} else if (selectedProgram.equals(getString(R.string.program_move_motor))) {
			playGame = new Intent(getApplicationContext(),
					GameMoveMotorActivity.class);
		} else if (selectedProgram.equals(getString(R.string.program_move_by_color))) {
			playGame = new Intent(getApplicationContext(),
					GameMoveColorActivity.class);
		} else if (selectedProgram.equals(getString(R.string.program_move_accelerometer))) {
			playGame = new Intent(getApplicationContext(),
					GameMoveAccelerometer.class);
		} else if (selectedProgram.equals(getString(R.string.program_read_line))){
			playGame = new Intent(getApplicationContext(),
					GameReadLine.class);
		} else if (selectedProgram.equals(getString(R.string.program_print_test))){
			playGame = new Intent(getApplicationContext(),
					GamePrinterTest.class);
		} else if (selectedProgram.equals(getString(R.string.program_print_test2))){
			playGame = new Intent(getApplicationContext(),
					GamePrintTest2.class);
		} else if (selectedProgram.equals(getString(R.string.program_pen_print_photo))){
			playGame = new Intent(getApplicationContext(),
					GamePenPrinter.class);	
		} else if (selectedProgram.equals(getString(R.string.program_drill_print_photo))){
			playGame = new Intent(getApplicationContext(),
					GameDrillPrinter.class);
		} else if (selectedProgram.equals(getString(R.string.program_segway))){
			Toast.makeText(getApplicationContext(), "in progress ...", Toast.LENGTH_SHORT).show();
//			playGame = new Intent(getApplicationContext(),
//					GameSegwayMove.class);
		}
		if (playGame != null) {			
			startActivity(playGame);
		}
	}

	public void chooseRobot(View view) { 
	    DialogFragment newFragment = ChooseRobotDialog.newInstance();
	    newFragment.show(getFragmentManager(), ChooseRobotDialog.TAG);
	}
	
	public void showHelp(){
		String helpText = null;
		switch (getRobotType(this)){
		case R.id.robot_type_lejos:
			helpText = getString(R.string.help_robot_lejos);
			break;
		case R.id.robot_type_printer:
			helpText = getString(R.string.help_robot_printer);
			break;
		case R.id.robot_type_segway:
			helpText = getString(R.string.help_robot_segway);
			break;
		case R.id.robot_type_tribot:
			helpText = getString(R.string.help_robot_tribot);
			break;
		}
		DialogFragment newFragment = HelpDialog.newInstance(helpText, HelpDialog.TYPE_HELP);
	    newFragment.show(getFragmentManager(), HelpDialog.TAG);
	}
	
	public void showAbout(){
		DialogFragment newFragment = HelpDialog.newInstance(null, HelpDialog.TYPE_ABOUT);
	    newFragment.show(getFragmentManager(), HelpDialog.TAG);
	}
	
	public void setRobotType() {
		int robotType = getRobotType(this);
		switch (robotType) {
		
		case R.id.robot_type_tribot:
			select.setImageDrawable(getResources().getDrawable(R.drawable.robot_tribot));
			robotName.setText("Tribot");
			break;
			
		case R.id.robot_type_lejos:
			select.setImageDrawable(getResources().getDrawable(R.drawable.robot_lejos));
			robotName.setText("leJOS");
			break;
			
		case R.id.robot_type_printer:
			robotName.setText("Printer");
			select.setImageDrawable(getResources().getDrawable(R.drawable.robot_printer));
			break;
			
		case R.id.robot_type_segway:
			robotName.setText("Segway");
			select.setImageDrawable(getResources().getDrawable(R.drawable.robot_segway));
			break;
		}

	}

	public static int getRobotType(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getInt(KEY_ROBOT_TYPE, R.id.robot_type_tribot);
	}
	
	public static void saveRobotType(Context ctx, int rototType) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().putInt(KEY_ROBOT_TYPE, rototType).commit();		
	}	
	
	public void selectNewRobot(int id){		 
		saveRobotType(getApplicationContext(), id);
		setRobotType();
		refreshAdapter();
	}
	
	public static int getRobotBtType(Context ctx) {
		int robotType = MainMenuActivity.getRobotType(ctx);
		switch (robotType) {
		case ROBOT_ID_LEJOS:
			return BTControls.ROBOT_TYPE_LEJOS;
		case ROBOT_ID_TRIBOT:
			return BTControls.ROBOT_TYPE_TRIBOT;			
		case ROBOT_ID_PRINTER:
			return BTControls.ROBOT_TYPE_PRINTER;
		case ROBOT_ID_SEGWAY:
			return BTControls.ROBOT_TYPE_SEGWAY;
		default:
			return -1;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		BluetoothAdapter.getDefaultAdapter().disable();
	}

}
