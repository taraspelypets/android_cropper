package com.example.cropperview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by Lenovo on 27.09.2016.
 */
 class CropperAnimAppearVew extends View{
    public CropperAnimAppearVew(Context context) {
        super(context);
        photoPaint = new Paint();
        photoPaint.setStyle(Paint.Style.FILL);
        photoPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        photoPaint.setFilterBitmap(true);
        photoPaint.setDither(true);
    }

    private float height;
    private float width;

    private float top;
    private float left;
    private float bottom;
    private float right;

    private Paint photoPaint;

    private Matrix matrix = new Matrix();
    private RectF startRect;
    private Bitmap imageBitmap;

    private ValueAnimator invalidate;

    private float croppedPhotoScale;
    private float croppedPhotoTranslateX;
    private float croppedPhotoTranslateY;

    private boolean isCreated = false;
    private boolean isShown;

    private void create() {
        if (!isCreated) {
            top = getPaddingTop();
            left = getPaddingLeft();
            bottom = getHeight() - getPaddingBottom();
            right = getWidth() - getPaddingRight();

            height = getHeight() - getPaddingTop() - getPaddingBottom();
            width = getWidth() - getPaddingLeft() - getPaddingRight();
            isCreated = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        create();

        if (isShown) {
            matrix.postScale(croppedPhotoScale, croppedPhotoScale);
            matrix.postTranslate(croppedPhotoTranslateX, croppedPhotoTranslateY);
            canvas.drawBitmap(imageBitmap, matrix, photoPaint);

            matrix.reset();
        }
        super.onDraw(canvas);
    }


    private ValueAnimator xcropAnimator = new ValueAnimator();
    private ValueAnimator ycropAnimator = new ValueAnimator();
    private ValueAnimator scaleAnimator = new ValueAnimator();

    public void show(Bitmap bitmap, RectF startRect){
        imageBitmap = bitmap;
        this.startRect = startRect;

        isShown = true;

        if(xcropAnimator.isRunning()){
            xcropAnimator.cancel();
        }
        if(ycropAnimator.isRunning()){
            ycropAnimator.cancel();
        }
        if(scaleAnimator.isRunning()){
            scaleAnimator.cancel();
        }

        float s = startRect.width()/imageBitmap.getWidth();

        scaleAnimator = ValueAnimator.ofFloat(s, 1f);
        scaleAnimator.setDuration(200);
        scaleAnimator.addUpdateListener(scalecropUpdateListener);

        scaleAnimator.start();


        float x = (startRect.right+startRect.left)/2;

        xcropAnimator = ValueAnimator.ofFloat(x, width/2);
        xcropAnimator.setDuration(200);
        xcropAnimator.addUpdateListener(xcropUpdateListener);

        xcropAnimator.start();


        float y = (startRect.bottom+startRect.top)/2;


        ycropAnimator = ValueAnimator.ofFloat(y, height/2);
        ycropAnimator.setDuration(200);
        ycropAnimator.addUpdateListener(ycropUpdateListener);

        ycropAnimator.start();

        if (invalidate != null) {
            invalidate.cancel();

        }
        invalidate = ValueAnimator.ofInt(0, 210);
        invalidate.setDuration(210);
        invalidate.addUpdateListener(invalidateAnimation);

        invalidate.start();

    }

    public void hide(RectF rect){

        if(xcropAnimator.isRunning()){
            xcropAnimator.cancel();
        }
        if(ycropAnimator.isRunning()){
            ycropAnimator.cancel();
        }
        if(scaleAnimator.isRunning()){
            scaleAnimator.cancel();
        }

        float s = rect.width()/imageBitmap.getWidth();

        scaleAnimator = ValueAnimator.ofFloat(1f, s);
        scaleAnimator.setDuration(200);
        scaleAnimator.addUpdateListener(scalecropUpdateListener);

        scaleAnimator.start();

        float x = (rect.right+rect.left)/2;

        xcropAnimator = ValueAnimator.ofFloat(width/2, x);
        xcropAnimator.setDuration(200);
        xcropAnimator.addUpdateListener(xcropUpdateListener);
        xcropAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShown = false;
                imageBitmap = null;
                invalidate();
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        xcropAnimator.start();


        float y = (rect.bottom + rect.top)/2;


        ycropAnimator = ValueAnimator.ofFloat(height/2, y);
        ycropAnimator.setDuration(200);
        ycropAnimator.addUpdateListener(ycropUpdateListener);

        ycropAnimator.start();

        if (invalidate != null) {
            invalidate.cancel();

        }
        invalidate = ValueAnimator.ofInt(0, 210);
        invalidate.setDuration(210);
        invalidate.addUpdateListener(invalidateAnimation);

        invalidate.start();
    }
    private ValueAnimator.AnimatorUpdateListener invalidateAnimation = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }
    };


    private ValueAnimator.AnimatorUpdateListener ycropUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            croppedPhotoTranslateY = (float)animation.getAnimatedValue()-(imageBitmap.getHeight()/2)*croppedPhotoScale;

        }
    };
    private ValueAnimator.AnimatorUpdateListener xcropUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            croppedPhotoTranslateX = (float)animation.getAnimatedValue()-(imageBitmap.getWidth()/2)*croppedPhotoScale;

        }
    };

    private ValueAnimator.AnimatorUpdateListener scalecropUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            croppedPhotoScale = (float)animation.getAnimatedValue();

        }
    };

    public void clear(){
        isShown = false;
        imageBitmap = null;
        matrix.reset();
        invalidate();
    }


}
