
# FaceVision
![Logo_FaceVision](https://user-images.githubusercontent.com/62130401/179417064-e1167c3a-24a8-43f9-95d3-52cfea669326.png)
<h1>IoT Project</h1>
<h2>Android + ESP32 + SERVER</h2>

The FaceVision app is a technological tool that uses machine learning (ML) and the mobile device with the Android operating system and allows it to identify wanted persons. The app compares the faces of the wanted persons in the database to the faces in the current frame from the camera and when matched, the name and additional information about the suspect are displayed and in addition the system shows its place on the map.
The system offers real-time identification of wanted persons, and can be used by the police, security personnel and the general public. This makes the app an effective tool for preventing crime and apprehending suspects.
Apart from using the phone camera, there is also the option of connecting an external camera that I built based on the esp32 microcontroller. This camera can be used to capture faces even without the phone camera being open.
The frames taken with this external camera are transmitted to the server and from there for decoding in the application using a special protocol, and upon identification of the requested information will be displayed on your device.





<h2 dir="rtl" align="right" > מחלקות (Classes) </h2>
<h1 align="center">RecognitionApi</h1>



<h2 align="right">הגדרת משתנים ואובייקטים</h2>



```java
    private FaceDetector detector; // אחראי ללכידת הפנים (ML Kit)
    Interpreter tfLite; // אחראי לפעולות הפענוח של הפנים (TensorFlow Lite)
    boolean start = true;
    private final HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //הפנים השמורות
    List<FaceDetectedEvent> listeners;
    FaceDetectedEvent faceDetectedEvent; // מאזין לזיהוי הפנים בפריים
    ViewModelStoreOwner owner;
    int[] intValues; //מערך של int חלק מתהליך זיהוי הפנים
    int inputSize = 112;  //גודל המודל
    boolean isModelQuantized = false;
    float[][] embeedings; //שמירת וקטורי הפנים שנלכדו
    float[][] emb;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model
    String modelFile = "mobile_face_net.tflite"; //model name
    Context activityContext;
    Gson gson;

```


<h2 align="right">פונקציות </h2>

```java
    public  void detect(Bitmap bitmap) {


        if (bitmap == null) {
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);


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
                                            Bitmap cropped_face = getCropBitmapByCPU(bitmap, rect);

                                            try {

                                                recognizeImage(cropped_face);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.d("RecognitionApi", "Error" + e.getMessage());
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

    
    
```



```java
    public  void detect(ImageProxy imageProxy)
    {

        InputImage image = null;


        if(imageProxy.getImage()==null)
        {
            return;
        }


        @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
        // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

        Image mediaImage = imageProxy.getImage();


        if (mediaImage != null) {
            image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            System.out.println("Rotation " + imageProxy.getImageInfo().getRotationDegrees());
        }


        Bitmap bitmap = toBitmap(mediaImage);

        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size()!=0)
                                        {
                                            Face face = faces.get(0);



                                            int rot = imageProxy.getImageInfo().getRotationDegrees();

                                            //Adjust orientation of Face
                                            Bitmap frame_bmp1 = rotateBitmap(bitmap, rot, false, false);


                                            //Get bounding box of face
                                            RectF boundingBox = new RectF(face.getBoundingBox());



                                            //Crop out bounding box from whole Bitmap(image)
                                            Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);



                                            Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);



                                            recognizeImage(scaled);




                                        }
                                    }
                                })


                        .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Face>> task) {
                                // detector.close();
                                imageProxy.close(); //v.important to acquire next frame for analysis
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



```


```java

    public  void initializeFaceDetector() {
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)

                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);

    }
```


```java
    public static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
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
```

```java
    public void loadModel() {
        try {

            tfLite = new Interpreter(loadModelFile (activityContext  , modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------

    private MappedByteBuffer loadModelFile(Context activity, String MODEL_FILE) throws IOException {
        AssetManager assetManager = activity.getAssets();
        AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
```

```java
    public void recognizeImage(final Bitmap bitmap) {


        Log.d("api", "recognizeImage: ");
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

        FaceRecViewModel.Embeddings = embeedings;

        //wantedAddedEvent.onWantedAdded(embeedings[0]);

        //Compare new face with saved Faces.
        if (registered.size() > 0) {

            final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face

            if (nearest != null) {

                distance = nearest.second;

                final String name = nearest.first;
                label = name;
                distance = nearest.second;



                if(distance<1.000f) {//If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    //  viewModel.setName(String.valueOf(name));
                    Log.d("faceD", "aaaa");
                   // viewModel.setName(String.valueOf(name));


                    faceDetectedEvent.onFaceDetected(bitmap,name, accuracyOfDetection(distance));




                }

                else{
                    faceDetectedEvent.onFaceDetected(bitmap,"UNKNOWN", accuracyOfDetection(distance));
                    //viewModel.setName("UNKNOWN");
                    Log.d("faceD", "UNKNOWN");

                }


            }
        }


    }

```

```java
    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];



            FaceRecViewModel.Embeddings = embeedings;



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
```

```java
    public Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    //----------------------------------------

    /**
     * <h1>YUV_420_888toNV21</h1>
     * <p>מעביר את התמונה לפורמט NV21</p>
     * @param image
     */
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

        if (rowStride == width) {
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            long yBufferPos = -rowStride;
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

```


```java
   public void setWantedList(ArrayList<Wanted> wanted){


        for (Wanted w : wanted) {
            SimilarityClassifier.Recognition rec = new SimilarityClassifier.Recognition("", "", 0.0f);
            emb = gson.fromJson(w.getId(), FaceRecViewModel.typeToken.getType());
            Object obj = emb;
            rec.setExtra(obj);
            this.registered.put(w.getFirstName() +" "+ w.getLastName(), rec);
        }
    }

    public void setFaceDetectedEvent(FaceDetectedEvent faceDetectedEvent){
        this.faceDetectedEvent = faceDetectedEvent;
        }


```

```java
    public String accuracyOfDetection(float distance) {
        String result = "";

        if (distance < 0.5f)
            result = "High";

        else if (distance >= 0.5f && distance < 0.8f)
            result = "Medium";

        else if (distance >= 0.8f && distance < 1f)
            result = "Low";

        else result = "No Match";

        return result;

    }

```


```java
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


    //----------------------------------------


    /**
     * Gets resized bitmap.
     *
     * @param bm        the bm
     * @param newWidth  the new width
     * @param newHeight the new height
     * @return the resized bitmap
     */
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

```


<h2 dir="rtl" align="right" > מאיזינים (Intefraces) </h2>

```FaceDetectedEvent```




***

<h1 align="center">DownloadBitmapApi</h1>

<h2 align="right">הגדרת משתנים ואובייקטים</h2>

```java
    private boolean isRunning = true;
    private String url;
    public Bitmap bitmap;
    private final Context context;
    int count = 0;
    ImageTakenEvent imageTakenEvent;
```

<h2 align="right">פונקציות </h2>

```java
public void start() {

        isRunning = true;

        if (url == null || !isRunning) {
            Log.d("Download Bitmap", "url is null or already running");
            return;
        }
        Log.d("Download Bitmap", "run: " + bitmap);

        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();



                while (isRunning) {

                    count++;

                    try {
                        bitmap = getBitmapFromURL(url + "?" + System.currentTimeMillis());


                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                imageTakenEvent.onImageTaken(bitmap);
                              //  Log.d("Download Bitmap", "run: " + bitmap);
                                Log.d("Download Bitmap", "run: " + count);

                            }
                        });

                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }).start();
    }


public void stop(){
        isRunning = false;
        }
```

```java

    public Bitmap getBitmapFromURL(String src) {
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
```

<h2 dir="rtl" align="right" > מאיזינים (Intefraces) </h2>

<code>ImageTakenEvent</code>

***

<h1 align="center">Repo</h1>
<p dir="rtl">מחלקה זו אחראית על התקשורת בין מאגר הנתונים בFirebase לאפליקציה, ומאפשרת עדכון נתונים באופן דינאמי.</p>

<h2 align=right>הגדרת משתנים ואובייקטים</h2>

```java
    static Repo instance;
    private final ArrayList<Wanted> wanteds = new ArrayList<>();
    private final ArrayList<User> users = new ArrayList<>();
    private final MutableLiveData<ArrayList<Wanted>> wanted = new MutableLiveData<>(); // wanted list
    private final MutableLiveData<ArrayList<User>> user = new MutableLiveData<>();
    static final DatabaseReference wantedRef = FirebaseDatabase.getInstance().getReference("wanted");
```

<h2 align="right">פונקציות </h2>


``` java
    public static Repo getInstance() {
        if (instance == null) {
            instance = new Repo();
        }
        return instance;
    }
    
 ```

```java

    public MutableLiveData<ArrayList<Wanted>> getWanteds() {

        if (wanteds.size() == 0) {
            retriveWanted();
        }

        wanted.setValue(wanteds);
        return wanted;

    }

    public MutableLiveData<ArrayList<User>> getUsers() {

        if (users.size() == 0) {
            retrieveUser();
        }

        user.setValue(users);
        return user;

    }





    public void retriveWanted() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Wanted");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wanted.getValue().clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    wanteds.add(ds.getValue(Wanted.class));
                }

                wanted.setValue(null);
                wanted.postValue(wanteds);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void retrieveUser(){

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                user.getValue().clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    users.add(ds.getValue(User.class));
                }

                user.setValue(null);
                user.postValue(users);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

```


***

<h2 dir="rtl">תצוגה של שמירת הנתונים בפיירביס כקובץ Json</h2>

![image](https://www.gstatic.com/devrel-devsite/prod/vb4b5f40392bd9ba34d31c8578d2b4bd0a5bd852eabd9554190f5a24f31b598a8/firebase/images/touchicon-180.png)

```json

{
  "Users": {
    "-N-IMudKi-oFy9FRHisJ": {
      "email": "yoan123@gmail.com",
      "password": "yyyyyy",
      "uid": "-N-IMudKi-oFy9FRHisJ"
    },
    "-N-IUGnR8YYSBJd1z1av": {
      "email": "jelly@gmail.com",
      "password": "yyyyyyy",
      "uid": "-N-IUGnR8YYSBJd1z1av"
    }
  },
  "Wanted": {
    "-N-DWhYgaWtkqU6Ssekb": {
      "firstName": "b",
      "id": "[[-0.0010410135,0.009628831,0.010786182,-0.0054620407,-0.017342247,0.16972956,0.035113342,0.12557173,-0.05729674,-0.23139378,-0.0011715854,0.0076675173,-0.0060608457,0.021590777,0.0037774534,0.06945441,-2.522286E-4,0.008889398,-0.0022686548,0.0012244228,-0.28658593,0.04478087,0.06483153,-0.0019388105,-0.005111154,0.011119399,-0.012869898,0.07635839,-0.07545111,0.003345836,-0.0045659943,-0.0779627,0.14756562,0.0030033942,-0.03442563,0.049341083,-0.06983834,-0.006458608,0.0032271834,0.15341885,9.31929E-4,0.0036573492,0.004183538,-4.8961537E-4,-0.007381257,-0.013672632,-0.0576776,0.13247061,0.0027207863,-0.022482777,0.041603774,-0.0031791043,0.15681735,-6.877006E-4,0.042630084,-0.008161148,-0.16617066,-0.0023209415,-0.013752797,0.023938121,0.0369629,0.0043679746,0.06325639,-0.039085478,-2.327944E-4,-0.16532621,9.510836E-4,-0.01461484,-0.0028614285,3.8600856E-4,-0.009465449,0.16616216,-0.13870268,0.0061112437,0.08317331,0.009896848,0.0019666047,0.0044759843,-0.092623666,-0.017751034,0.0024296262,0.031452626,-0.0076886266,0.24939343,-0.07279492,0.002314762,-0.00441885,0.1271898,0.017952092,-0.044802375,0.12992984,8.866098E-4,-0.0050643967,-0.015138161,-0.07330176,-0.13375889,0.048159875,0.0888026,-0.0010018667,-0.005603576,0.0016471365,4.709781E-6,-0.0069411206,-3.6836325E-4,-0.010652501,0.0013076725,-0.10219801,-4.5381248E-4,0.008744307,-0.0076356796,-0.051288262,0.003241733,0.0031532908,-0.28951693,0.011590097,-0.0073560756,-0.01427289,-0.016451342,-0.09925304,0.1354679,0.015786558,-0.018121727,0.012385498,-7.51372E-4,0.0037261806,0.001955275,0.0048412336,0.0042819185,-0.0034855728,0.19582444,0.0035607575,-0.005586806,0.0023442877,-0.018662522,0.12461485,-0.009434344,-0.07167587,0.0022182574,0.011361994,0.01909742,-0.003204724,0.0016544717,0.0068640774,0.23292816,0.16324323,-0.071317345,0.013641624,0.0023167657,-6.322562E-4,-0.00698097,0.002779571,0.040855035,0.07781138,-6.1080226E-4,-0.003179328,-0.0113768075,-0.008758626,0.023726327,-0.0066552125,0.004433571,-0.020477917,-0.0050954404,-0.008487688,0.0022544258,-5.7189533E-5,0.010942446,0.013927165,-0.04511513,-0.005983547,0.0015417954,0.27572143,0.10947642,7.0208334E-5,0.05793188,0.016310148,0.006158329,-0.10900652,-0.019061118,-0.0020322332,0.001597081,-0.07266038,-0.06417121,0.007374468,-0.0050402964,-0.08927689,0.03624378,-0.013405281,0.09158981,-0.011056186,0.026418133,-0.032307833,-0.0063484567]]",
      "imageUrl": "https://firebasestorage.googleapis.com/v0/b/facevision-mvvm.appspot.com/o/images%2F-N-DWhYgaWtkqU6Ssekb?alt=media&token=8ce80afd-dd43-4da9-bc6e-7b0e171077a4",
      "key": "-N-DWhYgaWtkqU6Ssekb",
      "lastName": "",
      "location": {
        "latitude": 32.0923393,
        "longitude": 34.9722727
      }
    },
    "-N-DXUgW5zPvmKc9akgN": {
      "firstName": "yoni",
      "id": "[[-0.013441408,0.026486065,0.008166146,-0.0065202746,-0.061931167,0.09741596,-0.09463319,-0.037456434,-0.03962274,-0.012304927,-0.027717628,0.0058319345,-0.008313347,0.023376876,-0.0036113793,-0.03789333,-0.025476761,-0.011577417,-6.766282E-5,0.005754016,-0.15575974,0.103555135,-0.01366982,0.015013042,-0.057849202,0.012086764,-0.047271363,0.10145963,0.16120847,-0.12074202,-0.0144114345,0.17240885,0.049797244,3.7592117E-4,-0.009133127,0.046619564,-0.009602987,-0.028868442,0.0010256692,0.06772082,0.0074122758,0.0056303665,0.02315589,-0.018348983,0.012992912,-0.05166491,-0.10818403,-0.058490284,-0.016850613,0.039827745,-0.11021288,-0.007353817,-0.24465373,-0.0025254176,-0.077699095,0.006147024,0.1936563,0.0048825275,-0.12100764,0.03015469,0.027357414,-0.09342482,-0.006017118,-0.025992416,-0.018265719,0.08169671,-0.008178296,0.028433014,0.0055204476,0.0048641083,-0.03712568,-0.11200052,-0.049314488,0.0040170033,-0.06331981,0.0072290427,0.0068534603,-0.0037178171,0.32575655,0.08395926,-0.010307969,-0.039482877,-0.02037033,0.085364096,-0.18602572,-0.0025206793,-0.0015801251,-0.04778169,0.051346727,-0.13373461,0.020333476,0.0027960027,0.011692848,-0.032425284,-0.06543713,-0.03911331,-0.06705735,-0.09943406,7.6033187E-4,0.015478297,0.0030875201,-0.016234295,-0.011231421,-0.003999902,-0.0043627364,0.006618547,-0.20851412,0.0044841133,0.001960007,0.010279733,-0.05570969,0.009542639,0.008797392,0.31869772,0.009778704,0.083896264,-0.0031833462,-0.04926956,0.09194644,0.14049953,0.11343999,0.0068182764,-0.1434014,-0.0054175924,-3.8446888E-4,-0.003315376,0.010097834,-0.0036533417,0.0040803854,0.10029941,0.014391219,0.025389371,-0.0035437655,-0.051729023,0.067614615,-0.01583081,-0.17691545,0.014623556,-0.009683874,0.015994517,-0.008506225,0.0046977997,0.0012025355,-0.023884052,-0.18820643,0.07653202,-0.03200735,-0.0053075175,0.008707199,-0.009126822,-0.007356528,-0.06266106,-0.06562633,-0.031104457,-0.0053448784,1.6502956E-4,0.006945444,0.005388296,-0.07420188,-0.003531259,-0.07241037,0.0024118822,-0.010206949,-0.002306631,0.0060584336,0.02316251,0.018091861,-0.099512555,7.348223E-4,5.9629546E-4,0.09591395,0.060623143,-0.0037737733,0.0043147355,0.044896863,0.0030318007,-0.018251896,-0.10206613,-0.011971983,0.008413606,-0.115981065,-0.03836338,1.337841E-4,-0.0032048554,0.09316348,-0.016252546,-0.071028836,0.089589275,0.10665064,-0.14276826,-0.07351477,-0.017121628]]",
      "imageUrl": "https://firebasestorage.googleapis.com/v0/b/facevision-mvvm.appspot.com/o/images%2F-N-DXUgW5zPvmKc9akgN?alt=media&token=aa5ca3f0-798a-4cb0-88ba-d1c38f426aec",
      "key": "-N-DXUgW5zPvmKc9akgN",
      "lastName": "",
      "location": {
        "latitude": 32.0923892,
        "longitude": 34.9723033
      }
    }
  }
}



```

<h1 align="center">Models</h1>

<h2>User</h2>

```java 
    private String email;
    private String password;
    private String uid;
```

<h2>Wanted</h2>

``` java 
    private String email;
    private String password;
    private String uid;
```

<h2>Location</h2>

```java 
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    
    public  static final  int LOCATION_SERVICE_ID = 175;
    public  static final String ACTION_START_SERVICE_LOCATION = "startLocationService";
    public  static final String ACTION_STOP_SERVICE_LOCATION = "stopLocationService";
```

<h2>SimilarityClassifier.Recognition</h2>

<p dir="rtl">מזהה ייחודי לפנים שזוהו. ספציפית 
למחלקה, לא למופע של האובייקט.</p>


<p dir="rtl">נמצא בתוך ממשק.</p>

```java
    private final String id;
    private final String title;
    private final Float distance;
    private Object extra;
```

***
<h1 align="center">ViewModels</h1>

<h2>UserViewModel</h2>
<h3 style="color:yellow" >extends ViewModel</h3>

```java
    private MutableLiveData<ArrayList<User>> userList;


    public UserViewModel() {
       init();
    }

    public void init() {

        if (userList != null) {
            return;

        }

        userList = Repo.getInstance().getUsers();

    }

    
    public LiveData<ArrayList<User>> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList.setValue(userList);
    }


    public  void clear()
    {
        userList.getValue().clear();
    }

```

<h2>WantedViewModel</h2>
<h3 style="color:yellow" >extends ViewModel</h3>


```java
    private MutableLiveData<ArrayList<Wanted>> wantedList;



    public  WantedViewModel(){
        init();
    }
    
    public LiveData<ArrayList<Wanted>> getWantedList()
    {
        return wantedList;
    }

    public void setWantedList(ArrayList<Wanted> wantedList)
    {
        this.wantedList.setValue(wantedList);
    }

    //private  Repo repo;

    public void init() {

        if (wantedList != null) {
           return;

        }

        wantedList = Repo.getInstance().getWanteds();

    }
    
    public  void clear()
    {
       wantedList.getValue().clear();
    }
```


<h1 align="center">Service</h1>
<h2>PushNotificationService</h2>
<h3 style="color:yellow" >extends FirebaseMessagingService</h3>
<h4 style="color:yellow">(FirebaseMessagingService > EnhancedIntentService > Service)</h4>


```java
@Override
public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
final String CHANNEL_ID = "NOTI_ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        channel = new NotificationChannel(CHANNEL_ID,
        "MyNotification",
        NotificationManager.IMPORTANCE_HIGH);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(R.drawable.ic_facial_recognition)
        .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(1, builder.build());

        }
        super.onMessageReceived(remoteMessage);
        }
```



<h1 align="center">Broadcast Receiver</h1>

<h2>WifiBroadcast</h2>

```java

    AlertDialog alertDialog;
    public WifiBroadcast(AlertDialog dialog) {

        this.alertDialog = dialog;

    }
    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.net.wifi.STATE_CHANGE")) {
            try {
                if(isConnected() == false){
                    Toast.makeText(context, "Wifi is not connected", Toast.LENGTH_SHORT).show();
                    alertDialog.setMessage("Wifi is not connected");
                }
                else {
                    Toast.makeText(context, "Wifi is connected", Toast.LENGTH_SHORT).show();
                    alertDialog.setMessage("Wifi is connected");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            alertDialog.show();
        }
    }
```


#מימוש באקטיביטי
```java
  WifiBroadcast wifi;
  
    public void createDialog(){
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);// מופע של בילדר
        builder.setTitle("wifi");
        builder.setCancelable(true);
        builder.setPositiveButton("press ok to continue", null);
        dialog = builder.create();// נפעיל את הבילדר ונחזיר רפרנס ל דיאלוג
    }



    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
        registerReceiver(wifi, intentFilter);
        }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifi);
        }
 ```

***

* CompileSdk Version: compileSdk 31 
* MinSdk Version: minSdk 21
* הגדרה לצורך קליטת מודל הזיהוי:
<code>  aaptOptions {
        noCompress "tflite"}
</code>
* מכשירים ואימולטרים שעליהם נבדק האפליקציה

| Pixel 4 XL | OnePlus Nord 2 | Pixel 4 |
|------------|----------------|---------|


***
