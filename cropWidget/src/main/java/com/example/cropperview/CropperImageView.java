package com.example.cropperview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateInterpolator;


/**
 * Created by Lenovo on 27.09.2016.
 */
class CropperImageView extends View{

    private static final String TAG = "CropperImageView";
    public CropperImageView(Context context) {
        super(context);
        init();
        Log.d(TAG, "created");
    }

    public CropperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private Paint photoPaint;
    private Paint debugPaint;

    private boolean isCreated = false;
    private boolean isPhotoPending = true;

    private PointF initialCenter;
    private PointF center = new PointF();

    private PointF LT;
    private PointF RT;
    private PointF RB;
    private PointF LB;

    private float height;
    private float width;

    private float top;
    private float left;
    private float bottom;
    private float right;

    private Bitmap photoBitmap;
    private Matrix drawMatrix = new Matrix();

    private float translateX = 0;
    private float translateY = 0;
    private float mRotation = 0;
    private float mMinScale = 1;
    private float currentScale = 1;

    private PointF[] imageCornersPoints;

    private GestureDetector mDetector = new GestureDetector(getContext(), new GestureListener());
    private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());



    private void init(){
        photoPaint = new Paint();
        photoPaint.setStyle(Paint.Style.FILL);
        photoPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        photoPaint.setFilterBitmap(true);
        photoPaint.setDither(true);

        debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setStrokeWidth(10);
        debugPaint.setColor(Color.RED);

    }

    private void create(){
        if(!isCreated) {
            top = getPaddingTop();
            left = getPaddingLeft();
            bottom = getHeight() - getPaddingBottom();
            right = getWidth() - getPaddingRight();

            height = getHeight() - getPaddingTop() - getPaddingBottom();
            width = getWidth() - getPaddingLeft() - getPaddingRight();

            LT = new PointF(left, top);
            RT = new PointF(right, top);
            RB = new PointF(right, bottom);
            LB = new PointF(left, bottom);

            isCreated = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        create();

        if(!isPhotoPending){
            canvas.drawBitmap(photoBitmap, drawMatrix, photoPaint);
//            canvas.drawPoint(center.x, center.y, debugPaint);
//            drawRect(canvas, photoBitmap.getWidth(), photoBitmap.getHeight(), center, 0, debugPaint);

            checkCorners();
        }else {
            canvas.drawColor(Color.TRANSPARENT);
        }

        super.onDraw(canvas);
    }



    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float lastFocusX;
        float lastFocusY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();
            return true;

        }



        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            //Zoom focus is where the fingers are centered,

//            transformationMatrix.postScale(mScaleFactor, mScaleFactor);

/* Adding focus shift to allow for scrolling with two pointers down. Remove it to skip this functionality. This could be done in fewer lines, but for clarity I do it this way here */
            //Edited after comment by chochim
            float focusShiftX = focusX - lastFocusX;
            float focusShiftY = focusY - lastFocusY;

            int correction;
            boolean b = false;
            boolean[] corners = checkCorners();
