package org.med.darknetandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

//This class is a custom imageView so that you can draw a rectangle on it
public class CanvasView extends androidx.appcompat.widget.AppCompatImageView { // or some other View-based class
    boolean drawRectangle = false;
    PointF beginCoordinate = new PointF();
    PointF endCoordinate = new PointF();
    Context drawContext;
    Paint mPaint;
    Canvas passCanvas;
    boolean drawn = false;
    int imageHeight;
    int imageWidth;

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    //setting up the paint in which we will use to draw
    public CanvasView(Context context) {
        super(context);
        this.drawContext=context;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);
        mPaint.setFilterBitmap(true);
        passCanvas = new Canvas();
    }

    //initializing another constructor
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.drawContext=context;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);
        mPaint.setFilterBitmap(true);
        passCanvas = new Canvas();
    }

    //handle touch events and extract the position of the rectangle in the canvas
    @SuppressLint({"ClickableViewAccessibility", "WrongCall"})
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                drawRectangle = true; // Start drawing the rectangle
                drawn = true;
                beginCoordinate.x = event.getX();
                beginCoordinate.y = event.getY();
                endCoordinate.x = event.getX();
                endCoordinate.y = event.getY();
                onDraw(passCanvas = new Canvas());
                invalidate(); // Tell View that the canvas needs to be redrawn
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getY() > imageHeight){
                    endCoordinate.y = imageHeight-5;
                }
                else if(event.getY() <=0){
                    endCoordinate.y = 5;
                } else {
                    endCoordinate.y = event.getY();
                }
                endCoordinate.x = event.getX();
                onDraw(passCanvas = new Canvas());
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                //This handler creates a delay to not spontaneously make the rectangle disappear
                new Handler().postDelayed(new Runnable(){
                    public void run(){
                        onDraw(passCanvas);
                        drawRectangle = false; // Stop drawing the rectangle
                    }
                }, 200);

                break;
        }
        return true;
    }
    //This method handles the drawing of the image
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawRectangle){
            canvas.drawRect(beginCoordinate.x, beginCoordinate.y, endCoordinate.x, endCoordinate.y,mPaint);
        }
    }

    public void onClear(){
        drawn = false;
        invalidate();
    }

    public boolean getDrawn(){
        return drawn;
    }

    public float getBeginX(){ return beginCoordinate.x; }
    public float getBeginY(){
        return beginCoordinate.y;
    }
    public float getEndX(){
        return endCoordinate.x;
    }
    public float getEndY(){
        return endCoordinate.y;
    }

}
