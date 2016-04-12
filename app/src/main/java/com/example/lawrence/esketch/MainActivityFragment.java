package com.example.lawrence.esketch;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * fragment containing a eSketch view.
 */
public class MainActivityFragment extends Fragment {

    // min threshold accel sensor to determine if a shake event took place
    private static final int ACCELERATION_THRESHOLD = 100000;

    // used to identify the request for using external storage
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    private eSketchView mESketchView;   // reference to eSketch View object
    private double acceleration;
    private double currentAcceleration;
    private double lastAcceleration;
    private boolean dialogOnScreen = false;     // flag to determine if any dialog on screen, to limit 1 dialog at a time.

    // callback when Fragment's view is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the view and get ref to view obj
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);    // fragment has menu items

        // get ref to eSketchView
        mESketchView = (eSketchView) view.findViewById(R.id.eSketchView);

        // init acceleration vals
        acceleration = 0.0;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        return view;
    }

    // start listening for sensor events
    // we attach onResume() and detach onPause() to save battery when app is/isn't active.
    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening(); // listen for shake event
    }

    // helper to enable accl sensor
    private void enableAccelerometerListening() {
        // get ref to SensorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        // register to listen for accel events
        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    // stop listening for accel events
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening();
    }

    // helper to disable accl sensor
    private void disableAccelerometerListening() {
        // get ref to SensorManger
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        // stop listening
        sensorManager.unregisterListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        );
    }

    // event handler for accel events
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // check if dialogs are on screen
            if (!dialogOnScreen) {
                // get x, y, z axis values
                double x = sensorEvent.values[0];
                double y = sensorEvent.values[1];
                double z = sensorEvent.values[2];

                // save previous value for comparison to new x, y, z values
                lastAcceleration = currentAcceleration;

                // use 3-d distance formula to compute if moved
                currentAcceleration = x * x + y * y + z * z;

                // calc change in distance
                acceleration = currentAcceleration * (currentAcceleration - lastAcceleration);

                // erase screen if shaken "hard enough"
                if (acceleration > ACCELERATION_THRESHOLD) confirmErase();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { /* not used */ }
    };

    // confirm dialog to erase screen
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    // display fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.esketch_fragment_menu, menu);
    }

    // handle user's choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialogFragment = new ColorDialogFragment();
                colorDialogFragment.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.line_width:
                LineWidthDialogFragment lineWidthDialogFragment = new LineWidthDialogFragment();
                lineWidthDialogFragment.show(getFragmentManager(), "line width");
                return true;
            case R.id.delete_drawing:
                confirmErase();
                return true;
            case R.id.save:
                saveImage();
                return true;
            // "return true" consumes the menu event
        }

        return super.onOptionsItemSelected(item);
    }

    // dialog to request for permission to save to external storage
    private void saveImage() {

        // checks if app does NOT have permission to save to external storage.
        // this is backwards compatible with older versions
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // show explanation for why permission is needed
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // get builder object to build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // set message to dialog
                builder.setMessage(R.string.permission_explanation);

                // okay button to dialog
                builder.setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        SAVE_IMAGE_PERMISSION_REQUEST_CODE
                                );
                            }
                        }
                );

                // create and display dialog
                builder.create().show();

            } else {
                // request permission
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE
                );
            }

        } else {
            // if app already has permission to write to external storage, just save image
            mESketchView.saveImage();
        }
    } // end saveImage()

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        // switch chooses appropriate action based on which feature requested permission
        switch (requestCode){
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if( grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    mESketchView.saveImage();
                }
                break;
            default:
                break;
        }
    }

    // getter for View object
    public eSketchView getSketchView(){
        return mESketchView;
    }

    // helper method to determine to display dialog or not
    public void setDialogOnScreen(boolean visible){
        dialogOnScreen = visible;
    }


}

















