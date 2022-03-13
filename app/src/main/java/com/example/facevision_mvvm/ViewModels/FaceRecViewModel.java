package com.example.facevision_mvvm.ViewModels;

import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.facevision_mvvm.Models.Reco.SimilarityClassifier;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class FaceRecViewModel extends ViewModel {

    private  MutableLiveData<Float> distance = new MutableLiveData<>();
    private MutableLiveData<String> name = new MutableLiveData<>();


//    private MutableLiveData<SimilarityClassifier.Recognition> recognition = new MutableLiveData<>();

    private MutableLiveData<Object> embeddings = new MutableLiveData<>();
    private final MutableLiveData<String> first_mame = new MutableLiveData<>();
    private final MutableLiveData<String> last_name = new MutableLiveData<>();





//    private MutableLiveData<HashMap<String, SimilarityClassifier.Recognition>> recognition_map = new MutableLiveData<>();



    public  static Bitmap bitmap;
    public static TypeToken<float[][]> typeToken = new TypeToken<float[][]>() {};



    public FaceRecViewModel() {
      //  init();

    }



    public void init()
    {
        distance = new MutableLiveData<>();
        name = new MutableLiveData<>();
        embeddings = new MutableLiveData<>();

    }




    public void setDistance(float distance){
        this.distance.setValue(distance);
    }


    public LiveData<Float> getDistance(){
        return this.distance;
    }

    public void setName(String name){
        this.name.setValue(name);
    }

    public LiveData<String> getName(){
        return this.name;
    }


//    public void setRecognition(SimilarityClassifier.Recognition recognition){
//        this.recognition.setValue(recognition);
//    }
//
//    public LiveData<SimilarityClassifier.Recognition> getRecognition(){
//        return this.recognition;
//    }

    public void setEmbeddings(Object embeddings){
        this.embeddings.setValue(embeddings);
    }

    public LiveData<Object> getEmbeddings(){
        return this.embeddings;
    }


    public void setFirst_mame(String first_mame){
        this.first_mame.setValue(first_mame);
    }

    public LiveData<String> getFirst_mame(){
        return this.first_mame;
    }

    public void setLast_name(String last_name){
        this.last_name.setValue(last_name);
    }

    public LiveData<String> getLast_name(){
        return this.last_name;
    }


//    public void setRecognition_map(HashMap<String, SimilarityClassifier.Recognition> recognition_map){
//        this.recognition_map.setValue(recognition_map);
//    }
//
//    public  LiveData<HashMap<String, SimilarityClassifier.Recognition>> getRecognition_map()
//    {
//        return this.recognition_map;
//    }


//    public void setPreviewBit(Bitmap previewBit){
//        this.previewBit.setValue(previewBit);
//    }
//
//    public LiveData<Bitmap> getPreviewBit(){
//        return this.previewBit;
//    }


}
