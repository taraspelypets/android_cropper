package com.example.lenovo.crop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lenovo on 21.09.2016.
 */
public class ImageSaver {


    private Bitmap bitmap;
    Bitmap.CompressFormat format;
    int rate;

    ImageSaver(){
    }

    public ImageSaver set(Bitmap bitmap, Bitmap.CompressFormat format, int rate){
        this.bitmap = bitmap;
        this.format = format;
        this.rate = rate;
        return this;
    }
    public void save(){
        String f;
        if(format == Bitmap.CompressFormat.PNG){
            f = ".png";
        }else {
            f = ".jpg";
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + f;
        try {
            File dir = new File(getDCIM_Directory().getPath() + "/Crop");

            Log.d("quick", "" + dir.mkdir());

            File file = new File(dir.getPath() + "/" + imageFileName);
            FileOutputStream fOs = new FileOutputStream(file);
            bitmap.compress(format, rate, fOs);

            fOs.flush(); // Not really required
            fOs.close(); // do not forget to close the stream
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File getDCIM_Directory(){
//        if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        }
//        else return null;
    }

}
