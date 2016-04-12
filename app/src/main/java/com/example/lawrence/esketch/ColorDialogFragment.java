package com.example.lawrence.esketch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * This is the dialog box for changing the color of the "pencil"
 */
public class ColorDialogFragment extends DialogFragment{
    private SeekBar mAlphaSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;
    private SeekBar mRedSeekBar;
    private View mColorView;        // area to view a sample of the color from seekbar combos
    private int mColor;

    // create an AlertDialog and return it
    @Override
    public Dialog onCreateDialog(Bundle bundle){
        // create dialog using dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // inflate xml into code
        View colorDialogView = getActivity()
                .getLayoutInflater().inflate(R.layout.fragment_color, null);
        // add GUI to dialog
        builder.setView(colorDialogView);

        // set dialogs msg
        builder.setTitle(R.string.title_color_dialog);

        // get color SeekBars and set the onChange listeners
        mAlphaSeekBar = (SeekBar) colorDialogView.findViewById(R.id.alphaSeekBar);
        mRedSeekBar = (SeekBar) colorDialogView.findViewById(R.id.redSeekBar);
        mGreenSeekBar = (SeekBar) colorDialogView.findViewById(R.id.greenSeekBar);
        mBlueSeekBar = (SeekBar) colorDialogView.findViewById(R.id.blueSeekBar);
        mColorView = colorDialogView.findViewById(R.id.colorView);

        // use current drawing color to set init seekbar vals
        final eSketchView sketchView = getSketchFragment().getSketchView();
        mColor = sketchView.getDrawingsColor();
        mBlueSeekBar.setProgress(Color.blue(mColor));
        mGreenSeekBar.setProgress(Color.green(mColor));
        mRedSeekBar.setProgress(Color.red(mColor));
        mAlphaSeekBar.setProgress(Color.alpha(mColor));

        // button to set color

        return builder.create();
    }

    // get ref to MainActivityFragment
    private MainActivityFragment getSketchFragment(){
        return (MainActivityFragment) getFragmentManager()
                .findFragmentById(R.id.esketchFragment);
    }

    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        MainActivityFragment fragment = getSketchFragment();
        if( fragment != null )  fragment.setDialogOnScreen(true);
    }

    // tell MainActivity that dialog is no longer displayed
    @Override
    public void onDetach(){
        super.onDetach();

        MainActivityFragment fragment = getSketchFragment();
        if( fragment != null ) fragment.setDialogOnScreen(false);
    }

    // seekbar listener
    private final OnSeekBarChangeListener colorChangedListener = new OnSeekBarChangeListener() {
        // display updated color
        @Override
        public void onProgressChanged(SeekBar seekBar, int integer, boolean fromUser) {
            if( fromUser ) { // user, not program, change SeekBar progress
                mColor = Color.argb(
                        mAlphaSeekBar.getProgress(),
                        mRedSeekBar.getProgress(),
                        mGreenSeekBar.getProgress(),
                        mBlueSeekBar.getProgress()
                );
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { /* not used */ }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { /* not used */ }
    };

}
