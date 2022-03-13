package com.example.facevision_mvvm.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.facevision_mvvm.CustomView.IPCamView;
import com.example.facevision_mvvm.R;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


public class FaceVisionCamFragment extends Fragment {


    public FaceVisionCamFragment() {
        // Required empty public constructor
    }


    IPCamView ipCamView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ipCamView.setUrl("http://webcam.abaco-digital.es/zuda/image2.jpg");


        ipCamView.setInterval(0); // In milliseconds, default 0
        if(getArguments() != null){
            ipCamView.setUrl(getArguments().getString("url"));
        }

        ipCamView.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_face_vision_cam, container, false);

        ipCamView = view.findViewById(R.id.ipCam);



        return  view;
    }
}