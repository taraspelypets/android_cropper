# android_cropper


## Usage
1. Place CropView whereware you want as any other View.

2. Call setImage(Bitmap bitmap) and pass your image Bitmap. 

3. In order to retrieve result, create an CropView.CropperResultListener object and assign it to your CropView by calling setResultListener(CropperResultListener listener)
```
cropView = (CropView)findViewById(R.id.cropview);
cropView.setResultListener(new CropperResultListener(){
     void onCropperResult(Bitmap bitmap, RectF cropperRect){
     // Handle result here
     }
});
```
In onCropperResult(Bitmap bitmap, RectF cropperRect) beside bitmap of cropped image, you also receive RectF, which represents selector boundaries of your CropperView. You might want to do something about this, like this cool animation:
![](https://github.com/taraspelypets/android_cropper/blob/master/Screenshots/ezgif-1227913016.gif)

* To crop image, simply call crop().

* To rotate, call rotate(float deg). After rotation image automatically adjusts to fill view:

![](https://github.com/taraspelypets/android_cropper/blob/master/Screenshots/ezgif-1629504541.gif)
