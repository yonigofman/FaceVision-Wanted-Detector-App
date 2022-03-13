package com.example.facevision_mvvm.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.*;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.facevision_mvvm.Fragments.FaceFragments.AccuracyFragment;
import com.example.facevision_mvvm.Fragments.FaceFragments.AddWantedFragment;
import com.example.facevision_mvvm.Models.Reco.SimilarityClassifier;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.Utils.FaceRecFragmentsPagerAdapter;
import com.example.facevision_mvvm.ViewModels.FaceRecViewModel;
import com.example.facevision_mvvm.ViewModels.WantedViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class FaceRecFragment extends Fragment {

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    Button add_face;
    FaceDetector detector;
    PreviewView previewView;
    CameraSelector cameraSelector;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Interpreter tfLite;
    ImageView face_preview;
    Context context = getContext();

    boolean flipX = false;
    boolean start = true;


    ViewPager viewPager;
    PagerAdapter pagerAdapter;


    private final HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces



    int[] intValues;
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;

    float[][] embeedings;
    float[][] emb;

    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model


    FaceRecViewModel viewModel;
    WantedViewModel wantedViewModel;

    String modelFile = "mobile_face_net.tflite"; //model name

    public FaceRecFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_face_rec, container, false);

        previewView = view.findViewById(R.id.previewView);
        face_preview = view.findViewById(R.id.face_preview);
        add_face = view.findViewById(R.id.add_face_button);
        viewPager = view.findViewById(R.id.viewPager);


        List<Fragment> fragmentList = new ArrayList<>();

        fragmentList.add(new AccuracyFragment());
        fragmentList.add(new AddWantedFragment());

        pagerAdapter = new FaceRecFragmentsPagerAdapter(getChildFragmentManager(), fragmentList);
        viewPager.setAdapter(pagerAdapter);


        add_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAddFace(); //TODO: Add face TEST
            }
        });


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( //Method of Fragment
                    new String[]{Manifest.permission.CAMERA}, 100
            );
        }



        Gson gson = new Gson();



        FirebaseMessaging.getInstance().setAutoInitEnabled(true);





        wantedViewModel = new ViewModelProvider(getActivity()).get(WantedViewModel.class);

        wantedViewModel.getWantedList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Wanted>>() {
            @Override
            public void onChanged(ArrayList<Wanted> wanteds) {
                if(wanteds != null) {
                    for (Wanted wanted : wanteds) {
                        SimilarityClassifier.Recognition rec = new SimilarityClassifier.Recognition("", "", 0.0f);
                        emb = gson.fromJson(wanted.getId(), FaceRecViewModel.typeToken.getType());
                        Object obj = emb;
                        rec.setExtra(obj);
                        registered.put(wanted.getFirstName() +" "+ wanted.getLastName(), rec);

                    }
                }
            }
        });






        //Load model
//        try {
//            tfLite = new Interpreter(loadModelFile(getActivity(), modelFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Initialize Face Detector
//        FaceDetectorOptions highAccuracyOpts =
//                new FaceDetectorOptions.Builder()
//                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                        .build();
//        detector = FaceDetection.getClient(highAccuracyOpts);


        //initialize viewModel
        viewModel =  new ViewModelProvider(getActivity()).get(FaceRecViewModel.class);
        viewModel.init();
        viewModel.setDistance(0);


        loadModel();
        initializeFaceDetector();
        bindCamera();
        return view;
    }


    public void loadModel() {
        try {
            tfLite = new Interpreter(loadModelFile(getActivity(), modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Initialize Face Detector
    public  void initializeFaceDetector() {
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }



    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void bindCamera() {


        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        bindPreview(cameraProvider);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                , ContextCompat.getMainExecutor(getContext()));


    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                InputImage image = null;


                @SuppressLint("UnsafeExperimentalUsageError")
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

                Image mediaImage = imageProxy.getImage();


                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    System.out.println("Rotation " + imageProxy.getImageInfo().getRotationDegrees());
                }

                System.out.println("ANALYSIS");

                //Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                if (faces.size() != 0) {
                                                    Face face = faces.get(0); //Get first face from detected faces
                                                    System.out.println(face);

                                                    //mediaImage to Bitmap
                                                    Bitmap frame_bmp = toBitmap(mediaImage);

                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();

                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);


                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                                    if (flipX)
                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                    //Scale the acquired Face to 112*112 which is required input for model
                                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                                    if (start)
                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                                    System.out.println(boundingBox);
                                                    try {
                                                        Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
//                                                    if(registered.isEmpty()) {
//                                                        // reco_name.setText("Add Face");
//
//                                                    }
//                                                    else {
//                                                        //  reco_name.setText("No Face Detected!");
//
//                                                    }
                                                }

                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {

                                        imageProxy.close(); //v.important to acquire next frame for analysis
                                    }
                                });


            }
        });


        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);


    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
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

    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos < ySize; pos += width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte) ~savePixel);
                if (uBuffer.get(0) == (byte) ~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            } catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    private Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;

    }


    public void recognizeImage(final Bitmap bitmap) {


        // set Face to Preview
        face_preview.setImageBitmap(bitmap);
        FaceRecViewModel.bitmap = bitmap;


        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable




        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model


        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";





        //Compare new face with saved Faces.
        if (registered.size() > 0) {

            final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face

            if (nearest != null) {

                distance = nearest.second;

                final String name = nearest.first;
                label = name;
                distance = nearest.second;
                viewModel.setDistance(distance);




                if(distance<1.000f) {//If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                  //  viewModel.setName(String.valueOf(name));
                    Log.d("face", "aaaa");
                    viewModel.setName(String.valueOf(name));

                }

                else{
                    viewModel.setName("UNKNOWN");
                    Log.d("face", "UNKNOWN");

                }
                //reco_name.setText("Unknown");
                System.out.println("nearest: " + name + " - distance: " + distance);
                //Toast.makeText(context, "nearest: " + name + " - distance: " + distance, Toast.LENGTH_SHORT).show();



            }
        }


    }

    public void addFace()
    {
        {

            start=false;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Enter Name");

            // Set up the input
            final EditText input = new EditText(getContext());

            input.setInputType(InputType.TYPE_CLASS_TEXT );
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

                    //Create and Initialize new object with Face embeddings and Name.
                    SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                            "0", "", -1f);
                    result.setExtra(embeedings);
                    registered.put( input.getText().toString(),result);

                    viewModel.setEmbeddings(embeedings);
                 //   viewModel.setRecognition(result);
                //    viewModel.getRecognition().getValue().setExtra(embeedings);


                    //helper.setRecognitionHashMap(input.getText().toString(),result);
                    start=true;

                    registered.put(input.getText().toString(),result);

                    viewModel.setEmbeddings(embeedings);
                 //   viewModel.setRecognition(result);
                 //   viewModel.getRecognition().getValue().setExtra(embeedings);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    start=true;
                    dialog.cancel();
                }
            });

            builder.show();

        }

    }


    String name;
    public  void myAddFace()
    {

        viewModel.getFirst_mame().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
               name = s +" "+ viewModel.getLast_name().getValue();
            }
        });

        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", "", -1f);
        result.setExtra(embeedings);
        //registered.put( name,result);


        viewModel.setEmbeddings(embeedings);
        //viewModel.setRecognition(result);

       // viewModel.getRecognition().getValue().setExtra(embeedings);


        //helper.setRecognitionHashMap(input.getText().toString(),result);
        start=true;

       // registered.put(name,result);
        viewModel.setEmbeddings(embeedings);
      //  viewModel.setRecognition(result);

       // viewModel.getRecognition().getValue().setExtra(embeedings);
    }















}
