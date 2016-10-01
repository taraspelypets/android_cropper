package com.example.cropperview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ResizeThread extends Thread{
    private static final String TAG = "ResizeThread";
    public static final String RESULT = "RESULT";


    public static final int SCALE_FIT = 1;
    public static final int SCALE_FIll = 2;

    private int scaleType;


    public ResizeThread(Bitmap photo, float viewWidth, float viewHeight, int scaleType, Handler resizeHandler) {
        this.photo = photo;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.scaleType = scaleType;
        this.resizeHandler = resizeHandler;
    }

    private Handler resizeHandler;
    private Bitmap photo;
    private float viewWidth;
    private float viewHeight;


    @Override
    public void run() {
        switch (scaleType){
            case SCALE_FIll:
                scaleFill();
                break;
            case SCALE_FIT:
                scaleFit();
                break;
        }
    }

    private void scaleFill(){
        float viewAspectRatio = viewWidth/viewHeight; // if viewAspectRatio>1 view is wide
        // if viewAspectRatio<=1 view is tall(or square, doesn't meter)

        float photoAspectRatio = (float)photo.getWidth()/(float)photo.getHeight(); // if photoAspectRatio>1 photo is in landscape orientation
        // if photoAspectRatio<=1 photo is in portrait orientation(or square, doesn't meter)
        Log.d(TAG, "" + viewAspectRatio + " " + photoAspectRatio);
        Log.d(TAG, "" + viewWidth + " " + viewHeight);

        if(viewAspectRatio>1 && photoAspectRatio>1){ // view - wide, photo - landscape
            float widthDifference = viewAspectRatio - photoAspectRatio;
            if(widthDifference<0){
//                    fit to height
                float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int) dstWidth, (int) viewHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }else{
//                    fit to width
                float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }
        }else if(viewAspectRatio>1 && photoAspectRatio<=1){  // view - wide, photo - portrait
//                fit to width
            float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);

            Bundle bundle = new Bundle();
            bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
            Message message = new Message();
            message.setData(bundle);
            resizeHandler.sendMessage(message);
        }else if(viewAspectRatio<=1 && photoAspectRatio>1){  // view - tall, photo - landscape
//                fit to height
            float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

            Bundle bundle = new Bundle();
            bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)dstWidth, (int)viewHeight, false));
            Message message = new Message();
            message.setData(bundle);
            resizeHandler.sendMessage(message);
        }else if(viewAspectRatio<=1 && photoAspectRatio<=1){  // view - tall, photo - portrait
            float widthDifference = viewAspectRatio - photoAspectRatio;
            if(widthDifference<0){
//                    fit to height
                float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)dstWidth, (int)viewHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }else{
//                    fit to width
                float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }
        }
    }

    private void scaleFit(){


        float viewAspectRatio = viewWidth/viewHeight; // if viewAspectRatio>1 view is wide
        // if viewAspectRatio<=1 view is tall(or square, doesn't meter)

        float photoAspectRatio = (float)photo.getWidth()/(float)photo.getHeight(); // if photoAspectRatio>1 photo is in landscape orientation
        // if photoAspectRatio<=1 photo is in portrait orientation(or square, doesn't meter)
        Log.d("Tag", ""+viewAspectRatio + " " + photoAspectRatio);
        Log.d("Tag", ""+viewWidth + " " + viewHeight);

        if(viewAspectRatio>1 && photoAspectRatio>1){ // view - wide, photo - landscape
            float widthDifference = viewAspectRatio - photoAspectRatio;
            if(widthDifference<0){
//                    fit to height
                float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);
                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }else{
//                    fit to width
                float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)dstWidth, (int)viewHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }
        }else if(viewAspectRatio>1 && photoAspectRatio<=1){  // view - wide, photo - portrait
//                fit to width
            float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

            Bundle bundle = new Bundle();
            bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)dstWidth, (int)viewHeight, false));
            Message message = new Message();
            message.setData(bundle);
            resizeHandler.sendMessage(message);
        }else if(viewAspectRatio<=1 && photoAspectRatio>1){  // view - tall, photo - landscape
//                fit to height
            float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);

            Bundle bundle = new Bundle();
            bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
            Message message = new Message();
            message.setData(bundle);
            resizeHandler.sendMessage(message);
        }else if(viewAspectRatio<=1 && photoAspectRatio<=1){  // view - tall, photo - portrait
            float widthDifference = viewAspectRatio - photoAspectRatio;
            if(widthDifference<0){
//                    fit to height
                float dstHeight = photo.getHeight()/(photo.getWidth()/viewWidth);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)viewWidth, (int)dstHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }else{
//                    fit to width
                float dstWidth = photo.getWidth()/(photo.getHeight()/viewHeight);

                Bundle bundle = new Bundle();
                bundle.putParcelable(RESULT, Bitmap.createScaledBitmap(photo, (int)dstWidth, (int)viewHeight, false));
                Message message = new Message();
                message.setData(bundle);
                resizeHandler.sendMessage(message);
            }
        }

    }
}
