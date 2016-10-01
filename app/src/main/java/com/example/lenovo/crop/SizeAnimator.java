package com.example.lenovo.crop;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;


public class SizeAnimator {
    private View mView;
    private int heightStart;
    private int widthStart;
    private int heightTarget;
    private int widthTarget;

    private int heightDuration;
    private int widthDuration;
    private int heightWait;
    private int widthWait;


    private int defaultHeightDuration = 200;
    private int defaultWidthDuration = 200;
    private int defaultWait =0;

    Animator.AnimatorListener animationListener;

    public AnimatorBuilder animate(View view){
        return new AnimatorBuilder(view);
    }

    class AnimatorBuilder {

        private AnimatorBuilder(View view){
            mView = view;
        }


        public AnimatorBuilder height(int startValue, int targetValue, int wait, int duration){
            heightStart = startValue;
            heightTarget = targetValue;
            heightWait = wait;
            heightDuration = duration;
            return this;
        }

        public AnimatorBuilder height(int startValue, int targetValue){
            return height(startValue, targetValue, defaultWait, defaultHeightDuration);
        }

        public AnimatorBuilder height(int targetValue){
            return height(mView.getHeight(), targetValue, defaultWait, defaultHeightDuration);
        }

        public AnimatorBuilder width(int startValue, int targetValue, int wait, int duration){
            widthStart = startValue;
            widthTarget = targetValue;
            widthWait = wait;
            widthDuration = duration;
            return this;
        }

        public AnimatorBuilder width(int startValue, int targetValue){
            return width(startValue, targetValue, defaultWait, defaultWidthDuration);
        }

        public AnimatorBuilder width(int targetValue){
            return width(mView.getHeight(), targetValue,defaultWait, defaultWidthDuration);
        }

        public AnimatorBuilder setAnimationListener(Animator.AnimatorListener listener){
            animationListener = listener;
            return this;
        }


        public void start(){
            ValueAnimator heightAnimator = ValueAnimator.ofInt(heightStart, heightTarget);
            heightAnimator.setDuration(heightDuration);
            heightAnimator.addUpdateListener(heightListener);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(widthStart, widthTarget);
            widthAnimator.setDuration(widthDuration);
            widthAnimator.addUpdateListener(widthListener);

            ValueAnimator invalidator = ValueAnimator.ofInt(0, 1000);
            int invalidatorDuration;
            if(heightDuration + heightWait > widthDuration + widthWait){
                invalidatorDuration = heightDuration + heightWait;
            }else{
                invalidatorDuration = widthDuration + widthWait;
            }
            invalidator.setDuration(invalidatorDuration);
            invalidator.addUpdateListener(invalidatorListener);
            if(animationListener!=null)
                invalidator.addListener(animationListener);


            AnimatorSet set = new AnimatorSet();
            set.play(heightAnimator).after(heightWait);
            set.play(widthAnimator).after(widthWait);
            set.play(invalidator).with(heightAnimator).with(widthAnimator);
            set.start();
        }

        ValueAnimator.AnimatorUpdateListener heightListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mView.getLayoutParams().height = (int)animation.getAnimatedValue();
                mView.requestLayout();

                Log.d("SizeAnimator", "size " + mView.getLayoutParams().height);
            }
        };

        ValueAnimator.AnimatorUpdateListener widthListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mView.getLayoutParams().width = (int)animation.getAnimatedValue();
                mView.requestLayout();

            }
        };
        ValueAnimator.AnimatorUpdateListener invalidatorListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                mView.invalidate();
            }
        };

    }


}
