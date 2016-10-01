package com.example.cropperview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Taras Pelypets on 27.09.2016.
 *
 * CropperSelectionView displays rectangle area allowing user to choose desirable part of image;
 */
class CropperSelectionView extends View {

    private static final String TAG = "CropperSelectionView";
    public CropperSelectionView(Context context) {
        super(context);
        init();
    }

    private static final int CROPPER_TRANSFORM_MODE = 1;
    private static final int CROPPER_DRAG_MODE = 2;

    private int currentMode;

    private Paint photoPaint;
    private Paint cropperPaint;
    private Paint cropperCornersPaint;
    private Paint cropperGridPaint;
    private Paint scrPaint;
    private Paint dstPaint;

    private boolean isCreated = false;
    private boolean isGridEnabled = false;

    private float height;
    private float width;

    private float top;
    private float left;
    private float bottom;
    private float right;

    private float touchDiapason;
    private float minCropperSize;
    private float initialCropperPadding;
//TODO add to resources
    private float cropperFrameWidth;
    private float cropperCornerWidth;
    private float cropperGridWidth;
//
    private int currentCropperCorner;
    private int currentCropperSide;
    private PointF lastCropperTouchPos;

    Canvas c;
    Bitmap darken;
    private RectF cropperRect;

    private GestureDetector mDetector = new GestureDetector(getContext(), new GestureListener());



    private void init(){
        cropperFrameWidth = getResources().getDimension(R.dimen.cropperFrameWidth);
        cropperCornerWidth = getResources().getDimension(R.dimen.cropperCornerWidth);
        cropperGridWidth = getResources().getDimension(R.dimen.cropperGridWidth);

        photoPaint = new Paint();
        photoPaint.setStyle(Paint.Style.FILL);
        photoPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        photoPaint.setDither(true);

        cropperPaint = new Paint();
        cropperPaint.setStyle(Paint.Style.STROKE);
        cropperPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        cropperPaint.setStrokeWidth(cropperFrameWidth);
        cropperPaint.setColor(Color.WHITE);

        cropperGridPaint = new Paint();
        cropperGridPaint.setStyle(Paint.Style.STROKE);
        cropperGridPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        cropperGridPaint.setStrokeWidth(cropperGridWidth);
        cropperGridPaint.setColor(Color.WHITE);
        cropperGridPaint.setAlpha(120);
//        cropperGridPaint.setPathEffect(new DashPathEffect(new float[] { 7, 3}, 0));

        cropperCornersPaint = new Paint();
        cropperCornersPaint.setStyle(Paint.Style.STROKE);
        cropperCornersPaint.setStrokeWidth(cropperCornerWidth);
        cropperCornersPaint.setColor(Color.WHITE);
        cropperCornersPaint.setStrokeJoin(Paint.Join.MITER);
        cropperCornersPaint.setStrokeCap(Paint.Cap.SQUARE);

        scrPaint = new Paint();
        scrPaint.setStyle(Paint.Style.FILL);
        scrPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        scrPaint.setStrokeWidth(5);
        scrPaint.setColor(Color.BLACK);
        scrPaint.setAlpha(100);

        dstPaint = new Paint();
        dstPaint.setStyle(Paint.Style.FILL);
        dstPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        dstPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

    }

