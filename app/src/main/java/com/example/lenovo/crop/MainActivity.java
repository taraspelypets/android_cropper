package com.example.lenovo.crop;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cropperview.CropView;
import com.example.lenovo.crop.Vidgets.WheelView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lenovo on 19.09.2016.
 */
public class MainActivity extends Activity implements WheelView.WheelListener, CropView.CropperResultListener{
    private static int PICK_IMAGE = 748;

    private boolean isPhotoChosen;
    private boolean isPhotoCCropped;
    private Context context = this;
    private RelativeLayout layoutPhotoChoice;
    private RelativeLayout layoutBottomControls;

//    Settings
    private View layoutCloseSettings;
    private ImageButton mButtonShowSettings;
    private ImageButton mButtonHideSettings;
    private View layoutSettings;
    private View layoutSettingsContent;
    private SwitchCompat isGridEnabled;
    private SwitchCompat isAspectRatioFixed;
    private Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
    private EditText mEditTextCompressionRate;

    private Button mButtonNewPhoto;
    private Button mButtonGallery;
    private Button mButtonRandom;
    private ImageButton mButtonStopLoading;

    private ImageButton mButtonConfirm;
    private ImageButton mButtonDeny;


    private Button mButtonCrop;
    private Button mButtonReset;
    private ImageButton mButtonRotate_90;

    private ImageLoader imageLoader;

//    private Cropper10 mCropper;
    private CropView mCropper;
    private WheelView mWheelViewRotation;

    private ProgressBar mProgressBarImageLoading;

    private float rotation = 0;
    private float rotationFactor = 3;

    int displayWidth;
    int displayHeight;

    private ImageSaver imageSaver = new ImageSaver();

