package com.example.cropperview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Taras Pelypets 27.09.2016.
 *
 * CropView combines together all parts of cropper and allows you to manage them.
 * Call {@link #setImage(Bitmap)} to begin. You don't need to resize image before calling, CropView does this itself;
 * In order to obtain cropped image pass {@link CropperResultListener} to {@link #setResultListener(CropperResultListener)}  }
 * You can use build in animation to show cropped image. {@link #builtInAnimShow(Bitmap, RectF)}, {@link #builtInAnimHide(RectF)}
 */
public class CropView extends FrameLayout {



    private static final String TAG = "CropView";

    private float height;
    private float width;


    private Bitmap sourceImage;

    private boolean isSet = false;

//
    private CropperSelectionView cropperSelectionView;
    private CropperImageView cropperImageView;
    private CropperAnimAppearVew cropperAnimAppearView;

    public CropView(Context context) {
        super(context);
        cropperSelectionView = new CropperSelectionView(context);
        cropperImageView = new CropperImageView(context);
        cropperAnimAppearView = new CropperAnimAppearVew(context);

        cropperSelectionView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        cropperImageView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        cropperAnimAppearView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        cropperSelectionView.setEnabled(true);
        cropperImageView.setEnabled(true);
        cropperAnimAppearView.setEnabled(true);

        this.addView(cropperImageView);
        this.addView(cropperSelectionView);
        this.addView(cropperAnimAppearView);

        cropperSelectionView.invalidate();
        cropperImageView.invalidate();
        cropperAnimAppearView.invalidate();

        cropperAnimAppearView.setVisibility(GONE);

    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cropperSelectionView = new CropperSelectionView(context);
        cropperImageView = new CropperImageView(context);
        cropperAnimAppearView = new CropperAnimAppearVew(context);

        cropperSelectionView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        cropperImageView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        cropperAnimAppearView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        cropperSelectionView.setEnabled(true);


        this.addView(cropperImageView);
        this.addView(cropperSelectionView);
        this.addView(cropperAnimAppearView);

        cropperImageView.setEnabled(false);
        cropperSelectionView.setEnabled(false);
        cropperAnimAppearView.setEnabled(false);
    }

    private boolean isCreated = false;
    private void create(){
        if (!isCreated) {

            height = getHeight() - getPaddingTop() - getPaddingBottom();
            width = getWidth() - getPaddingLeft() - getPaddingRight();

            Log.d(TAG, "created w:" + width + " h:" + height);
            isCreated = true;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        create();


        super.onDraw(canvas);
    }

    public void setImage(Bitmap bitmap){
        invalidate();
        create();


        sourceImage = bitmap;

        Log.d(TAG, "" + getWidth() + " " + getHeight());

         ResizeThread resizeThread = new ResizeThread(bitmap, getWidth(), getHeight(), ResizeThread.SCALE_FIll, new Handler(){

             @Override
             public void handleMessage(Message msg) {
                 cropperImageView.setEnabled(true);
                 cropperSelectionView.setEnabled(true);
                 cropperImageView.setImage((Bitmap) msg.getData().getParcelable(ResizeThread.RESULT));
                 isSet = true;
                 super.handleMessage(msg);
             }
         });
        resizeThread.start();
    }

    private Handler cropThreadResultHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                listener.onCropperResult((Bitmap) msg.getData().getParcelable(CropThread.RESULT), cropperSelectionView.getCropperRect());
            }catch (NullPointerException e){
            }
            super.handleMessage(msg);
        }
    };

    public void crop(){
        if (!isEnabled()) return;

        RectF cropperRect = cropperSelectionView.getCropperRect();
        PointF []imageCornerPoints = cropperImageView.getImageCornersPoints();
        double cL = (cropperRect.left - imageCornerPoints[3].x)/(imageCornerPoints[1].x - imageCornerPoints[3].x);
        double cR = (cropperRect.right - imageCornerPoints[3].x)/(imageCornerPoints[1].x - imageCornerPoints[3].x);
        double cT = (cropperRect.top - imageCornerPoints[0].y)/(imageCornerPoints[2].y - imageCornerPoints[0].y);
        double cB = (cropperRect.bottom - imageCornerPoints[0].y)/(imageCornerPoints[2].y - imageCornerPoints[0].y);

        CropThread ct = new CropThread(sourceImage, cL, cT, cR, cB, cropperImageView.getImageRotation(), cropThreadResultHandler);
        ct.start();
    }




    public void builtInAnimShow(Bitmap bitmap, final RectF cropperRect){
        ResizeThread resizeThread = new ResizeThread(bitmap, width, height, ResizeThread.SCALE_FIT, new Handler(){

            @Override
            public void handleMessage(Message msg) {
                cropperImageView.animate().alpha(0).setDuration(200).start();
                cropperSelectionView.animate().alpha(0).setDuration(200).start();


                cropperAnimAppearView.setVisibility(VISIBLE);
                cropperAnimAppearView.setClickable(true);

                cropperAnimAppearView.show((Bitmap) msg.getData().getParcelable(ResizeThread.RESULT), cropperRect);
                super.handleMessage(msg);
            }
        });
        resizeThread.start();

    }

    private CropperResultListener listener;

    public void setResultListener(CropperResultListener listener){
        this.listener = listener;
    }

    public interface CropperResultListener{
        void onCropperResult(Bitmap bitmap, RectF cropperRect);
    }

    public void builtInAnimHide(RectF cropperRect){
        cropperAnimAppearView.hide(cropperRect);
        cropperImageView.animate().alpha(1f).setDuration(200).start();
        cropperSelectionView.animate().alpha(1f).setDuration(200).start();
    }

    public void builtInAnimHide(){
        builtInAnimHide(cropperSelectionView.getCropperRect());
    }
    public void rotate(float deg){
        if(!isSet)return;
        cropperImageView.rotate(deg);
    }

    public void enableGrid(boolean enable){
        cropperSelectionView.enableGrid(enable);
    }

    public void reset(){
        cropperImageView.reset();
        cropperSelectionView.reset();
    }

    public void clear(){
        cropperImageView.clear();
        cropperAnimAppearView.clear();
        cropperAnimAppearView.setClickable(false);
    }

}
