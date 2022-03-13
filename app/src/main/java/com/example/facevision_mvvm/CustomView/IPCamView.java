/*
 * IPCamImageView - IPCamView.java
 * Created by Marcos Calvo García on 14/12/18 16:14.
 * Copyright (c) 2018. All rights reserved.
 *
 * Last modified 14/12/18 16:14.
 */

package com.example.facevision_mvvm.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import com.example.facevision_mvvm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class IPCamView extends androidx.appcompat.widget.AppCompatImageView {

    protected Bitmap bitmap = null;
    protected boolean run = false;
    protected String url = null;
    protected int interval = 0;
    FaceDetector detector;

    public IPCamView(Context context) {
        super(context);
    }

    public IPCamView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public IPCamView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public IPCamView setUrl(String url) {
        this.url = url;
        return this;
    }

    public IPCamView setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public void start() {
        if (url == null || run)
            return;

        run = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    try {
                        bitmap = getBitmapFromURL(url + "?" + System.currentTimeMillis());
                        post(new Runnable() {
                            public void run() {
                               // setImageBitmap(bitmap);
                                detect(bitmap);


                            }
                        });

                        Thread.sleep(interval);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void start(String url) {
        setUrl(url);
        start();
    }

    public void stop() {
        run = false;
    }

    protected Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void init(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IPCamView, defStyleAttr, 0);
            try {
                url = typedArray.getString(R.styleable.IPCamView_url);
                interval = typedArray.getInteger(R.styleable.IPCamView_interval, 1000);
            } finally {
                typedArray.recycle();
            }
        }
        initializeFaceDetector();
    }


    //Initialize Face Detector
    public  void initializeFaceDetector() {
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);


    }

    public  void detect(Bitmap bmp)
    {
        InputImage image = InputImage.fromBitmap(bmp, 0);

        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size()!=0)
                                        {
                                            Face face = faces.get(0);

                                            RectF rect = new RectF(face.getBoundingBox());
                                            Bitmap cropped_face = getCropBitmapByCPU(bmp, rect);

                                            try {
                                                setImageBitmap(cropped_face);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }




                                        }
                                    }
                                })


                        .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Face>> task) {
                                // detector.close();
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(//from  w w  w. ja v  a  2s. c  om
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }


















}

