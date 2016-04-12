package com.example.lawrence.esketch;

import android.support.v4.app.DialogFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * Dialog box to confirm erase image.
 */
public class EraseImageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_erase);

        // add Erase Button
        builder.setPositiveButton(R.string.button_erase,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getSketchFragment().getSketchView().clear(); // clear image
                    }
                }
        );

        // add cancel Button
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create(); // return dialog
    }

    // gets a reference to the MainActivityFragment
    private MainActivityFragment getSketchFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.esketchFragment);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getSketchFragment();

        if (fragment != null) fragment.setDialogOnScreen(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getSketchFragment();

        if (fragment != null) fragment.setDialogOnScreen(false);
    }
}
