package com.example.facevision_mvvm.Fragments.FaceFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.ViewModels.FaceRecViewModel;
import com.google.android.material.textview.MaterialTextView;


public class AccuracyFragment extends Fragment {

    MaterialTextView name;
    MaterialTextView distance;
    String nameS;
    Float distanceS;

    FaceRecViewModel viewModel;




    //



    public AccuracyFragment()
    {}

    public static AccuracyFragment newInstance() {
        return new AccuracyFragment();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_accuracy, container, false);

        name = rootView.findViewById(R.id.name);
        distance = rootView.findViewById(R.id.distance);


        return rootView;

    }


    // Accuracy checker
    public String accuracyOfDetection(float distance)
    {
        String result = "";

        if(distance < 0.4f)
            result = "High";

        else if(distance >= 0.4f && distance < 0.8f)
            result = "Medium";

        else if(distance >= 0.8f && distance < 1f)
            result = "Low";

        else result = "No Match";

        return result;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        viewModel  = new ViewModelProvider(getActivity()).get(ViewModel.class);
//        viewModel.getDistance().observe(this, new Observer<Float>() {
//            @Override
//            public void onChanged(Float s) {
//                distance.setText(accuracyOfDetection(s));
//            }
//        });
//
//        viewModel.getName().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                name.setText(s);
//            }
//        });



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel  = new ViewModelProvider(getActivity()).get(FaceRecViewModel.class);
        viewModel.getDistance().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float s) {
                distance.setText(accuracyOfDetection(s));
            }
        });

        viewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                name.setText(s);
            }
        });



    }
}