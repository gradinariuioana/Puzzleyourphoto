package com.example.puzzleyourphoto;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

 class TouchListener implements View.OnTouchListener {
    private float xDelta;
    private float yDelta;
    private Context context;
    final GameActivity gameActivity;

    TouchListener(Context context){
        this.context = context;
        gameActivity = (GameActivity) context;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        DisplayMetrics metrices = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrices);
        int windowWidth = metrices.widthPixels;
        int windowHeight = metrices.heightPixels;
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        final double tolerance = Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2)) / 10;

        PuzzlePiece piece = (PuzzlePiece) view;
        if (!piece.canMove) {
            return true;
        }
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = x - lParams.leftMargin;
                yDelta = y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_MOVE:
                lParams.leftMargin = (int) (x - xDelta);
                lParams.topMargin = (int) (y - yDelta);
                lParams.bottomMargin = windowHeight - Math.round(y- yDelta - piece.getHeight());
                lParams.rightMargin= windowWidth - Math.round(x - xDelta - piece.getWidth());

                view.setLayoutParams(lParams);
                break;
            case MotionEvent.ACTION_UP:
                int xDiff = Math.abs(piece.xCoord - lParams.leftMargin);
                int yDiff = Math.abs(piece.yCoord - lParams.topMargin);
                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCoord;
                    lParams.topMargin = piece.yCoord;

                    piece.setLayoutParams(lParams);
                    piece.canMove = false;
                    sendViewToBack(piece);
                }
                if (JigsawGame.isFinished())
                    GameActivity.displayFinish(context, gameActivity);
                break;
        }
        return true;
    }

     public static void sendViewToBack(final View child) {
         final ViewGroup parent = (ViewGroup)child.getParent();
         if (null != parent) {
             parent.removeView(child);
             parent.addView(child, 0);
         }
     }
}