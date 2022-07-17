# FaceVision_mvvm
![Logo_FaceVision](https://user-images.githubusercontent.com/62130401/179417064-e1167c3a-24a8-43f9-95d3-52cfea669326.png)
<h1>IoT Project</h1>
<h2>Android + ESP32 + SERVER</h2>

The FaceVision app is a technological tool that uses machine learning (ML) and the mobile device with the Android operating system and allows it to identify wanted persons. The app compares the faces of the wanted persons in the database to the faces in the current frame from the camera and when matched, the name and additional information about the suspect are displayed and in addition the system shows its place on the map.
The system offers real-time identification of wanted persons, and can be used by the police, security personnel and the general public. This makes the app an effective tool for preventing crime and apprehending suspects.
Apart from using the phone camera, there is also the option of connecting an external camera that I built based on the esp32 microcontroller. This camera can be used to capture faces even without the phone camera being open.
The frames taken with this external camera are transmitted to the server and from there for decoding in the application using a special protocol, and upon identification of the requested information will be displayed on your device.


Initailize FaceDetector 
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
