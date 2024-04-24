package com.example.funscitech;

import android.content.res.AssetManager;
import android.graphics.Bitmap;


public class OnnxInference {
    public native boolean Init(AssetManager mgr);
    public native boolean inference(Bitmap bitmap, boolean use_gpu);
    static {
        System.loadLibrary("CartoonStyle");
    }
}