package com.example.lawrence.esketch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // determine screen size using a bitmask
        int size = getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;

        // set to landscape for extra large tablets
        if( size == Configuration.SCREENLAYOUT_SIZE_XLARGE ){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    // not using MainActivity's default menus (onCreateOptionsMenu() and onOptionsItemSelect())
    // so they have been removed.
    // we'll use MainActivity's Fragment to do the menu logic.

}
