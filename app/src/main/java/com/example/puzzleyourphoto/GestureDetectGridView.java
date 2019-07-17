package com.example.puzzleyourphoto;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridView;

public class GestureDetectGridView extends GridView {
    private GestureDetector gestureDetector;
    private boolean isFling = false;
    private float myTouchX;
    private float myTouchY;

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    public GestureDetectGridView(Context context) {
        super(context);
        init(context);
    }

    public GestureDetectGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureDetectGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(final Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                return true;
            }

            @Override
            //A fling is one of several "gestures" recognised in Android; it is a quick swipe action that has no specific target.
            //In Android, a fling generates a "velocity" value, which can be used to calculate how widgets should react after the user's finger has left the screen.
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int position = GestureDetectGridView.this.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY()));
                //IF THE DISTANCE BETWEEN Y'S IS TOO BIG -> A VERTICAL MOVE
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    //If the other coordinate is too far or the Y swipe was too little
                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
                        return false;
                    }
                    //e1 above e2
                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                        GameActivity.moveTiles(context, GameActivity.up, position);
                    }
                    //e2 above e1
                    else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                        GameActivity.moveTiles(context, GameActivity.down, position);
                    }
                }
                else {
                    if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                        return false;
                    }
                    //e1 to the right of e2
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                        GameActivity.moveTiles(context, GameActivity.left, position);
                    }
                    //e2 to the right of e1
                    else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                        GameActivity.moveTiles(context, GameActivity.right, position);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        gestureDetector.onTouchEvent(event);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isFling = false;
        }
        else if (action == MotionEvent.ACTION_DOWN) {
            myTouchX = event.getX();
            myTouchY = event.getY();
        } else {
            if (isFling) { return true; }
            float dX = (Math.abs(event.getX() - myTouchX));
            float dY = (Math.abs(event.getY() - myTouchY));
            if ((dX > SWIPE_MIN_DISTANCE) || (dY > SWIPE_MIN_DISTANCE)) {
                isFling = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}