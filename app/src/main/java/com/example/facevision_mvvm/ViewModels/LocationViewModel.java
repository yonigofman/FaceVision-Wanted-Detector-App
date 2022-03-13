package com.example.facevision_mvvm.ViewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.facevision_mvvm.Models.Location;

public class LocationViewModel extends ViewModel {
    private final MutableLiveData<Location> location = new MutableLiveData<>();

    public  static double StaticLat;
    public  static double StaticLng;

    public void setLocation(Location location) {
        this.location.setValue(location);
    }

    public MutableLiveData<Location> getLocation() {
        return this.location;
    }

}
