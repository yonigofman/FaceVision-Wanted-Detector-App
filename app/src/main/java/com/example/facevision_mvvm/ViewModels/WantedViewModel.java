package com.example.facevision_mvvm.ViewModels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.Repositories.Repo;

import java.util.ArrayList;

public class WantedViewModel  extends ViewModel {

    private MutableLiveData<ArrayList<Wanted>> wantedList;



    public  WantedViewModel(){
        init();
    }

//    public void  init()
//    {
//        wantedList = new MutableLiveData<>();
//    }

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

}
