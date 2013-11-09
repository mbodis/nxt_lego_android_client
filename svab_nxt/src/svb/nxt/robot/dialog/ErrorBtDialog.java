package svb.nxt.robot.dialog;

import svb.nxt.robot.R;
import svb.nxt.robot.logic.GameTemplateClass;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorBtDialog extends DialogFragment {

	public static final String TAG = "error_bt_dailog";

	AlertDialog.Builder builder;
	AlertDialog alertDialog;

	public static ErrorBtDialog newInstance() {
		return new ErrorBtDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(
				getResources().getString(R.string.bt_error_dialog_title))
				.setMessage(
						getResources().getString(
								R.string.bt_error_dialog_message))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								((GameTemplateClass) getActivity())
										.setBtErrorPending(false);
								dialog.cancel();
								((GameTemplateClass) getActivity()).selectNXT();
							}
						});
		return builder.create();
	}
}
