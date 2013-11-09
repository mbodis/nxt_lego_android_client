package svb.nxt.robot.dialog;

import svb.lib.SvbSupportClass;
import svb.nxt.robot.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class HelpDialog extends DialogFragment{

	public static final String TAG = "help_robot_dailog";
		
	public static final int TYPE_HELP = 0;
	public static final int TYPE_ABOUT = 1;
	
	private AlertDialog.Builder builder;
	private AlertDialog alertDialog;
	private String content;
	private int type = TYPE_HELP;
	

	public static HelpDialog newInstance() {
		 return new HelpDialog();
	}
	
	public static HelpDialog newInstance(String content) {
		HelpDialog hd = new HelpDialog();
		hd.content = content;		
		return hd;
	}
	
	public static HelpDialog newInstance(String content, int type) {
		HelpDialog hd = new HelpDialog();
		hd.content = content;
		hd.type = type;
		return hd;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		View v;
		switch(type){
		case TYPE_HELP:
			v = inflater.inflate(R.layout.dialog_robot_type, null);
			((TextView)v.findViewById(R.id.textView1)).setText(content);
			break;
		case TYPE_ABOUT:	
			v = inflater.inflate(R.layout.dialog_robot_type, null);
			String text = getString(R.string.app_name) + " " + getString(R.string.version);
			text += "\n\n";
			text += SvbSupportClass.loadFile(getResources(), R.raw.licence_apache2);
			text += "\n\n";
			text += SvbSupportClass.loadFile(getResources(), R.raw.licence_mind_droid);			
			
			((TextView)v.findViewById(R.id.textView1)).setText(text);
			break;
		default:
			v = inflater.inflate(R.layout.dialog_robot_type, null);
			break;
				
		}
			
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setView(v);
		alertDialog = builder.create();		
		
		return alertDialog;
	}
	

}
