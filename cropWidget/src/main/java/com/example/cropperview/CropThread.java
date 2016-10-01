package com.example.cropperview;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Taras Pelypets on 27.09.2016.
 *
 * CropThread cuts a part of image in separated thread and returns it in {@link #resultHandler}
 * !!! WARNING !!! may occur{@link OutOfMemoryError} while cropping very large image.
 *
 */


class CropThread extends Thread{
    private static final String TAG = "CropThread";
    public static final String RESULT = "RESULT";

    private Bitmap unCroppedPhoto;
    private Handler resultHandler;

    private double L;
    private double T;
    private double R;
    private double B;
    private float rotation;

    public CropThread(Bitmap unCroppedPhoto, double l, double t, double r, double b, float rotation, Handler resultHandler) {
        this.unCroppedPhoto = unCroppedPhoto;
        this.resultHandler = resultHandler;
        L = l;
        T = t;
        R = r;
        B = b;
        this.rotation = rotation;
    }



    @Override
    public void run() {

        PointF urLT = new PointF(0, 0);
        PointF urRT = new PointF(unCroppedPhoto.getWidth(), 0);
        PointF urRB = new PointF(unCroppedPhoto.getWidth(), unCroppedPhoto.getHeight());
        PointF urLB = new PointF(0, unCroppedPhoto.getHeight());

        PointF center = new PointF(unCroppedPhoto.getWidth()/2, unCroppedPhoto.getHeight()/2);

        Bitmap fCropBitmap = null;
        float cropWidth = 0;
        float cropHeight = 0;


        if(rotation >= 0f && rotation < 90f) {
            PointF rLT = Mathematics.rotatePoint(center, urLT, rotation);
            PointF rRT = Mathematics.rotatePoint(center, urRT, rotation);
            PointF rRB = Mathematics.rotatePoint(center, urRB, rotation);
            PointF rLB = Mathematics.rotatePoint(center, urLB, rotation);

            float rLeft = rLB.x;
            float rTop = rLT.y;
            float rRight = rRT.x;
            float rBottom = rRB.y;

            float rWidth = rRight - rLeft;
            float rHeight = rBottom - rTop;

            float crLeft = (float) (rLeft + (rWidth * L));
            float crTop = (float) (rTop + (rHeight * T));
            float crRight = (float) (rLeft + (rWidth * R));
            float crBottom = (float) (rTop + (rHeight * B));

            PointF crLT = new PointF(crLeft, crTop);
            PointF crRT = new PointF(crRight, crTop);
            PointF crRB = new PointF(crRight, crBottom);
            PointF crLB = new PointF(crLeft, crBottom);

            cropHeight = crBottom - crTop;
            cropWidth = crRight-crLeft;

            PointF cLT = Mathematics.rotatePoint(center, crLT, -rotation);
            PointF cRT = Mathematics.rotatePoint(center, crRT, -rotation);
            PointF cRB = Mathematics.rotatePoint(center, crRB, -rotation);
            PointF cLB = Mathematics.rotatePoint(center, crLB, -rotation);



            float left = cLT.x;
            float top = cRT.y;
            float right = cRB.x;
            float bottom = cLB.y;


            Log.d("croppp", "" + left + " " + top + " " + right + " " + bottom);


            fCropBitmap = Bitmap.createBitmap(unCroppedPhoto, (int) left, (int) top, (int) (right - left), (int) (bottom - top), null, false);

        }else if(rotation >= 90f && rotation < 180f) {
            PointF rLT = Mathematics.rotatePoint(center, urLT, rotation);
            PointF rRT = Mathematics.rotatePoint(center, urRT, rotation);
            PointF rRB = Mathematics.rotatePoint(center, urRB, rotation);
            PointF rLB = Mathematics.rotatePoint(center, urLB, rotation);

            float rLeft = rRB.x;
            float rTop = rLB.y;
            float rRight = rLT.x;
            float rBottom = rRT.y;

            float rWidth = rRight - rLeft;
            float rHeight = rBottom - rTop;

            float crLeft = (float) (rLeft + (rWidth * L));
            float crTop = (float) (rTop + (rHeight * T));
            float crRight = (float) (rLeft + (rWidth * R));
            float crBottom = (float) (rTop + (rHeight * B));

            PointF crLT = new PointF(crLeft, crTop);
            PointF crRT = new PointF(crRight, crTop);
            PointF crRB = new PointF(crRight, crBottom);
            PointF crLB = new PointF(crLeft, crBottom);

            cropHeight = crBottom - crTop;
            cropWidth = crRight-crLeft;

            PointF cLT = Mathematics.rotatePoint(center, crLT, -rotation);
            PointF cRT = Mathematics.rotatePoint(center, crRT, -rotation);
            PointF cRB = Mathematics.rotatePoint(center, crRB, -rotation);
            PointF cLB = Mathematics.rotatePoint(center, crLB, -rotation);

            float left = cRT.x;
            float top = cRB.y;
            float right = cLB.x;
            float bottom = cLT.y;


            Log.d("croppp", "" + left + " " + top + " " + right + " " + bottom);


            fCropBitmap = Bitmap.createBitmap(unCroppedPhoto, (int) left, (int) top, (int) (right - left), (int) (bottom - top), null, false);

        }else if(rotation >= 180f && rotation < 270f) {
            PointF rLT = Mathematics.rotatePoint(center, urLT, rotation);
            PointF rRT = Mathematics.rotatePoint(center, urRT, rotation);
            PointF rRB = Mathematics.rotatePoint(center, urRB, rotation);
            PointF rLB = Mathematics.rotatePoint(center, urLB, rotation);

            float rLeft = rRT.x;
            float rTop = rRB.y;
            float rRight = rLB.x;
            float rBottom = rLT.y;

            float rWidth = rRight - rLeft;
            float rHeight = rBottom - rTop;

            float crLeft = (float) (rLeft + (rWidth * L));
            float crTop = (float) (rTop + (rHeight * T));
            float crRight = (float) (rLeft + (rWidth * R));
            float crBottom = (float) (rTop + (rHeight * B));

            cropHeight = crBottom - crTop;
            cropWidth = crRight-crLeft;


            PointF crLT = new PointF(crLeft, crTop);
            PointF crRT = new PointF(crRight, crTop);
            PointF crRB = new PointF(crRight, crBottom);
            PointF crLB = new PointF(crLeft, crBottom);

            PointF cLT = Mathematics.rotatePoint(center, crLT, -rotation);
            PointF cRT = Mathematics.rotatePoint(center, crRT, -rotation);
            PointF cRB = Mathematics.rotatePoint(center, crRB, -rotation);
            PointF cLB = Mathematics.rotatePoint(center, crLB, -rotation);

            float left = cRB.x;
            float top = cLB.y;
            float right = cLT.x;
            float bottom = cRT.y;



            Log.d("croppp", "" + left + " " + top + " " + right + " " + bottom);


            fCropBitmap = Bitmap.createBitmap(unCroppedPhoto, (int) left, (int) top, (int) (right - left), (int) (bottom - top), null, false);

        }else if(rotation >= 270f && rotation < 360f) {
            PointF rLT = Mathematics.rotatePoint(center, urLT, rotation);
            PointF rRT = Mathematics.rotatePoint(center, urRT, rotation);
            PointF rRB = Mathematics.rotatePoint(center, urRB, rotation);
            PointF rLB = Mathematics.rotatePoint(center, urLB, rotation);

            float rLeft = rLT.x;
            float rTop = rRT.y;
            float rRight = rRB.x;
            float rBottom = rLB.y;

            float rWidth = rRight - rLeft;
            float rHeight = rBottom - rTop;

            float crLeft = (float) (rLeft + (rWidth * L));
            float crTop = (float) (rTop + (rHeight * T));
            float crRight = (float) (rLeft + (rWidth * R));
            float crBottom = (float) (rTop + (rHeight * B));

            cropHeight = crBottom - crTop;
            cropWidth = crRight-crLeft;

            PointF crLT = new PointF(crLeft, crTop);
            PointF crRT = new PointF(crRight, crTop);
            PointF crRB = new PointF(crRight, crBottom);
            PointF crLB = new PointF(crLeft, crBottom);

            PointF cLT = Mathematics.rotatePoint(center, crLT, -rotation);
            PointF cRT = Mathematics.rotatePoint(center, crRT, -rotation);
            PointF cRB = Mathematics.rotatePoint(center, crRB, -rotation);
            PointF cLB = Mathematics.rotatePoint(center, crLB, -rotation);

            float left = cLB.x;
            float top = cLT.y;
            float right = cRT.x;
            float bottom = cRB.y;



            Log.d("croppp", "" + left + " " + top + " " + right + " " + bottom);


            fCropBitmap = Bitmap.createBitmap(unCroppedPhoto, (int) left, (int) top, (int) (right - left), (int) (bottom - top), null, false);

        }

        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap rotateBitmap = Bitmap.createBitmap(fCropBitmap, 0, 0, fCropBitmap.getWidth(), fCropBitmap.getHeight(), m, false);
        fCropBitmap.recycle();



        Log.d("croppppp", "" + cropWidth + " " + cropHeight);

        float r =rotation;
        int i = 0;
        if(rotation>=90){
            i = (int)(rotation/90);

            r = rotation-(i*90);
        }

        Log.d("croppppp", "" + r );


        int h1 = (int)(cropWidth*Math.cos(Math.toRadians(r))*Math.sin(Math.toRadians(r)));
        int h2 = (int)(cropHeight*Math.cos(Math.toRadians(r))*Math.sin(Math.toRadians(r)));

        Log.d("croppppp", "" + h2 + " " + h1);

        Bitmap sCropBitmap = Bitmap.createBitmap(rotateBitmap, h2, h1, (int)cropWidth, (int)cropHeight, null, false);



        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable(RESULT, sCropBitmap);
            Message message = new Message();
            message.setData(bundle);
            resultHandler.sendMessage(message);
        }catch (Exception e){}



        super.run();
    }
}
