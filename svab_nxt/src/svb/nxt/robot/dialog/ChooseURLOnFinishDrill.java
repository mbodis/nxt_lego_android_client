package svb.nxt.robot.dialog;

import svb.nxt.robot.R;
import svb.nxt.robot.game.GameDrillPrinter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ChooseURLOnFinishDrill extends DialogFragment {

	public static final String TAG = "choose_url_finish_drill";

	AlertDialog.Builder builder;
	AlertDialog alertDialog;

	public static ChooseURLOnFinishDrill newInstance() {
		return new ChooseURLOnFinishDrill();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String previousURL = PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getString(GameDrillPrinter.PRINT_FINISH_URL, "");

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_save_url, null);
		
		if (previousURL != ""){
			((EditText) layout.findViewById(R.id.editText1)).setText(previousURL);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(
				getResources().getString(R.string.dialog_finish_url_title))

				.setView(layout)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								String URL = ((EditText) layout
										.findViewById(R.id.editText1))
										.getText().toString();
								// save the url
								PreferenceManager
										.getDefaultSharedPreferences(
												getActivity())
										.edit()
										.putString(
												GameDrillPrinter.PRINT_FINISH_URL,
												URL).commit();
							}
						});
		return builder.create();
	}
}