    private void setupViews(){
        mButtonNewPhoto = (Button)findViewById(R.id.button_newPhoto);
        mButtonGallery = (Button)findViewById(R.id.button_fromGallery);
        mButtonRandom = (Button)findViewById(R.id.button_Random);

        mButtonCrop = (Button)findViewById(R.id.button_Crop);
        mButtonReset = (Button)findViewById(R.id.button_Reset);

        mButtonConfirm = (ImageButton)findViewById(R.id.imageButton_Confirm);
        mButtonDeny = (ImageButton)findViewById(R.id.imageButton_Deny);
        mButtonConfirm.setAlpha(0f);
        mButtonConfirm.setEnabled(false);
        mButtonDeny.setAlpha(0f);
        mButtonDeny.setEnabled(false);

        mButtonRotate_90 = (ImageButton) findViewById(R.id.imageButton_rotate90);
        mButtonStopLoading = (ImageButton)findViewById(R.id.imageButton_stopLoading);

        mButtonShowSettings = (ImageButton)findViewById(R.id.imageButton_ShowSettings);
        mButtonShowSettings.setOnClickListener(settingsOnClickListener);

        mButtonHideSettings = (ImageButton)findViewById(R.id.imageButton_HideSettings);
        mButtonHideSettings.setOnClickListener(settingsOnClickListener);

        mCropper = (CropView) findViewById(R.id.cropView);
        mCropper.setVisibility(View.VISIBLE);

        mWheelViewRotation = (WheelView)findViewById(R.id.wheelView_Rottation);

        layoutPhotoChoice = (RelativeLayout) findViewById(R.id.layout_photoChoice);
        layoutBottomControls = (RelativeLayout) findViewById(R.id.layout_BottomControls);

        mProgressBarImageLoading = (ProgressBar) findViewById(R.id.progressBar_imageLoading);

        mButtonNewPhoto.setOnClickListener(photoChoiceOnClickListener);
        mButtonGallery.setOnClickListener(photoChoiceOnClickListener);
        mButtonRandom.setOnClickListener(photoChoiceOnClickListener);
        mButtonStopLoading.setOnClickListener(photoChoiceOnClickListener);

        mButtonCrop.setOnClickListener(controlsOnClickListener);
        mButtonReset.setOnClickListener(controlsOnClickListener);

        mButtonCrop.setEnabled(false);
        mButtonReset.setEnabled(false);

        mButtonConfirm.setOnClickListener(confirmationOnClickListener);
        mButtonDeny.setOnClickListener(confirmationOnClickListener);

        mButtonRotate_90.setOnClickListener(controlsOnClickListener);

//        mCropper.setVisibility(View.GONE);
        mCropper.setEnabled(true);
        mCropper.setResultListener(this);

        mWheelViewRotation.setWheelListener(this);

        layoutPhotoChoice.setVisibility(View.VISIBLE);

        mProgressBarImageLoading.setVisibility(View.INVISIBLE);

        mButtonNewPhoto.setAlpha(1);

        layoutSettings = findViewById(R.id.layout_settings);
        layoutSettingsContent = findViewById(R.id.layout_settingsContent);
        layoutSettingsContent.setVisibility(View.GONE);
        layoutCloseSettings = findViewById(R.id.clickableArea);
        layoutCloseSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSettings();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.formats_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(photoFormatOnSelectedListener);

        isGridEnabled = (SwitchCompat)findViewById(R.id.switch_isGridOn);
        isAspectRatioFixed = (SwitchCompat)findViewById(R.id.switch_isAspectRatioFixed);
        mEditTextCompressionRate = (EditText)findViewById(R.id.editText_compressionRate);
        isGridEnabled.setOnCheckedChangeListener(switchesOnCheckedChangeListener);

        mEditTextCompressionRate.setEnabled(false);
        mEditTextCompressionRate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(mEditTextCompressionRate.getText().length()<=0){
                        mEditTextCompressionRate.setText("0");
                    }
                }
            }
        });
        mEditTextCompressionRate.addTextChangedListener(new TextWatcher() {

            CharSequence s;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                this.s = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0){
                }
                try {
                    int number = Integer.parseInt(s.toString());
                    if (number > 100) {
                        mEditTextCompressionRate.setText("100");
                    }
                    if (number < 0) {
                        mEditTextCompressionRate.setText("0");
                    }
                }catch (NumberFormatException e){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private AdapterView.OnItemSelectedListener photoFormatOnSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d("MainAct", "" + position);

            switch (position){
                case 0:
                    format = Bitmap.CompressFormat.PNG;
                    mEditTextCompressionRate.setEnabled(false);
                    break;
                case 1:
                    format = Bitmap.CompressFormat.JPEG;
                    mEditTextCompressionRate.setEnabled(true);

                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private boolean isSettingShown = false;

    private View.OnClickListener settingsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isSettingShown = true;
            float wrappedSize = getResources().getDimension(R.dimen.size_48dp);

            switch (v.getId()){
                case R.id.imageButton_ShowSettings:
                    layoutCloseSettings.setAlpha(0f);
                    layoutCloseSettings.setVisibility(View.VISIBLE);
                    layoutCloseSettings.animate().alpha(1f).setDuration(350).setListener(null) .start();
                    layoutSettings.animate().alpha(1).setStartDelay(0).setDuration(120).start();

                    layoutSettings.setVisibility(View.VISIBLE);
                    new SizeAnimator().animate(layoutSettings)
                            .width((int)wrappedSize, displayWidth, 0, 275)
                            .height((int)wrappedSize,displayHeight/2, 50, 325)
                            .setAnimationListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    layoutSettingsContent.setVisibility(View.VISIBLE);
                                    layoutSettingsContent.setAlpha(0);
                                    layoutSettingsContent.animate().alpha(1).setDuration(175).start();
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) {}
                                @Override
                                public void onAnimationRepeat(Animator animation) {}
                            })
                            .start();
                    break;
                case R.id.imageButton_HideSettings:
                  hideSettings();
                    break;
            }
        }
    };

    private void hideSettings(){
        float wrappedSize = getResources().getDimension(R.dimen.size_48dp);

        layoutCloseSettings.animate().alpha(0f).setDuration(350).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layoutCloseSettings.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
        layoutSettings.animate().alpha(0).setStartDelay(230).setDuration(120).start();

        if(layoutSettingsContent.getAlpha() > 0f){
            layoutSettingsContent.animate().alpha(0).setDuration(175).start();
        }

        new SizeAnimator().animate(layoutSettings)
                .width(layoutSettings.getWidth(), (int)wrappedSize, 75, 275)
                .height(layoutSettings.getHeight(), (int)wrappedSize, 0, 325)
                .setAnimationListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        layoutSettings.setVisibility(View.GONE);
                        isSettingShown = false;
                    }
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                })
                .start();
    }
    private CompoundButton.OnCheckedChangeListener switchesOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.switch_isGridOn:
                    mCropper.enableGrid(isChecked);
                    break;
            }
        }
    };

    private View.OnClickListener photoChoiceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_newPhoto:
                    setButtonsEnabled(false);
                    mButtonRandom.animate().alpha(0).setDuration(150).start();
                    mButtonGallery.animate().alpha(0).setDuration(150).start();
                    rotation = 0;
                    dispatchTakePictureIntent();
                    break;
                case R.id.button_fromGallery:
                    rotation = 0;
                    setButtonsEnabled(false);
                    mButtonNewPhoto.animate().alpha(0).setDuration(150).start();
                    mButtonRandom.animate().alpha(0).setDuration(150).start();

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                    break;
                case R.id.button_Random:
                    if(!isNetworkConnected()) {
                        CharSequence text = getResources().getString(R.string.no_internet);
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } else{
                        rotation = 0;
                        setButtonsEnabled(false);
                        mButtonNewPhoto.animate().alpha(0).setDuration(150).start();
                        mButtonGallery.animate().alpha(0).setDuration(150).start();

                        mProgressBarImageLoading.setVisibility(View.VISIBLE);
                        imageLoader = new ImageLoader();
                        imageLoader.execute("http://lorempixel.com/600/400");
                        mButtonStopLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.imageButton_stopLoading:
                    mButtonStopLoading.setVisibility(View.GONE);
                    setButtonsEnabled(true);
                    mButtonNewPhoto.animate().alpha(1).setDuration(150).start();
                    mButtonGallery.animate().alpha(1).setDuration(150).start();
                    mButtonRandom.animate().alpha(1).setDuration(150).start();
                    mProgressBarImageLoading.setVisibility(View.INVISIBLE  );
                    imageLoader.cancel(true);
                    imageLoader = null;

                    break;
            }
        }
    };

    private View.OnClickListener controlsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_Crop:
                    if(!isPhotoChosen)break;
                    isPhotoCCropped = true;

                    mCropper.crop();
                    layoutBottomControls.animate().translationY(0).setDuration(150).start();
                    mButtonCrop.animate().alpha(0).setDuration(50).start();
                    mButtonReset.animate().alpha(0).setDuration(50).start();

                    mButtonCrop.setEnabled(false);
                    mButtonReset.setEnabled(false);

                    mButtonConfirm.setEnabled(true);
                    mButtonConfirm.setAlpha(0f);
                    mButtonDeny.setEnabled(true);
                    mButtonDeny.setAlpha(0f);

                    mButtonConfirm.animate().alpha(1).setDuration(100).start();
                    mButtonDeny.animate().alpha(1).setDuration(100).start();

                    break;
                case R.id.button_Reset:
                    if(!isPhotoChosen)break;
                    rotation = 0;