    private void create(){
        if(!isCreated) {
            top = getPaddingTop();
            left = getPaddingLeft();
            bottom = getHeight() - getPaddingBottom();
            right = getWidth() - getPaddingRight();

            height = getHeight() - getPaddingTop() - getPaddingBottom();
            width = getWidth() - getPaddingLeft() - getPaddingRight();

            touchDiapason = getResources().getDimension(R.dimen.touch_diapason);
            minCropperSize = getResources().getDimension(R.dimen.min_cropper_size);
            initialCropperPadding = getResources().getDimension(R.dimen.initial_cropper_padding);


            cropperRect = new RectF(left + initialCropperPadding, top + initialCropperPadding, right - initialCropperPadding, bottom - initialCropperPadding);

            darken = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
            c = new Canvas(darken);

            isCreated = true;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        create();

        if(isEnabled()) {

            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            c.drawRect(0, 0, width, height, scrPaint);
            c.drawRect(cropperRect, dstPaint);

            canvas.drawBitmap(darken, 0, 0, photoPaint);

            canvas.drawRect(cropperRect, cropperPaint);
            drawCorners(canvas);
            if(isGridEnabled) drawGrid(canvas);
        }
     }
    private void drawCorners(Canvas canvas){
        float cornerSize = getResources().getDimension(R.dimen.corner);

        // Left-top corner
        canvas.drawLine(cropperRect.left, cropperRect.top, cropperRect.left, cropperRect.top + cornerSize, cropperCornersPaint);
        canvas.drawLine(cropperRect.left, cropperRect.top, cropperRect.left + cornerSize, cropperRect.top, cropperCornersPaint);

        // Right-top corner
        canvas.drawLine(cropperRect.right, cropperRect.top, cropperRect.right, cropperRect.top + cornerSize, cropperCornersPaint);
        canvas.drawLine(cropperRect.right, cropperRect.top, cropperRect.right - cornerSize, cropperRect.top, cropperCornersPaint);

        // Right-bottom corner
        canvas.drawLine(cropperRect.right, cropperRect.bottom, cropperRect.right, cropperRect.bottom - cornerSize, cropperCornersPaint);
        canvas.drawLine(cropperRect.right, cropperRect.bottom, cropperRect.right - cornerSize, cropperRect.bottom, cropperCornersPaint);

        // Left-bottom corner
        canvas.drawLine(cropperRect.left, cropperRect.bottom, cropperRect.left, cropperRect.bottom - cornerSize, cropperCornersPaint);
        canvas.drawLine(cropperRect.left, cropperRect.bottom, cropperRect.left + cornerSize, cropperRect.bottom, cropperCornersPaint);
    }

    //  this method draws 3x3 gird on cropper area
    private void drawGrid(Canvas canvas){

        //        draw vertical lines
        float h = cropperRect.width()/3;
        Path v1 = new Path();
        v1.moveTo(cropperRect.left + h, cropperRect.top);
        v1.lineTo(cropperRect.left + h, cropperRect.bottom);

        Path v2 = new Path();
        v2.moveTo(cropperRect.left + h*2, cropperRect.top);
        v2.lineTo(cropperRect.left + h*2, cropperRect.bottom);

        canvas.drawPath(v1, cropperGridPaint);
        canvas.drawPath(v2, cropperGridPaint);



        //        draw horizontal lines
        float w = cropperRect.height()/3;
        Path h1 = new Path();
        h1.moveTo(cropperRect.left, cropperRect.top + w);
        h1.lineTo(cropperRect.right, cropperRect.top + w);

        Path h2 = new Path();
        h2.moveTo(cropperRect.left, cropperRect.top + w*2);
        h2.lineTo(cropperRect.right, cropperRect.top + w*2);

        canvas.drawPath(h1, cropperGridPaint);
        canvas.drawPath(h2, cropperGridPaint);


    }

    public boolean onTouchEvent(MotionEvent event) {


        mDetector.onTouchEvent(event);


        PointF tPoint = new PointF(event.getX(0), event.getY(0));

        if(isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                boolean cropperMode = false;


                PointF cropperLT = new PointF(cropperRect.left, cropperRect.top);
                PointF cropperRT = new PointF(cropperRect.right, cropperRect.top);
                PointF cropperRB = new PointF(cropperRect.right, cropperRect.bottom);
                PointF cropperLB = new PointF(cropperRect.left, cropperRect.bottom);
                if (Mathematics.distance(tPoint, cropperLT) < touchDiapason) {
                    cropperMode = true;
                    currentMode = CROPPER_TRANSFORM_MODE;
                    currentCropperCorner = 0;

                    Log.d("cropperT", "LT ");
                }
                if (Mathematics.distance(tPoint, cropperRT) < touchDiapason) {
                    cropperMode = true;
                    currentMode = CROPPER_TRANSFORM_MODE;
                    currentCropperCorner = 1;

                    Log.d("cropperT", "RT ");
                }
                if (Mathematics.distance(tPoint, cropperRB) < touchDiapason) {
                    cropperMode = true;
                    currentMode = CROPPER_TRANSFORM_MODE;
                    currentCropperCorner = 2;

                    Log.d("cropperT", "RB ");
                }
                if (Mathematics.distance(tPoint, cropperLB) < touchDiapason) {
                    cropperMode = true;
                    currentMode = CROPPER_TRANSFORM_MODE;

                    currentCropperCorner = 3;

                    Log.d("cropperT", "LB ");
                }

                if (currentMode != CROPPER_TRANSFORM_MODE) {
                    Log.d(TAG, "1");

                    PointF cPoint = Mathematics.findClosestPoint(new PointF(cropperRect.left, cropperRect.top), new PointF(cropperRect.right, cropperRect.top), tPoint, true);
                    try {
                        float dist = Mathematics.distance(tPoint, cPoint);

                        if (dist < touchDiapason) {
                            cropperMode = true;
                            currentCropperSide = 0;
                            currentMode = CROPPER_DRAG_MODE;

                            Log.d("cropperT", "T ");
                        }
                    } catch (NullPointerException e) {
                    }


                    cPoint = Mathematics.findClosestPoint(new PointF(cropperRect.right, cropperRect.top), new PointF(cropperRect.right, cropperRect.bottom), tPoint, true);
                    try {
                        float dist = Mathematics.distance(tPoint, cPoint);

                        if (dist < touchDiapason) {
                            cropperMode = true;
                            currentCropperSide = 1;
                            currentMode = CROPPER_DRAG_MODE;

                            Log.d("cropperT", "R ");
                        }
                    } catch (NullPointerException e) {
                    }

                    cPoint = Mathematics.findClosestPoint(new PointF(cropperRect.right, cropperRect.bottom), new PointF(cropperRect.left, cropperRect.bottom), tPoint, true);
                    try {
                        float dist = Mathematics.distance(tPoint, cPoint);


                        if (dist < touchDiapason) {
                            cropperMode = true;
                            currentCropperSide = 2;
                            currentMode = CROPPER_DRAG_MODE;

                            Log.d("cropperT", "B ");
                        }
                    } catch (NullPointerException e) {
                    }

                    cPoint = Mathematics.findClosestPoint(new PointF(cropperRect.left, cropperRect.bottom), new PointF(cropperRect.left, cropperRect.top), tPoint, true);
                    try {
                        float dist = Mathematics.distance(tPoint, cPoint);

                        if (dist < touchDiapason) {
                            cropperMode = true;
                            currentCropperSide = 3;
                            currentMode = CROPPER_DRAG_MODE;

                            Log.d("cropperT", "L ");
                        }
                    } catch (NullPointerException e) {
                    }
                }

                switch (currentMode){
                    case CROPPER_DRAG_MODE:
                        Log.d(TAG, "CROPPER_DRAG_MODE");
                        break;
                    case CROPPER_TRANSFORM_MODE:
                        Log.d(TAG, "CROPPER_TRANSFORM_MODE");
                        break;
                }

                if (!cropperMode) {
                    return false;
                }



                lastCropperTouchPos = tPoint;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (currentMode == CROPPER_TRANSFORM_MODE) {
                    if (currentCropperCorner == 0) {
                        cropperRect.left -= (lastCropperTouchPos.x - tPoint.x);
                        cropperRect.top -= (lastCropperTouchPos.y - tPoint.y);

                        if (cropperRect.left < left) {
                            cropperRect.left = left;
                        }
                        if (cropperRect.top < top) {
                            cropperRect.top = top;
                        }

                        if (cropperRect.width() < minCropperSize) {
                            cropperRect.left = (cropperRect.right - minCropperSize);
                        }
                        if (cropperRect.height() < minCropperSize) {
                            cropperRect.top = (cropperRect.bottom - minCropperSize);
                        }
                        invalidate();

                    }

                    if (currentCropperCorner == 1) {
                        cropperRect.right -= (lastCropperTouchPos.x - tPoint.x);
                        cropperRect.top -= (lastCropperTouchPos.y - tPoint.y);

                        if (cropperRect.right > right) {
                            cropperRect.right = right;
                        }
                        if (cropperRect.top < top) {
                            cropperRect.top = top;
                        }

                        if (cropperRect.width() < minCropperSize) {
                            cropperRect.right = (cropperRect.left + minCropperSize);
                        }
                        if (cropperRect.height() < minCropperSize) {
                            cropperRect.top = (cropperRect.bottom - minCropperSize);
                        }

                        invalidate();

                    }

                    if (currentCropperCorner == 2) {
                        cropperRect.right -= (lastCropperTouchPos.x - tPoint.x);
                        cropperRect.bottom -= (lastCropperTouchPos.y - tPoint.y);

                        if (cropperRect.right > right) {
                            cropperRect.right = right;
                        }
                        if (cropperRect.bottom > bottom) {
                            cropperRect.bottom = bottom;
                        }

                        if (cropperRect.width() < minCropperSize) {
                            cropperRect.right = (cropperRect.left + minCropperSize);
                        }
                        if (cropperRect.height() < minCropperSize) {
                            cropperRect.bottom = (cropperRect.top + minCropperSize);
                        }


                        invalidate();

                    }

                    if (currentCropperCorner == 3) {
                        cropperRect.left -= (lastCropperTouchPos.x - tPoint.x);
                        cropperRect.bottom -= (lastCropperTouchPos.y - tPoint.y);

                        if (cropperRect.left < left) {
                            cropperRect.left = left;
                        }
                        if (cropperRect.bottom > bottom) {
                            cropperRect.bottom = bottom;
                        }

                        if (cropperRect.width() < minCropperSize) {
                            cropperRect.left = (cropperRect.right - minCropperSize);
                        }
                        if (cropperRect.height() < minCropperSize) {
                            cropperRect.bottom = (cropperRect.top + minCropperSize);
                        }

                        invalidate();

                    }
                }
                if (currentMode == CROPPER_DRAG_MODE) {
                    cropperRect.left -= (lastCropperTouchPos.x - tPoint.x);
                    cropperRect.bottom -= (lastCropperTouchPos.y - tPoint.y);
                    cropperRect.right -= (lastCropperTouchPos.x - tPoint.x);
                    cropperRect.top -= (lastCropperTouchPos.y - tPoint.y);

                    if (cropperRect.top < top) {
                        cropperRect.bottom = top + cropperRect.height();
                        cropperRect.top = top;
                    }

                    if (cropperRect.bottom > bottom) {

                        cropperRect.top = bottom - cropperRect.height();
                        cropperRect.bottom = bottom;
                    }
                    if (cropperRect.left < left) {
                        cropperRect.right = left + cropperRect.width();
                        cropperRect.left = left;
                    }

                    if (cropperRect.right > right) {
                        cropperRect.left = right - cropperRect.width();
                        cropperRect.right = right;
                    }

                    invalidate();
                }

                lastCropperTouchPos = tPoint;
            }

        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            currentMode=0;
        }


            return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
//    Public
    public RectF getCropperRect(){
        return cropperRect;
    }

    public void enableGrid(boolean enable){
        isGridEnabled = enable;
        invalidate();
    }

    public void reset(){
            cropperRect = new RectF(left + initialCropperPadding, top + initialCropperPadding, right - initialCropperPadding, bottom - initialCropperPadding);
            invalidate();
    }

}