//            for (int i = 0; i < corners.length; i++) {
//                if (corners[i]) {
//                    b = true;
//                    break;
//                }
//            }
            for (boolean c:corners) {
                if(c) {
                    b = true;
                    break;
                }
            }

            if (b) correction = 2;
            else correction = 1;

            translateX += -focusX / correction;
            translateY += -focusY / correction;

            translateX += (focusX + focusShiftX) / correction;
            translateY += (focusY + focusShiftY) / correction;

            currentScale *= detector.getScaleFactor();

            lastFocusX = focusX;
            lastFocusY = focusY;

            if (xAnimator != null && xAnimator.isRunning()) {
                xAnimator.cancel();
            }
            if (yAnimator != null && yAnimator.isRunning()) {
                yAnimator.cancel();

            }
            if (scaleAnimator != null && scaleAnimator.isRunning()) {
                scaleAnimator.cancel();

            }
            if (invalidate != null) {
                invalidate.cancel();

            }
            invalidate = ValueAnimator.ofInt(-0, 100);
            invalidate.setDuration(100);
            invalidate.addUpdateListener(invalidateAnimation);

            invalidate.start();
            return true;
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled())return false;
        if(isPhotoPending)return false;


        mDetector.onTouchEvent(event);

        mScaleDetector.onTouchEvent(event);


        if(event.getAction() ==MotionEvent.ACTION_UP){
            if(currentScale<mMinScale) {
                if(scaleAnimator.isRunning()){
                    scaleAnimator.cancel();
                }

                scaleAnimator = ValueAnimator.ofFloat(currentScale, mMinScale);
                scaleAnimator.setDuration(200);
                scaleAnimator.addUpdateListener(scaleUpdateListener);
                scaleAnimator.setInterpolator(new AccelerateInterpolator());
                scaleAnimator.start();


                float x = center.x - ((left+right)/2);
                float y = center.y - ((top+bottom)/2);

                startXAnim(x);
                startYAnim(y);


                if (invalidate != null) {
                    invalidate.cancel();
                }
                invalidate = ValueAnimator.ofInt(0, 300);
                invalidate.setDuration(200);
                invalidate.addUpdateListener(invalidateAnimation);


                invalidate.start();

            }else {

                if((mRotation%90)==0){

                    boolean[] sides = checkSides();
                    int numOfSides = 0;
                    for (boolean b : sides) {
                        if (b) numOfSides++;
                    }

                    if(numOfSides == 1 ||numOfSides == 2){
                        if(sides[0]){
                            float diff = imageCornersPoints[0].y-top;
                            startYAnim(diff);
                        }
                        if(sides[1]){
                            float diff = right- imageCornersPoints[2].x;
                            startXAnim(-diff);
                        }
                        if(sides[2]){
                            float diff = bottom- imageCornersPoints[3].y;
                            startYAnim(-diff);

                        }
                        if(sides[3]){
                            float diff = imageCornersPoints[3].x-left;
                            startXAnim(diff);
                        }


                    }


                }else {


                    boolean[] corners = checkCorners();
                    int numOfCorners = 0;
                    for (boolean b : corners) {
                        if (b) numOfCorners++;
                    }

                    Log.d("trans", "" + numOfCorners);

                    if (numOfCorners == 1) {
                        int c = 0;
                        for (int i = 0; 1 < corners.length; i++) {
                            if (corners[i]) {
                                c = i;
                                break;
                            }
                        }
                        if (c == 0) {
                            Log.d("trans", "c0");
                            PointF cp = Mathematics.findClosestPoint(imageCornersPoints[0], imageCornersPoints[1], RT, false);
                            float x = RT.x - cp.x;
                            float y = cp.y - RT.y;
                            startXAnim(-x);
                            startYAnim(y);

                        }
                        if (c == 1) {
                            Log.d("trans", "c1");
                            PointF cp = Mathematics.findClosestPoint(imageCornersPoints[1], imageCornersPoints[2], RB, false);
                            float x = RB.x - cp.x;
                            float y = RB.y - cp.y;
                            startXAnim(-x);
                            startYAnim(-y);
                        }
                        if (c == 2) {
                            Log.d("trans", "c2");
                            PointF cp = Mathematics.findClosestPoint(imageCornersPoints[2], imageCornersPoints[3], LB, false);
                            float x = cp.x - LB.x;
                            float y = LB.y - cp.y;
                            startXAnim(x);
                            startYAnim(-y);
                        }
                        if (c == 3) {
                            Log.d("trans", "c3");
                            PointF cp = Mathematics.findClosestPoint(imageCornersPoints[3], imageCornersPoints[0], LT, false);
                            float x = cp.x - LT.x;
                            float y = cp.y - LT.y;
                            startXAnim(x);
                            startYAnim(y);
                        }


                    }
                    if (numOfCorners == 2) {

                        if (corners[0] && corners[1]) {
                            float rotation;
                            if(mRotation>90){
                                rotation = mRotation%90;
                            }else{
                                rotation = mRotation;
                            }
                            float r1 = (float) (height * Math.sin(Math.toRadians(rotation)));
                            float r2 = (float) (height * Math.cos(Math.toRadians(rotation)));

                            PointF[] points = Mathematics.getPointsOfIntersection(RT, r1, RB, r2);
                            Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);

                            PointF p;
                            if (points[0].x > points[1].x) {
                                p = points[0];
                            } else {
                                p = points[1];
                            }

                            float x = imageCornersPoints[1].x - p.x;
                            float y = imageCornersPoints[1].y - p.y;

                            startXAnim(x);
                            startYAnim(y);
                        }

                        if (corners[1] && corners[2]) {

                            float rotation;
                            if(mRotation>90){
                                rotation = mRotation%90;
                            }else{
                                rotation = mRotation;
                            }

                            float r1 = (float) (width * Math.sin(Math.toRadians(rotation)));
                            float r2 = (float) (width * Math.cos(Math.toRadians(rotation)));

                            PointF[] points = Mathematics.getPointsOfIntersection(RB, r1, LB, r2);
                            Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                            PointF p;
                            if (points[0].y > points[1].y) {
                                p = points[0];
                            } else {
                                p = points[1];
                            }

                            float x = imageCornersPoints[2].x - p.x;
                            float y = p.y - imageCornersPoints[2].y;

                            startXAnim(x);
                            startYAnim(-y);
                        }
                        if (corners[2] && corners[3]) {
                            float rotation;
                            if(mRotation>90){
                                rotation = mRotation%90;
                            }else{
                                rotation = mRotation;
                            }
                            float r1 = (float) (height * Math.sin(Math.toRadians(rotation)));
                            float r2 = (float) (height * Math.cos(Math.toRadians(rotation)));

                            PointF[] points = Mathematics.getPointsOfIntersection(LB, r1, LT, r2);
                            Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                            PointF p;
                            if (points[0].x < points[1].x) {
                                p = points[0];
                            } else {
                                p = points[1];
                            }

                            float x = imageCornersPoints[3].x - p.x;
                            float y = imageCornersPoints[3].y - p.y;

                            startXAnim(x);
                            startYAnim(y);
                        }
                        if (corners[3] && corners[0]) {
                            float rotation;
                            if(mRotation>90){
                                rotation = mRotation%90;
                            }else{
                                rotation = mRotation;
                            }
                            float r1 = (float) (width * Math.sin(Math.toRadians(rotation)));
                            float r2 = (float) (width * Math.cos(Math.toRadians(rotation)));

                            PointF[] points = Mathematics.getPointsOfIntersection(LT, r1, RT, r2);
                            Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                            PointF p;
                            if (points[0].y < points[1].y) {
                                p = points[0];
                            } else {
                                p = points[1];
                            }

                            float x = imageCornersPoints[0].x - p.x;
                            float y = imageCornersPoints[0].y - p.y;

                            startXAnim(x);
                            startYAnim(y);
                        }
                    }
                }
            }



        }

        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            int correction;
            boolean b = false;
            boolean[] corners = checkCorners();
            for (int i = 0; i < corners.length; i++) {
                if (corners[i]) {
                    b = true;
                    break;
                }
            }

            if (b) correction = 2;
            else correction = 1;


            translateX += -distanceX / correction;
            translateY += -distanceY / correction;


            if (invalidate != null) {
                invalidate.cancel();
            }
            invalidate = ValueAnimator.ofInt(-0, 100);
            invalidate.setDuration(100);
            invalidate.addUpdateListener(invalidateAnimation);

            invalidate.start();
            return true;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////


    ValueAnimator invalidate;

    ValueAnimator xAnimator = new ValueAnimator();
    ValueAnimator yAnimator = new ValueAnimator();
    ValueAnimator scaleAnimator = new ValueAnimator();

    private ValueAnimator.AnimatorUpdateListener invalidateAnimation = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            Matrix transformationMatrix = new Matrix();