//                    TODO
                    mCropper.reset();
                    break;
                case R.id.imageButton_rotate90:
                    if(!isPhotoChosen)break;
                    rotation += 90;
                    mCropper.rotate(rotation);
                    break;
            }
        }
    };

    private View.OnClickListener confirmationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imageButton_Confirm:
                    animHideConfirmationDialog();
                    imageSaver.save();
                    toTheBeginning();
                    isPhotoCCropped = false;
                    break;
                case R.id.imageButton_Deny:
                    animHideConfirmationDialog();
                    mCropper.builtInAnimHide();
                    isPhotoCCropped = false;
                    break;
            }
        }
    };

    private void animHideConfirmationDialog(){

        mButtonConfirm.setEnabled(false);
        mButtonDeny.setEnabled(false);

        mButtonCrop.animate().alpha(1).setDuration(50).start();
        mButtonReset.animate().alpha(1).setDuration(50).start();
        mButtonCrop.setEnabled(true);
        mButtonReset.setEnabled(true);

        mButtonConfirm.animate().alpha(0).setDuration(100).start();
        mButtonDeny.animate().alpha(0).setDuration(100).start();

        layoutBottomControls.animate().translationY(getResources().getDimension(R.dimen.bottom_anim)).start();
    }

    private void setButtonsEnabled(boolean val){
        mButtonNewPhoto.setEnabled(val);
        mButtonGallery.setEnabled(val);
        mButtonRandom.setEnabled(val);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;

        setupViews();
    }
    String mCurrentPhotoPath;


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    Uri photoURI;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO){
                mCropper.setVisibility(View.VISIBLE);
                layoutPhotoChoice.setVisibility(View.GONE);
                try {
                    mCropper.setImage( MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI));
                    mButtonCrop.setEnabled(true);
                    mButtonReset.setEnabled(true);
                    isPhotoChosen = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (requestCode == PICK_IMAGE){
                mCropper.setVisibility(View.VISIBLE);
                layoutPhotoChoice.setVisibility(View.GONE);

                Uri selectedImageUri = data.getData();
                try {
                    mCropper.setImage( MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri));
                    isPhotoChosen = true;
                    mButtonCrop.setEnabled(true);
                    mButtonReset.setEnabled(true);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            setButtonsEnabled(true);
            mButtonNewPhoto.animate().alpha(1).setDuration(150).start();
            mButtonGallery.animate().alpha(1).setDuration(150).start();
            mButtonRandom.animate().alpha(1).setDuration(150).start();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public void onWheelRotated(float value) {
        if(isPhotoChosen){
            Log.d("WheelView", "" + value);
            rotation += value/rotationFactor;
            mCropper.rotate(rotation);
        }
    }


    @Override
    public void onCropperResult(Bitmap bitmap, RectF cropperRect) {
        imageSaver.set(bitmap, format, Integer.parseInt(mEditTextCompressionRate.getText().toString()));
        mCropper.builtInAnimShow(bitmap, cropperRect);
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap>{



        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
                return null;
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(isCancelled())return;
            mProgressBarImageLoading.setVisibility(View.INVISIBLE);
            if(result!=null){
            mCropper.setVisibility(View.VISIBLE);
            layoutPhotoChoice.setVisibility(View.GONE);
            mCropper.setImage(result);
                mButtonCrop.setEnabled(true);
                mButtonReset.setEnabled(true);
            isPhotoChosen = true;

            }else {
                mButtonNewPhoto.animate().alpha(1).setDuration(150).start();
                mButtonGallery.animate().alpha(1).setDuration(150).start();
                mProgressBarImageLoading.setVisibility(View.INVISIBLE);
                setButtonsEnabled(true);
                CharSequence text = getResources().getString(R.string.fail_to_load);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    private boolean toTheBeginning(){
        if(isPhotoChosen){
            mCropper.clear();
            mCropper.setVisibility(View.GONE);
            layoutPhotoChoice.setVisibility(View.VISIBLE);
            mButtonStopLoading.setVisibility(View.GONE);
            mProgressBarImageLoading.setVisibility(View.INVISIBLE);

            rotation = 0;

            mButtonNewPhoto.setAlpha(0);
            mButtonRandom.setAlpha(0);
            mButtonGallery.setAlpha(0);

            mButtonNewPhoto.animate().alpha(1).setDuration(150).start();
            mButtonRandom.animate().alpha(1).setDuration(150).start();
            mButtonGallery.animate().alpha(1).setDuration(150).start();

            mButtonCrop.setEnabled(false);
            mButtonReset.setEnabled(false);
            setButtonsEnabled(true);

        }
        return isPhotoChosen;
    }

    @Override
    public void onBackPressed() {
        if(isSettingShown){
            hideSettings();
        }else if (isPhotoCCropped){
            animHideConfirmationDialog();
            mCropper.builtInAnimHide();
            isPhotoCCropped = false;

        }else if (!toTheBeginning()) super.onBackPressed();
    }
}
