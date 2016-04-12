package com.example.lawrence.esketch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.view.Gravity;
import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * view for eSketch object.
 */
public class eSketchView extends View {

    // used to determine whether user moved a finger enough to draw again
    private static final float TOUCH_TOLERANCE = 5;

    private Bitmap mBitmap; // drawing surface for displaying or saving
    private Canvas mCanvas; // used to draw on bitmap
    private final Paint mPaintScreen; // used to draw bitmap onto screen
    private final Paint mPaintLine; // used to draw lines onto bitmap

    // maps of current paths being drawn and points in those paths
    // each "finger" has a pointerID
    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Point> previousPointMap = new HashMap<>(); // previous positions of each finger

    // constructor
    public eSketchView(Context context, AttributeSet attrs) {
        super(context, attrs);  // pass context to View's constructor

        mPaintScreen = new Paint();

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);      // anti-alias smooths edges
        // set defaults for color, width, solid, rounded line.
        mPaintLine.setColor(Color.BLACK);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(5);
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);
    }

    // create Bitmap and Canvas based on View's size (the size of the screen for that device)
    // this callback is also used when apps do stuff if screen is rotated
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // 8888 means that color is stored suing 8 bits (1 byte) for alpha, red, green, blue
        mCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(Color.WHITE);
    }

    // clear the screen
    public void clear(){
        // clear data
        pathMap.clear();
        previousPointMap.clear();
        mBitmap.eraseColor(Color.WHITE);
        invalidate();   // refresh screen
    }

    // the next couple of methods are setters/getters to change the color, width
    // of the "pencil"/"finger"
    // set the line's color
    public void setDrawingColor(int color){
        mPaintLine.setColor(color);
    }

    // return the line's color
    public int getDrawingsColor(){
        return mPaintLine.getColor();
    }

    // set the painted line's width
    public void setLineWidth(int width) {
        mPaintLine.setStrokeWidth(width);
    }

    // return the painted line's width
    public int getLineWidth() {
        return (int) mPaintLine.getStrokeWidth();
    }

    // perform custom drawing when eSketchView is refreshed
    @Override
    protected void onDraw(Canvas canvas) {
        // draw background screen
        canvas.drawBitmap(mBitmap, 0, 0, mPaintScreen);

        // draw line for each path
        for( Integer key : pathMap.keySet() ){
            canvas.drawPath(pathMap.get(key), mPaintLine);
        }
    }

    //handle touch event
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        int action = motionEvent.getActionMasked(); // event type
        int actionIndex = motionEvent.getActionIndex(); // pointer (i.e. finger)

        // determine whether touch started, ended, or moving
        if( action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN ){
            touchStarted(
                    motionEvent.getX(actionIndex),
                    motionEvent.getY(actionIndex),
                    motionEvent.getPointerId(actionIndex)
            );
        } else if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP ){
            touchEnded(motionEvent.getPointerId(actionIndex));
        } else {
            touchMoved(motionEvent);
        }

        invalidate();       // redraw
        return true;
    }

    // method for when user touches screen
    private void touchStarted(float x, float y, int lineID){
        Path path;
        Point point;

        // if there is already a path of lineID
        if( pathMap.containsKey(lineID) ){
            path = pathMap.get(lineID);     // get the Path
            path.reset(); // reset Path because new touch has started
            point = previousPointMap.get(lineID); // get Path's last point
        } else {
            path = new Path();
            pathMap.put(lineID, path);  // add the Path to Map
            point = new Point(); // create a new Point
            previousPointMap.put(lineID, point); // add the Point
        }

        // move to coordinates of the touch
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    // method for when user drags along the screen
    private void touchMoved(MotionEvent motionEvent){
        // for each of the pointers in the given MotionEvent
        for( int i=0; i < motionEvent.getPointerCount(); ++i ){
            int pointerID = motionEvent.getPointerId(i);
            int pointerIndex = motionEvent.findPointerIndex(pointerID);

            // if there is a path associated with pointer
            if( pathMap.containsKey(pointerID) ){
                float newX = motionEvent.getX(pointerIndex);
                float newY = motionEvent.getY(pointerIndex);

                // get the path and prev point associated with this pointer
                Path path = pathMap.get(pointerID);
                Point point = previousPointMap.get(pointerID);

                // calc distance moved from prev point.
                float diffX = Math.abs(newX - point.x);
                float diffY = Math.abs(newY - point.y);

                // if distance is significatn enough to matter
                if( diffX >= TOUCH_TOLERANCE || diffY >= TOUCH_TOLERANCE ){
                    path.quadTo(point.x, point.y, (newX + point.x)/2, (newY + point.y)/2);

                    // store the new coords
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    } // end touchMoved()

    // method for when user finishes a touch
    private void touchEnded(int lineID){
        Path path = pathMap.get(lineID); // get corresponding Path
        mCanvas.drawPath(path, mPaintLine);     // draw to Canvas
        path.reset(); // reset the path
    }

    // save the current image to the Gallery
    public void saveImage(){
        fixMediaDir(); // fixes bug in Android 4.4

        // name file eSketch + timestamp
        final String filename = "eSketch" + System.currentTimeMillis() + ".jpg";

        // insert image on the device
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), mBitmap, filename, "eSketch Drawing"
        );

        if( location != null ){
            // notify user image was saved successfully
            Toast msg = Toast.makeText( getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
            msg.setGravity(Gravity.CENTER, msg.getXOffset()/2, msg.getYOffset()/2);
            msg.show();
        } else {
            // notify user image was NOT saved successfully
            Toast msg = Toast.makeText( getContext(), R.string.message_error_saving, Toast.LENGTH_SHORT);
            msg.setGravity(Gravity.CENTER, msg.getXOffset()/2, msg.getYOffset()/2);
            msg.show();
        }
    }

    // this fixes a bug in Android 4.4 (https://code.google.com/p/android/issues/detail?id=75447)
    // where it happens when the user hasn't taken a photo on the device before (i.e. gallery is empty and hasn't been initialized.)
    void fixMediaDir(){
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            File mediaDir = new File(sdcard, "DCIM/Camera");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }
        }
    }

    // TODO: share image via text, facebook, google+, etc.  or just save and share via gallery app

}
