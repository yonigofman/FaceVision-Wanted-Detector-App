package com.example.facevision_mvvm.Fragments;

import android.graphics.Bitmap;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import com.example.facevision_mvvm.Models.Location;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.ViewModels.LocationViewModel;
import com.example.facevision_mvvm.ViewModels.WantedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.*;

public class MapsFragment extends Fragment {

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {



            googleMap.clear();
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Wanted Detected in:"+ latLng.latitude + "," + latLng.longitude));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
        }



    };

    MapsFragment()
    {
        // Required empty public constructor
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View  view = inflater.inflate(R.layout.fragment_maps, container, false);
        test = view.findViewById(R.id.map_button);





        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationViewModel.setLocation(new Location(LocationViewModel.StaticLat,LocationViewModel.StaticLng));

            }
        });



        return view;
    }




    LocationViewModel locationViewModel;
    LatLng latLng;
    Button test;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        latLng =new LatLng(34,34);

        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        locationViewModel.getLocation().observe(getViewLifecycleOwner(),location -> {
            if(location != null)
            {
                this.latLng = new LatLng(location.getLatitude(),location.getLongitude());
                if (mapFragment != null) {
                    mapFragment.getMapAsync(callback);
                }
            }
        });





    }
}