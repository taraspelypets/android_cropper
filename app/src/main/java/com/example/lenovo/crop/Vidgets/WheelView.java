package com.example.lenovo.crop.Vidgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.lenovo.crop.R;


/**
 * Created by ENVY23 on 06.05.2016.
 */
public class WheelView extends View {

    private final String TAG = "Wheel";

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private int alphaDifference = 210;
    private int maxAlpha = 255;

    private boolean isCreated = false;

    private float height, width, left, top, right, bottom;

    float scaleTop, scaleBottom;
    float cursorTop, cursorBottom;


    private float firstStep = 20;

    private Paint scalePaint, cursorPaint;
    int numberOfLines = 31;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    boolean isActive = true;

    private int accent;

    private void init(){
        scalePaint = new Paint();
        scalePaint.setColor(Color.DKGRAY);
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setStrokeWidth(getResources().getDimension(R.dimen.scale_line_width));

        cursorPaint = new Paint();
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        cursorPaint.setStrokeWidth(getResources().getDimension(R.dimen.cursor_line_width));

        firstStep = getResources().getDimension(R.dimen.first_step);
    }

    private void create() {
        accent = getResources().getColor(R.color.colorAccent);
        cursorPaint.setColor(accent);
        isCreated = true;

        height = getHeight()-getPaddingTop()-getPaddingBottom();
        width = getWidth()-getPaddingLeft()-getPaddingRight();

        left = getPaddingLeft();
        top = getPaddingTop();
        right = getWidth()-getPaddingRight();
        bottom = getHeight() - getPaddingBottom();

        scaleTop = height/2 - getResources().getDimension(R.dimen.scale_line_height)/2;
        scaleBottom = height/2 + getResources().getDimension(R.dimen.scale_line_height)/2;

        cursorTop = height/2 - getResources().getDimension(R.dimen.cursor_line_height)/2;
        cursorBottom = height/2 + getResources().getDimension(R.dimen.cursor_line_height)/2;

        coef = width/ firstStep;
        int i = 0;
        float dist = firstStep;
        float step = firstStep;
        while(dist <=  width/2){
            step -= step/coef;
            dist += step;
            i++;
        }
        numberOfLines = i*2+1;
        lastStep = step;




    }


    float lastStep;
    float coef;

    public boolean isCentralBlue = false;

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isCreated)create();

        int alpha = maxAlpha;
        scalePaint.setAlpha(alpha);


        if(blueCount<0){
            scalePaint.setColor(Color.BLACK);
            scalePaint.setAlpha(alpha);
            canvas .drawLine(width / 2 + transition, scaleTop, width / 2 + transition, scaleBottom, scalePaint);
        }else if(blueCount>0){
            scalePaint.setAlpha(alpha);
            canvas .drawLine(width / 2 + transition, scaleTop, width / 2 + transition, scaleBottom, scalePaint);
        }else{
            scalePaint.setColor(Color.DKGRAY);
            scalePaint.setAlpha(alpha);
            canvas .drawLine(width / 2 + transition, scaleTop, width / 2 + transition, scaleBottom, scalePaint);
        }


        int deltaAlpha = alphaDifference/(numberOfLines/2-1);
        coef = width/ firstStep;
        float dist = firstStep;
        float step = firstStep;
        for(int i = 0; i <= numberOfLines/2 - 1; i++){
            alpha -= deltaAlpha;
            if(i < blueCount-1){
                scalePaint.setAlpha(alpha);
                canvas .drawLine(width / 2 + dist + transition * (step / firstStep), scaleTop, width / 2 + dist + transition * (step / firstStep), scaleBottom, scalePaint);
            }else{
                scalePaint.setColor(Color.DKGRAY);
                scalePaint.setAlpha(alpha);
                canvas .drawLine(width / 2 + dist + transition * (step / firstStep), scaleTop, width / 2 + dist + transition * (step / firstStep), scaleBottom, scalePaint);
            }
            step -= step/coef;
            dist += step;
        }
        dist = firstStep;
        step = firstStep;
        alpha = maxAlpha;
        for(int i = 0; i <= numberOfLines/2 - 1; i++){
            alpha -= deltaAlpha;
            scalePaint.setAlpha(alpha);
            if(-i >blueCount){
                scalePaint.setAlpha(alpha);
                canvas .drawLine(width / 2 - dist + transition * (step / firstStep), scaleTop, width / 2 - dist + transition * (step / firstStep), scaleBottom, scalePaint);
            }else{
                scalePaint.setColor(Color.DKGRAY);
                scalePaint.setAlpha(alpha);
                canvas .drawLine(width / 2 - dist + transition * (step / firstStep), scaleTop, width / 2 - dist + transition * (step / firstStep), scaleBottom, scalePaint);

            }

            step -= step/coef;
            dist += step;

        }

        canvas.drawLine(width / 2 , cursorTop, width / 2 , cursorBottom, cursorPaint);

        super.onDraw(canvas);
    }

    float transition = 0;

    private ValueAnimator animator;



    boolean isStartCount = false;
    float fulldistance = 0;
    int blueCount = 0;
    GestureDetector mDetector = new GestureDetector(getContext(), new GestureListener());

    private float prevTouchX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_UP){
            blueCount = 0;
            isCentralBlue = false;

            invalidate();
        }
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            prevTouchX = event.getX();

        }
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            float distance  = event.getX() - prevTouchX;
            prevTouchX = event.getX();
            transition -= distance;
            fulldistance -= distance;
            float divisionCount = fulldistance/firstStep;


            if(transition >= firstStep){
                int i = 0;
                float temp = transition-firstStep;
                for(; temp>0; i++){
                    temp -=firstStep;
                }
                transition = 0+((transition)-firstStep*i);
                isCentralBlue = true;
                blueCount +=i;

            }
            if(transition < 0) {
                int i = 0;
                float temp = transition+firstStep;
                for(; temp<firstStep; i++){
                    temp +=firstStep;
                }
                transition = firstStep*i + transition;
                blueCount -=i;
//                isCentralBlue = true;

            }


            deliverRotation(-distance);
            invalidate();
        }




        if(isActive){
            return mDetector.onTouchEvent(event);

        }else{
            return false;
        }


    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {


            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }




    private WheelListener listener;
    private WheelDivisionsListener wheelDivisionsListener;

    public void setWheelListener(WheelListener listener){
        this.listener = listener;
    }

    private void deliverRotation(float value){
        if (listener!=null){
            listener.onWheelRotated(value);
        }
        if (wheelDivisionsListener!=null){
            wheelDivisionsListener.onWheelRotatedDivisions(value);
        }
    }
    public interface WheelListener{
        public void onWheelRotated(float value);
    }

    public interface WheelDivisionsListener{
        public void onWheelRotatedDivisions(float value);
    }



}
