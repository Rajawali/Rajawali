package org.rajawali3d.examples.examples;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.rajawali3d.examples.R;

public class ExceptionDialog extends DialogFragment {

    private static final String BUNDLE_KEY_TITLE = "ExceptionDialog.BUNDLE_KEY_TITLE";
    private static final String BUNDLE_KEY_MESSAGE = "ExceptionDialog.BUNDLE_KEY_MESSAGE";
    public static final String TAG = "ExceptionDialog.TAG";

    public static ExceptionDialog newInstance(@NonNull String title, @NonNull String message) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_TITLE, title);
        bundle.putString(BUNDLE_KEY_MESSAGE, message);

        ExceptionDialog exceptionDialog = new ExceptionDialog();
        exceptionDialog.setArguments(bundle);
        return exceptionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
