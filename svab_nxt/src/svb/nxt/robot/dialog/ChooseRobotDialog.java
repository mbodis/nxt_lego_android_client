package svb.nxt.robot.dialog;

import svb.nxt.robot.R;
import svb.nxt.robot.activity.MainMenuActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ChooseRobotDialog extends DialogFragment{

	public static final String TAG = "choose_robot_dailog";
	
	AlertDialog.Builder builder;
	AlertDialog alertDialog;

	public static ChooseRobotDialog newInstance() {
		 return new ChooseRobotDialog();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_choose_robot_layout, null);	
		
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((MainMenuActivity)getActivity()).selectNewRobot(v.getId());
				alertDialog.dismiss();				
			}
		};
		
		ImageView iv1 = (ImageView)(v.findViewById(R.id.robot_type_lejos));
		iv1.setOnClickListener(listener);
		ImageView iv2 = (ImageView)(v.findViewById(R.id.robot_type_printer));
		iv2.setOnClickListener(listener);
		ImageView iv3 = (ImageView)(v.findViewById(R.id.robot_type_segway));
		iv3.setOnClickListener(listener);
		ImageView iv4 = (ImageView)(v.findViewById(R.id.robot_type_tribot));
		iv4.setOnClickListener(listener);
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setView(v);
		alertDialog = builder.create();		
		
		return alertDialog;
	}
	

}