//            transformationMatrix.postScale(mScaleFactor, mScaleFactor);
            center = new PointF((width / 2) + translateX, (height / 2) + translateY);
            transformationMatrix.postTranslate(width / 2 - initialCenter.x, height / 2 - initialCenter.y);
            transformationMatrix.postTranslate(translateX, translateY);
            transformationMatrix.postScale(currentScale, currentScale, center.x, center.y);
            transformationMatrix.postRotate(mRotation, center.x, center.y);

            imageCornersPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), center, currentScale, mRotation);

            drawMatrix.reset();
            drawMatrix.postConcat(transformationMatrix);

            invalidate();
        }
    };

        private ValueAnimator.AnimatorUpdateListener scaleUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentScale = (float)animation.getAnimatedValue();
            }
        };

        private ValueAnimator.AnimatorUpdateListener xUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translateX = (float)animation.getAnimatedValue();
            }
        };

        private ValueAnimator.AnimatorUpdateListener yUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translateY = (float)animation.getAnimatedValue();
            }
        };

        private void startXAnim(float value){
            if(xAnimator.isRunning()){
                xAnimator.cancel();
            }
            xAnimator = ValueAnimator.ofFloat(translateX, translateX-value);
            xAnimator.setDuration(200);
            xAnimator.setInterpolator(new AccelerateInterpolator());
            xAnimator.addUpdateListener(xUpdateListener);

            xAnimator.start();

            if (invalidate != null) {
                invalidate.cancel();
            }
            invalidate = ValueAnimator.ofInt(0, 300);
            invalidate.setDuration(300);
            invalidate.addUpdateListener(invalidateAnimation);


            invalidate.start();
        }

        private void startYAnim(float value){
            if(yAnimator.isRunning()){
                yAnimator.cancel();
            }
            yAnimator = ValueAnimator.ofFloat(translateY, translateY-value);
            yAnimator.setDuration(200);
            yAnimator.setInterpolator(new AccelerateInterpolator());
            yAnimator.addUpdateListener(yUpdateListener);

            yAnimator.start();

            if (invalidate != null) {
                invalidate.cancel();
            }
            invalidate = ValueAnimator.ofInt(0, 300);
            invalidate.setDuration(300);
            invalidate.addUpdateListener(invalidateAnimation);


            invalidate.start();
        }

    ////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////
    ///////////////////////////////////
    ///////////////////////////////////////


    private boolean[] checkSides(){
        boolean[] result = new boolean[4];
        if(LT.y< imageCornersPoints[0].y){
            result[0] = true;
        }
        if(LT.x< imageCornersPoints[0].x){
            result[3] = true;
        }
        if(RB.y> imageCornersPoints[2].y){
            result[2] = true;
        }
        if(RB.x> imageCornersPoints[2].x){
            result[1] = true;
        }

        return result;

    }

    private boolean[] checkCorners() {
        boolean[] result = new boolean[4];
        PointF side0 = Mathematics.findClosestPoint(imageCornersPoints[0], imageCornersPoints[1], RT, false);
        try {
            if (side0.x < RT.x && side0.y > RT.y) {
                result[0] = true;
            } else {
                result[0] = false;
            }
        } catch (NullPointerException e) {
            result[0] = true;
        }

        try {
            PointF side1 = Mathematics.findClosestPoint(imageCornersPoints[1], imageCornersPoints[2], RB, false);
            if (side1.x < RB.x && side1.y < RB.y) {
                result[1] = true;
            } else {
                result[1] = false;
            }
        } catch (NullPointerException e) {
            result[1] = true;
        }


        try {
            PointF side2 = Mathematics.findClosestPoint(imageCornersPoints[2], imageCornersPoints[3], LB, false);
            if (side2.x > LB.x && side2.y < LB.y) {
                result[2] = true;
            } else {
                result[2] = false;
            }
        } catch (NullPointerException e) {
            result[2] = true;
        }

        try {
            PointF side3 = Mathematics.findClosestPoint(imageCornersPoints[3], imageCornersPoints[0], LT, false);

            if (side3.x > LT.x && side3.y > LT.y) {
                result[3] = true;
            } else {
                result[3] = false;
            }
        } catch (NullPointerException e) {
            result[3] = true;
        }

        Log.d("check"," " + result[0] + " " + result[1] + " " +result[2] + " " +result[3]);

        return result;

    }

    private PointF[] getRect(float sourceWidth, float sourceHeight, PointF center, float scale, float deg){

        PointF unRotatedLT = new PointF(center.x-sourceWidth/2*scale, center.y-sourceHeight/2*scale);
        PointF unRotatedRT = new PointF(center.x+sourceWidth/2*scale, center.y-sourceHeight/2*scale);
        PointF unRotatedLB = new PointF(center.x-sourceWidth/2*scale, center.y+sourceHeight/2*scale);
        PointF unRotatedRB = new PointF(center.x+sourceWidth/2*scale, center.y+sourceHeight/2*scale);


        PointF rotatedLT = Mathematics.rotatePoint(center, unRotatedLT, deg);
        PointF rotatedRT = Mathematics.rotatePoint(center, unRotatedRT, deg);
        PointF rotatedLB = Mathematics.rotatePoint(center, unRotatedLB, deg);
        PointF rotatedRB = Mathematics.rotatePoint(center, unRotatedRB, deg);


        PointF[] points = new PointF[4];
        points[0] = rotatedLT;
        points[1] = rotatedRT;
        points[2] = rotatedRB;
        points[3] = rotatedLB;

        Log.d("rot", "" + mRotation);
        if(mRotation >=0 && mRotation <90){
            Log.d("rot", "0 - 90");
        }
        if(mRotation >=90 && mRotation <180){
            Log.d("rot", "90 - 180");
            PointF[] bufferRect = new PointF[4];
            for(int i = 0; i<bufferRect.length; i++){
                bufferRect[i] = points[i];
            }
            points[0] = bufferRect[3];
            points[1] = bufferRect[0];
            points[2] = bufferRect[1];
            points[3] = bufferRect[2];
        }
        if(mRotation >=180 && mRotation <270){
            Log.d("rot", "180 - 270");
            PointF[] bufferRect = new PointF[4];
            for(int i = 0; i<bufferRect.length; i++){
                bufferRect[i] = points[i];
            }
            points[0] = bufferRect[2];
            points[1] = bufferRect[3];
            points[2] = bufferRect[0];
            points[3] = bufferRect[1];
        }
        if(mRotation >=270 && mRotation <360){
            Log.d("rot", "270 - 0");
            PointF[] bufferRect = new PointF[4];
            for(int i = 0; i<bufferRect.length; i++){
                bufferRect[i] = points[i];
            }
            points[0] = bufferRect[1];
            points[1] = bufferRect[2];
            points[2] = bufferRect[3];
            points[3] = bufferRect[0];
        }

        return points;
    }

    private void drawPath(Canvas canvas, PointF[] points, Paint paint){
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);

        for(int i = 1; i<points.length;i++){
            path.lineTo(points[i].x, points[i].y);

        }
        path.lineTo(points[0].x, points[0].y);
        canvas.drawPath(path, paint);
    }

    private float calculateMinZoom(){

        if(mRotation%180 == 0){
            return 1f;
        }else if(mRotation%90 == 0) {
            if(photoBitmap.getHeight()/photoBitmap.getWidth()<1){
                float f = width/photoBitmap.getHeight();
                return f;

            }else{
                float f = height/photoBitmap.getWidth();
                return f;
            }

        }else if((mRotation>0 && mRotation<90)||(mRotation>180 && mRotation<270)) {
            if(photoBitmap.getHeight()/photoBitmap.getWidth()>=1) {
                PointF[] rotatedUnscaledPhotoPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), new PointF(width / 2, height / 2), 1, mRotation);

                PointF C = Mathematics.findCrossingPoint(new PointF(width / 2, height / 2), new PointF(left, top), rotatedUnscaledPhotoPoints[0], rotatedUnscaledPhotoPoints[3]);

                float d1 = Mathematics.distance(new PointF(width / 2, height / 2), new PointF(left, top));
                float d2 = Mathematics.distance(new PointF(width / 2, height / 2), C);
//        TODO


                Log.d("alz", "" + d1 / d2);
                return d1 / d2;
            }else{
                PointF[] rotatedUnscaledPhotoPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), new PointF(width / 2, height / 2), 1, mRotation);

                PointF C = Mathematics.findCrossingPoint(new PointF(width / 2, height / 2), new PointF(right, top), rotatedUnscaledPhotoPoints[0], rotatedUnscaledPhotoPoints[1]);

                float d1 = Mathematics.distance(new PointF(width / 2, height / 2), new PointF(left, top));
                float d2 = Mathematics.distance(new PointF(width / 2, height / 2), C);


                Log.d("alz", "" + d1 / d2);
                return d1 / d2;
            }
        }else {
            if(photoBitmap.getHeight()/photoBitmap.getWidth()<1) {
                PointF[] rotatedUnscaledPhotoPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), new PointF(width / 2, height / 2), 1, mRotation);

                PointF C = Mathematics.findCrossingPoint(new PointF(width / 2, height / 2), new PointF(left, top), rotatedUnscaledPhotoPoints[0], rotatedUnscaledPhotoPoints[3]);

                float d1 = Mathematics.distance(new PointF(width / 2, height / 2), new PointF(left, top));
                float d2 = Mathematics.distance(new PointF(width / 2, height / 2), C);
