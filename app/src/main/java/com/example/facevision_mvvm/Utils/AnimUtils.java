package com.example.facevision_mvvm.Utils;

import android.animation.ObjectAnimator;
import android.view.View;

public class AnimUtils {


    public  ObjectAnimator fadeIn() {
        ObjectAnimator a  = new ObjectAnimator();
        return a = ObjectAnimator.ofFloat(null, View.ALPHA, 0.2f, 1f);
    }



    public static  ObjectAnimator fadeOut() {
         ObjectAnimator a  = new ObjectAnimator();
         return  a = ObjectAnimator.ofFloat(null, View.ALPHA, 1f, 0.2f);
    }
}
