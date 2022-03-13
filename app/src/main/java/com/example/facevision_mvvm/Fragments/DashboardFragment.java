package com.example.facevision_mvvm.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.facevision_mvvm.Fragments.FaceFragments.Utils.Communicator;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.databinding.FragmentDashboardBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class DashboardFragment extends Fragment {



    FragmentDashboardBinding binding;
    Dialog dialog;
    TextInputEditText httpCam;
    MaterialButton btn_connect;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_dashboard, container, false);
        binding =FragmentDashboardBinding.inflate(inflater,container,false);


        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.wantedlistCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openListViewFragment();
            }
        });

        binding.mapCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapsFragment();
            }
        });

        binding.detectorCardviewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFaceRecFragment();
            }
        });


        binding.profileCardviewMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openProfileFragment();
            }
        });


        binding.surveliumcamCardviewMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                connectCamDialog();
            }
        });


    }


    /**
     * to avoid memory leaks
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    Communicator c;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof Communicator)
        {
            c = (Communicator) context;
        }
    }

    /**
     * open the list view fragment
     *
     */
    public void openListViewFragment()
    {
        Fragment fragment = new ListFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openMapsFragment()
    {
        Fragment fragment = new MapsFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


     FaceRecFragment MyFragment;
    public void openFaceRecFragment()
    {
        c.openFaceRec();

    }
    public void openIpCamViewFragment()
    {

        Bundle bundle = new Bundle();
        bundle.putString("url",httpCam.getText().toString());
        Fragment fragment = new FaceVisionCamFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openProfileFragment()
    {
        c.openProfile();
    }



    public  void  connectCamDialog()
    {
        dialog = new Dialog(getContext());
        Window window = dialog.getWindow();

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.face_vision_cam_connect_dialog);
        dialog.setTitle("Connect to Camera");
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        httpCam = dialog.findViewById(R.id.etIpCam);
        btn_connect = dialog.findViewById(R.id.btnConnectCam);


        httpCam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_connect.setEnabled(!httpCam.getText().toString().isEmpty());


            }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                btn_connect.setEnabled(!httpCam.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_connect.setEnabled(!httpCam.getText().toString().isEmpty());

            }
        });


        btn_connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                openIpCamViewFragment();
                dialog.dismiss();
            }
        });
        dialog.show();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);



    }




}