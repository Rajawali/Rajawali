package org.rajawali3d.examples.examples.about.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import org.rajawali3d.examples.R;

public class ExceptionDialog extends DialogFragment {

	public static final String BUNDLE_KEY_TITLE = "ExceptionDialog.BUNDLE_KEY_TITLE";
	public static final String BUNDLE_KEY_MESSAGE = "ExceptionDialog.BUNDLE_KEY_MESSAGE";
	public static final String TAG = "ExceptionDialog.TAG";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.exception_dialog_title);
		builder.setMessage(R.string.exception_dialog_message_unknown);

		final Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey(BUNDLE_KEY_MESSAGE))
				builder.setMessage(bundle.getString(BUNDLE_KEY_MESSAGE));

			if (bundle.containsKey(BUNDLE_KEY_TITLE))
				builder.setTitle(bundle.getString(BUNDLE_KEY_TITLE));
		}

		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(false);

		return builder.create();
	}
}
