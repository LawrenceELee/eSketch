package com.example.lawrence.esketch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * This is the dialog to choose the line's (pencil) width.
 */
public class LineWidthDialogFragment extends DialogFragment{
    private ImageView mWidthImageView;

    // create AlertDialgo and return it
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create dialog using dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity()
                .getLayoutInflater().inflate(R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView); // add GUI to dialog

        // set dialog's msg
        builder.setTitle(R.string.title_line_width_dialog);

        // get ImageView
        mWidthImageView = (ImageView) lineWidthDialogView.findViewById(R.id.widthImageView);

        // configure widthSeekBar
        final eSketchView sketchView = getSketchFragment().getSketchView();
        final SeekBar widthSeekBar = (SeekBar) lineWidthDialogView.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChangeListener);
        widthSeekBar.setProgress(sketchView.getLineWidth());

        // add button to set width change
        builder.setPositiveButton(
                R.string.button_set_line_width,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sketchView.setLineWidth(widthSeekBar.getProgress());
                    }
                }
        );

        return builder.create();
    }

    // helper to get ref to MainActivityFragment
    private MainActivityFragment getSketchFragment(){
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.esketchFragment);
    }

    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        MainActivityFragment fragment = getSketchFragment();
        if( fragment != null )  fragment.setDialogOnScreen(true);
    }

    // tell MainActivityFragment that dialog is no longer displayed
    @Override
    public void onDetach() {
        super.onDetach();

        MainActivityFragment fragment = getSketchFragment();
        if( fragment != null )  fragment.setDialogOnScreen(false);
    }

    private final OnSeekBarChangeListener lineWidthChangeListener = new OnSeekBarChangeListener() {
        final Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap); // draws into bitmap

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // configure a Paint obj for current SeekBar
            Paint p = new Paint();
            p.setColor(getSketchFragment().getSketchView().getDrawingsColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            // erase the bitmap and redraw the line
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.drawLine(30, 50, 370, 50, p);
            mWidthImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { /* not used */ }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { /* not used */ }
    };
}