//        TODO


                Log.d("alz", "" + d1 / d2);
                return d1 / d2;
            }else{
                PointF[] rotatedUnscaledPhotoPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), new PointF(width / 2, height / 2), 1, mRotation);

                PointF C = Mathematics.findCrossingPoint(new PointF(width / 2, height / 2), new PointF(right, top), rotatedUnscaledPhotoPoints[0], rotatedUnscaledPhotoPoints[1]);

                float d1 = Mathematics.distance(new PointF(width / 2, height / 2), new PointF(left, top));
                float d2 = Mathematics.distance(new PointF(width / 2, height / 2), C);


                Log.d("alz", "" + d1 / d2);
                return d1 / d2;
            }
        }
    }

//    Public methods
    public void setImage(Bitmap bitmap){
        photoBitmap = bitmap;
        isPhotoPending = false;
        // set initial position
        initialCenter = new PointF(photoBitmap.getWidth()/2, photoBitmap.getHeight()/2);
        translateX = 0;
        translateY = 0;
        center = new PointF(width / 2, height / 2);
        mMinScale = calculateMinZoom();
        currentScale = mMinScale;

        imageCornersPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), center, currentScale, mRotation);
//set matrix
        drawMatrix.reset();
        drawMatrix.postTranslate(width / 2 - initialCenter.x, height / 2 - initialCenter.y);
        drawMatrix.postScale(mMinScale, mMinScale, center.x, center.y);
        drawMatrix.postRotate(mRotation, center.x, center.y);

        checkCorners();
    invalidate();
}

    public void rotate(float deg){
        if(isPhotoPending)return;
        if(!isEnabled())return;

        mRotation = 0;
        if(deg>=0){
            if(deg>=360){
                mRotation += deg%360;
            }else{
                mRotation += deg;
            }
        }else{
            deg = -deg;
            if(deg>=360){
                mRotation += deg%360;
            }else{
                mRotation += deg;
            }
            mRotation = 360 - mRotation;
        }

        mMinScale = calculateMinZoom();

        if (invalidate != null) {
            invalidate.cancel();
        }
        invalidate = ValueAnimator.ofInt(0, 300);
        invalidate.setDuration(100);
        invalidate.addUpdateListener(invalidateAnimation);

        invalidate.start();


        if(currentScale<mMinScale) {
            if(scaleAnimator.isRunning()){
                scaleAnimator.cancel();
            }

            scaleAnimator = ValueAnimator.ofFloat(currentScale, mMinScale);
            scaleAnimator.setDuration(200);
            scaleAnimator.addUpdateListener(scaleUpdateListener);
            scaleAnimator.setInterpolator(new AccelerateInterpolator());
            scaleAnimator.start();


            float x = center.x - ((left+right)/2);
            float y = center.y - ((top+bottom)/2);

            startXAnim(x);
            startYAnim(y);


            if (invalidate != null) {
                invalidate.cancel();
            }
            invalidate = ValueAnimator.ofInt(0, 300);
            invalidate.setDuration(200);
            invalidate.addUpdateListener(invalidateAnimation);


            invalidate.start();

        }else {

            if((mRotation%90)==0){

                boolean[] sides = checkSides();
                int numOfSides = 0;
                for (boolean b : sides) {
                    if (b) numOfSides++;
                }

                if(numOfSides == 1 ||numOfSides == 2){
                    if(sides[0]){
                        float diff = imageCornersPoints[0].y-top;
                        startYAnim(diff);
                    }
                    if(sides[1]){
                        float diff = right- imageCornersPoints[2].x;
                        startXAnim(-diff);
                    }
                    if(sides[2]){
                        float diff = bottom- imageCornersPoints[3].y;
                        startYAnim(-diff);

                    }
                    if(sides[3]){
                        float diff = imageCornersPoints[3].x-left;
                        startXAnim(diff);
                    }


                }


            }else {


                boolean[] corners = checkCorners();
                int numOfCorners = 0;
                for (boolean b : corners) {
                    if (b) numOfCorners++;
                }

                Log.d("trans", "" + numOfCorners);

                if (numOfCorners == 1) {
                    int c = 0;
                    for (int i = 0; 1 < corners.length; i++) {
                        if (corners[i]) {
                            c = i;
                            break;
                        }
                    }
                    if (c == 0) {
                        Log.d("trans", "c0");
                        PointF cp = Mathematics.findClosestPoint(imageCornersPoints[0], imageCornersPoints[1], RT, false);
                        float x = RT.x - cp.x;
                        float y = cp.y - RT.y;
                        startXAnim(-x);
                        startYAnim(y);

                    }
                    if (c == 1) {
                        Log.d("trans", "c1");
                        PointF cp = Mathematics.findClosestPoint(imageCornersPoints[1], imageCornersPoints[2], RB, false);
                        float x = RB.x - cp.x;
                        float y = RB.y - cp.y;
                        startXAnim(-x);
                        startYAnim(-y);
                    }
                    if (c == 2) {
                        Log.d("trans", "c2");
                        PointF cp = Mathematics.findClosestPoint(imageCornersPoints[2], imageCornersPoints[3], LB, false);
                        float x = cp.x - LB.x;
                        float y = LB.y - cp.y;
                        startXAnim(x);
                        startYAnim(-y);
                    }
                    if (c == 3) {
                        Log.d("trans", "c3");
                        PointF cp = Mathematics.findClosestPoint(imageCornersPoints[3], imageCornersPoints[0], LT, false);
                        float x = cp.x - LT.x;
                        float y = cp.y - LT.y;
                        startXAnim(x);
                        startYAnim(y);
                    }


                }
                if (numOfCorners == 2) {

                    if (corners[0] && corners[1]) {
                        float rotation;
                        if(mRotation>90){
                            rotation = mRotation%90;
                        }else{
                            rotation = mRotation;
                        }
                        float r1 = (float) (height * Math.sin(Math.toRadians(rotation)));
                        float r2 = (float) (height * Math.cos(Math.toRadians(rotation)));

                        PointF[] points = Mathematics.getPointsOfIntersection(RT, r1, RB, r2);
                        Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);

                        PointF p;
                        if (points[0].x > points[1].x) {
                            p = points[0];
                        } else {
                            p = points[1];
                        }

                        float x = imageCornersPoints[1].x - p.x;
                        float y = imageCornersPoints[1].y - p.y;

                        startXAnim(x);
                        startYAnim(y);
                    }

                    if (corners[1] && corners[2]) {

                        float rotation;
                        if(mRotation>90){
                            rotation = mRotation%90;
                        }else{
                            rotation = mRotation;
                        }

                        float r1 = (float) (width * Math.sin(Math.toRadians(rotation)));
                        float r2 = (float) (width * Math.cos(Math.toRadians(rotation)));

                        PointF[] points = Mathematics.getPointsOfIntersection(RB, r1, LB, r2);
                        Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                        PointF p;
                        if (points[0].y > points[1].y) {
                            p = points[0];
                        } else {
                            p = points[1];
                        }

                        float x = imageCornersPoints[2].x - p.x;
                        float y = p.y - imageCornersPoints[2].y;

                        startXAnim(x);
                        startYAnim(-y);
                    }
                    if (corners[2] && corners[3]) {
                        float rotation;
                        if(mRotation>90){
                            rotation = mRotation%90;
                        }else{
                            rotation = mRotation;
                        }
                        float r1 = (float) (height * Math.sin(Math.toRadians(rotation)));
                        float r2 = (float) (height * Math.cos(Math.toRadians(rotation)));

                        PointF[] points = Mathematics.getPointsOfIntersection(LB, r1, LT, r2);
                        Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                        PointF p;
                        if (points[0].x < points[1].x) {
                            p = points[0];
                        } else {
                            p = points[1];
                        }

                        float x = imageCornersPoints[3].x - p.x;
                        float y = imageCornersPoints[3].y - p.y;

                        startXAnim(x);
                        startYAnim(y);
                    }
                    if (corners[3] && corners[0]) {
                        float rotation;
                        if(mRotation>90){
                            rotation = mRotation%90;
                        }else{
                            rotation = mRotation;
                        }
                        float r1 = (float) (width * Math.sin(Math.toRadians(rotation)));
                        float r2 = (float) (width * Math.cos(Math.toRadians(rotation)));

                        PointF[] points = Mathematics.getPointsOfIntersection(LT, r1, RT, r2);
                        Log.d("error", " "+ points[0].x + " "+ points[0].y + " "+ points[1].x + " "+ points[0].y);
                        PointF p;
                        if (points[0].y < points[1].y) {
                            p = points[0];
                        } else {
                            p = points[1];
                        }

                        float x = imageCornersPoints[0].x - p.x;
                        float y = imageCornersPoints[0].y - p.y;

                        startXAnim(x);
                        startYAnim(y);
                    }


                }
            }
        }


    }

    public PointF[] getImageCornersPoints(){
        return imageCornersPoints;
    }

    public float getImageRotation(){
        return mRotation;
    }

    public void reset(){
        try {
            initialCenter = new PointF(photoBitmap.getWidth() / 2, photoBitmap.getHeight() / 2);

            isPhotoPending = false;

            mRotation = 0;
            translateX = 0;
            translateY = 0;
            center = new PointF(width / 2, height / 2);
            mMinScale = calculateMinZoom();
            currentScale = mMinScale;

            imageCornersPoints = getRect(photoBitmap.getWidth(), photoBitmap.getHeight(), center, currentScale, mRotation);

            drawMatrix.reset();
            drawMatrix.postTranslate(width / 2 - initialCenter.x, height / 2 - initialCenter.y);
            drawMatrix.postScale(mMinScale, mMinScale, center.x, center.y);
            drawMatrix.postRotate(mRotation, center.x, center.y);

            invalidate();
        }catch (Exception e){
        }
    }

    public void clear(){
        mRotation = 0;
        photoBitmap = null;
        isPhotoPending = true;
        invalidate();
    }



}

